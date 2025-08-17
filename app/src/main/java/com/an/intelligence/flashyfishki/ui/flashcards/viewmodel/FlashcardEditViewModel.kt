package com.an.intelligence.flashyfishki.ui.flashcards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.domain.dao.FlashcardDao
import com.an.intelligence.flashyfishki.domain.model.Category
import com.an.intelligence.flashyfishki.domain.model.Flashcard
import com.an.intelligence.flashyfishki.domain.repository.AuthRepository
import com.an.intelligence.flashyfishki.ui.flashcards.model.FlashcardFormState
import com.an.intelligence.flashyfishki.ui.flashcards.model.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class FlashcardEditViewModel @Inject constructor(
    private val flashcardDao: FlashcardDao,
    private val categoryDao: CategoryDao,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _formState = MutableStateFlow(FlashcardFormState())
    val formState: StateFlow<FlashcardFormState> = _formState.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()
    
    private val _currentFlashcardId = MutableStateFlow<Long?>(null)

    // Get current user ID from auth repository
    private val currentUserId: StateFlow<Long?> = authRepository.currentUser
        .map { it?.userId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // All categories for dropdown
    val categories = categoryDao.getAllCategories()
        .catch { throwable ->
            _error.value = "Failed to load categories: ${throwable.message}"
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Initialize for creating new flashcard
     */
    fun initializeForNewFlashcard(preselectedCategoryId: Long? = null) {
        viewModelScope.launch {
            _isEditMode.value = false
            _formState.value = FlashcardFormState(
                categoryId = preselectedCategoryId ?: 0L
            )
            validateForm()
        }
    }

    /**
     * Initialize for editing existing flashcard
     */
    fun initializeForEditFlashcard(flashcardId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _isEditMode.value = true

                val flashcard = flashcardDao.getFlashcardById(flashcardId)
                if (flashcard == null) {
                    _error.value = "Flashcard not found"
                    return@launch
                }

                // Check if user owns this flashcard
                val userId = authRepository.currentUser.value?.userId
                if (userId == null || flashcard.userId != userId) {
                    _error.value = "Access denied: You can only edit your own flashcards"
                    return@launch
                }

                _currentFlashcardId.value = flashcardId
                _formState.value = FlashcardFormState(
                    question = flashcard.question,
                    answer = flashcard.answer,
                    categoryId = flashcard.categoryId,
                    difficultyLevel = flashcard.difficultyLevel,
                    isPublic = flashcard.isPublic
                )

                validateForm()

            } catch (e: Exception) {
                _error.value = "Failed to load flashcard: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update form field with validation
     */
    fun updateFormField(
        question: String? = null,
        answer: String? = null,
        categoryId: Long? = null,
        difficultyLevel: Int? = null,
        isPublic: Boolean? = null
    ) {
        val currentState = _formState.value
        
        _formState.value = currentState.copy(
            question = question ?: currentState.question,
            answer = answer ?: currentState.answer,
            categoryId = categoryId ?: currentState.categoryId,
            difficultyLevel = difficultyLevel ?: currentState.difficultyLevel,
            isPublic = isPublic ?: currentState.isPublic
        )

        // Debounced validation
        validateForm()
    }

    /**
     * Validate form and update validation state
     */
    private fun validateForm() {
        val state = _formState.value
        val errors = mutableListOf<String>()
        
        var questionError: String? = null
        var answerError: String? = null
        var categoryError: String? = null

        // Question validation
        when {
            state.question.isBlank() -> {
                questionError = "Question cannot be empty"
                errors.add("Question is required")
            }
            state.question.length > 500 -> {
                questionError = "Question cannot exceed 500 characters"
                errors.add("Question too long")
            }
        }

        // Answer validation
        when {
            state.answer.isBlank() -> {
                answerError = "Answer cannot be empty"
                errors.add("Answer is required")
            }
            state.answer.length > 1000 -> {
                answerError = "Answer cannot exceed 1000 characters"
                errors.add("Answer too long")
            }
        }

        // Category validation
        if (state.categoryId <= 0) {
            categoryError = "Please select a category"
            errors.add("Category is required")
        }

        val isValid = errors.isEmpty()

        _formState.value = state.copy(
            questionError = questionError,
            answerError = answerError,
            categoryError = categoryError,
            isValid = isValid
        )
    }

    /**
     * Save flashcard (create or update)
     */
    fun saveFlashcard(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                _error.value = null

                val state = _formState.value
                
                // Wait for current user to be available
                val userId = authRepository.currentUser.value?.userId
                
                // Debug log
                android.util.Log.d("FlashcardEdit", "Current user ID: $userId, Auth user: ${authRepository.currentUser.value?.userId}")

                if (userId == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }

                if (!state.isValid) {
                    _error.value = "Please fix validation errors before saving"
                    return@launch
                }

                // Check flashcard limit for new flashcards
                if (!_isEditMode.value) {
                    val userFlashcardCount = flashcardDao.countUserFlashcards(userId)
                    if (userFlashcardCount >= 1000) {
                        _error.value = "You have reached the maximum limit of 1000 flashcards"
                        return@launch
                    }
                }

                val currentDate = Date()
                
                if (_isEditMode.value) {
                    // Update existing flashcard
                    val flashcardId = _currentFlashcardId.value
                    if (flashcardId != null) {
                        val existingFlashcard = flashcardDao.getFlashcardById(flashcardId)
                        if (existingFlashcard != null) {
                            val updatedFlashcard = existingFlashcard.copy(
                                question = state.question.trim(),
                                answer = state.answer.trim(),
                                categoryId = state.categoryId,
                                difficultyLevel = state.difficultyLevel,
                                isPublic = state.isPublic,
                                updatedAt = currentDate
                            )
                            flashcardDao.updateFlashcard(updatedFlashcard)
                        }
                    }
                } else {
                    // Create new flashcard
                    val newFlashcard = Flashcard(
                        userId = userId,
                        categoryId = state.categoryId,
                        question = state.question.trim(),
                        answer = state.answer.trim(),
                        difficultyLevel = state.difficultyLevel,
                        learningStatus = 0, // New flashcard
                        nextReviewDate = null,
                        isPublic = state.isPublic,
                        originalFlashcardId = null,
                        copiesCount = 0,
                        createdAt = currentDate,
                        updatedAt = currentDate
                    )

                    val flashcardId = flashcardDao.insertFlashcard(newFlashcard)
                    if (flashcardId <= 0) {
                        _error.value = "Failed to create flashcard"
                        return@launch
                    }
                }

                onSuccess()

            } catch (e: Exception) {
                _error.value = "Failed to save flashcard: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Reset form
     */
    fun resetForm() {
        _formState.value = FlashcardFormState()
    }

    /**
     * Get remaining character count for question
     */
    fun getQuestionRemainingChars(): Int {
        return 500 - _formState.value.question.length
    }

    /**
     * Get remaining character count for answer
     */
    fun getAnswerRemainingChars(): Int {
        return 1000 - _formState.value.answer.length
    }
}

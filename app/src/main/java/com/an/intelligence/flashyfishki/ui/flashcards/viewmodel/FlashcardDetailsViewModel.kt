package com.an.intelligence.flashyfishki.ui.flashcards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.domain.dao.FlashcardDao
import com.an.intelligence.flashyfishki.domain.model.Category
import com.an.intelligence.flashyfishki.domain.model.Flashcard
import com.an.intelligence.flashyfishki.domain.repository.AuthRepository
import com.an.intelligence.flashyfishki.ui.flashcards.model.DeleteConfirmationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class FlashcardDetailsViewModel @Inject constructor(
    private val flashcardDao: FlashcardDao,
    private val categoryDao: CategoryDao,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _flashcard = MutableStateFlow<Flashcard?>(null)
    val flashcard: StateFlow<Flashcard?> = _flashcard.asStateFlow()

    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category.asStateFlow()

    private val _deleteConfirmationState = MutableStateFlow(DeleteConfirmationState())
    val deleteConfirmationState: StateFlow<DeleteConfirmationState> = _deleteConfirmationState.asStateFlow()

    // Get current user ID from auth repository
    private val currentUserId: StateFlow<Long?> = authRepository.currentUser
        .map { it?.userId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Load flashcard details
     */
    fun loadFlashcard(flashcardId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val flashcard = flashcardDao.getFlashcardById(flashcardId)
                if (flashcard == null) {
                    _error.value = "Flashcard not found"
                    return@launch
                }

                // Check if user owns this flashcard
                val userId = authRepository.currentUser.value?.userId
                if (userId == null || flashcard.userId != userId) {
                    _error.value = "Access denied: You can only view your own flashcards"
                    return@launch
                }

                _flashcard.value = flashcard
                
                // Load category details
                loadCategory(flashcard.categoryId)

            } catch (e: Exception) {
                _error.value = "Failed to load flashcard: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load category details
     */
    fun loadCategory(categoryId: Long) {
        viewModelScope.launch {
            try {
                val category = categoryDao.getCategoryById(categoryId)
                _category.value = category
            } catch (e: Exception) {
                // Don't show error for category loading failure
                _category.value = null
            }
        }
    }

    /**
     * Delete flashcard
     */
    fun deleteFlashcard() {
        viewModelScope.launch {
            try {
                val flashcard = _flashcard.value
                if (flashcard == null) {
                    _error.value = "No flashcard to delete"
                    return@launch
                }

                // Verify ownership again
                val userId = authRepository.currentUser.value?.userId
                if (userId == null || flashcard.userId != userId) {
                    _error.value = "Access denied: You can only delete your own flashcards"
                    return@launch
                }

                flashcardDao.deleteFlashcard(flashcard)
                
                // Clear the flashcard from state to indicate successful deletion
                _flashcard.value = null

            } catch (e: Exception) {
                _error.value = "Failed to delete flashcard: ${e.message}"
            }
        }
    }

    /**
     * Restore flashcard to learning mode (status 0)
     */
    fun restoreToLearning() {
        viewModelScope.launch {
            try {
                val flashcard = _flashcard.value
                if (flashcard == null) {
                    _error.value = "No flashcard to restore"
                    return@launch
                }

                // Only allow restoration for learned flashcards (status 3)
                if (flashcard.learningStatus != 3) {
                    _error.value = "Only learned flashcards can be restored to learning mode"
                    return@launch
                }

                // Verify ownership
                val userId = authRepository.currentUser.value?.userId
                if (userId == null || flashcard.userId != userId) {
                    _error.value = "Access denied: You can only modify your own flashcards"
                    return@launch
                }

                val currentDate = Date()
                flashcardDao.updateFlashcardLearningStatus(
                    flashcardId = flashcard.flashcardId,
                    newStatus = 0, // Reset to new
                    nextReviewDate = null, // Clear review date
                    updateTime = currentDate
                )

                // Update local state
                _flashcard.value = flashcard.copy(
                    learningStatus = 0,
                    nextReviewDate = null,
                    updatedAt = currentDate
                )

            } catch (e: Exception) {
                _error.value = "Failed to restore flashcard: ${e.message}"
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
     * Refresh flashcard data
     */
    fun refresh() {
        _flashcard.value?.let { flashcard ->
            loadFlashcard(flashcard.flashcardId)
        }
    }
}

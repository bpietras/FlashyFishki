package com.an.intelligence.flashyfishki.ui.flashcards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.domain.model.Category
import com.an.intelligence.flashyfishki.domain.repository.AuthRepository
import com.an.intelligence.flashyfishki.ui.flashcards.model.CategoryFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _categoryFormState = MutableStateFlow(CategoryFormState())
    val categoryFormState: StateFlow<CategoryFormState> = _categoryFormState.asStateFlow()

    // Get current user ID from auth repository
    private val currentUserId: StateFlow<Long?> = authRepository.currentUser
        .map { it?.userId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    // Categories with learning statistics
    val categoriesWithStats = currentUserId
        .filterNotNull()
        .flatMapLatest { userId ->
            categoryDao.getUserCategoriesWithLearningStats(userId)
        }
        .catch { throwable ->
            _error.value = "Failed to load categories: ${throwable.message}"
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Load categories for the current user
     */
    fun loadCategories() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                // Categories are automatically loaded via StateFlow
            } catch (e: Exception) {
                _error.value = "Failed to load categories: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Create a new category
     */
    fun createCategory(name: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val userId = currentUserId.value
                if (userId == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }

                // Validate category name
                val validationResult = validateCategoryName(name)
                if (!validationResult) {
                    return@launch
                }

                // Check if category with this name already exists
                val existingCategory = categoryDao.getCategoryByName(name)
                if (existingCategory != null) {
                    _categoryFormState.value = _categoryFormState.value.copy(
                        nameError = "Category with this name already exists",
                        isValid = false
                    )
                    return@launch
                }

                // Create new category
                val category = Category(name = name.trim())
                val categoryId = categoryDao.insertCategory(category)

                if (categoryId > 0) {
                    // Reset form state
                    _categoryFormState.value = CategoryFormState()
                } else {
                    _error.value = "Failed to create category"
                }

            } catch (e: Exception) {
                _error.value = "Failed to create category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Validate category name and update form state
     */
    fun validateCategoryName(name: String): Boolean {
        val trimmedName = name.trim()
        
        when {
            trimmedName.isEmpty() -> {
                _categoryFormState.value = _categoryFormState.value.copy(
                    name = name,
                    nameError = "Category name cannot be empty",
                    isValid = false
                )
                return false
            }
            trimmedName.length > 100 -> {
                _categoryFormState.value = _categoryFormState.value.copy(
                    name = name,
                    nameError = "Category name cannot exceed 100 characters",
                    isValid = false
                )
                return false
            }
            else -> {
                _categoryFormState.value = _categoryFormState.value.copy(
                    name = name,
                    nameError = null,
                    isValid = true
                )
                return true
            }
        }
    }

    /**
     * Update category form field
     */
    fun updateCategoryFormField(name: String) {
        validateCategoryName(name)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Reset category form
     */
    fun resetCategoryForm() {
        _categoryFormState.value = CategoryFormState()
    }
}

package com.an.intelligence.flashyfishki.ui.flashcards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.domain.dao.FlashcardDao
import com.an.intelligence.flashyfishki.domain.model.Category
import com.an.intelligence.flashyfishki.domain.model.Flashcard
import com.an.intelligence.flashyfishki.domain.repository.AuthRepository
import com.an.intelligence.flashyfishki.ui.flashcards.model.FlashcardFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryFlashcardsViewModel @Inject constructor(
    private val flashcardDao: FlashcardDao,
    private val categoryDao: CategoryDao,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _filter = MutableStateFlow(FlashcardFilter())
    val filter: StateFlow<FlashcardFilter> = _filter.asStateFlow()

    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category.asStateFlow()

    private val _categoryId = MutableStateFlow<Long?>(null)

    // Get current user ID from auth repository
    private val currentUserId: StateFlow<Long?> = authRepository.currentUser
        .map { it?.userId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Raw flashcards from database
    private val rawFlashcards = combine(
        currentUserId,
        _categoryId
    ) { userId, categoryId ->
        if (userId != null && categoryId != null) {
            flashcardDao.getUserFlashcardsByCategory(userId, categoryId)
        } else {
            flowOf(emptyList())
        }
    }.flatMapLatest { it }
    .catch { throwable ->
        _error.value = "Failed to load flashcards: ${throwable.message}"
    }

    // Filtered and sorted flashcards
    val flashcards = combine(
        rawFlashcards,
        filter
    ) { flashcards, currentFilter ->
        var filteredList = flashcards

        // Apply learning status filter
        currentFilter.learningStatus?.let { status ->
            filteredList = filteredList.filter { it.learningStatus == status }
        }

        // Apply difficulty level filter
        currentFilter.difficultyLevel?.let { difficulty ->
            filteredList = filteredList.filter { it.difficultyLevel == difficulty }
        }

        // Apply sorting
        when (currentFilter.sortBy) {
            com.an.intelligence.flashyfishki.ui.flashcards.model.SortBy.CREATED_DATE_DESC -> 
                filteredList.sortedByDescending { it.createdAt }
            com.an.intelligence.flashyfishki.ui.flashcards.model.SortBy.CREATED_DATE_ASC -> 
                filteredList.sortedBy { it.createdAt }
            com.an.intelligence.flashyfishki.ui.flashcards.model.SortBy.LEARNING_STATUS -> 
                filteredList.sortedBy { it.learningStatus }
            com.an.intelligence.flashyfishki.ui.flashcards.model.SortBy.DIFFICULTY_LEVEL -> 
                filteredList.sortedBy { it.difficultyLevel }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Load flashcards for a specific category
     */
    fun loadFlashcards(categoryId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _categoryId.value = categoryId
                
                // Also load category details
                loadCategory(categoryId)
                
            } catch (e: Exception) {
                _error.value = "Failed to load flashcards: ${e.message}"
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
                
                if (category == null) {
                    _error.value = "Category not found"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load category: ${e.message}"
            }
        }
    }

    /**
     * Apply filter to flashcards
     */
    fun applyFilter(newFilter: FlashcardFilter) {
        _filter.value = newFilter
    }

    /**
     * Clear learning status filter
     */
    fun clearLearningStatusFilter() {
        _filter.value = _filter.value.copy(learningStatus = null)
    }

    /**
     * Clear difficulty level filter
     */
    fun clearDifficultyFilter() {
        _filter.value = _filter.value.copy(difficultyLevel = null)
    }

    /**
     * Reset all filters
     */
    fun resetFilters() {
        _filter.value = FlashcardFilter()
    }

    /**
     * Update sort order
     */
    fun updateSortBy(sortBy: com.an.intelligence.flashyfishki.ui.flashcards.model.SortBy) {
        _filter.value = _filter.value.copy(sortBy = sortBy)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Refresh data
     */
    fun refresh() {
        _categoryId.value?.let { categoryId ->
            loadFlashcards(categoryId)
        }
    }
}

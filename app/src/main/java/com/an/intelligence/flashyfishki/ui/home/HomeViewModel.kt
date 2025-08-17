package com.an.intelligence.flashyfishki.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.domain.dao.FlashcardDao
import com.an.intelligence.flashyfishki.domain.dao.ReportDao
import com.an.intelligence.flashyfishki.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeStats(
    val categoriesCount: Int = 0,
    val totalFlashcards: Int = 0,
    val cardsStudied: Int = 0,
    val successRate: Float = 0f
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val flashcardDao: FlashcardDao,
    private val reportDao: ReportDao,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Get current user ID from auth repository
    private val currentUserId: StateFlow<Long?> = authRepository.currentUser
        .map { it?.userId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

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

    // Home statistics
    val homeStats = currentUserId
        .filterNotNull()
        .map { userId ->
            try {
                val userStats = reportDao.getUserStatistics(userId)
                val categories = categoriesWithStats.value
                
                val successRate = if ((userStats.correctAnswers + userStats.incorrectAnswers) > 0) {
                    (userStats.correctAnswers.toFloat() / (userStats.correctAnswers + userStats.incorrectAnswers)) * 100f
                } else {
                    0f
                }

                HomeStats(
                    categoriesCount = categories.size,
                    totalFlashcards = userStats.flashcardCount,
                    cardsStudied = userStats.totalCardsReviewed,
                    successRate = successRate
                )
            } catch (e: Exception) {
                _error.value = "Failed to load statistics: ${e.message}"
                HomeStats()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeStats()
        )

    /**
     * Load home data
     */
    fun loadHomeData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                // Data is automatically loaded via StateFlows
            } catch (e: Exception) {
                _error.value = "Failed to load home data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}

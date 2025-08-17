package com.an.intelligence.flashyfishki.ui.study.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.intelligence.flashyfishki.domain.repository.StudyRepository
import com.an.intelligence.flashyfishki.ui.study.model.StudySessionStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudySummaryViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {
    
    private val _sessionStats = MutableStateFlow<StudySessionStats?>(null)
    val sessionStats: StateFlow<StudySessionStats?> = _sessionStats.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun initializeWithSessionStats(stats: StudySessionStats) {
        _sessionStats.value = stats
    }
    
    fun saveLearningStatistics(
        userId: Long,
        categoryId: Long,
        sessionStats: StudySessionStats
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                studyRepository.saveLearningStatistics(
                    userId = userId,
                    categoryId = categoryId,
                    sessionStats = sessionStats
                )
                _isLoading.value = false
            } catch (exception: Exception) {
                _error.value = "Failed to save session statistics: ${exception.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun getSessionSummary(): SessionSummary? {
        val stats = _sessionStats.value ?: return null
        
        return SessionSummary(
            totalCards = stats.totalCards,
            completedCards = stats.completedCards,
            correctAnswers = stats.correctAnswers,
            incorrectAnswers = stats.incorrectAnswers,
            accuracyPercentage = stats.accuracyPercentage,
            sessionDurationMinutes = stats.sessionDurationMinutes,
            hasValidData = stats.completedCards > 0
        )
    }
    
    fun clearError() {
        _error.value = null
    }
    
    data class SessionSummary(
        val totalCards: Int,
        val completedCards: Int,
        val correctAnswers: Int,
        val incorrectAnswers: Int,
        val accuracyPercentage: Float,
        val sessionDurationMinutes: Int,
        val hasValidData: Boolean
    )
}

package com.an.intelligence.flashyfishki.ui.study.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.intelligence.flashyfishki.domain.repository.StudyRepository
import com.an.intelligence.flashyfishki.ui.study.model.StudyAction
import com.an.intelligence.flashyfishki.ui.study.model.StudySessionState
import com.an.intelligence.flashyfishki.ui.study.model.StudySessionStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {
    
    private val _sessionState = MutableStateFlow<StudySessionState?>(null)
    val sessionState: StateFlow<StudySessionState?> = _sessionState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _completedSessionStats = MutableStateFlow<StudySessionStats?>(null)
    val completedSessionStats: StateFlow<StudySessionStats?> = _completedSessionStats.asStateFlow()
    
    fun handleAction(action: StudyAction) {
        when (action) {
            is StudyAction.ShowAnswer -> showAnswer()
            is StudyAction.CorrectAnswer -> evaluateAnswer(true)
            is StudyAction.IncorrectAnswer -> evaluateAnswer(false)
            is StudyAction.NextFlashcard -> nextFlashcard()
            is StudyAction.EndSession -> endSession()
            else -> {
                // Other actions are handled by different ViewModels
            }
        }
    }
    
    fun startStudySession(categoryId: Long, userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                studyRepository.getFlashcardsForStudy(userId, categoryId)
                    .catch { exception ->
                        _error.value = "Failed to load flashcards: ${exception.message}"
                        _isLoading.value = false
                    }
                    .collect { flashcards ->
                        if (flashcards.isEmpty()) {
                            _error.value = "No flashcards available for study in this category"
                        } else {
                            val sessionStats = StudySessionStats(
                                totalCards = flashcards.size,
                                sessionStartTime = System.currentTimeMillis()
                            )
                            
                            _sessionState.value = StudySessionState(
                                categoryId = categoryId,
                                flashcards = flashcards,
                                currentIndex = 0,
                                isAnswerVisible = false,
                                sessionStats = sessionStats
                            )
                        }
                        _isLoading.value = false
                    }
            } catch (exception: Exception) {
                _error.value = "Failed to start study session: ${exception.message}"
                _isLoading.value = false
            }
        }
    }
    
    private fun showAnswer() {
        _sessionState.value?.let { currentState ->
            _sessionState.value = currentState.copy(isAnswerVisible = true)
        }
    }
    
    private fun evaluateAnswer(isCorrect: Boolean) {
        val currentState = _sessionState.value ?: return
        
        if (!currentState.isAnswerVisible) {
            _error.value = "Answer must be visible before evaluation"
            return
        }
        
        val currentFlashcard = currentState.flashcards.getOrNull(currentState.currentIndex) ?: return
        
        viewModelScope.launch {
            try {
                // Update flashcard learning status in repository
                studyRepository.updateFlashcardLearningStatus(
                    flashcardId = currentFlashcard.flashcardId,
                    isCorrect = isCorrect,
                    userId = currentFlashcard.userId
                )
                
                // Update session statistics
                val updatedStats = currentState.sessionStats.copy(
                    completedCards = currentState.sessionStats.completedCards + 1,
                    correctAnswers = if (isCorrect) currentState.sessionStats.correctAnswers + 1 
                                   else currentState.sessionStats.correctAnswers,
                    incorrectAnswers = if (!isCorrect) currentState.sessionStats.incorrectAnswers + 1 
                                     else currentState.sessionStats.incorrectAnswers
                )
                
                _sessionState.value = currentState.copy(
                    sessionStats = updatedStats
                )
                
                // Automatically move to next flashcard after a short delay
                nextFlashcard()
                
            } catch (exception: Exception) {
                _error.value = "Failed to update flashcard status: ${exception.message}"
            }
        }
    }
    
    private fun nextFlashcard() {
        val currentState = _sessionState.value ?: return
        
        if (currentState.currentIndex < currentState.flashcards.size - 1) {
            // Move to next flashcard
            _sessionState.value = currentState.copy(
                currentIndex = currentState.currentIndex + 1,
                isAnswerVisible = false
            )
        } else {
            // Session completed - all flashcards reviewed
            endSession()
        }
    }
    
    private fun endSession() {
        val currentState = _sessionState.value ?: return
        
        val finalStats = currentState.sessionStats.copy(
            sessionEndTime = System.currentTimeMillis()
        )
        
        viewModelScope.launch {
            try {
                // Save session statistics
                studyRepository.saveLearningStatistics(
                    userId = currentState.flashcards.firstOrNull()?.userId ?: 0L,
                    categoryId = currentState.categoryId,
                    sessionStats = finalStats
                )
                
                // Update session state with final stats and store completed stats
                _sessionState.value = currentState.copy(sessionStats = finalStats)
                _completedSessionStats.value = finalStats
                
            } catch (exception: Exception) {
                _error.value = "Failed to save session statistics: ${exception.message}"
            }
        }
    }
    
    fun getCurrentFlashcard() = _sessionState.value?.let { state ->
        state.flashcards.getOrNull(state.currentIndex)
    }
    
    fun getSessionProgress() = _sessionState.value?.let { state ->
        if (state.flashcards.isNotEmpty()) {
            (state.currentIndex + 1).toFloat() / state.flashcards.size.toFloat()
        } else {
            0f
        }
    } ?: 0f
    
    fun canEvaluateAnswer() = _sessionState.value?.isAnswerVisible == true
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearCompletedSessionStats() {
        _completedSessionStats.value = null
    }
}

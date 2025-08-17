package com.an.intelligence.flashyfishki.ui.study.model

import com.an.intelligence.flashyfishki.domain.model.Flashcard
import kotlinx.serialization.Serializable

/**
 * Data class representing category with study statistics
 */
data class CategoryWithStudyStats(
    val categoryId: Long,
    val name: String,
    val totalFlashcards: Int,
    val flashcardsToReview: Int, // statusy 0-2
    val newFlashcards: Int, // status 0
    val reviewFlashcards: Int // statusy 1-2
)

/**
 * State for study session
 */
data class StudySessionState(
    val categoryId: Long,
    val flashcards: List<Flashcard>,
    val currentIndex: Int = 0,
    val isAnswerVisible: Boolean = false,
    val sessionStats: StudySessionStats = StudySessionStats()
)

/**
 * Statistics for study session
 */
@Serializable
data class StudySessionStats(
    val totalCards: Int = 0,
    val completedCards: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val sessionStartTime: Long = System.currentTimeMillis(),
    val sessionEndTime: Long? = null
) {
    val sessionDurationMinutes: Int
        get() = (((sessionEndTime ?: System.currentTimeMillis()) - sessionStartTime) / (1000 * 60)).toInt()
    
    val accuracyPercentage: Float
        get() = if (completedCards > 0) (correctAnswers.toFloat() / completedCards) * 100 else 0f
}

/**
 * UI state for study selection screen
 */
data class StudyUiState(
    val isLoading: Boolean = false,
    val categories: List<CategoryWithStudyStats> = emptyList(),
    val error: String? = null
)

/**
 * Actions for study functionality
 */
sealed class StudyAction {
    object LoadCategories : StudyAction()
    data class StartStudy(val categoryId: Long) : StudyAction()
    object ShowAnswer : StudyAction()
    object CorrectAnswer : StudyAction()
    object IncorrectAnswer : StudyAction()
    object NextFlashcard : StudyAction()
    object EndSession : StudyAction()
}

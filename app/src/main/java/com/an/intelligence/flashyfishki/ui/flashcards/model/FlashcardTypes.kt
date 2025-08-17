package com.an.intelligence.flashyfishki.ui.flashcards.model

import androidx.compose.ui.graphics.Color

/**
 * State for flashcard form (create/edit)
 */
data class FlashcardFormState(
    val question: String = "",
    val answer: String = "",
    val categoryId: Long = 0L,
    val difficultyLevel: Int = 1,
    val isPublic: Boolean = false,
    val questionError: String? = null,
    val answerError: String? = null,
    val categoryError: String? = null,
    val isValid: Boolean = false
)

/**
 * State for category form (create)
 */
data class CategoryFormState(
    val name: String = "",
    val nameError: String? = null,
    val isValid: Boolean = false
)

/**
 * Export progress tracking
 */
data class ExportProgress(
    val isExporting: Boolean = false,
    val progress: Float = 0f,
    val exportedCount: Int = 0,
    val totalCount: Int = 0,
    val isComplete: Boolean = false,
    val error: String? = null,
    val filePath: String? = null
)

/**
 * Flashcard filtering options
 */
data class FlashcardFilter(
    val learningStatus: Int? = null,
    val difficultyLevel: Int? = null,
    val sortBy: SortBy = SortBy.CREATED_DATE_DESC
)

enum class SortBy {
    CREATED_DATE_DESC,
    CREATED_DATE_ASC,
    LEARNING_STATUS,
    DIFFICULTY_LEVEL
}

/**
 * Form validation result
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)

/**
 * Learning status enum with display properties
 */
enum class LearningStatus(val value: Int, val displayName: String, val color: Color) {
    NEW(0, "Nowe", Color.Gray),
    FIRST_REPEAT(1, "Pierwsza powtórka", Color(0xFFFFEB3B)), // Yellow
    SECOND_REPEAT(2, "Druga powtórka", Color(0xFF2196F3)), // Blue
    LEARNED(3, "Nauczone", Color(0xFF4CAF50)); // Green

    companion object {
        fun fromValue(value: Int): LearningStatus {
            return values().find { it.value == value } ?: NEW
        }
    }
}

/**
 * Delete confirmation state
 */
data class DeleteConfirmationState(
    val isShowing: Boolean = false,
    val itemName: String = "",
    val onConfirm: () -> Unit = {},
    val onDismiss: () -> Unit = {}
)

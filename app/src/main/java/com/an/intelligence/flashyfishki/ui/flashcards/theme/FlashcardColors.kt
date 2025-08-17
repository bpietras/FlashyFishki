package com.an.intelligence.flashyfishki.ui.flashcards.theme

import androidx.compose.ui.graphics.Color

/**
 * Custom colors for flashcard module
 */
object FlashcardColors {
    
    // Learning status colors - more vibrant and accessible
    val NewStatus = Color(0xFF6B7280) // Cool gray
    val FirstRepeatStatus = Color(0xFFF59E0B) // Amber
    val SecondRepeatStatus = Color(0xFF3B82F6) // Blue
    val LearnedStatus = Color(0xFF10B981) // Emerald
    
    // Difficulty level colors
    val DifficultyEasy = Color(0xFF22C55E) // Green
    val DifficultyMedium = Color(0xFFF59E0B) // Amber
    val DifficultyHard = Color(0xFFEF4444) // Red
    
    // Progress colors
    val ProgressBackground = Color(0xFFE5E7EB)
    val ProgressForeground = Color(0xFF3B82F6)
    
    // Export status colors
    val ExportSuccess = Color(0xFF059669)
    val ExportError = Color(0xFFDC2626)
    val ExportWarning = Color(0xFFD97706)
}

/**
 * Get difficulty color based on level
 */
fun getDifficultyColor(level: Int): Color {
    return when (level) {
        1, 2 -> FlashcardColors.DifficultyEasy
        3 -> FlashcardColors.DifficultyMedium
        4, 5 -> FlashcardColors.DifficultyHard
        else -> FlashcardColors.DifficultyMedium
    }
}

/**
 * Get learning status color with improved accessibility
 */
fun getLearningStatusColor(status: Int): Color {
    return when (status) {
        0 -> FlashcardColors.NewStatus
        1 -> FlashcardColors.FirstRepeatStatus
        2 -> FlashcardColors.SecondRepeatStatus
        3 -> FlashcardColors.LearnedStatus
        else -> FlashcardColors.NewStatus
    }
}

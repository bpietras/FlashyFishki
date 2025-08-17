package com.an.intelligence.flashyfishki.domain.repository

import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.domain.dao.FlashcardDao
import com.an.intelligence.flashyfishki.domain.dao.UserDao
import com.an.intelligence.flashyfishki.domain.model.Flashcard
import com.an.intelligence.flashyfishki.ui.study.model.CategoryWithStudyStats
import com.an.intelligence.flashyfishki.ui.study.model.StudySessionStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyRepository @Inject constructor(
    private val flashcardDao: FlashcardDao,
    private val categoryDao: CategoryDao,
    private val userDao: UserDao
) {
    
    /**
     * Get categories with study statistics for a user
     */
    fun getCategoriesWithStudyStats(userId: Long): Flow<List<CategoryWithStudyStats>> {
        return categoryDao.getUserCategoriesWithLearningStats(userId).map { categories ->
            categories.map { category ->
                CategoryWithStudyStats(
                    categoryId = category.categoryId,
                    name = category.name,
                    totalFlashcards = category.flashcardCount,
                    flashcardsToReview = category.newCount + category.firstRepeatCount + category.secondRepeatCount,
                    newFlashcards = category.newCount,
                    reviewFlashcards = category.firstRepeatCount + category.secondRepeatCount
                )
            }
        }
    }
    
    /**
     * Get flashcards ready for review in a specific category
     */
    fun getFlashcardsForStudy(userId: Long, categoryId: Long): Flow<List<Flashcard>> {
        val currentDate = Date()
        return flashcardDao.getFlashcardsForReviewByCategory(userId, categoryId, currentDate)
    }
    
    /**
     * Update flashcard learning status based on user's answer
     * Implements the 3-5-7 spaced repetition algorithm
     */
    suspend fun updateFlashcardLearningStatus(
        flashcardId: Long,
        isCorrect: Boolean,
        userId: Long
    ) {
        val flashcard = flashcardDao.getFlashcardById(flashcardId)
        
        if (flashcard != null && flashcard.userId == userId) {
            val newStatus: Int
            val nextReviewDate: Date?
            
            if (isCorrect) {
                // Correct answer: increment status
                newStatus = minOf(flashcard.learningStatus + 1, 3)
                nextReviewDate = calculateNextReviewDate(newStatus)
            } else {
                // Incorrect answer: reset to status 0
                newStatus = 0
                nextReviewDate = null // Review immediately
            }
            
            val updateTime = Date()
            flashcardDao.updateFlashcardLearningStatus(
                flashcardId = flashcardId,
                newStatus = newStatus,
                nextReviewDate = nextReviewDate,
                updateTime = updateTime
            )
            
            // Update user statistics
            userDao.incrementTotalCardsReviewed(userId)
            if (isCorrect) {
                userDao.incrementCorrectAnswers(userId)
            } else {
                userDao.incrementIncorrectAnswers(userId)
            }
        }
    }
    
    /**
     * Calculate next review date based on learning status
     * Implements 3-5-7 algorithm:
     * - Status 1: Review in 3 days
     * - Status 2: Review in 5 days  
     * - Status 3: Review in 7 days (learned)
     */
    private fun calculateNextReviewDate(status: Int): Date? {
        if (status == 0) return null // Review immediately
        
        val calendar = Calendar.getInstance()
        when (status) {
            1 -> calendar.add(Calendar.DAY_OF_YEAR, 3) // First repeat: 3 days
            2 -> calendar.add(Calendar.DAY_OF_YEAR, 5) // Second repeat: 5 days
            3 -> calendar.add(Calendar.DAY_OF_YEAR, 7) // Third repeat: 7 days (learned)
            else -> return null
        }
        
        return calendar.time
    }
    
    /**
     * Save learning session statistics
     */
    suspend fun saveLearningStatistics(
        userId: Long,
        categoryId: Long,
        sessionStats: StudySessionStats
    ) {
        // This will be implemented when LearningStatisticsDao is created
        // For now, we just update user statistics which are already handled
        // in updateFlashcardLearningStatus method
    }
    
    /**
     * Get total cards to review for a user across all categories
     */
    suspend fun getTotalCardsToReview(userId: Long): Int {
        val currentDate = Date()
        return flashcardDao.getFlashcardsForReview(userId, currentDate).let { flow ->
            // Convert Flow to list to get count
            // In real implementation, we might want to have a specific DAO method for counting
            0 // Placeholder - would need proper implementation
        }
    }
}

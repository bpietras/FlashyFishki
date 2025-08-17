package com.an.intelligence.flashyfishki.domain.dao

import androidx.room.Dao
import androidx.room.Query
import java.util.Date

@Dao
interface ReportDao {
    
    // Tygodniowe statystyki użytkownika
    @Query("""
        SELECT 
            COUNT(DISTINCT f.flashcardId) as reviewedCount,
            SUM(ls.correctAnswersCount) as correctCount,
            SUM(ls.incorrectAnswersCount) as incorrectCount
        FROM flashcards f
        JOIN learningStatistics ls ON f.flashcardId = ls.flashcardId
        WHERE f.userId = :userId
        AND ls.lastUpdated BETWEEN :startDate AND :endDate
    """)
    suspend fun getWeeklyStatistics(userId: Long, startDate: Date, endDate: Date): WeeklyStats
    
    // Statystyki użytkownika dla poszczególnych kategorii
    @Query("""
        SELECT 
            f.categoryId as categoryId,
            c.name as categoryName,
            COUNT(f.flashcardId) as flashcardCount,
            SUM(CASE WHEN f.learningStatus = 3 THEN 1 ELSE 0 END) as learnedCount,
            SUM(ls.correctAnswersCount) as correctAnswersCount,
            SUM(ls.incorrectAnswersCount) as incorrectAnswersCount
        FROM flashcards f
        JOIN categories c ON f.categoryId = c.categoryId
        LEFT JOIN learningStatistics ls ON f.flashcardId = ls.flashcardId
        WHERE f.userId = :userId
        GROUP BY f.categoryId
        ORDER BY c.name ASC
    """)
    suspend fun getUserLearningStatisticsByCategory(userId: Long): List<CategoryLearningStatistics>
    
    // Statystyki tygodniowe z podziałem na kategorie
    @Query("""
        SELECT 
            c.categoryId as categoryId,
            c.name as categoryName,
            SUM(ls.correctAnswersCount) as correctCount,
            SUM(ls.incorrectAnswersCount) as incorrectCount
        FROM learningStatistics ls
        JOIN flashcards f ON ls.flashcardId = f.flashcardId
        JOIN categories c ON f.categoryId = c.categoryId
        WHERE f.userId = :userId
        AND ls.lastUpdated BETWEEN :startDate AND :endDate
        GROUP BY c.categoryId
        ORDER BY c.name
    """)
    suspend fun getWeeklyStatisticsByCategory(
        userId: Long, 
        startDate: Date, 
        endDate: Date
    ): List<CategoryWeeklySummary>
    
    // Podsumowanie profilu użytkownika
    @Query("""
        SELECT 
            u.*, 
            COUNT(f.flashcardId) as flashcardCount,
            SUM(CASE WHEN f.isPublic = 1 THEN 1 ELSE 0 END) as publicFlashcardsCount,
            SUM(CASE WHEN f.originalFlashcardId IS NOT NULL THEN 1 ELSE 0 END) as copiedFlashcardsCount
        FROM users u
        LEFT JOIN flashcards f ON u.userId = f.userId
        WHERE u.userId = :userId
        GROUP BY u.userId
    """)
    suspend fun getUserStatistics(userId: Long): UserStatistics
    
    // Klasy dla danych wynikowych
    data class WeeklyStats(
        val reviewedCount: Int,
        val correctCount: Int,
        val incorrectCount: Int
    )
    
    data class CategoryLearningStatistics(
        val categoryId: Long,
        val categoryName: String,
        val flashcardCount: Int,
        val learnedCount: Int,
        val correctAnswersCount: Int,
        val incorrectAnswersCount: Int
    )
    
    data class CategoryWeeklySummary(
        val categoryId: Long,
        val categoryName: String,
        val correctCount: Int,
        val incorrectCount: Int
    )
    
    data class UserStatistics(
        val userId: Long,
        val email: String,
        val lastLoginDate: Date?,
        val totalCardsReviewed: Int,
        val correctAnswers: Int,
        val incorrectAnswers: Int,
        val createdAt: Date,
        val flashcardCount: Int,
        val publicFlashcardsCount: Int,
        val copiedFlashcardsCount: Int
    )
}

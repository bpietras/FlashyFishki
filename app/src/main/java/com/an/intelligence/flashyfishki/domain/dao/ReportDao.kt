package com.an.intelligence.flashyfishki.domain.dao

import androidx.room.Dao
import androidx.room.Query
import java.util.Date

@Dao
interface ReportDao {
    
    // Tygodniowe statystyki użytkownika
    @Query("""
        SELECT 
            COUNT(DISTINCT f.id) as reviewedCount,
            SUM(ls.correct_answers_count) as correctCount,
            SUM(ls.incorrect_answers_count) as incorrectCount
        FROM flashcards f
        JOIN learning_statistics ls ON f.id = ls.flashcard_id
        WHERE f.user_id = :userId
        AND ls.last_updated BETWEEN :startDate AND :endDate
    """)
    suspend fun getWeeklyStatistics(userId: Long, startDate: Date, endDate: Date): WeeklyStats
    
    // Statystyki użytkownika dla poszczególnych kategorii
    @Query("""
        SELECT 
            f.category_id as categoryId,
            c.name as categoryName,
            COUNT(f.id) as flashcardCount,
            SUM(CASE WHEN f.learning_status = 3 THEN 1 ELSE 0 END) as learnedCount,
            SUM(ls.correct_answers_count) as correctAnswersCount,
            SUM(ls.incorrect_answers_count) as incorrectAnswersCount
        FROM flashcards f
        JOIN categories c ON f.category_id = c.id
        LEFT JOIN learning_statistics ls ON f.id = ls.flashcard_id
        WHERE f.user_id = :userId
        GROUP BY f.category_id
        ORDER BY c.name ASC
    """)
    suspend fun getUserLearningStatisticsByCategory(userId: Long): List<CategoryLearningStatistics>
    
    // Statystyki tygodniowe z podziałem na kategorie
    @Query("""
        SELECT 
            c.id as categoryId,
            c.name as categoryName,
            SUM(ls.correct_answers_count) as correctCount,
            SUM(ls.incorrect_answers_count) as incorrectCount
        FROM learning_statistics ls
        JOIN flashcards f ON ls.flashcard_id = f.id
        JOIN categories c ON f.category_id = c.id
        WHERE f.user_id = :userId
        AND ls.last_updated BETWEEN :startDate AND :endDate
        GROUP BY c.id
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
            COUNT(f.id) as flashcard_count,
            SUM(CASE WHEN f.is_public = 1 THEN 1 ELSE 0 END) as public_flashcards_count,
            SUM(CASE WHEN f.original_flashcard_id IS NOT NULL THEN 1 ELSE 0 END) as copied_flashcards_count
        FROM users u
        LEFT JOIN flashcards f ON u.id = f.user_id
        WHERE u.id = :userId
        GROUP BY u.id
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

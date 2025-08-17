package com.an.intelligence.flashyfishki.domain.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.an.intelligence.flashyfishki.domain.model.LearningStatistics
import java.util.Date
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningStatisticsDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(learningStatistics: LearningStatistics): Long
    
    @Update
    suspend fun updateStatistics(learningStatistics: LearningStatistics)
    
    @Delete
    suspend fun deleteStatistics(learningStatistics: LearningStatistics)
    
    @Query("SELECT * FROM learning_statistics WHERE flashcard_id = :flashcardId")
    suspend fun getStatisticsByFlashcardId(flashcardId: Long): LearningStatistics?
    
    @Query("""
        SELECT * FROM learning_statistics 
        WHERE flashcard_id IN (
            SELECT id FROM flashcards WHERE user_id = :userId
        )
    """)
    fun getUserStatistics(userId: Long): Flow<List<LearningStatistics>>
    
    @Query("""
        SELECT * FROM learning_statistics 
        WHERE flashcard_id IN (
            SELECT id FROM flashcards 
            WHERE user_id = :userId AND category_id = :categoryId
        )
    """)
    fun getUserStatisticsByCategory(userId: Long, categoryId: Long): Flow<List<LearningStatistics>>
    
    @Query("""
        UPDATE learning_statistics 
        SET correct_answers_count = correct_answers_count + 1,
            last_updated = :updateTime
        WHERE flashcard_id = :flashcardId
    """)
    suspend fun incrementCorrectAnswers(flashcardId: Long, updateTime: Date)
    
    @Query("""
        UPDATE learning_statistics 
        SET incorrect_answers_count = incorrect_answers_count + 1,
            last_updated = :updateTime
        WHERE flashcard_id = :flashcardId
    """)
    suspend fun incrementIncorrectAnswers(flashcardId: Long, updateTime: Date)
    
    @Transaction
    suspend fun recordAnswer(flashcardId: Long, isCorrect: Boolean) {
        val currentTime = Date()
        var statistics = getStatisticsByFlashcardId(flashcardId)
        
        if (statistics == null) {
            statistics = LearningStatistics(
                flashcardId = flashcardId,
                correctAnswersCount = 0,
                incorrectAnswersCount = 0,
                lastUpdated = currentTime
            )
            insertStatistics(statistics)
        }
        
        if (isCorrect) {
            incrementCorrectAnswers(flashcardId, currentTime)
        } else {
            incrementIncorrectAnswers(flashcardId, currentTime)
        }
    }
    
    @Query("""
        SELECT 
            SUM(ls.correct_answers_count) as totalCorrect,
            SUM(ls.incorrect_answers_count) as totalIncorrect,
            COUNT(DISTINCT f.id) as uniqueFlashcardsReviewed
        FROM learning_statistics ls
        JOIN flashcards f ON ls.flashcard_id = f.id
        WHERE f.user_id = :userId
        AND ls.last_updated BETWEEN :startDate AND :endDate
    """)
    suspend fun getWeeklyReviewSummary(
        userId: Long, 
        startDate: Date, 
        endDate: Date
    ): WeeklyReviewSummary
    
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
    
    data class WeeklyReviewSummary(
        val totalCorrect: Int,
        val totalIncorrect: Int,
        val uniqueFlashcardsReviewed: Int
    )
    
    data class CategoryWeeklySummary(
        val categoryId: Long,
        val categoryName: String,
        val correctCount: Int,
        val incorrectCount: Int
    )
}

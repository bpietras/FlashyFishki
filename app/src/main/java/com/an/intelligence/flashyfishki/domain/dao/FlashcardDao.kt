package com.an.intelligence.flashyfishki.domain.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.an.intelligence.flashyfishki.domain.model.Flashcard
import java.util.Date
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard): Long
    
    @Update
    suspend fun updateFlashcard(flashcard: Flashcard)
    
    @Delete
    suspend fun deleteFlashcard(flashcard: Flashcard)
    
    @Query("SELECT * FROM flashcards WHERE id = :flashcardId")
    suspend fun getFlashcardById(flashcardId: Long): Flashcard?
    
    @Query("SELECT * FROM flashcards WHERE user_id = :userId ORDER BY created_at DESC")
    fun getUserFlashcards(userId: Long): Flow<List<Flashcard>>
    
    @Query("SELECT * FROM flashcards WHERE user_id = :userId AND category_id = :categoryId ORDER BY created_at DESC")
    fun getUserFlashcardsByCategory(userId: Long, categoryId: Long): Flow<List<Flashcard>>
    
    @Query("""
        SELECT * FROM flashcards 
        WHERE user_id = :userId 
        AND learning_status = :learningStatus 
        ORDER BY next_review_date ASC, created_at DESC
    """)
    fun getUserFlashcardsByLearningStatus(userId: Long, learningStatus: Int): Flow<List<Flashcard>>
    
    @Query("""
        SELECT * FROM flashcards 
        WHERE user_id = :userId 
        AND category_id = :categoryId 
        AND learning_status = :learningStatus 
        ORDER BY next_review_date ASC, created_at DESC
    """)
    fun getUserFlashcardsByCategoryAndLearningStatus(
        userId: Long, 
        categoryId: Long, 
        learningStatus: Int
    ): Flow<List<Flashcard>>
    
    @Query("""
        SELECT * FROM flashcards 
        WHERE user_id = :userId 
        AND next_review_date <= :currentDate 
        AND learning_status < 3
        ORDER BY learning_status DESC, next_review_date ASC
    """)
    fun getFlashcardsForReview(userId: Long, currentDate: Date): Flow<List<Flashcard>>
    
    @Query("""
        SELECT * FROM flashcards 
        WHERE user_id = :userId 
        AND category_id = :categoryId 
        AND next_review_date <= :currentDate 
        AND learning_status < 3
        ORDER BY learning_status DESC, next_review_date ASC
    """)
    fun getFlashcardsForReviewByCategory(
        userId: Long, 
        categoryId: Long, 
        currentDate: Date
    ): Flow<List<Flashcard>>
    
    @Query("SELECT * FROM flashcards WHERE is_public = 1 ORDER BY created_at DESC")
    fun getPublicFlashcards(): Flow<List<Flashcard>>
    
    @Query("""
        SELECT * FROM flashcards 
        WHERE is_public = 1 
        AND category_id = :categoryId 
        ORDER BY created_at DESC
    """)
    fun getPublicFlashcardsByCategory(categoryId: Long): Flow<List<Flashcard>>
    
    @Query("""
        SELECT * FROM flashcards 
        WHERE is_public = 1 
        AND difficulty_level = :difficultyLevel 
        ORDER BY created_at DESC
    """)
    fun getPublicFlashcardsByDifficulty(difficultyLevel: Int): Flow<List<Flashcard>>
    
    @Query("""
        SELECT * FROM flashcards 
        WHERE is_public = 1 
        AND category_id = :categoryId 
        AND difficulty_level = :difficultyLevel 
        ORDER BY created_at DESC
    """)
    fun getPublicFlashcardsByCategoryAndDifficulty(
        categoryId: Long, 
        difficultyLevel: Int
    ): Flow<List<Flashcard>>
    
    @Query("SELECT COUNT(*) FROM flashcards WHERE user_id = :userId")
    suspend fun countUserFlashcards(userId: Long): Int
    
    @Query("SELECT COUNT(*) FROM flashcards WHERE user_id = :userId AND learning_status = 3")
    suspend fun countLearnedFlashcards(userId: Long): Int
    
    @Query("""
        UPDATE flashcards 
        SET learning_status = :newStatus,
            next_review_date = :nextReviewDate,
            updated_at = :updateTime
        WHERE id = :flashcardId
    """)
    suspend fun updateFlashcardLearningStatus(
        flashcardId: Long, 
        newStatus: Int, 
        nextReviewDate: Date?, 
        updateTime: Date
    )
    
    @Query("""
        UPDATE flashcards 
        SET is_public = :isPublic,
            updated_at = :updateTime
        WHERE id = :flashcardId
    """)
    suspend fun updateFlashcardPublicStatus(flashcardId: Long, isPublic: Boolean, updateTime: Date)
    
    @Query("""
        UPDATE flashcards 
        SET copies_count = copies_count + 1
        WHERE id = :originalFlashcardId
    """)
    suspend fun incrementCopiesCount(originalFlashcardId: Long)
    
    @Transaction
    suspend fun copyPublicFlashcard(flashcardId: Long, newUserId: Long): Long? {
        val originalFlashcard = getFlashcardById(flashcardId)
        
        return if (originalFlashcard != null && originalFlashcard.isPublic) {
            val currentDate = Date()
            val copiedFlashcard = Flashcard(
                userId = newUserId,
                categoryId = originalFlashcard.categoryId,
                question = originalFlashcard.question,
                answer = originalFlashcard.answer,
                difficultyLevel = originalFlashcard.difficultyLevel,
                learningStatus = 0, // Reset to new
                nextReviewDate = null, // Reset review date
                isPublic = false, // Set to private by default
                originalFlashcardId = flashcardId,
                copiesCount = 0,
                createdAt = currentDate,
                updatedAt = currentDate
            )
            
            val newId = insertFlashcard(copiedFlashcard)
            incrementCopiesCount(flashcardId)
            newId
        } else {
            null
        }
    }
    
    // Methods for weekly reports
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
    
    @Query("""
        SELECT
            COUNT(f.id) as reviewedCount,
            SUM(ls.correct_answers_count) as correctCount,
            SUM(ls.incorrect_answers_count) as incorrectCount
        FROM flashcards f
        JOIN learning_statistics ls ON f.id = ls.flashcard_id
        WHERE f.user_id = :userId
        AND ls.last_updated BETWEEN :startDate AND :endDate
    """)
    suspend fun getWeeklyLearningStatistics(
        userId: Long, 
        startDate: Date, 
        endDate: Date
    ): WeeklyLearningStatistics
    
    data class CategoryLearningStatistics(
        val categoryId: Long,
        val categoryName: String,
        val flashcardCount: Int,
        val learnedCount: Int,
        val correctAnswersCount: Int,
        val incorrectAnswersCount: Int
    )
    
    data class WeeklyLearningStatistics(
        val reviewedCount: Int,
        val correctCount: Int,
        val incorrectCount: Int
    )
}

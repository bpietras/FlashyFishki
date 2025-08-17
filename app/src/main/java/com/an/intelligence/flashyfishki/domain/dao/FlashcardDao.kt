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
    
    @Query("SELECT * FROM flashcards WHERE flashcardId = :flashcardId")
    suspend fun getFlashcardById(flashcardId: Long): Flashcard?
    
    @Query("SELECT * FROM flashcards WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserFlashcards(userId: Long): Flow<List<Flashcard>>
    
    @Query("SELECT * FROM flashcards WHERE userId = :userId AND categoryId = :categoryId ORDER BY createdAt DESC")
    fun getUserFlashcardsByCategory(userId: Long, categoryId: Long): Flow<List<Flashcard>>
    
    @Query("""
        SELECT * FROM flashcards 
        WHERE userId = :userId 
        AND learningStatus = :learningStatus 
        ORDER BY nextReviewDate ASC, createdAt DESC
    """)
    fun getUserFlashcardsByLearningStatus(userId: Long, learningStatus: Int): Flow<List<Flashcard>>
    
    @Query("""
        SELECT * FROM flashcards 
        WHERE userId = :userId 
        AND categoryId = :categoryId 
        AND learningStatus = :learningStatus 
        ORDER BY nextReviewDate ASC, createdAt DESC
    """)
    fun getUserFlashcardsByCategoryAndLearningStatus(
        userId: Long, 
        categoryId: Long, 
        learningStatus: Int
    ): Flow<List<Flashcard>>
    
    @Query("""
        SELECT * FROM flashcards 
        WHERE userId = :userId 
        AND nextReviewDate <= :currentDate 
        AND learningStatus < 3
        ORDER BY learningStatus DESC, nextReviewDate ASC
    """)
    fun getFlashcardsForReview(userId: Long, currentDate: Date): Flow<List<Flashcard>>
    
    @Query("""
        SELECT * FROM flashcards 
        WHERE userId = :userId 
        AND categoryId = :categoryId 
        AND nextReviewDate <= :currentDate 
        AND learningStatus < 3
        ORDER BY learningStatus DESC, nextReviewDate ASC
    """)
    fun getFlashcardsForReviewByCategory(
        userId: Long, 
        categoryId: Long, 
        currentDate: Date
    ): Flow<List<Flashcard>>
    
    @Query("SELECT * FROM flashcards WHERE isPublic = 1 ORDER BY createdAt DESC")
    fun getPublicFlashcards(): Flow<List<Flashcard>>
    
    @Query("""
        SELECT * FROM flashcards 
        WHERE isPublic = 1 
        AND categoryId = :categoryId 
        ORDER BY createdAt DESC
    """)
    fun getPublicFlashcardsByCategory(categoryId: Long): Flow<List<Flashcard>>
    
    @Query("""
        SELECT * FROM flashcards 
        WHERE isPublic = 1 
        AND difficultyLevel = :difficultyLevel 
        ORDER BY createdAt DESC
    """)
    fun getPublicFlashcardsByDifficulty(difficultyLevel: Int): Flow<List<Flashcard>>
    
    @Query("""
        SELECT * FROM flashcards 
        WHERE isPublic = 1 
        AND categoryId = :categoryId 
        AND difficultyLevel = :difficultyLevel 
        ORDER BY createdAt DESC
    """)
    fun getPublicFlashcardsByCategoryAndDifficulty(
        categoryId: Long, 
        difficultyLevel: Int
    ): Flow<List<Flashcard>>
    
    @Query("SELECT COUNT(*) FROM flashcards WHERE userId = :userId")
    suspend fun countUserFlashcards(userId: Long): Int
    
    @Query("SELECT COUNT(*) FROM flashcards WHERE userId = :userId AND learningStatus = 3")
    suspend fun countLearnedFlashcards(userId: Long): Int
    
    @Query("""
        UPDATE flashcards 
        SET learningStatus = :newStatus,
            nextReviewDate = :nextReviewDate,
            updatedAt = :updateTime
        WHERE flashcardId = :flashcardId
    """)
    suspend fun updateFlashcardLearningStatus(
        flashcardId: Long, 
        newStatus: Int, 
        nextReviewDate: Date?, 
        updateTime: Date
    )
    
    @Query("""
        UPDATE flashcards 
        SET isPublic = :isPublic,
            updatedAt = :updateTime
        WHERE flashcardId = :flashcardId
    """)
    suspend fun updateFlashcardPublicStatus(flashcardId: Long, isPublic: Boolean, updateTime: Date)
    
    @Query("""
        UPDATE flashcards 
        SET copiesCount = copiesCount + 1
        WHERE flashcardId = :originalFlashcardId
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
    
    // Usunięto metody raportowe, przeniesione do ReportDao
}

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
    
    /**
     * Rejestruje odpowiedź na fiszkę i aktualizuje statystyki
     * 
     * @param flashcardId ID fiszki
     * @param userId ID użytkownika
     * @param isCorrect czy odpowiedź była poprawna
     * @param userDao referencja do UserDao potrzebna do aktualizacji statystyk użytkownika
     */
    @Transaction
    suspend fun recordAnswer(flashcardId: Long, userId: Long, isCorrect: Boolean, userDao: UserDao) {
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
        
        // Aktualizacja statystyk fiszki
        if (isCorrect) {
            incrementCorrectAnswers(flashcardId, currentTime)
        } else {
            incrementIncorrectAnswers(flashcardId, currentTime)
        }
        
        // Aktualizacja statystyk użytkownika w tabeli User
        if (isCorrect) {
            userDao.incrementCorrectAnswers(userId)
        } else {
            userDao.incrementIncorrectAnswers(userId)
        }
        userDao.incrementTotalCardsReviewed(userId)
    }
    
    // Metody raportowe przeniesione do ReportDao
}

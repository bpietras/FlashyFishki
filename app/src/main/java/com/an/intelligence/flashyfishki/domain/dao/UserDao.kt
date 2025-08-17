package com.an.intelligence.flashyfishki.domain.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.an.intelligence.flashyfishki.domain.model.User
import java.util.Date
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): User?
    
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
    
    @Query("UPDATE users SET last_login_date = :loginDate WHERE id = :userId")
    suspend fun updateLastLoginDate(userId: Long, loginDate: Date)
    
    @Query("UPDATE users SET total_cards_reviewed = total_cards_reviewed + 1 WHERE id = :userId")
    suspend fun incrementTotalCardsReviewed(userId: Long)
    
    @Query("UPDATE users SET correct_answers = correct_answers + 1 WHERE id = :userId")
    suspend fun incrementCorrectAnswers(userId: Long)
    
    @Query("UPDATE users SET incorrect_answers = incorrect_answers + 1 WHERE id = :userId")
    suspend fun incrementIncorrectAnswers(userId: Long)
    
    @Query("SELECT COUNT(*) FROM flashcards WHERE user_id = :userId")
    suspend fun countUserFlashcards(userId: Long): Int
    
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
    
    @Query("""
        SELECT 
            SUM(CASE WHEN ls.correct_answers_count > 0 OR ls.incorrect_answers_count > 0 THEN 1 ELSE 0 END) as reviewed_count,
            SUM(ls.correct_answers_count) as correct_count,
            SUM(ls.incorrect_answers_count) as incorrect_count
        FROM flashcards f
        LEFT JOIN learning_statistics ls ON f.id = ls.flashcard_id
        WHERE f.user_id = :userId
        AND ls.last_updated >= :startDate
        AND ls.last_updated <= :endDate
    """)
    suspend fun getWeeklyStatistics(userId: Long, startDate: Date, endDate: Date): WeeklyStats
    
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
    
    data class WeeklyStats(
        val reviewedCount: Int,
        val correctCount: Int,
        val incorrectCount: Int
    )
}

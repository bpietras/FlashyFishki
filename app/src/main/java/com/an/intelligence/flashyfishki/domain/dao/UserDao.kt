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
}

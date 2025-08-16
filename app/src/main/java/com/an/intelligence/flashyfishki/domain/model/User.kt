package com.an.intelligence.flashyfishki.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true)
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val userId: Long = 0,
    
    val email: String,
    val password_hash: String,
    
    @ColumnInfo(name = "last_login_date")
    val lastLoginDate: Date? = null,
    
    @ColumnInfo(name = "total_cards_reviewed", defaultValue = "0")
    val totalCardsReviewed: Int = 0,
    
    @ColumnInfo(name = "correct_answers", defaultValue = "0")
    val correctAnswers: Int = 0,
    
    @ColumnInfo(name = "incorrect_answers", defaultValue = "0")
    val incorrectAnswers: Int = 0,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
)

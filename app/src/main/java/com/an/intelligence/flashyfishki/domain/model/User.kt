package com.an.intelligence.flashyfishki.domain.model

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
    val userId: Long = 0,
    
    val email: String,
    val passwordHash: String,
    
    val lastLoginDate: Date? = null,
    
    val totalCardsReviewed: Int = 0,
    
    val correctAnswers: Int = 0,
    
    val incorrectAnswers: Int = 0,
    
    val createdAt: Date = Date()
)

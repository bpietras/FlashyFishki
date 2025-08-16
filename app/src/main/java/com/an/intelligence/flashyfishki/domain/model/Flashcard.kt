package com.an.intelligence.flashyfishki.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Flashcard::class,
            parentColumns = ["id"],
            childColumns = ["original_flashcard_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["category_id"]),
        Index(value = ["original_flashcard_id"]),
        Index(value = ["is_public", "category_id", "difficulty_level"]),
        Index(value = ["is_public", "created_at"]),
        Index(value = ["user_id", "learning_status", "next_review_date"])
    ]
)
data class Flashcard(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val flashcardId: Long = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: Long,
    
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
    
    val question: String,
    val answer: String,
    
    @ColumnInfo(name = "difficulty_level")
    val difficultyLevel: Int, // 1-5
    
    @ColumnInfo(name = "learning_status", defaultValue = "0")
    val learningStatus: Int, // 0-3
    
    @ColumnInfo(name = "next_review_date")
    val nextReviewDate: Date? = null,
    
    @ColumnInfo(name = "is_public", defaultValue = "0")
    val isPublic: Boolean = false,
    
    @ColumnInfo(name = "original_flashcard_id")
    val originalFlashcardId: Long? = null,
    
    @ColumnInfo(name = "copies_count", defaultValue = "0")
    val copiesCount: Int = 0,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)

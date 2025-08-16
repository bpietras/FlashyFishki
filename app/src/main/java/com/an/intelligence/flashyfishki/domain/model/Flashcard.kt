package com.an.intelligence.flashyfishki.domain.model

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
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Flashcard::class,
            parentColumns = ["flashcardId"],
            childColumns = ["originalFlashcardId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["categoryId"]),
        Index(value = ["originalFlashcardId"])
    ]
)
data class Flashcard(
    @PrimaryKey(autoGenerate = true)
    val flashcardId: Long = 0,
    val userId: Long,
    val categoryId: Long,
    val question: String,
    val answer: String,
    val difficultyLevel: Int, // 1-5
    val learningStatus: Int, // 0-3
    val nextReviewDate: Date? = null,
    val isPublic: Boolean = false,
    val originalFlashcardId: Long? = null,
    val copiesCount: Int = 0,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

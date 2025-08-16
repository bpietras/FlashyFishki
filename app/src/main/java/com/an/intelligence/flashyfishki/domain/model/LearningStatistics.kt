package com.an.intelligence.flashyfishki.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "learning_statistics",
    foreignKeys = [
        ForeignKey(
            entity = Flashcard::class,
            parentColumns = ["flashcardId"],
            childColumns = ["flashcardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["flashcardId"], unique = true)
    ]
)
data class LearningStatistics(
    @PrimaryKey(autoGenerate = true)
    val statisticId: Long = 0,
    val flashcardId: Long,
    val correctAnswersCount: Int = 0,
    val incorrectAnswersCount: Int = 0
)

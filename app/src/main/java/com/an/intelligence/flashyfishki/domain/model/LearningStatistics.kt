package com.an.intelligence.flashyfishki.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "learning_statistics",
    foreignKeys = [
        ForeignKey(
            entity = Flashcard::class,
            parentColumns = ["id"],
            childColumns = ["flashcard_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["flashcard_id"], unique = true)
    ]
)
data class LearningStatistics(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val statisticId: Long = 0,
    
    @ColumnInfo(name = "flashcard_id")
    val flashcardId: Long,
    
    @ColumnInfo(name = "correct_answers_count", defaultValue = "0")
    val correctAnswersCount: Int = 0,
    
    @ColumnInfo(name = "incorrect_answers_count", defaultValue = "0")
    val incorrectAnswersCount: Int = 0,
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Date = Date()
)

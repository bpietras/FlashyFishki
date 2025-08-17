package com.an.intelligence.flashyfishki.domain.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.domain.dao.FlashcardDao
import com.an.intelligence.flashyfishki.domain.dao.LearningStatisticsDao
import com.an.intelligence.flashyfishki.domain.dao.UserDao

@Database(
    entities = [
        User::class,
        Category::class,
        Flashcard::class,
        LearningStatistics::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FlashyFishkiDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun learningStatisticsDao(): LearningStatisticsDao

    companion object {
        const val DATABASE_NAME = "flash_fishki_database.db"
    }
}

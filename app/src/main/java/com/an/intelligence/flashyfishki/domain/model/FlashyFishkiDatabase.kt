package com.an.intelligence.flashyfishki.domain.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

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
    // DAOs będą dodane później
}

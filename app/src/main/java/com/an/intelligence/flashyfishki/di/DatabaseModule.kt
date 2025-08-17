package com.an.intelligence.flashyfishki.di

import android.content.Context
import androidx.room.Room
import com.an.intelligence.flashyfishki.domain.dao.UserDao
import com.an.intelligence.flashyfishki.domain.model.FlashyFishkiDatabase
import com.an.intelligence.flashyfishki.domain.repository.SessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideFlashyFishkiDatabase(@ApplicationContext context: Context): FlashyFishkiDatabase {
        return Room.databaseBuilder(
            context,
            FlashyFishkiDatabase::class.java,
            FlashyFishkiDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideUserDao(database: FlashyFishkiDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    @Singleton
    fun provideSessionRepository(
        @ApplicationContext context: Context,
        userDao: UserDao
    ): SessionRepository {
        return SessionRepository(context, userDao)
    }
}

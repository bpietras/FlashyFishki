package com.an.intelligence.flashyfishki.domain.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.an.intelligence.flashyfishki.domain.dao.UserDao
import com.an.intelligence.flashyfishki.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

@Singleton
class SessionRepository @Inject constructor(
    private val context: Context,
    private val userDao: UserDao
) {
    private val dataStore = context.dataStore
    
    companion object {
        val USER_ID_KEY = longPreferencesKey("user_id")
    }
    
    val currentUserId: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }
    
    val currentUser: Flow<User?> = currentUserId.map { userId ->
        userId?.let { userDao.getUserById(it) }
    }
    
    suspend fun saveSession(userId: Long) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }
    
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }
    
    suspend fun isLoggedIn(): Boolean {
        return currentUserId.firstOrNull() != null
    }
    
    suspend fun getCurrentUser(): User? {
        val userId = currentUserId.firstOrNull()
        return userId?.let { userDao.getUserById(it) }
    }
}

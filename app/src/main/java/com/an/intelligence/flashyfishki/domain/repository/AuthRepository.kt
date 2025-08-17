package com.an.intelligence.flashyfishki.domain.repository

import com.an.intelligence.flashyfishki.domain.dao.UserDao
import com.an.intelligence.flashyfishki.domain.model.User
import com.an.intelligence.flashyfishki.utils.PasswordUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao
) {
    
    // Current logged in user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    suspend fun registerUser(email: String, password: String): AuthResult {
        return try {
            // Check if user already exists
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return AuthResult.Error("User with this email already exists")
            }
            
            // Validate email format
            if (!isValidEmail(email)) {
                return AuthResult.Error("Please enter a valid email address")
            }
            
            // Validate password strength
            if (!isValidPassword(password)) {
                return AuthResult.Error("Password must be at least 8 characters long and contain at least one letter and one number")
            }
            
            // Hash password and create user
            val passwordHash = PasswordUtils.hashPassword(password)
            val newUser = User(
                email = email,
                passwordHash = passwordHash,
                createdAt = Date()
            )
            
            userDao.insertUser(newUser)
            AuthResult.Success
            
        } catch (e: Exception) {
            AuthResult.Error("Registration failed: ${e.message}")
        }
    }
    
    suspend fun loginUser(email: String, password: String): LoginResult {
        return try {
            val user = userDao.getUserByEmail(email)
            
            if (user == null) {
                return LoginResult.Error("User not found")
            }
            
            if (!PasswordUtils.verifyPassword(password, user.passwordHash)) {
                return LoginResult.Error("Invalid password")
            }
            
            // Update last login date
            userDao.updateLastLoginDate(user.userId, Date())
            
            // Return updated user and set current user
            val updatedUser = userDao.getUserById(user.userId) ?: user
            _currentUser.value = updatedUser
            LoginResult.Success(updatedUser)
            
        } catch (e: Exception) {
            LoginResult.Error("Login failed: ${e.message}")
        }
    }
    
    suspend fun logout() {
        _currentUser.value = null
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8 && 
               password.any { it.isLetter() } && 
               password.any { it.isDigit() }
    }
}

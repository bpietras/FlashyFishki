package com.an.intelligence.flashyfishki.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.an.intelligence.flashyfishki.domain.dao.UserDao
import com.an.intelligence.flashyfishki.domain.model.FlashyFishkiDatabase
import com.an.intelligence.flashyfishki.domain.repository.AuthRepository
import com.an.intelligence.flashyfishki.domain.repository.AuthResult
import com.an.intelligence.flashyfishki.domain.repository.LoginResult
import com.an.intelligence.flashyfishki.utils.PasswordUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Integration tests for AuthRepository with Room Database
 * Tests authentication flows with real database interactions
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@MediumTest
class AuthRepositoryIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: FlashyFishkiDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var userDao: UserDao

    @Before
    fun setup() {
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FlashyFishkiDatabase::class.java
        ).allowMainThreadQueries().build()
        
        userDao = database.userDao()
        
        // Create AuthRepository with actual DAO
        authRepository = AuthRepository(userDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun registerUser_validCredentials_succeeds() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"

        // Act
        val result = authRepository.registerUser(email, password)

        // Assert
        assertTrue("Registration should succeed", result is AuthResult.Success)
        
        // Verify user was actually saved to database
        val savedUser = userDao.getUserByEmail(email)
        assertNotNull("User should be saved to database", savedUser)
        assertEquals(email, savedUser!!.email)
        assertTrue("Password should be hashed", 
            PasswordUtils.verifyPassword(password, savedUser.passwordHash))
    }

    @Test
    fun registerUser_duplicateEmail_fails() = runTest {
        // Arrange
        val email = "duplicate@example.com"
        val password = "password123"
        
        // First registration
        authRepository.registerUser(email, password)

        // Act - Try to register again with same email
        val result = authRepository.registerUser(email, "differentpassword456")

        // Assert
        assertTrue("Second registration should fail", result is AuthResult.Error)
        assertEquals("User with this email already exists", 
            (result as AuthResult.Error).message)
    }

    @Test
    fun registerUser_invalidEmail_fails() = runTest {
        // Arrange
        val invalidEmail = "not-an-email"
        val password = "password123"

        // Act
        val result = authRepository.registerUser(invalidEmail, password)

        // Assert
        assertTrue("Registration should fail for invalid email", result is AuthResult.Error)
        assertEquals("Please enter a valid email address", 
            (result as AuthResult.Error).message)
        
        // Verify no user was saved
        val savedUser = userDao.getUserByEmail(invalidEmail)
        assertNull("No user should be saved for invalid email", savedUser)
    }

    @Test
    fun registerUser_weakPassword_fails() = runTest {
        // Arrange
        val email = "test@example.com"
        val weakPassword = "123" // Too short

        // Act
        val result = authRepository.registerUser(email, weakPassword)

        // Assert
        assertTrue("Registration should fail for weak password", result is AuthResult.Error)
        assertTrue("Error message should mention password requirements", 
            (result as AuthResult.Error).message.contains("at least 8 characters"))
        
        // Verify no user was saved
        val savedUser = userDao.getUserByEmail(email)
        assertNull("No user should be saved for weak password", savedUser)
    }

    @Test
    fun registerUser_passwordWithoutLetter_fails() = runTest {
        // Arrange
        val email = "test@example.com"
        val passwordWithoutLetter = "12345678" // Only digits

        // Act
        val result = authRepository.registerUser(email, passwordWithoutLetter)

        // Assert
        assertTrue("Registration should fail", result is AuthResult.Error)
        assertTrue("Error should mention letter requirement",
            (result as AuthResult.Error).message.contains("one letter"))
    }

    @Test
    fun registerUser_passwordWithoutDigit_fails() = runTest {
        // Arrange
        val email = "test@example.com"
        val passwordWithoutDigit = "abcdefgh" // Only letters

        // Act
        val result = authRepository.registerUser(email, passwordWithoutDigit)

        // Assert
        assertTrue("Registration should fail", result is AuthResult.Error)
        assertTrue("Error should mention digit requirement",
            (result as AuthResult.Error).message.contains("one number"))
    }

    @Test
    fun loginUser_validCredentials_succeeds() = runTest {
        // Arrange
        val email = "login@example.com"
        val password = "password123"
        
        // Register user first
        authRepository.registerUser(email, password)

        // Act
        val result = authRepository.loginUser(email, password)

        // Assert
        assertTrue("Login should succeed", result is LoginResult.Success)
        val user = (result as LoginResult.Success).user
        assertEquals(email, user.email)
        assertNotNull("Last login date should be updated", user.lastLoginDate)
        
        // Verify current user is set
        val currentUser = authRepository.currentUser.first()
        assertNotNull("Current user should be set", currentUser)
        assertEquals(email, currentUser!!.email)
    }

    @Test
    fun loginUser_nonExistentUser_fails() = runTest {
        // Arrange
        val email = "nonexistent@example.com"
        val password = "password123"

        // Act
        val result = authRepository.loginUser(email, password)

        // Assert
        assertTrue("Login should fail for non-existent user", result is LoginResult.Error)
        assertEquals("User not found", (result as LoginResult.Error).message)
        
        // Verify current user is not set
        val currentUser = authRepository.currentUser.first()
        assertNull("Current user should not be set", currentUser)
    }

    @Test
    fun loginUser_incorrectPassword_fails() = runTest {
        // Arrange
        val email = "login@example.com"
        val correctPassword = "password123"
        val incorrectPassword = "wrongpassword"
        
        // Register user first
        authRepository.registerUser(email, correctPassword)

        // Act
        val result = authRepository.loginUser(email, incorrectPassword)

        // Assert
        assertTrue("Login should fail for incorrect password", result is LoginResult.Error)
        assertEquals("Invalid password", (result as LoginResult.Error).message)
        
        // Verify current user is not set
        val currentUser = authRepository.currentUser.first()
        assertNull("Current user should not be set", currentUser)
    }

    @Test
    fun loginUser_updatesLastLoginDate() = runTest {
        // Arrange
        val email = "login@example.com"
        val password = "password123"
        
        // Register user
        authRepository.registerUser(email, password)
        val userBeforeLogin = userDao.getUserByEmail(email)!!
        assertNull("Initial last login should be null", userBeforeLogin.lastLoginDate)

        // Act
        val result = authRepository.loginUser(email, password)

        // Assert
        assertTrue("Login should succeed", result is LoginResult.Success)
        
        // Verify last login date was updated in database
        val userAfterLogin = userDao.getUserByEmail(email)!!
        assertNotNull("Last login date should be updated", userAfterLogin.lastLoginDate)
        
        // Verify the returned user also has updated last login date
        val returnedUser = (result as LoginResult.Success).user
        assertNotNull("Returned user should have last login date", returnedUser.lastLoginDate)
        assertEquals("Database and returned user should match", 
            userAfterLogin.lastLoginDate!!.time, returnedUser.lastLoginDate!!.time)
    }

    @Test
    fun logout_clearsCurrentUser() = runTest {
        // Arrange
        val email = "logout@example.com"
        val password = "password123"
        
        // Register and login user
        authRepository.registerUser(email, password)
        authRepository.loginUser(email, password)
        
        // Verify user is logged in
        val currentUserBefore = authRepository.currentUser.first()
        assertNotNull("User should be logged in", currentUserBefore)

        // Act
        authRepository.logout()

        // Assert
        val currentUserAfter = authRepository.currentUser.first()
        assertNull("Current user should be cleared", currentUserAfter)
    }

    @Test
    fun setCurrentUser_updatesCurrentUserState() = runTest {
        // Arrange
        val email = "setuser@example.com"
        val password = "password123"
        
        // Register user
        authRepository.registerUser(email, password)
        val savedUser = userDao.getUserByEmail(email)!!

        // Act
        authRepository.setCurrentUser(savedUser)

        // Assert
        val currentUser = authRepository.currentUser.first()
        assertNotNull("Current user should be set", currentUser)
        assertEquals(savedUser.userId, currentUser!!.userId)
        assertEquals(savedUser.email, currentUser.email)
    }

    @Test
    fun setCurrentUser_withNull_clearsCurrentUser() = runTest {
        // Arrange
        val email = "clearuser@example.com"
        val password = "password123"
        
        // Register and set user
        authRepository.registerUser(email, password)
        val savedUser = userDao.getUserByEmail(email)!!
        authRepository.setCurrentUser(savedUser)
        
        // Verify user is set
        val currentUserBefore = authRepository.currentUser.first()
        assertNotNull("User should be set", currentUserBefore)

        // Act
        authRepository.setCurrentUser(null)

        // Assert
        val currentUserAfter = authRepository.currentUser.first()
        assertNull("Current user should be cleared", currentUserAfter)
    }

    @Test
    fun multipleUsers_canRegisterAndLoginIndependently() = runTest {
        // Arrange
        val user1Email = "user1@example.com"
        val user1Password = "password123"
        val user2Email = "user2@example.com"
        val user2Password = "differentpass456"

        // Act - Register both users
        val register1Result = authRepository.registerUser(user1Email, user1Password)
        val register2Result = authRepository.registerUser(user2Email, user2Password)

        // Assert registration
        assertTrue("User 1 registration should succeed", register1Result is AuthResult.Success)
        assertTrue("User 2 registration should succeed", register2Result is AuthResult.Success)

        // Act - Login user 1
        val login1Result = authRepository.loginUser(user1Email, user1Password)
        assertTrue("User 1 login should succeed", login1Result is LoginResult.Success)
        
        val currentUser1 = authRepository.currentUser.first()
        assertEquals(user1Email, currentUser1!!.email)

        // Act - Logout and login user 2
        authRepository.logout()
        val login2Result = authRepository.loginUser(user2Email, user2Password)
        assertTrue("User 2 login should succeed", login2Result is LoginResult.Success)
        
        val currentUser2 = authRepository.currentUser.first()
        assertEquals(user2Email, currentUser2!!.email)

        // Verify both users exist in database
        val savedUser1 = userDao.getUserByEmail(user1Email)
        val savedUser2 = userDao.getUserByEmail(user2Email)
        assertNotNull("User 1 should exist in database", savedUser1)
        assertNotNull("User 2 should exist in database", savedUser2)
        assertNotEquals("Users should have different IDs", savedUser1!!.userId, savedUser2!!.userId)
    }

    @Test
    fun passwordSecurity_hashesPasswordsProperly() = runTest {
        // Arrange
        val email = "security@example.com"
        val password = "mysecretpassword123"

        // Act
        authRepository.registerUser(email, password)

        // Assert
        val savedUser = userDao.getUserByEmail(email)!!
        
        // Verify password is hashed (not stored in plain text)
        assertNotEquals("Password should not be stored in plain text", 
            password, savedUser.passwordHash)
        
        // Verify hash is verifiable
        assertTrue("Password hash should be verifiable",
            PasswordUtils.verifyPassword(password, savedUser.passwordHash))
        
        // Verify wrong password doesn't verify
        assertFalse("Wrong password should not verify",
            PasswordUtils.verifyPassword("wrongpassword", savedUser.passwordHash))
    }
}

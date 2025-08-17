package com.an.intelligence.flashyfishki.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.an.intelligence.flashyfishki.domain.dao.UserDao
import com.an.intelligence.flashyfishki.domain.model.FlashyFishkiDatabase
import com.an.intelligence.flashyfishki.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.util.Date

/**
 * Integration tests for UserDao with Room Database
 * Tests user authentication and statistics operations
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@SmallTest
class UserDaoIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: FlashyFishkiDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FlashyFishkiDatabase::class.java
        ).allowMainThreadQueries().build()
        
        userDao = database.userDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertUser_savesUserCorrectly() = runTest {
        // Arrange
        val user = User(
            email = "test@example.com",
            passwordHash = "hashed_password_123",
            createdAt = Date()
        )

        // Act
        val userId = userDao.insertUser(user)

        // Assert
        assertTrue(userId > 0)
        val savedUser = userDao.getUserById(userId)
        assertNotNull(savedUser)
        assertEquals("test@example.com", savedUser!!.email)
        assertEquals("hashed_password_123", savedUser.passwordHash)
        assertEquals(0, savedUser.totalCardsReviewed)
        assertEquals(0, savedUser.correctAnswers)
        assertEquals(0, savedUser.incorrectAnswers)
        assertNull(savedUser.lastLoginDate)
    }

    @Test
    fun insertUser_throwsExceptionForDuplicateEmail() = runTest {
        // Arrange
        val user1 = User(
            email = "duplicate@example.com",
            passwordHash = "hash1",
            createdAt = Date()
        )
        val user2 = User(
            email = "duplicate@example.com",
            passwordHash = "hash2",
            createdAt = Date()
        )

        // Act & Assert
        userDao.insertUser(user1)
        
        try {
            userDao.insertUser(user2)
            assertTrue("Expected exception for duplicate email", false)
        } catch (e: Exception) {
            // Expected - duplicate email should throw exception due to unique constraint
            assertTrue(true)
        }
    }

    @Test
    fun getUserByEmail_findsCorrectUser() = runTest {
        // Arrange
        val user = User(
            email = "findme@example.com",
            passwordHash = "password_hash",
            createdAt = Date()
        )
        userDao.insertUser(user)

        // Act
        val foundUser = userDao.getUserByEmail("findme@example.com")

        // Assert
        assertNotNull(foundUser)
        assertEquals("findme@example.com", foundUser!!.email)
        assertEquals("password_hash", foundUser.passwordHash)
    }

    @Test
    fun getUserByEmail_returnsNullForNonExistent() = runTest {
        // Act
        val foundUser = userDao.getUserByEmail("nonexistent@example.com")

        // Assert
        assertNull(foundUser)
    }

    @Test
    fun getUserById_findsCorrectUser() = runTest {
        // Arrange
        val user = User(
            email = "byid@example.com",
            passwordHash = "hash",
            createdAt = Date()
        )
        val userId = userDao.insertUser(user)

        // Act
        val foundUser = userDao.getUserById(userId)

        // Assert
        assertNotNull(foundUser)
        assertEquals(userId, foundUser!!.userId)
        assertEquals("byid@example.com", foundUser.email)
    }

    @Test
    fun getUserById_returnsNullForNonExistent() = runTest {
        // Act
        val foundUser = userDao.getUserById(999L)

        // Assert
        assertNull(foundUser)
    }

    @Test
    fun getAllUsers_returnsAllUsers() = runTest {
        // Arrange
        val user1 = User(email = "user1@example.com", passwordHash = "hash1", createdAt = Date())
        val user2 = User(email = "user2@example.com", passwordHash = "hash2", createdAt = Date())
        val user3 = User(email = "user3@example.com", passwordHash = "hash3", createdAt = Date())

        userDao.insertUser(user1)
        userDao.insertUser(user2)
        userDao.insertUser(user3)

        // Act
        val allUsers = userDao.getAllUsers().first()

        // Assert
        assertEquals(3, allUsers.size)
        assertTrue(allUsers.any { it.email == "user1@example.com" })
        assertTrue(allUsers.any { it.email == "user2@example.com" })
        assertTrue(allUsers.any { it.email == "user3@example.com" })
    }

    @Test
    fun updateLastLoginDate_updatesCorrectly() = runTest {
        // Arrange
        val user = User(
            email = "login@example.com",
            passwordHash = "hash",
            createdAt = Date()
        )
        val userId = userDao.insertUser(user)
        val loginDate = Date()

        // Act
        userDao.updateLastLoginDate(userId, loginDate)

        // Assert
        val updatedUser = userDao.getUserById(userId)
        assertNotNull(updatedUser)
        assertNotNull(updatedUser!!.lastLoginDate)
        assertEquals(loginDate.time, updatedUser.lastLoginDate!!.time)
    }

    @Test
    fun incrementTotalCardsReviewed_increasesCount() = runTest {
        // Arrange
        val user = User(
            email = "reviewed@example.com",
            passwordHash = "hash",
            totalCardsReviewed = 5,
            createdAt = Date()
        )
        val userId = userDao.insertUser(user)

        // Act
        userDao.incrementTotalCardsReviewed(userId)

        // Assert
        val updatedUser = userDao.getUserById(userId)
        assertNotNull(updatedUser)
        assertEquals(6, updatedUser!!.totalCardsReviewed)
    }

    @Test
    fun incrementCorrectAnswers_increasesCount() = runTest {
        // Arrange
        val user = User(
            email = "correct@example.com",
            passwordHash = "hash",
            correctAnswers = 10,
            createdAt = Date()
        )
        val userId = userDao.insertUser(user)

        // Act
        userDao.incrementCorrectAnswers(userId)

        // Assert
        val updatedUser = userDao.getUserById(userId)
        assertNotNull(updatedUser)
        assertEquals(11, updatedUser!!.correctAnswers)
    }

    @Test
    fun incrementIncorrectAnswers_increasesCount() = runTest {
        // Arrange
        val user = User(
            email = "incorrect@example.com",
            passwordHash = "hash",
            incorrectAnswers = 3,
            createdAt = Date()
        )
        val userId = userDao.insertUser(user)

        // Act
        userDao.incrementIncorrectAnswers(userId)

        // Assert
        val updatedUser = userDao.getUserById(userId)
        assertNotNull(updatedUser)
        assertEquals(4, updatedUser!!.incorrectAnswers)
    }

    @Test
    fun updateUser_updatesAllFields() = runTest {
        // Arrange
        val user = User(
            email = "update@example.com",
            passwordHash = "old_hash",
            totalCardsReviewed = 5,
            correctAnswers = 3,
            incorrectAnswers = 2,
            createdAt = Date()
        )
        val userId = userDao.insertUser(user)
        val originalUser = userDao.getUserById(userId)!!
        
        val loginDate = Date()
        val updatedUser = originalUser.copy(
            passwordHash = "new_hash",
            lastLoginDate = loginDate,
            totalCardsReviewed = 10,
            correctAnswers = 8,
            incorrectAnswers = 2
        )

        // Act
        userDao.updateUser(updatedUser)

        // Assert
        val savedUser = userDao.getUserById(userId)
        assertNotNull(savedUser)
        assertEquals("new_hash", savedUser!!.passwordHash)
        assertEquals(loginDate.time, savedUser.lastLoginDate!!.time)
        assertEquals(10, savedUser.totalCardsReviewed)
        assertEquals(8, savedUser.correctAnswers)
        assertEquals(2, savedUser.incorrectAnswers)
    }

    @Test
    fun deleteUser_removesFromDatabase() = runTest {
        // Arrange
        val user = User(
            email = "delete@example.com",
            passwordHash = "hash",
            createdAt = Date()
        )
        val userId = userDao.insertUser(user)
        val savedUser = userDao.getUserById(userId)!!

        // Act
        userDao.deleteUser(savedUser)

        // Assert
        val deletedUser = userDao.getUserById(userId)
        assertNull(deletedUser)
    }

    @Test
    fun multipleOperations_workCorrectly() = runTest {
        // Arrange
        val user = User(
            email = "multi@example.com",
            passwordHash = "hash",
            createdAt = Date()
        )
        val userId = userDao.insertUser(user)

        // Act - Simulate multiple learning operations
        userDao.incrementTotalCardsReviewed(userId)
        userDao.incrementCorrectAnswers(userId)
        userDao.incrementTotalCardsReviewed(userId)
        userDao.incrementIncorrectAnswers(userId)
        userDao.incrementTotalCardsReviewed(userId)
        userDao.incrementCorrectAnswers(userId)

        // Assert
        val finalUser = userDao.getUserById(userId)
        assertNotNull(finalUser)
        assertEquals(3, finalUser!!.totalCardsReviewed)
        assertEquals(2, finalUser.correctAnswers)
        assertEquals(1, finalUser.incorrectAnswers)
    }
}

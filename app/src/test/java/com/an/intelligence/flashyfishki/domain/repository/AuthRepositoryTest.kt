package com.an.intelligence.flashyfishki.domain.repository

import com.an.intelligence.flashyfishki.domain.dao.UserDao
import com.an.intelligence.flashyfishki.domain.model.User
import com.an.intelligence.flashyfishki.utils.PasswordUtils
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class AuthRepositoryTest {

    private lateinit var userDao: UserDao
    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        userDao = mockk()
        authRepository = AuthRepository(userDao)
    }

    @Test
    fun `registerUser should succeed with valid email and password`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val hashedPassword = "hashed_password"
        
        coEvery { userDao.getUserByEmail(email) } returns null
        
        mockkObject(PasswordUtils) {
            every { PasswordUtils.hashPassword(password) } returns hashedPassword
            coEvery { userDao.insertUser(any()) } returns 1L

            // When
            val result = authRepository.registerUser(email, password)

            // Then
            assertTrue("Result should be AuthResult.Success but was $result", result is AuthResult.Success)
            coVerify { userDao.insertUser(any()) }
        }
    }

    @Test
    fun `registerUser should fail when user already exists`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val existingUser = User(
            userId = 1L,
            email = email,
            passwordHash = "existing_hash",
            createdAt = Date()
        )
        
        coEvery { userDao.getUserByEmail(email) } returns existingUser

        // When
        val result = authRepository.registerUser(email, password)

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("User with this email already exists", (result as AuthResult.Error).message)
        coVerify(exactly = 0) { userDao.insertUser(any()) }
    }

    @Test
    fun `registerUser should fail with invalid email`() = runTest {
        // Given
        val email = "invalid-email"
        val password = "password123"
        
        // Mock the getUserByEmail call that happens before validation
        coEvery { userDao.getUserByEmail(email) } returns null

        // When
        val result = authRepository.registerUser(email, password)

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("Please enter a valid email address", (result as AuthResult.Error).message)
        coVerify(exactly = 0) { userDao.insertUser(any()) }
    }

    @Test
    fun `registerUser should fail with weak password`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "weak"
        
        // Mock the getUserByEmail call that happens before validation
        coEvery { userDao.getUserByEmail(email) } returns null

        // When
        val result = authRepository.registerUser(email, password)

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("Password must be at least 8 characters long and contain at least one letter and one number", (result as AuthResult.Error).message)
        coVerify(exactly = 0) { userDao.insertUser(any()) }
    }

    @Test
    fun `loginUser should succeed with correct credentials`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val user = User(
            userId = 1L,
            email = email,
            passwordHash = "hashed_password",
            createdAt = Date()
        )
        
        coEvery { userDao.getUserByEmail(email) } returns user
        coEvery { userDao.getUserById(1L) } returns user
        coEvery { userDao.updateLastLoginDate(1L, any()) } just Runs
        
        mockkObject(PasswordUtils) {
            every { PasswordUtils.verifyPassword(password, user.passwordHash) } returns true

            // When
            val result = authRepository.loginUser(email, password)

            // Then
            assertTrue(result is LoginResult.Success)
            assertEquals(user, (result as LoginResult.Success).user)
            coVerify { userDao.updateLastLoginDate(eq(1L), any()) }
        }
    }

    @Test
    fun `loginUser should fail with non-existent user`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        
        coEvery { userDao.getUserByEmail(email) } returns null

        // When
        val result = authRepository.loginUser(email, password)

        // Then
        assertTrue(result is LoginResult.Error)
        assertEquals("User not found", (result as LoginResult.Error).message)
    }

    @Test
    fun `loginUser should fail with wrong password`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        val user = User(
            userId = 1L,
            email = email,
            passwordHash = "hashed_password",
            createdAt = Date()
        )
        
        coEvery { userDao.getUserByEmail(email) } returns user
        
        mockkObject(PasswordUtils) {
            every { PasswordUtils.verifyPassword(password, user.passwordHash) } returns false

            // When
            val result = authRepository.loginUser(email, password)

            // Then
            assertTrue(result is LoginResult.Error)
            assertEquals("Invalid password", (result as LoginResult.Error).message)
        }
    }

    @Test
    fun `logout should complete without errors`() = runTest {
        // When
        authRepository.logout()

        // Then
        // No exception should be thrown and method should complete
        assertTrue(true)
    }
}

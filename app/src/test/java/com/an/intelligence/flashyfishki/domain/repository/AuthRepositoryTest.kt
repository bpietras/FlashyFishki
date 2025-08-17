package com.an.intelligence.flashyfishki.domain.repository

import com.an.intelligence.flashyfishki.domain.dao.UserDao
import com.an.intelligence.flashyfishki.domain.model.User
import com.an.intelligence.flashyfishki.utils.PasswordUtils
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.*

class AuthRepositoryTest {

    @Mock
    private lateinit var userDao: UserDao

    @Mock
    private lateinit var sessionRepository: SessionRepository

    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        authRepository = AuthRepository(userDao, sessionRepository)
    }

    @Test
    fun `registerUser should succeed with valid email and password`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val hashedPassword = "hashed_password"
        
        `when`(userDao.getUserByEmail(email)).thenReturn(null)
        
        mockStatic(PasswordUtils::class.java).use { mockedPasswordUtils ->
            mockedPasswordUtils.`when`<String> { PasswordUtils.hashPassword(password) }
                .thenReturn(hashedPassword)
            
            `when`(userDao.insertUser(any(User::class.java))).thenReturn(1L)

            // When
            val result = authRepository.registerUser(email, password)

            // Then
            assertTrue(result is AuthResult.Success)
            verify(userDao).insertUser(any(User::class.java))
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
        
        `when`(userDao.getUserByEmail(email)).thenReturn(existingUser)

        // When
        val result = authRepository.registerUser(email, password)

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("User with this email already exists", (result as AuthResult.Error).message)
        verify(userDao, never()).insertUser(any(User::class.java))
    }

    @Test
    fun `registerUser should fail with invalid email`() = runTest {
        // Given
        val email = "invalid-email"
        val password = "password123"

        // When
        val result = authRepository.registerUser(email, password)

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("Please enter a valid email address", (result as AuthResult.Error).message)
        verify(userDao, never()).insertUser(any(User::class.java))
    }

    @Test
    fun `registerUser should fail with weak password`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "weak"

        // When
        val result = authRepository.registerUser(email, password)

        // Then
        assertTrue(result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.contains("Password must be at least 8 characters"))
        verify(userDao, never()).insertUser(any(User::class.java))
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
        
        `when`(userDao.getUserByEmail(email)).thenReturn(user)
        `when`(userDao.getUserById(1L)).thenReturn(user)
        
        mockStatic(PasswordUtils::class.java).use { mockedPasswordUtils ->
            mockedPasswordUtils.`when`<Boolean> { 
                PasswordUtils.verifyPassword(password, user.passwordHash) 
            }.thenReturn(true)

            // When
            val result = authRepository.loginUser(email, password)

            // Then
            assertTrue(result is LoginResult.Success)
            assertEquals(user, (result as LoginResult.Success).user)
            verify(userDao).updateLastLoginDate(eq(1L), any(Date::class.java))
            verify(sessionRepository).saveSession(1L)
        }
    }

    @Test
    fun `loginUser should fail with non-existent user`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        
        `when`(userDao.getUserByEmail(email)).thenReturn(null)

        // When
        val result = authRepository.loginUser(email, password)

        // Then
        assertTrue(result is LoginResult.Error)
        assertEquals("User not found", (result as LoginResult.Error).message)
        verify(sessionRepository, never()).saveSession(any())
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
        
        `when`(userDao.getUserByEmail(email)).thenReturn(user)
        
        mockStatic(PasswordUtils::class.java).use { mockedPasswordUtils ->
            mockedPasswordUtils.`when`<Boolean> { 
                PasswordUtils.verifyPassword(password, user.passwordHash) 
            }.thenReturn(false)

            // When
            val result = authRepository.loginUser(email, password)

            // Then
            assertTrue(result is LoginResult.Error)
            assertEquals("Invalid password", (result as LoginResult.Error).message)
            verify(sessionRepository, never()).saveSession(any())
        }
    }

    @Test
    fun `logout should clear session`() = runTest {
        // When
        authRepository.logout()

        // Then
        verify(sessionRepository).clearSession()
    }

    @Test
    fun `checkAutoLogin should return current user`() = runTest {
        // Given
        val user = User(
            userId = 1L,
            email = "test@example.com",
            passwordHash = "hashed_password",
            createdAt = Date()
        )
        
        `when`(sessionRepository.getCurrentUser()).thenReturn(user)

        // When
        val result = authRepository.checkAutoLogin()

        // Then
        assertEquals(user, result)
        verify(sessionRepository).getCurrentUser()
    }
}

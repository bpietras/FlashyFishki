package com.an.intelligence.flashyfishki.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.an.intelligence.flashyfishki.domain.dao.UserDao
import com.an.intelligence.flashyfishki.domain.model.FlashyFishkiDatabase
import com.an.intelligence.flashyfishki.domain.repository.AuthRepository
import com.an.intelligence.flashyfishki.ui.auth.AuthUiState
import com.an.intelligence.flashyfishki.ui.auth.AuthViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import kotlinx.coroutines.Dispatchers

/**
 * Integration tests for AuthViewModel with AuthRepository and Room Database
 * Tests the complete authentication flow from ViewModel to Database
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@MediumTest
class AuthViewModelIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: FlashyFishkiDatabase
    private lateinit var userDao: UserDao
    private lateinit var authRepository: AuthRepository
    private lateinit var authViewModel: AuthViewModel
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        
        // Create in-memory database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FlashyFishkiDatabase::class.java
        ).allowMainThreadQueries().build()
        
        userDao = database.userDao()
        authRepository = AuthRepository(userDao)
        authViewModel = AuthViewModel(authRepository)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun initialState_isCorrect() = runTest {
        // Act
        val initialState = authViewModel.uiState.first()

        // Assert
        assertEquals(AuthUiState(), initialState)
        assertFalse(initialState.isLoading)
        assertFalse(initialState.isLoggedIn)
        assertNull(initialState.currentUser)
        assertNull(initialState.errorMessage)
        assertFalse(initialState.isRegistrationMode)
    }

    @Test
    fun login_validCredentials_updatesStateCorrectly() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        
        // Register user first
        authRepository.registerUser(email, password)

        // Act
        authViewModel.login(email, password)

        // Assert
        val finalState = authViewModel.uiState.first()
        assertTrue("Should be logged in", finalState.isLoggedIn)
        assertFalse("Should not be loading", finalState.isLoading)
        assertNotNull("Current user should be set", finalState.currentUser)
        assertEquals(email, finalState.currentUser!!.email)
        assertNull("No error message", finalState.errorMessage)
    }

    @Test
    fun login_invalidCredentials_showsError() = runTest {
        // Arrange
        val email = "nonexistent@example.com"
        val password = "wrongpassword"

        // Act
        authViewModel.login(email, password)

        // Assert
        val finalState = authViewModel.uiState.first()
        assertFalse("Should not be logged in", finalState.isLoggedIn)
        assertFalse("Should not be loading", finalState.isLoading)
        assertNull("Current user should be null", finalState.currentUser)
        assertNotNull("Error message should be present", finalState.errorMessage)
        assertEquals("User not found", finalState.errorMessage)
    }

    @Test
    fun register_validCredentials_autoLogsIn() = runTest {
        // Arrange
        val email = "register@example.com"
        val password = "password123"
        val confirmPassword = "password123"

        // Act
        authViewModel.register(email, password, confirmPassword)

        // Assert
        val finalState = authViewModel.uiState.first()
        assertTrue("Should be logged in after registration", finalState.isLoggedIn)
        assertFalse("Should not be loading", finalState.isLoading)
        assertNotNull("Current user should be set", finalState.currentUser)
        assertEquals(email, finalState.currentUser!!.email)
        assertNull("No error message", finalState.errorMessage)
        
        // Verify user was saved to database
        val savedUser = userDao.getUserByEmail(email)
        assertNotNull("User should be saved to database", savedUser)
        assertEquals(email, savedUser!!.email)
    }

    @Test
    fun register_passwordsDoNotMatch_showsError() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val confirmPassword = "differentpassword"

        // Act
        authViewModel.register(email, password, confirmPassword)

        // Assert
        val finalState = authViewModel.uiState.first()
        assertFalse("Should not be logged in", finalState.isLoggedIn)
        assertFalse("Should not be loading", finalState.isLoading)
        assertNull("Current user should be null", finalState.currentUser)
        assertNotNull("Error message should be present", finalState.errorMessage)
        assertEquals("Passwords do not match", finalState.errorMessage)
        
        // Verify no user was saved to database
        val savedUser = userDao.getUserByEmail(email)
        assertNull("No user should be saved", savedUser)
    }

    @Test
    fun register_weakPassword_showsError() = runTest {
        // Arrange
        val email = "weak@example.com"
        val weakPassword = "123"
        val confirmPassword = "123"

        // Act
        authViewModel.register(email, weakPassword, confirmPassword)

        // Assert
        val finalState = authViewModel.uiState.first()
        assertFalse("Should not be logged in", finalState.isLoggedIn)
        assertFalse("Should not be loading", finalState.isLoading)
        assertNull("Current user should be null", finalState.currentUser)
        assertNotNull("Error message should be present", finalState.errorMessage)
        assertTrue("Error should mention password requirements", 
            finalState.errorMessage!!.contains("at least 8 characters"))
        
        // Verify no user was saved to database
        val savedUser = userDao.getUserByEmail(email)
        assertNull("No user should be saved", savedUser)
    }

    @Test
    fun register_duplicateEmail_showsError() = runTest {
        // Arrange
        val email = "duplicate@example.com"
        val password = "password123"
        
        // Register user first
        authRepository.registerUser(email, password)

        // Act - Try to register again
        authViewModel.register(email, "differentpass456", "differentpass456")

        // Assert
        val finalState = authViewModel.uiState.first()
        assertFalse("Should not be logged in", finalState.isLoggedIn)
        assertFalse("Should not be loading", finalState.isLoading)
        assertNull("Current user should be null", finalState.currentUser)
        assertNotNull("Error message should be present", finalState.errorMessage)
        assertEquals("User with this email already exists", finalState.errorMessage)
    }

    @Test
    fun toggleRegistrationMode_changesMode() = runTest {
        // Act & Assert - Initial state
        val initialState = authViewModel.uiState.first()
        assertFalse("Initial mode should be login", initialState.isRegistrationMode)

        // Act - Toggle to registration
        authViewModel.toggleRegistrationMode()
        val registrationState = authViewModel.uiState.first()
        assertTrue("Should be in registration mode", registrationState.isRegistrationMode)
        assertNull("Error should be cleared", registrationState.errorMessage)

        // Act - Toggle back to login
        authViewModel.toggleRegistrationMode()
        val loginState = authViewModel.uiState.first()
        assertFalse("Should be back in login mode", loginState.isRegistrationMode)
        assertNull("Error should remain cleared", loginState.errorMessage)
    }

    @Test
    fun clearError_removesErrorMessage() = runTest {
        // Arrange - Create an error state
        authViewModel.login("invalid@example.com", "wrongpassword")
        val stateWithError = authViewModel.uiState.first()
        assertNotNull("Error should be present", stateWithError.errorMessage)

        // Act
        authViewModel.clearError()

        // Assert
        val finalState = authViewModel.uiState.first()
        assertNull("Error should be cleared", finalState.errorMessage)
        // Other state should remain unchanged
        assertFalse("Should still not be logged in", finalState.isLoggedIn)
    }

    @Test
    fun logout_clearsUserState() = runTest {
        // Arrange - Login first
        val email = "logout@example.com"
        val password = "password123"
        
        authRepository.registerUser(email, password)
        authViewModel.login(email, password)
        
        val loggedInState = authViewModel.uiState.first()
        assertTrue("Should be logged in", loggedInState.isLoggedIn)
        assertNotNull("Current user should be set", loggedInState.currentUser)

        // Act
        authViewModel.logout()

        // Assert
        val loggedOutState = authViewModel.uiState.first()
        assertEquals(AuthUiState(), loggedOutState)
        assertFalse("Should not be logged in", loggedOutState.isLoggedIn)
        assertNull("Current user should be null", loggedOutState.currentUser)
        assertNull("Error should be null", loggedOutState.errorMessage)
        assertFalse("Should not be loading", loggedOutState.isLoading)
        assertFalse("Should be in login mode", loggedOutState.isRegistrationMode)
    }

    @Test
    fun fullUserJourney_registrationToLogout_worksCorrectly() = runTest {
        // Arrange
        val email = "journey@example.com"
        val password = "password123"

        // Step 1: Register user
        authViewModel.register(email, password, password)
        val registeredState = authViewModel.uiState.first()
        assertTrue("Should be logged in after registration", registeredState.isLoggedIn)
        assertEquals(email, registeredState.currentUser!!.email)

        // Step 2: Logout
        authViewModel.logout()
        val loggedOutState = authViewModel.uiState.first()
        assertFalse("Should be logged out", loggedOutState.isLoggedIn)
        assertNull("Current user should be null", loggedOutState.currentUser)

        // Step 3: Login again
        authViewModel.login(email, password)
        val loggedInAgainState = authViewModel.uiState.first()
        assertTrue("Should be logged in again", loggedInAgainState.isLoggedIn)
        assertEquals(email, loggedInAgainState.currentUser!!.email)
        
        // Step 4: Verify user persisted in database
        val persistedUser = userDao.getUserByEmail(email)
        assertNotNull("User should still exist in database", persistedUser)
        assertEquals(email, persistedUser!!.email)
        assertNotNull("Last login date should be updated", persistedUser.lastLoginDate)
    }

    @Test
    fun errorStates_clearProperlyBetweenOperations() = runTest {
        // Step 1: Create error with invalid login
        authViewModel.login("invalid@example.com", "wrongpass")
        val errorState = authViewModel.uiState.first()
        assertNotNull("Error should be present", errorState.errorMessage)

        // Step 2: Toggle mode should clear error
        authViewModel.toggleRegistrationMode()
        val clearedState = authViewModel.uiState.first()
        assertNull("Error should be cleared after mode toggle", clearedState.errorMessage)

        // Step 3: Create another error with invalid registration
        authViewModel.register("invalid", "123", "123")
        val errorState2 = authViewModel.uiState.first()
        assertNotNull("Error should be present again", errorState2.errorMessage)

        // Step 4: Manual clear should work
        authViewModel.clearError()
        val manualClearState = authViewModel.uiState.first()
        assertNull("Error should be manually cleared", manualClearState.errorMessage)
    }

    @Test
    fun loadingStates_showCorrectlyDuringOperations() = runTest {
        // This test would require more complex coroutine timing control
        // For integration test purposes, we verify that loading ends up false
        val email = "loading@example.com"
        val password = "password123"
        
        // Register to ensure user exists
        authRepository.registerUser(email, password)
        
        // Login operation
        authViewModel.login(email, password)
        
        // Verify final state shows loading = false
        val finalState = authViewModel.uiState.first()
        assertFalse("Loading should be false after operation", finalState.isLoading)
        assertTrue("Should be logged in", finalState.isLoggedIn)
    }
}

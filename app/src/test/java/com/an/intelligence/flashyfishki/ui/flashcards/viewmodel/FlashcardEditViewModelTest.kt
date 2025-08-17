package com.an.intelligence.flashyfishki.ui.flashcards.viewmodel

import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.domain.dao.FlashcardDao
import com.an.intelligence.flashyfishki.domain.model.Category
import com.an.intelligence.flashyfishki.domain.model.Flashcard
import com.an.intelligence.flashyfishki.domain.model.User
import com.an.intelligence.flashyfishki.domain.repository.AuthRepository
import com.an.intelligence.flashyfishki.ui.flashcards.model.FlashcardFormState
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class FlashcardEditViewModelTest {
    
    private lateinit var viewModel: FlashcardEditViewModel
    private lateinit var flashcardDao: FlashcardDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var authRepository: AuthRepository
    
    private val testDispatcher = StandardTestDispatcher()
    
    private val testUser = User(
        userId = 1L,
        email = "test@example.com",
        passwordHash = "hashedPassword"
    )
    
    private val testCategory = Category(
        categoryId = 1L,
        name = "Test Category"
    )
    
    private val testFlashcard = Flashcard(
        flashcardId = 1L,
        userId = 1L,
        categoryId = 1L,
        question = "Test Question",
        answer = "Test Answer",
        difficultyLevel = 3,
        learningStatus = 0,
        isPublic = false
    )
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        flashcardDao = mockk()
        categoryDao = mockk()
        authRepository = mockk()
        
        // Mock auth repository
        every { authRepository.currentUser } returns MutableStateFlow(testUser)
        
        // Mock category DAO
        every { categoryDao.getAllCategories() } returns flowOf(listOf(testCategory))
        
        viewModel = FlashcardEditViewModel(flashcardDao, categoryDao, authRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initializeForNewFlashcard should set correct initial state`() = runTest {
        // Given
        val preselectedCategoryId = 5L
        
        // When
        viewModel.initializeForNewFlashcard(preselectedCategoryId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertFalse(viewModel.isEditMode.value)
        assertEquals(preselectedCategoryId, viewModel.formState.value.categoryId)
        assertEquals("", viewModel.formState.value.question)
        assertEquals("", viewModel.formState.value.answer)
    }
    
    @Test
    fun `initializeForEditFlashcard should load existing flashcard`() = runTest {
        // Given
        coEvery { flashcardDao.getFlashcardById(testFlashcard.flashcardId) } returns testFlashcard
        
        // When
        viewModel.initializeForEditFlashcard(testFlashcard.flashcardId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertTrue(viewModel.isEditMode.value)
        val formState = viewModel.formState.value
        assertEquals(testFlashcard.question, formState.question)
        assertEquals(testFlashcard.answer, formState.answer)
        assertEquals(testFlashcard.categoryId, formState.categoryId)
        assertEquals(testFlashcard.difficultyLevel, formState.difficultyLevel)
        assertEquals(testFlashcard.isPublic, formState.isPublic)
    }
    
    @Test
    fun `initializeForEditFlashcard should handle non-existent flashcard`() = runTest {
        // Given
        val nonExistentId = 999L
        coEvery { flashcardDao.getFlashcardById(nonExistentId) } returns null
        
        // When
        viewModel.initializeForEditFlashcard(nonExistentId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals("Flashcard not found", viewModel.error.value)
    }
    
    @Test
    fun `updateFormField should update question and validate`() {
        // Given
        val newQuestion = "New Question"
        
        // When
        viewModel.updateFormField(question = newQuestion)
        
        // Then
        assertEquals(newQuestion, viewModel.formState.value.question)
    }
    
    @Test
    fun `form validation should fail for empty question`() {
        // When
        viewModel.updateFormField(question = "", answer = "Valid answer", categoryId = 1L)
        
        // Then
        val formState = viewModel.formState.value
        assertEquals("Question cannot be empty", formState.questionError)
        assertFalse(formState.isValid)
    }
    
    @Test
    fun `form validation should fail for question too long`() {
        // Given
        val longQuestion = "a".repeat(501)
        
        // When
        viewModel.updateFormField(question = longQuestion, answer = "Valid answer", categoryId = 1L)
        
        // Then
        val formState = viewModel.formState.value
        assertEquals("Question cannot exceed 500 characters", formState.questionError)
        assertFalse(formState.isValid)
    }
    
    @Test
    fun `form validation should fail for empty answer`() {
        // When
        viewModel.updateFormField(question = "Valid question", answer = "", categoryId = 1L)
        
        // Then
        val formState = viewModel.formState.value
        assertEquals("Answer cannot be empty", formState.answerError)
        assertFalse(formState.isValid)
    }
    
    @Test
    fun `form validation should fail for answer too long`() {
        // Given
        val longAnswer = "a".repeat(1001)
        
        // When
        viewModel.updateFormField(question = "Valid question", answer = longAnswer, categoryId = 1L)
        
        // Then
        val formState = viewModel.formState.value
        assertEquals("Answer cannot exceed 1000 characters", formState.answerError)
        assertFalse(formState.isValid)
    }
    
    @Test
    fun `form validation should fail for no category selected`() {
        // When
        viewModel.updateFormField(question = "Valid question", answer = "Valid answer", categoryId = 0L)
        
        // Then
        val formState = viewModel.formState.value
        assertEquals("Please select a category", formState.categoryError)
        assertFalse(formState.isValid)
    }
    
    @Test
    fun `form validation should pass for valid input`() {
        // When
        viewModel.updateFormField(
            question = "Valid question",
            answer = "Valid answer",
            categoryId = 1L,
            difficultyLevel = 3
        )
        
        // Then
        val formState = viewModel.formState.value
        assertNull(formState.questionError)
        assertNull(formState.answerError)
        assertNull(formState.categoryError)
        assertTrue(formState.isValid)
    }
    
    @Test
    fun `saveFlashcard should create new flashcard when not in edit mode`() = runTest {
        // Given
        viewModel.initializeForNewFlashcard()
        viewModel.updateFormField(
            question = "Test Question",
            answer = "Test Answer",
            categoryId = 1L
        )
        
        coEvery { flashcardDao.countUserFlashcards(testUser.userId) } returns 50
        coEvery { flashcardDao.insertFlashcard(any()) } returns 1L
        
        var successCallbackCalled = false
        
        // When
        viewModel.saveFlashcard { successCallbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify { flashcardDao.insertFlashcard(any()) }
        assertTrue(successCallbackCalled)
    }
    
    @Test
    fun `saveFlashcard should fail when flashcard limit exceeded`() = runTest {
        // Given
        viewModel.initializeForNewFlashcard()
        viewModel.updateFormField(
            question = "Test Question",
            answer = "Test Answer",
            categoryId = 1L
        )
        
        coEvery { flashcardDao.countUserFlashcards(testUser.userId) } returns 1000
        
        // When
        viewModel.saveFlashcard {}
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals("You have reached the maximum limit of 1000 flashcards", viewModel.error.value)
        coVerify(exactly = 0) { flashcardDao.insertFlashcard(any()) }
    }
    
    @Test
    fun `getQuestionRemainingChars should return correct count`() {
        // Given
        val question = "Test question"
        viewModel.updateFormField(question = question)
        
        // When
        val remaining = viewModel.getQuestionRemainingChars()
        
        // Then
        assertEquals(500 - question.length, remaining)
    }
    
    @Test
    fun `getAnswerRemainingChars should return correct count`() {
        // Given
        val answer = "Test answer"
        viewModel.updateFormField(answer = answer)
        
        // When
        val remaining = viewModel.getAnswerRemainingChars()
        
        // Then
        assertEquals(1000 - answer.length, remaining)
    }
}

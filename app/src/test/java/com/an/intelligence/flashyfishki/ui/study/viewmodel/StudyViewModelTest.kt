package com.an.intelligence.flashyfishki.ui.study.viewmodel

import com.an.intelligence.flashyfishki.domain.model.Flashcard
import com.an.intelligence.flashyfishki.domain.repository.StudyRepository
import com.an.intelligence.flashyfishki.ui.study.model.StudyAction
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class StudyViewModelTest {
    
    private lateinit var viewModel: StudyViewModel
    private lateinit var mockRepository: StudyRepository
    private val testDispatcher = UnconfinedTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        viewModel = StudyViewModel(mockRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `startStudySession should load flashcards and update state`() = runTest {
        // Given
        val categoryId = 1L
        val userId = 100L
        val flashcards = listOf(
            createTestFlashcard(1L, "Question 1", "Answer 1"),
            createTestFlashcard(2L, "Question 2", "Answer 2")
        )
        
        every { mockRepository.getFlashcardsForStudy(userId, categoryId) } returns flowOf(flashcards)
        
        // When
        viewModel.startStudySession(categoryId, userId)
        
        // Then
        val sessionState = viewModel.sessionState.value
        assert(sessionState != null)
        assert(sessionState!!.categoryId == categoryId)
        assert(sessionState.flashcards == flashcards)
        assert(sessionState.currentIndex == 0)
        assert(!sessionState.isAnswerVisible)
        assert(sessionState.sessionStats.totalCards == flashcards.size)
    }
    
    @Test
    fun `startStudySession with empty flashcards should set error`() = runTest {
        // Given
        val categoryId = 1L
        val userId = 100L
        
        every { mockRepository.getFlashcardsForStudy(userId, categoryId) } returns flowOf(emptyList())
        
        // When
        viewModel.startStudySession(categoryId, userId)
        
        // Then
        assert(viewModel.sessionState.value == null)
        assert(viewModel.error.value == "No flashcards available for study in this category")
    }
    
    @Test
    fun `handleAction ShowAnswer should make answer visible`() = runTest {
        // Given
        setupViewModelWithFlashcards()
        
        // When
        viewModel.handleAction(StudyAction.ShowAnswer)
        
        // Then
        val sessionState = viewModel.sessionState.value
        assert(sessionState != null)
        assert(sessionState!!.isAnswerVisible)
    }
    
    @Test
    fun `handleAction CorrectAnswer should update flashcard status and stats`() = runTest {
        // Given
        setupViewModelWithFlashcards()
        viewModel.handleAction(StudyAction.ShowAnswer) // Answer must be visible first
        
        coEvery { mockRepository.updateFlashcardLearningStatus(any(), true, any()) } just Runs
        
        // When
        viewModel.handleAction(StudyAction.CorrectAnswer)
        
        // Then
        val sessionState = viewModel.sessionState.value
        assert(sessionState != null)
        assert(sessionState!!.sessionStats.completedCards == 1)
        assert(sessionState.sessionStats.correctAnswers == 1)
        assert(sessionState.sessionStats.incorrectAnswers == 0)
        
        coVerify { mockRepository.updateFlashcardLearningStatus(1L, true, 100L) }
    }
    
    @Test
    fun `handleAction IncorrectAnswer should update flashcard status and stats`() = runTest {
        // Given
        setupViewModelWithFlashcards()
        viewModel.handleAction(StudyAction.ShowAnswer) // Answer must be visible first
        
        coEvery { mockRepository.updateFlashcardLearningStatus(any(), false, any()) } just Runs
        
        // When
        viewModel.handleAction(StudyAction.IncorrectAnswer)
        
        // Then
        val sessionState = viewModel.sessionState.value
        assert(sessionState != null)
        assert(sessionState!!.sessionStats.completedCards == 1)
        assert(sessionState.sessionStats.correctAnswers == 0)
        assert(sessionState.sessionStats.incorrectAnswers == 1)
        
        coVerify { mockRepository.updateFlashcardLearningStatus(1L, false, 100L) }
    }
    
    @Test
    fun `evaluateAnswer without visible answer should set error`() = runTest {
        // Given
        setupViewModelWithFlashcards()
        // Don't show answer first
        
        // When
        viewModel.handleAction(StudyAction.CorrectAnswer)
        
        // Then
        assert(viewModel.error.value == "Answer must be visible before evaluation")
    }
    
    @Test
    fun `handleAction EndSession should set session end time and save statistics`() = runTest {
        // Given
        setupViewModelWithFlashcards()
        
        coEvery { mockRepository.saveLearningStatistics(any(), any(), any()) } just Runs
        
        // When
        viewModel.handleAction(StudyAction.EndSession)
        
        // Then
        val sessionState = viewModel.sessionState.value
        assert(sessionState != null)
        assert(sessionState!!.sessionStats.sessionEndTime != null)
        
        val completedStats = viewModel.completedSessionStats.value
        assert(completedStats != null)
        assert(completedStats!!.sessionEndTime != null)
        
        coVerify { mockRepository.saveLearningStatistics(100L, 1L, any()) }
    }
    
    @Test
    fun `getCurrentFlashcard should return current flashcard`() = runTest {
        // Given
        setupViewModelWithFlashcards()
        
        // When
        val currentFlashcard = viewModel.getCurrentFlashcard()
        
        // Then
        assert(currentFlashcard != null)
        assert(currentFlashcard!!.flashcardId == 1L)
        assert(currentFlashcard.question == "Question 1")
    }
    
    @Test
    fun `getSessionProgress should calculate correct progress`() = runTest {
        // Given
        setupViewModelWithFlashcards()
        
        // When
        val progress = viewModel.getSessionProgress()
        
        // Then
        assert(progress == 0.5f) // 1 out of 2 cards (index 0, so card 1)
    }
    
    @Test
    fun `canEvaluateAnswer should return false when answer not visible`() = runTest {
        // Given
        setupViewModelWithFlashcards()
        
        // When
        val canEvaluate = viewModel.canEvaluateAnswer()
        
        // Then
        assert(!canEvaluate)
    }
    
    @Test
    fun `canEvaluateAnswer should return true when answer is visible`() = runTest {
        // Given
        setupViewModelWithFlashcards()
        viewModel.handleAction(StudyAction.ShowAnswer)
        
        // When
        val canEvaluate = viewModel.canEvaluateAnswer()
        
        // Then
        assert(canEvaluate)
    }
    
    private suspend fun setupViewModelWithFlashcards() {
        val flashcards = listOf(
            createTestFlashcard(1L, "Question 1", "Answer 1"),
            createTestFlashcard(2L, "Question 2", "Answer 2")
        )
        
        every { mockRepository.getFlashcardsForStudy(100L, 1L) } returns flowOf(flashcards)
        viewModel.startStudySession(1L, 100L)
    }
    
    private fun createTestFlashcard(
        id: Long,
        question: String,
        answer: String
    ) = Flashcard(
        flashcardId = id,
        userId = 100L,
        categoryId = 1L,
        question = question,
        answer = answer,
        difficultyLevel = 1,
        learningStatus = 0,
        nextReviewDate = null,
        isPublic = false,
        originalFlashcardId = null,
        copiesCount = 0,
        createdAt = Date(),
        updatedAt = Date()
    )
}

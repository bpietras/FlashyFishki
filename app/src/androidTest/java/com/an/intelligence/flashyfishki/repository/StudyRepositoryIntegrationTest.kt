package com.an.intelligence.flashyfishki.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.domain.dao.FlashcardDao
import com.an.intelligence.flashyfishki.domain.dao.UserDao
import com.an.intelligence.flashyfishki.domain.model.Category
import com.an.intelligence.flashyfishki.domain.model.Flashcard
import com.an.intelligence.flashyfishki.domain.model.FlashyFishkiDatabase
import com.an.intelligence.flashyfishki.domain.model.User
import com.an.intelligence.flashyfishki.domain.repository.StudyRepository
import com.an.intelligence.flashyfishki.ui.study.model.StudySessionStats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.util.Calendar
import java.util.Date

/**
 * Integration tests for StudyRepository with Room Database
 * Tests the interaction between repository and DAO layers for study functionality
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@MediumTest
class StudyRepositoryIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: FlashyFishkiDatabase
    private lateinit var studyRepository: StudyRepository
    private lateinit var flashcardDao: FlashcardDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var userDao: UserDao
    
    private lateinit var testUser: User
    private lateinit var testCategory1: Category
    private lateinit var testCategory2: Category
    private var testUserId: Long = 0
    private var testCategory1Id: Long = 0
    private var testCategory2Id: Long = 0

    @Before
    fun setup() {
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FlashyFishkiDatabase::class.java
        ).allowMainThreadQueries().build()
        
        flashcardDao = database.flashcardDao()
        categoryDao = database.categoryDao()
        userDao = database.userDao()
        
        // Create StudyRepository with actual DAOs
        studyRepository = StudyRepository(flashcardDao, categoryDao, userDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    private suspend fun createTestUserAndCategories() {
        // Create test user
        testUser = User(
            email = "study@test.com",
            passwordHash = "hashed_password",
            createdAt = Date()
        )
        testUserId = userDao.insertUser(testUser)
        
        // Create test categories
        testCategory1 = Category(name = "Mathematics")
        testCategory2 = Category(name = "Science")
        testCategory1Id = categoryDao.insertCategory(testCategory1)
        testCategory2Id = categoryDao.insertCategory(testCategory2)
    }

    private suspend fun createTestFlashcards() {
        // Math category flashcards with different learning statuses
        val mathFlashcards = listOf(
            Flashcard(
                userId = testUserId,
                categoryId = testCategory1Id,
                question = "What is 2 + 2?",
                answer = "4",
                difficultyLevel = 1,
                learningStatus = 0 // New
            ),
            Flashcard(
                userId = testUserId,
                categoryId = testCategory1Id,
                question = "What is 5 * 3?",
                answer = "15",
                difficultyLevel = 2,
                learningStatus = 1, // First repeat
                nextReviewDate = getPastDate()
            ),
            Flashcard(
                userId = testUserId,
                categoryId = testCategory1Id,
                question = "What is sqrt(16)?",
                answer = "4",
                difficultyLevel = 3,
                learningStatus = 2, // Second repeat
                nextReviewDate = getPastDate()
            ),
            Flashcard(
                userId = testUserId,
                categoryId = testCategory1Id,
                question = "What is 10 / 2?",
                answer = "5",
                difficultyLevel = 1,
                learningStatus = 3 // Learned
            )
        )
        
        // Science category flashcards
        val scienceFlashcards = listOf(
            Flashcard(
                userId = testUserId,
                categoryId = testCategory2Id,
                question = "What is H2O?",
                answer = "Water",
                difficultyLevel = 1,
                learningStatus = 0 // New
            ),
            Flashcard(
                userId = testUserId,
                categoryId = testCategory2Id,
                question = "What is CO2?",
                answer = "Carbon Dioxide",
                difficultyLevel = 2,
                learningStatus = 1, // First repeat
                nextReviewDate = getFutureDate() // Not ready for review
            )
        )
        
        (mathFlashcards + scienceFlashcards).forEach { flashcard ->
            flashcardDao.insertFlashcard(flashcard)
        }
    }

    private fun getPastDate(): Date {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.time
    }

    private fun getFutureDate(): Date {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }.time
    }

    @Test
    fun getCategoriesWithStudyStats_returnsCorrectStatistics() = runTest {
        // Arrange
        createTestUserAndCategories()
        createTestFlashcards()

        // Act
        val categoriesWithStats = studyRepository.getCategoriesWithStudyStats(testUserId).first()

        // Assert
        assertEquals(2, categoriesWithStats.size)
        
        val mathCategory = categoriesWithStats.find { it.name == "Mathematics" }
        val scienceCategory = categoriesWithStats.find { it.name == "Science" }
        
        assertNotNull(mathCategory)
        assertNotNull(scienceCategory)
        
        // Mathematics category: 4 total, 3 to review (statuses 0,1,2), 1 new, 2 review
        assertEquals(4, mathCategory!!.totalFlashcards)
        assertEquals(3, mathCategory.flashcardsToReview)
        assertEquals(1, mathCategory.newFlashcards)
        assertEquals(2, mathCategory.reviewFlashcards)
        
        // Science category: 2 total, 2 to review (statuses 0,1), 1 new, 1 review  
        assertEquals(2, scienceCategory!!.totalFlashcards)
        assertEquals(2, scienceCategory.flashcardsToReview)
        assertEquals(1, scienceCategory.newFlashcards)
        assertEquals(1, scienceCategory.reviewFlashcards)
    }

    @Test
    fun getFlashcardsForStudy_returnsOnlyReviewableCards() = runTest {
        // Arrange
        createTestUserAndCategories()
        createTestFlashcards()

        // Act
        val studyFlashcards = studyRepository.getFlashcardsForStudy(testUserId, testCategory1Id).first()

        // Assert
        // Should return 3 cards: 1 new (status 0) + 2 ready for review (status 1,2 with past dates)
        // Should NOT include: learned card (status 3)
        assertEquals(3, studyFlashcards.size)
        
        val questions = studyFlashcards.map { it.question }
        assertTrue(questions.contains("What is 2 + 2?")) // New card
        assertTrue(questions.contains("What is 5 * 3?")) // First repeat, past date
        assertTrue(questions.contains("What is sqrt(16)?")) // Second repeat, past date
        assertFalse(questions.contains("What is 10 / 2?")) // Learned card
        
        // Verify ordering (by learning status, then review date)
        val firstCard = studyFlashcards[0]
        assertEquals(0, firstCard.learningStatus) // New cards first
    }

    @Test
    fun getFlashcardsForStudy_filtersCorrectlyByCategory() = runTest {
        // Arrange
        createTestUserAndCategories()
        createTestFlashcards()

        // Act - Get flashcards for Science category
        val scienceFlashcards = studyRepository.getFlashcardsForStudy(testUserId, testCategory2Id).first()

        // Assert
        // Science category should have 1 card ready for review (new card)
        // The card with future review date should not be included
        assertEquals(1, scienceFlashcards.size)
        assertEquals("What is H2O?", scienceFlashcards[0].question)
        assertEquals(0, scienceFlashcards[0].learningStatus)
    }

    @Test
    fun updateFlashcardLearningStatus_correctAnswer_advancesStatus() = runTest {
        // Arrange
        createTestUserAndCategories()
        val flashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategory1Id,
            question = "Test Question",
            answer = "Test Answer",
            difficultyLevel = 1,
            learningStatus = 0
        )
        val flashcardId = flashcardDao.insertFlashcard(flashcard)

        // Act
        studyRepository.updateFlashcardLearningStatus(flashcardId, true, testUserId)

        // Assert
        val updatedFlashcard = flashcardDao.getFlashcardById(flashcardId)
        assertNotNull(updatedFlashcard)
        assertEquals(1, updatedFlashcard!!.learningStatus)
        assertNotNull(updatedFlashcard.nextReviewDate)
        
        // Verify user statistics were updated
        val updatedUser = userDao.getUserById(testUserId)
        assertNotNull(updatedUser)
        assertEquals(1, updatedUser!!.totalCardsReviewed)
        assertEquals(1, updatedUser.correctAnswers)
        assertEquals(0, updatedUser.incorrectAnswers)
    }

    @Test
    fun updateFlashcardLearningStatus_incorrectAnswer_resetsStatus() = runTest {
        // Arrange
        createTestUserAndCategories()
        val flashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategory1Id,
            question = "Test Question",
            answer = "Test Answer",
            difficultyLevel = 1,
            learningStatus = 2 // Second repeat
        )
        val flashcardId = flashcardDao.insertFlashcard(flashcard)

        // Act
        studyRepository.updateFlashcardLearningStatus(flashcardId, false, testUserId)

        // Assert
        val updatedFlashcard = flashcardDao.getFlashcardById(flashcardId)
        assertNotNull(updatedFlashcard)
        assertEquals(0, updatedFlashcard!!.learningStatus) // Reset to new
        assertNull(updatedFlashcard.nextReviewDate) // Review immediately
        
        // Verify user statistics were updated
        val updatedUser = userDao.getUserById(testUserId)
        assertNotNull(updatedUser)
        assertEquals(1, updatedUser!!.totalCardsReviewed)
        assertEquals(0, updatedUser.correctAnswers)
        assertEquals(1, updatedUser.incorrectAnswers)
    }

    @Test
    fun updateFlashcardLearningStatus_spacedRepetitionAlgorithm_works() = runTest {
        // Arrange
        createTestUserAndCategories()
        val flashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategory1Id,
            question = "Test Question",
            answer = "Test Answer",
            difficultyLevel = 1,
            learningStatus = 0
        )
        val flashcardId = flashcardDao.insertFlashcard(flashcard)

        // Act & Assert - Test progression through 3-5-7 algorithm
        
        // First correct answer: 0 -> 1 (3 days)
        studyRepository.updateFlashcardLearningStatus(flashcardId, true, testUserId)
        var updatedFlashcard = flashcardDao.getFlashcardById(flashcardId)!!
        assertEquals(1, updatedFlashcard.learningStatus)
        assertNotNull(updatedFlashcard.nextReviewDate)
        
        // Verify it's approximately 3 days from now
        val threeDaysFromNow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }.time
        val reviewDate1 = updatedFlashcard.nextReviewDate!!
        val diffDays1 = (reviewDate1.time - Date().time) / (1000 * 60 * 60 * 24)
        assertTrue("Review date should be ~3 days", diffDays1 in 2..4)

        // Second correct answer: 1 -> 2 (5 days)
        studyRepository.updateFlashcardLearningStatus(flashcardId, true, testUserId)
        updatedFlashcard = flashcardDao.getFlashcardById(flashcardId)!!
        assertEquals(2, updatedFlashcard.learningStatus)
        
        val reviewDate2 = updatedFlashcard.nextReviewDate!!
        val diffDays2 = (reviewDate2.time - Date().time) / (1000 * 60 * 60 * 24)
        assertTrue("Review date should be ~5 days", diffDays2 in 4..6)

        // Third correct answer: 2 -> 3 (7 days, learned)
        studyRepository.updateFlashcardLearningStatus(flashcardId, true, testUserId)
        updatedFlashcard = flashcardDao.getFlashcardById(flashcardId)!!
        assertEquals(3, updatedFlashcard.learningStatus)
        
        val reviewDate3 = updatedFlashcard.nextReviewDate!!
        val diffDays3 = (reviewDate3.time - Date().time) / (1000 * 60 * 60 * 24)
        assertTrue("Review date should be ~7 days", diffDays3 in 6..8)
    }

    @Test
    fun updateFlashcardLearningStatus_maxStatusReached_staysAtMax() = runTest {
        // Arrange
        createTestUserAndCategories()
        val flashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategory1Id,
            question = "Test Question",
            answer = "Test Answer",
            difficultyLevel = 1,
            learningStatus = 3 // Already learned
        )
        val flashcardId = flashcardDao.insertFlashcard(flashcard)

        // Act
        studyRepository.updateFlashcardLearningStatus(flashcardId, true, testUserId)

        // Assert
        val updatedFlashcard = flashcardDao.getFlashcardById(flashcardId)
        assertNotNull(updatedFlashcard)
        assertEquals(3, updatedFlashcard!!.learningStatus) // Should stay at max
    }

    @Test
    fun updateFlashcardLearningStatus_unauthorizedUser_doesNotUpdate() = runTest {
        // Arrange
        createTestUserAndCategories()
        val flashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategory1Id,
            question = "Test Question",
            answer = "Test Answer",
            difficultyLevel = 1,
            learningStatus = 0
        )
        val flashcardId = flashcardDao.insertFlashcard(flashcard)

        // Act - Try to update with different user ID
        val unauthorizedUserId = 999L
        studyRepository.updateFlashcardLearningStatus(flashcardId, true, unauthorizedUserId)

        // Assert - Flashcard should remain unchanged
        val unchangedFlashcard = flashcardDao.getFlashcardById(flashcardId)
        assertNotNull(unchangedFlashcard)
        assertEquals(0, unchangedFlashcard!!.learningStatus) // Should not change
        assertNull(unchangedFlashcard.nextReviewDate)
    }

    @Test
    fun saveLearningStatistics_integrationPlaceholder() = runTest {
        // Arrange
        createTestUserAndCategories()
        val sessionStats = StudySessionStats(
            totalCards = 5,
            completedCards = 4,
            correctAnswers = 3,
            incorrectAnswers = 1
        )

        // Act - Currently this is a placeholder implementation
        studyRepository.saveLearningStatistics(testUserId, testCategory1Id, sessionStats)

        // Assert - For now, just verify it doesn't throw exceptions
        // When LearningStatisticsDao is implemented, this test should verify
        // that statistics are properly saved to the database
        assertTrue("Method should not throw exceptions", true)
    }

    @Test
    fun multipleOperations_workCorrectlyTogether() = runTest {
        // Arrange
        createTestUserAndCategories()
        createTestFlashcards()

        // Act - Perform multiple study operations
        val initialStats = studyRepository.getCategoriesWithStudyStats(testUserId).first()
        val studyFlashcards = studyRepository.getFlashcardsForStudy(testUserId, testCategory1Id).first()
        
        // Study the first flashcard correctly
        val firstFlashcard = studyFlashcards[0]
        studyRepository.updateFlashcardLearningStatus(firstFlashcard.flashcardId, true, testUserId)
        
        // Get updated stats
        val updatedStats = studyRepository.getCategoriesWithStudyStats(testUserId).first()

        // Assert
        val initialMathStats = initialStats.find { it.name == "Mathematics" }!!
        val updatedMathStats = updatedStats.find { it.name == "Mathematics" }!!
        
        // If a new card (status 0) was answered correctly, it becomes first repeat (status 1)
        // This means flashcardsToReview count might change based on the card's next review date
        assertEquals(initialMathStats.totalFlashcards, updatedMathStats.totalFlashcards)
        
        // Verify user statistics were updated
        val finalUser = userDao.getUserById(testUserId)
        assertNotNull(finalUser)
        assertTrue("User should have reviewed at least 1 card", finalUser!!.totalCardsReviewed >= 1)
    }

    @Test
    fun emptyCategory_returnsEmptyResults() = runTest {
        // Arrange
        createTestUserAndCategories()
        // Don't create any flashcards

        // Act
        val categoriesWithStats = studyRepository.getCategoriesWithStudyStats(testUserId).first()
        val studyFlashcards = studyRepository.getFlashcardsForStudy(testUserId, testCategory1Id).first()

        // Assert
        assertTrue("Should return empty flashcard list", studyFlashcards.isEmpty())
        
        val mathCategory = categoriesWithStats.find { it.name == "Mathematics" }
        if (mathCategory != null) {
            assertEquals(0, mathCategory.totalFlashcards)
            assertEquals(0, mathCategory.flashcardsToReview)
            assertEquals(0, mathCategory.newFlashcards)
            assertEquals(0, mathCategory.reviewFlashcards)
        }
    }
}

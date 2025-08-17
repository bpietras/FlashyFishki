package com.an.intelligence.flashyfishki.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.domain.dao.FlashcardDao
import com.an.intelligence.flashyfishki.domain.dao.UserDao
import com.an.intelligence.flashyfishki.domain.model.Category
import com.an.intelligence.flashyfishki.domain.model.Flashcard
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
import java.util.Calendar
import java.util.Date

/**
 * Integration tests for FlashcardDao with Room Database
 * Tests the complete database interaction flow for flashcard operations
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@SmallTest
class FlashcardDaoIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: FlashyFishkiDatabase
    private lateinit var flashcardDao: FlashcardDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var userDao: UserDao
    
    private lateinit var testUser: User
    private lateinit var testCategory: Category
    private var testUserId: Long = 0
    private var testCategoryId: Long = 0

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
    }

    @After
    fun teardown() {
        database.close()
    }

    private suspend fun createTestUserAndCategory() {
        testUser = User(
            email = "test@example.com",
            passwordHash = "hashed_password",
            createdAt = Date()
        )
        testUserId = userDao.insertUser(testUser)
        
        testCategory = Category(name = "Test Category")
        testCategoryId = categoryDao.insertCategory(testCategory)
    }

    @Test
    fun insertFlashcard_savesFlashcardCorrectly() = runTest {
        // Arrange
        createTestUserAndCategory()
        val flashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "What is the capital of Poland?",
            answer = "Warsaw",
            difficultyLevel = 2,
            learningStatus = 0,
            isPublic = false
        )

        // Act
        val flashcardId = flashcardDao.insertFlashcard(flashcard)

        // Assert
        assertTrue(flashcardId > 0)
        val savedFlashcard = flashcardDao.getFlashcardById(flashcardId)
        assertNotNull(savedFlashcard)
        assertEquals(flashcard.question, savedFlashcard!!.question)
        assertEquals(flashcard.answer, savedFlashcard.answer)
        assertEquals(flashcard.userId, savedFlashcard.userId)
        assertEquals(flashcard.categoryId, savedFlashcard.categoryId)
        assertEquals(flashcard.difficultyLevel, savedFlashcard.difficultyLevel)
        assertEquals(flashcard.learningStatus, savedFlashcard.learningStatus)
        assertFalse(savedFlashcard.isPublic)
    }

    @Test
    fun getUserFlashcards_returnsCorrectFlashcards() = runTest {
        // Arrange
        createTestUserAndCategory()
        val flashcard1 = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "Question 1",
            answer = "Answer 1",
            difficultyLevel = 1
        )
        val flashcard2 = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "Question 2",
            answer = "Answer 2",
            difficultyLevel = 2
        )

        flashcardDao.insertFlashcard(flashcard1)
        flashcardDao.insertFlashcard(flashcard2)

        // Act
        val userFlashcards = flashcardDao.getUserFlashcards(testUserId).first()

        // Assert
        assertEquals(2, userFlashcards.size)
        assertTrue(userFlashcards.any { it.question == "Question 1" })
        assertTrue(userFlashcards.any { it.question == "Question 2" })
    }

    @Test
    fun getUserFlashcardsByCategory_filtersCorrectly() = runTest {
        // Arrange
        createTestUserAndCategory()
        val category2 = Category(name = "Second Category")
        val category2Id = categoryDao.insertCategory(category2)
        
        val flashcard1 = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "Question 1",
            answer = "Answer 1",
            difficultyLevel = 1
        )
        val flashcard2 = Flashcard(
            userId = testUserId,
            categoryId = category2Id,
            question = "Question 2",
            answer = "Answer 2",
            difficultyLevel = 2
        )

        flashcardDao.insertFlashcard(flashcard1)
        flashcardDao.insertFlashcard(flashcard2)

        // Act
        val categoryFlashcards = flashcardDao.getUserFlashcardsByCategory(testUserId, testCategoryId).first()

        // Assert
        assertEquals(1, categoryFlashcards.size)
        assertEquals("Question 1", categoryFlashcards[0].question)
        assertEquals(testCategoryId, categoryFlashcards[0].categoryId)
    }

    @Test
    fun getFlashcardsForReview_returnsOnlyReviewableCards() = runTest {
        // Arrange
        createTestUserAndCategory()
        val currentDate = Date()
        val pastDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.time
        val futureDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }.time

        // Flashcard ready for review (past date)
        val reviewableFlashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "Reviewable",
            answer = "Answer",
            difficultyLevel = 1,
            learningStatus = 1,
            nextReviewDate = pastDate
        )
        
        // Flashcard not ready for review (future date)
        val futureFlashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "Future",
            answer = "Answer",
            difficultyLevel = 1,
            learningStatus = 1,
            nextReviewDate = futureDate
        )
        
        // New flashcard (no review date)
        val newFlashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "New",
            answer = "Answer",
            difficultyLevel = 1,
            learningStatus = 0,
            nextReviewDate = null
        )
        
        // Learned flashcard (status 3)
        val learnedFlashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "Learned",
            answer = "Answer",
            difficultyLevel = 1,
            learningStatus = 3,
            nextReviewDate = pastDate
        )

        flashcardDao.insertFlashcard(reviewableFlashcard)
        flashcardDao.insertFlashcard(futureFlashcard)
        flashcardDao.insertFlashcard(newFlashcard)
        flashcardDao.insertFlashcard(learnedFlashcard)

        // Act
        val flashcardsForReview = flashcardDao.getFlashcardsForReview(testUserId, currentDate).first()

        // Assert
        assertEquals(2, flashcardsForReview.size)
        assertTrue(flashcardsForReview.any { it.question == "Reviewable" })
        assertTrue(flashcardsForReview.any { it.question == "New" })
        assertFalse(flashcardsForReview.any { it.question == "Future" })
        assertFalse(flashcardsForReview.any { it.question == "Learned" })
    }

    @Test
    fun updateFlashcardLearningStatus_updatesCorrectly() = runTest {
        // Arrange
        createTestUserAndCategory()
        val flashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "Question",
            answer = "Answer",
            difficultyLevel = 1,
            learningStatus = 0,
            nextReviewDate = null
        )
        val flashcardId = flashcardDao.insertFlashcard(flashcard)
        
        val nextReviewDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 3)
        }.time
        val updateTime = Date()

        // Act
        flashcardDao.updateFlashcardLearningStatus(
            flashcardId = flashcardId,
            newStatus = 1,
            nextReviewDate = nextReviewDate,
            updateTime = updateTime
        )

        // Assert
        val updatedFlashcard = flashcardDao.getFlashcardById(flashcardId)
        assertNotNull(updatedFlashcard)
        assertEquals(1, updatedFlashcard!!.learningStatus)
        assertNotNull(updatedFlashcard.nextReviewDate)
        assertEquals(nextReviewDate.time, updatedFlashcard.nextReviewDate!!.time)
    }

    @Test
    fun copyPublicFlashcard_createsCorrectCopy() = runTest {
        // Arrange
        createTestUserAndCategory()
        val originalFlashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "Original Question",
            answer = "Original Answer",
            difficultyLevel = 3,
            learningStatus = 2,
            isPublic = true,
            copiesCount = 0
        )
        val originalId = flashcardDao.insertFlashcard(originalFlashcard)
        
        // Create another user
        val newUser = User(
            email = "new@example.com",
            passwordHash = "hash",
            createdAt = Date()
        )
        val newUserId = userDao.insertUser(newUser)

        // Act
        val copiedId = flashcardDao.copyPublicFlashcard(originalId, newUserId)

        // Assert
        assertNotNull(copiedId)
        assertTrue(copiedId!! > 0)
        
        val copiedFlashcard = flashcardDao.getFlashcardById(copiedId)
        assertNotNull(copiedFlashcard)
        assertEquals(newUserId, copiedFlashcard!!.userId)
        assertEquals("Original Question", copiedFlashcard.question)
        assertEquals("Original Answer", copiedFlashcard.answer)
        assertEquals(3, copiedFlashcard.difficultyLevel)
        assertEquals(0, copiedFlashcard.learningStatus) // Reset to new
        assertNull(copiedFlashcard.nextReviewDate) // Reset review date
        assertFalse(copiedFlashcard.isPublic) // Set to private
        assertEquals(originalId, copiedFlashcard.originalFlashcardId)
        assertEquals(0, copiedFlashcard.copiesCount)
        
        // Check that original flashcard's copies count was incremented
        val originalUpdated = flashcardDao.getFlashcardById(originalId)
        assertNotNull(originalUpdated)
        assertEquals(1, originalUpdated!!.copiesCount)
    }

    @Test
    fun copyPublicFlashcard_failsForPrivateFlashcard() = runTest {
        // Arrange
        createTestUserAndCategory()
        val privateFlashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "Private Question",
            answer = "Private Answer",
            difficultyLevel = 1,
            isPublic = false
        )
        val privateId = flashcardDao.insertFlashcard(privateFlashcard)
        
        val newUser = User(
            email = "new@example.com",
            passwordHash = "hash",
            createdAt = Date()
        )
        val newUserId = userDao.insertUser(newUser)

        // Act
        val copiedId = flashcardDao.copyPublicFlashcard(privateId, newUserId)

        // Assert
        assertNull(copiedId)
    }

    @Test
    fun countUserFlashcards_returnsCorrectCount() = runTest {
        // Arrange
        createTestUserAndCategory()
        val flashcard1 = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "Q1",
            answer = "A1",
            difficultyLevel = 1
        )
        val flashcard2 = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "Q2",
            answer = "A2",
            difficultyLevel = 2
        )

        flashcardDao.insertFlashcard(flashcard1)
        flashcardDao.insertFlashcard(flashcard2)

        // Act
        val count = flashcardDao.countUserFlashcards(testUserId)

        // Assert
        assertEquals(2, count)
    }

    @Test
    fun countLearnedFlashcards_countsOnlyLearned() = runTest {
        // Arrange
        createTestUserAndCategory()
        val newFlashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "New",
            answer = "A",
            difficultyLevel = 1,
            learningStatus = 0
        )
        val learnedFlashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "Learned",
            answer = "A",
            difficultyLevel = 1,
            learningStatus = 3
        )

        flashcardDao.insertFlashcard(newFlashcard)
        flashcardDao.insertFlashcard(learnedFlashcard)

        // Act
        val learnedCount = flashcardDao.countLearnedFlashcards(testUserId)

        // Assert
        assertEquals(1, learnedCount)
    }

    @Test
    fun updateFlashcardPublicStatus_changesVisibility() = runTest {
        // Arrange
        createTestUserAndCategory()
        val flashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "Question",
            answer = "Answer",
            difficultyLevel = 1,
            isPublic = false
        )
        val flashcardId = flashcardDao.insertFlashcard(flashcard)
        val updateTime = Date()

        // Act
        flashcardDao.updateFlashcardPublicStatus(flashcardId, true, updateTime)

        // Assert
        val updatedFlashcard = flashcardDao.getFlashcardById(flashcardId)
        assertNotNull(updatedFlashcard)
        assertTrue(updatedFlashcard!!.isPublic)
    }

    @Test
    fun deleteFlashcard_removesFromDatabase() = runTest {
        // Arrange
        createTestUserAndCategory()
        val flashcard = Flashcard(
            userId = testUserId,
            categoryId = testCategoryId,
            question = "To Delete",
            answer = "Answer",
            difficultyLevel = 1
        )
        val flashcardId = flashcardDao.insertFlashcard(flashcard)
        
        // Verify it exists
        var savedFlashcard = flashcardDao.getFlashcardById(flashcardId)
        assertNotNull(savedFlashcard)

        // Act
        flashcardDao.deleteFlashcard(savedFlashcard!!)

        // Assert
        savedFlashcard = flashcardDao.getFlashcardById(flashcardId)
        assertNull(savedFlashcard)
    }
}

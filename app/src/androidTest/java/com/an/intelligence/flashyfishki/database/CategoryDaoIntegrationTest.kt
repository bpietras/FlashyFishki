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
import java.util.Date

/**
 * Integration tests for CategoryDao with Room Database
 * Tests category operations and relationships with flashcards
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@SmallTest
class CategoryDaoIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: FlashyFishkiDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var flashcardDao: FlashcardDao
    private lateinit var userDao: UserDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FlashyFishkiDatabase::class.java
        ).allowMainThreadQueries().build()
        
        categoryDao = database.categoryDao()
        flashcardDao = database.flashcardDao()
        userDao = database.userDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertCategory_savesCorrectly() = runTest {
        // Arrange
        val category = Category(name = "Programming")

        // Act
        val categoryId = categoryDao.insertCategory(category)

        // Assert
        assertTrue(categoryId > 0)
        val savedCategory = categoryDao.getCategoryById(categoryId)
        assertNotNull(savedCategory)
        assertEquals("Programming", savedCategory!!.name)
        assertEquals(categoryId, savedCategory.categoryId)
    }

    @Test
    fun insertCategory_throwsExceptionForDuplicateName() = runTest {
        // Arrange
        val category1 = Category(name = "Programming")
        val category2 = Category(name = "Programming")

        // Act & Assert
        categoryDao.insertCategory(category1)
        
        try {
            categoryDao.insertCategory(category2)
            assertTrue("Expected exception for duplicate category name", false)
        } catch (e: Exception) {
            // Expected - duplicate name should throw exception due to unique constraint
            assertTrue(true)
        }
    }

    @Test
    fun getAllCategories_returnsAllCategoriesInOrder() = runTest {
        // Arrange
        val category1 = Category(name = "Zebra")
        val category2 = Category(name = "Apple")
        val category3 = Category(name = "Book")

        categoryDao.insertCategory(category1)
        categoryDao.insertCategory(category2)
        categoryDao.insertCategory(category3)

        // Act
        val categories = categoryDao.getAllCategories().first()

        // Assert
        assertEquals(3, categories.size)
        assertEquals("Apple", categories[0].name) // Alphabetical order
        assertEquals("Book", categories[1].name)
        assertEquals("Zebra", categories[2].name)
    }

    @Test
    fun getCategoryByName_findsCorrectCategory() = runTest {
        // Arrange
        val category = Category(name = "Mathematics")
        categoryDao.insertCategory(category)

        // Act
        val foundCategory = categoryDao.getCategoryByName("Mathematics")

        // Assert
        assertNotNull(foundCategory)
        assertEquals("Mathematics", foundCategory!!.name)
    }

    @Test
    fun getCategoryByName_returnsNullForNonExistent() = runTest {
        // Act
        val foundCategory = categoryDao.getCategoryByName("NonExistent")

        // Assert
        assertNull(foundCategory)
    }

    @Test
    fun hasCategoryFlashcards_returnsFalseForEmptyCategory() = runTest {
        // Arrange
        val category = Category(name = "Empty Category")
        val categoryId = categoryDao.insertCategory(category)

        // Act
        val hasFlashcards = categoryDao.hasCategoryFlashcards(categoryId)

        // Assert
        assertFalse(hasFlashcards)
    }

    @Test
    fun hasCategoryFlashcards_returnsTrueForCategoryWithFlashcards() = runTest {
        // Arrange
        val user = User(
            email = "test@example.com",
            passwordHash = "hash",
            createdAt = Date()
        )
        val userId = userDao.insertUser(user)
        
        val category = Category(name = "Category With Cards")
        val categoryId = categoryDao.insertCategory(category)
        
        val flashcard = Flashcard(
            userId = userId,
            categoryId = categoryId,
            question = "Question",
            answer = "Answer",
            difficultyLevel = 1
        )
        flashcardDao.insertFlashcard(flashcard)

        // Act
        val hasFlashcards = categoryDao.hasCategoryFlashcards(categoryId)

        // Assert
        assertTrue(hasFlashcards)
    }

    @Test
    fun getCategoriesWithFlashcardCount_calculatesCountCorrectly() = runTest {
        // Arrange
        val user = User(
            email = "test@example.com",
            passwordHash = "hash",
            createdAt = Date()
        )
        val userId = userDao.insertUser(user)
        
        val category1 = Category(name = "Category 1")
        val category2 = Category(name = "Category 2")
        val categoryId1 = categoryDao.insertCategory(category1)
        val categoryId2 = categoryDao.insertCategory(category2)
        
        // Add 2 flashcards to category1, 1 to category2
        repeat(2) { index ->
            flashcardDao.insertFlashcard(
                Flashcard(
                    userId = userId,
                    categoryId = categoryId1,
                    question = "Question $index",
                    answer = "Answer $index",
                    difficultyLevel = 1
                )
            )
        }
        
        flashcardDao.insertFlashcard(
            Flashcard(
                userId = userId,
                categoryId = categoryId2,
                question = "Question",
                answer = "Answer",
                difficultyLevel = 1
            )
        )

        // Act
        val categoriesWithCount = categoryDao.getCategoriesWithFlashcardCount().first()

        // Assert
        assertEquals(2, categoriesWithCount.size)
        val cat1WithCount = categoriesWithCount.find { it.name == "Category 1" }
        val cat2WithCount = categoriesWithCount.find { it.name == "Category 2" }
        
        assertNotNull(cat1WithCount)
        assertNotNull(cat2WithCount)
        assertEquals(2, cat1WithCount!!.flashcardCount)
        assertEquals(1, cat2WithCount!!.flashcardCount)
    }

    @Test
    fun getUserCategoriesWithFlashcardCount_filtersCorrectly() = runTest {
        // Arrange
        val user1 = User(email = "user1@example.com", passwordHash = "hash1", createdAt = Date())
        val user2 = User(email = "user2@example.com", passwordHash = "hash2", createdAt = Date())
        val userId1 = userDao.insertUser(user1)
        val userId2 = userDao.insertUser(user2)
        
        val category = Category(name = "Shared Category")
        val categoryId = categoryDao.insertCategory(category)
        
        // Add flashcards for both users
        flashcardDao.insertFlashcard(
            Flashcard(
                userId = userId1,
                categoryId = categoryId,
                question = "User1 Question",
                answer = "Answer",
                difficultyLevel = 1
            )
        )
        
        repeat(2) { index ->
            flashcardDao.insertFlashcard(
                Flashcard(
                    userId = userId2,
                    categoryId = categoryId,
                    question = "User2 Question $index",
                    answer = "Answer",
                    difficultyLevel = 1
                )
            )
        }

        // Act
        val user1Categories = categoryDao.getUserCategoriesWithFlashcardCount(userId1).first()
        val user2Categories = categoryDao.getUserCategoriesWithFlashcardCount(userId2).first()

        // Assert
        assertEquals(1, user1Categories.size)
        assertEquals(1, user2Categories.size)
        assertEquals(1, user1Categories[0].flashcardCount)
        assertEquals(2, user2Categories[0].flashcardCount)
    }

    @Test
    fun getUserCategoriesWithLearningStats_calculatesStatsCorrectly() = runTest {
        // Arrange
        val user = User(
            email = "test@example.com",
            passwordHash = "hash",
            createdAt = Date()
        )
        val userId = userDao.insertUser(user)
        
        val category = Category(name = "Study Category")
        val categoryId = categoryDao.insertCategory(category)
        
        // Add flashcards with different learning statuses
        val statuses = listOf(0, 0, 1, 1, 2, 3) // 2 new, 2 first repeat, 1 second repeat, 1 learned
        statuses.forEachIndexed { index, status ->
            flashcardDao.insertFlashcard(
                Flashcard(
                    userId = userId,
                    categoryId = categoryId,
                    question = "Question $index",
                    answer = "Answer $index",
                    difficultyLevel = 1,
                    learningStatus = status
                )
            )
        }

        // Act
        val categoriesWithStats = categoryDao.getUserCategoriesWithLearningStats(userId).first()

        // Assert
        assertEquals(1, categoriesWithStats.size)
        val categoryWithStats = categoriesWithStats[0]
        assertEquals(6, categoryWithStats.flashcardCount)
        assertEquals(2, categoryWithStats.newCount)
        assertEquals(2, categoryWithStats.firstRepeatCount)
        assertEquals(1, categoryWithStats.secondRepeatCount)
        assertEquals(1, categoryWithStats.learnedCount)
    }

    @Test
    fun safeDeleteCategory_deletesEmptyCategory() = runTest {
        // Arrange
        val category = Category(name = "Empty Category")
        val categoryId = categoryDao.insertCategory(category)

        // Act
        val wasDeleted = categoryDao.safeDeleteCategory(categoryId)

        // Assert
        assertTrue(wasDeleted)
        val deletedCategory = categoryDao.getCategoryById(categoryId)
        assertNull(deletedCategory)
    }

    @Test
    fun safeDeleteCategory_doesNotDeleteCategoryWithFlashcards() = runTest {
        // Arrange
        val user = User(
            email = "test@example.com",
            passwordHash = "hash",
            createdAt = Date()
        )
        val userId = userDao.insertUser(user)
        
        val category = Category(name = "Category With Cards")
        val categoryId = categoryDao.insertCategory(category)
        
        val flashcard = Flashcard(
            userId = userId,
            categoryId = categoryId,
            question = "Question",
            answer = "Answer",
            difficultyLevel = 1
        )
        flashcardDao.insertFlashcard(flashcard)

        // Act
        val wasDeleted = categoryDao.safeDeleteCategory(categoryId)

        // Assert
        assertFalse(wasDeleted)
        val category_still_exists = categoryDao.getCategoryById(categoryId)
        assertNotNull(category_still_exists)
    }

    @Test
    fun safeDeleteCategory_returnsFalseForNonExistentCategory() = runTest {
        // Act
        val wasDeleted = categoryDao.safeDeleteCategory(999L)

        // Assert
        assertFalse(wasDeleted)
    }

    @Test
    fun updateCategory_updatesCorrectly() = runTest {
        // Arrange
        val category = Category(name = "Original Name")
        val categoryId = categoryDao.insertCategory(category)
        val originalCategory = categoryDao.getCategoryById(categoryId)!!
        
        val updatedCategory = originalCategory.copy(name = "Updated Name")

        // Act
        categoryDao.updateCategory(updatedCategory)

        // Assert
        val savedCategory = categoryDao.getCategoryById(categoryId)
        assertNotNull(savedCategory)
        assertEquals("Updated Name", savedCategory!!.name)
    }

    @Test
    fun deleteCategory_removesFromDatabase() = runTest {
        // Arrange
        val category = Category(name = "To Delete")
        val categoryId = categoryDao.insertCategory(category)
        val savedCategory = categoryDao.getCategoryById(categoryId)!!

        // Act
        categoryDao.deleteCategory(savedCategory)

        // Assert
        val deletedCategory = categoryDao.getCategoryById(categoryId)
        assertNull(deletedCategory)
    }
}

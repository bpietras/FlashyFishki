package com.an.intelligence.flashyfishki.ui.flashcards.viewmodel

import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.domain.dao.FlashcardDao
import com.an.intelligence.flashyfishki.domain.model.Category
import com.an.intelligence.flashyfishki.domain.model.Flashcard
import com.an.intelligence.flashyfishki.domain.model.User
import com.an.intelligence.flashyfishki.domain.repository.AuthRepository
import com.an.intelligence.flashyfishki.ui.flashcards.model.ExportProgress
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class ExportViewModelTest {
    
    private lateinit var viewModel: ExportViewModel
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
    
    private val testFlashcards = listOf(
        Flashcard(
            flashcardId = 1L,
            userId = 1L,
            categoryId = 1L,
            question = "Question 1",
            answer = "Answer 1",
            difficultyLevel = 1,
            learningStatus = 0
        ),
        Flashcard(
            flashcardId = 2L,
            userId = 1L,
            categoryId = 1L,
            question = "Question 2",
            answer = "Answer 2",
            difficultyLevel = 3,
            learningStatus = 2
        )
    )
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        flashcardDao = mockk()
        categoryDao = mockk()
        authRepository = mockk()
        
        // Mock auth repository
        every { authRepository.currentUser } returns flowOf(testUser)
        
        viewModel = ExportViewModel(flashcardDao, categoryDao, authRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadExportData should load category and flashcards`() = runTest {
        // Given
        coEvery { categoryDao.getCategoryById(testCategory.categoryId) } returns testCategory
        every { flashcardDao.getUserFlashcardsByCategory(testUser.userId, testCategory.categoryId) } returns flowOf(testFlashcards)
        
        // When
        viewModel.loadExportData(testCategory.categoryId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(testCategory, viewModel.category.value)
        assertEquals(testFlashcards, viewModel.flashcards.value)
        assertEquals(testFlashcards.size, viewModel.exportProgress.value.totalCount)
        assertNull(viewModel.error.value)
    }
    
    @Test
    fun `loadExportData should handle non-existent category`() = runTest {
        // Given
        val nonExistentCategoryId = 999L
        coEvery { categoryDao.getCategoryById(nonExistentCategoryId) } returns null
        
        // When
        viewModel.loadExportData(nonExistentCategoryId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals("Category not found", viewModel.error.value)
    }
    
    @Test
    fun `loadExportData should handle unauthenticated user`() = runTest {
        // Given
        every { authRepository.currentUser } returns flowOf(null)
        
        // When
        viewModel.loadExportData(testCategory.categoryId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals("User not authenticated", viewModel.error.value)
    }
    
    @Test
    fun `startExport should handle empty flashcards list`() = runTest {
        // Given - set up empty state
        coEvery { categoryDao.getCategoryById(testCategory.categoryId) } returns testCategory
        every { flashcardDao.getUserFlashcardsByCategory(testUser.userId, testCategory.categoryId) } returns flowOf(emptyList())
        
        viewModel.loadExportData(testCategory.categoryId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.startExport()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val exportProgress = viewModel.exportProgress.value
        assertEquals("No flashcards to export", exportProgress.error)
        assertFalse(exportProgress.isExporting)
    }
    
    @Test
    fun `startExport should handle no category selected`() = runTest {
        // When - start export without loading data
        viewModel.startExport()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val exportProgress = viewModel.exportProgress.value
        assertEquals("No category selected for export", exportProgress.error)
        assertFalse(exportProgress.isExporting)
    }
    
    @Test
    fun `cancelExport should reset export progress`() {
        // Given - set some export progress
        viewModel.startExport()
        
        // When
        viewModel.cancelExport()
        
        // Then
        assertEquals(ExportProgress(), viewModel.exportProgress.value)
    }
    
    @Test
    fun `clearError should reset error state`() = runTest {
        // Given - trigger an error
        viewModel.loadExportData(999L) // Non-existent category
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.error.value)
    }
    
    @Test
    fun `resetExport should reset export progress`() {
        // Given - set some export progress
        viewModel.startExport()
        
        // When
        viewModel.resetExport()
        
        // Then
        assertEquals(ExportProgress(), viewModel.exportProgress.value)
    }
    
    // Note: Testing the actual export functionality would require mocking file operations
    // which is complex due to the file system interactions. In a real scenario, you would
    // extract the file operations into a separate testable service.
}

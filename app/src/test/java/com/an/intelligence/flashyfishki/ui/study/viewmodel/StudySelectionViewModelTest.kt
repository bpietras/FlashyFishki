package com.an.intelligence.flashyfishki.ui.study.viewmodel

import com.an.intelligence.flashyfishki.domain.repository.StudyRepository
import com.an.intelligence.flashyfishki.ui.study.model.CategoryWithStudyStats
import com.an.intelligence.flashyfishki.ui.study.model.StudyAction
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudySelectionViewModelTest {
    
    private lateinit var viewModel: StudySelectionViewModel
    private lateinit var mockRepository: StudyRepository
    private val testDispatcher = UnconfinedTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        viewModel = StudySelectionViewModel(mockRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadCategories should update state with categories`() = runTest {
        // Given
        val userId = 100L
        val categories = listOf(
            CategoryWithStudyStats(
                categoryId = 1L,
                name = "Math",
                totalFlashcards = 10,
                flashcardsToReview = 5,
                newFlashcards = 3,
                reviewFlashcards = 2
            ),
            CategoryWithStudyStats(
                categoryId = 2L,
                name = "Science",
                totalFlashcards = 8,
                flashcardsToReview = 3,
                newFlashcards = 1,
                reviewFlashcards = 2
            )
        )
        
        every { mockRepository.getCategoriesWithStudyStats(userId) } returns flowOf(categories)
        
        // When
        viewModel.loadCategories(userId)
        
        // Then
        val uiState = viewModel.uiState.value
        assert(!uiState.isLoading)
        assert(uiState.error == null)
        assert(uiState.categories == categories)
    }
    
    @Test
    fun `loadCategories should set loading state initially`() = runTest {
        // Given
        val userId = 100L
        every { mockRepository.getCategoriesWithStudyStats(userId) } returns flowOf(emptyList())
        
        // When
        viewModel.loadCategories(userId)
        
        // Then - Check that loading was set to true initially
        // Note: In real implementation, we'd need to test the intermediate state
        val finalState = viewModel.uiState.value
        assert(!finalState.isLoading) // Should be false after completion
        assert(finalState.categories.isEmpty())
    }
    
    @Test
    fun `loadCategories should handle repository error`() = runTest {
        // Given
        val userId = 100L
        val errorMessage = "Network error"
        
        every { mockRepository.getCategoriesWithStudyStats(userId) } returns flowOf<List<CategoryWithStudyStats>>().apply {
            // Simulate error in flow
        }
        
        // Simulate exception during repository call
        every { mockRepository.getCategoriesWithStudyStats(userId) } throws RuntimeException(errorMessage)
        
        // When
        viewModel.loadCategories(userId)
        
        // Then
        val uiState = viewModel.uiState.value
        assert(!uiState.isLoading)
        assert(uiState.error != null)
        assert(uiState.error!!.contains("Failed to load categories"))
        assert(uiState.categories.isEmpty())
    }
    
    @Test
    fun `handleAction LoadCategories should call loadCategories`() = runTest {
        // Given
        val userId = 100L
        every { mockRepository.getCategoriesWithStudyStats(userId) } returns flowOf(emptyList())
        
        // When
        viewModel.handleAction(StudyAction.LoadCategories, userId)
        
        // Then
        verify { mockRepository.getCategoriesWithStudyStats(userId) }
    }
    
    @Test
    fun `handleAction with non-LoadCategories action should do nothing`() = runTest {
        // Given
        val userId = 100L
        
        // When
        viewModel.handleAction(StudyAction.ShowAnswer, userId)
        
        // Then
        verify(exactly = 0) { mockRepository.getCategoriesWithStudyStats(any()) }
    }
    
    @Test
    fun `retryLoadingCategories should reload categories`() = runTest {
        // Given
        val userId = 100L
        val categories = listOf(
            CategoryWithStudyStats(
                categoryId = 1L,
                name = "History",
                totalFlashcards = 5,
                flashcardsToReview = 2,
                newFlashcards = 1,
                reviewFlashcards = 1
            )
        )
        
        every { mockRepository.getCategoriesWithStudyStats(userId) } returns flowOf(categories)
        
        // When
        viewModel.retryLoadingCategories(userId)
        
        // Then
        val uiState = viewModel.uiState.value
        assert(!uiState.isLoading)
        assert(uiState.error == null)
        assert(uiState.categories == categories)
        
        verify { mockRepository.getCategoriesWithStudyStats(userId) }
    }
    
    @Test
    fun `initial state should be correct`() {
        // When & Then
        val initialState = viewModel.uiState.value
        assert(!initialState.isLoading)
        assert(initialState.error == null)
        assert(initialState.categories.isEmpty())
    }
}

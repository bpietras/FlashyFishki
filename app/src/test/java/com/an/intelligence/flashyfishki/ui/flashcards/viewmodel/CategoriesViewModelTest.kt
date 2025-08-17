package com.an.intelligence.flashyfishki.ui.flashcards.viewmodel

import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.domain.model.Category
import com.an.intelligence.flashyfishki.domain.model.User
import com.an.intelligence.flashyfishki.domain.repository.AuthRepository
import com.an.intelligence.flashyfishki.ui.flashcards.model.CategoryFormState
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

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {
    
    private lateinit var viewModel: CategoriesViewModel
    private lateinit var categoryDao: CategoryDao
    private lateinit var authRepository: AuthRepository
    
    private val testDispatcher = StandardTestDispatcher()
    
    private val testUser = User(
        userId = 1L,
        email = "test@example.com",
        passwordHash = "hashedPassword"
    )
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        categoryDao = mockk()
        authRepository = mockk()
        
        // Mock auth repository to return test user
        every { authRepository.currentUser } returns MutableStateFlow(testUser)
        
        // Mock category DAO
        every { categoryDao.getUserCategoriesWithLearningStats(any()) } returns flowOf(emptyList())
        
        viewModel = CategoriesViewModel(categoryDao, authRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadCategories should update loading state`() = runTest {
        // Given
        val categories = listOf(
            CategoryDao.CategoryWithLearningStats(
                categoryId = 1L,
                name = "Test Category",
                flashcardCount = 5,
                newCount = 2,
                firstRepeatCount = 1,
                secondRepeatCount = 1,
                learnedCount = 1
            )
        )
        every { categoryDao.getUserCategoriesWithLearningStats(testUser.userId) } returns flowOf(categories)
        
        // When
        viewModel.loadCategories()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }
    
    @Test
    fun `createCategory should validate name and create category`() = runTest {
        // Given
        val categoryName = "New Category"
        val newCategory = Category(name = categoryName)
        
        coEvery { categoryDao.getCategoryByName(categoryName) } returns null
        coEvery { categoryDao.insertCategory(any()) } returns 1L
        
        // When
        viewModel.createCategory(categoryName)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify { categoryDao.insertCategory(match { it.name == categoryName }) }
        assertEquals(CategoryFormState(), viewModel.categoryFormState.value)
    }
    
    @Test
    fun `createCategory should fail for duplicate name`() = runTest {
        // Given
        val categoryName = "Existing Category"
        val existingCategory = Category(categoryId = 1L, name = categoryName)
        
        coEvery { categoryDao.getCategoryByName(categoryName) } returns existingCategory
        
        // When
        viewModel.createCategory(categoryName)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val formState = viewModel.categoryFormState.value
        assertEquals("Category with this name already exists", formState.nameError)
        assertFalse(formState.isValid)
        coVerify(exactly = 0) { categoryDao.insertCategory(any()) }
    }
    
    @Test
    fun `validateCategoryName should validate empty name`() {
        // When
        val result = viewModel.validateCategoryName("")
        
        // Then
        assertFalse(result)
        val formState = viewModel.categoryFormState.value
        assertEquals("Category name cannot be empty", formState.nameError)
        assertFalse(formState.isValid)
    }
    
    @Test
    fun `validateCategoryName should validate name too long`() {
        // Given
        val longName = "a".repeat(101)
        
        // When
        val result = viewModel.validateCategoryName(longName)
        
        // Then
        assertFalse(result)
        val formState = viewModel.categoryFormState.value
        assertEquals("Category name cannot exceed 100 characters", formState.nameError)
        assertFalse(formState.isValid)
    }
    
    @Test
    fun `validateCategoryName should accept valid name`() {
        // Given
        val validName = "Valid Category Name"
        
        // When
        val result = viewModel.validateCategoryName(validName)
        
        // Then
        assertTrue(result)
        val formState = viewModel.categoryFormState.value
        assertEquals(validName, formState.name)
        assertNull(formState.nameError)
        assertTrue(formState.isValid)
    }
    
    @Test
    fun `clearError should reset error state`() {
        // Given - set error state
        viewModel.loadCategories()
        // Simulate error by making DAO throw exception
        every { categoryDao.getUserCategoriesWithLearningStats(any()) } throws RuntimeException("Test error")
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.error.value)
    }
    
    @Test
    fun `resetCategoryForm should reset form state`() {
        // Given - set some form state
        viewModel.updateCategoryFormField("Some name")
        
        // When
        viewModel.resetCategoryForm()
        
        // Then
        assertEquals(CategoryFormState(), viewModel.categoryFormState.value)
    }
}

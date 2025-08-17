package com.an.intelligence.flashyfishki.ui.flashcards.cache

import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.domain.model.Category
import com.an.intelligence.flashyfishki.domain.model.Flashcard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory cache for flashcard data to improve performance
 */
@Singleton
class FlashcardCache @Inject constructor() {
    
    // Category cache
    private val _categoriesCache = MutableStateFlow<Map<Long, CategoryDao.CategoryWithLearningStats>>(emptyMap())
    val categoriesCache: StateFlow<Map<Long, CategoryDao.CategoryWithLearningStats>> = _categoriesCache.asStateFlow()
    
    // Flashcard cache by category
    private val _flashcardsCache = MutableStateFlow<Map<Long, List<Flashcard>>>(emptyMap())
    val flashcardsCache: StateFlow<Map<Long, List<Flashcard>>> = _flashcardsCache.asStateFlow()
    
    // Single flashcard cache
    private val _flashcardCache = MutableStateFlow<Map<Long, Flashcard>>(emptyMap())
    val flashcardCache: StateFlow<Map<Long, Flashcard>> = _flashcardCache.asStateFlow()
    
    // Category details cache
    private val _categoryDetailsCache = MutableStateFlow<Map<Long, Category>>(emptyMap())
    val categoryDetailsCache: StateFlow<Map<Long, Category>> = _categoryDetailsCache.asStateFlow()
    
    // Cache timestamp for invalidation
    private val _lastUpdated = MutableStateFlow<Map<String, Long>>(emptyMap())
    private val cacheTimeout = 5 * 60 * 1000L // 5 minutes
    
    /**
     * Cache categories with stats
     */
    fun cacheCategories(categories: List<CategoryDao.CategoryWithLearningStats>) {
        val categoryMap = categories.associateBy { it.categoryId }
        _categoriesCache.value = categoryMap
        updateTimestamp("categories")
    }
    
    /**
     * Get cached categories if valid
     */
    fun getCachedCategories(): List<CategoryDao.CategoryWithLearningStats>? {
        return if (isCacheValid("categories")) {
            _categoriesCache.value.values.toList()
        } else {
            null
        }
    }
    
    /**
     * Cache flashcards for a category
     */
    fun cacheFlashcards(categoryId: Long, flashcards: List<Flashcard>) {
        val currentCache = _flashcardsCache.value.toMutableMap()
        currentCache[categoryId] = flashcards
        _flashcardsCache.value = currentCache
        
        // Also cache individual flashcards
        val flashcardMap = _flashcardCache.value.toMutableMap()
        flashcards.forEach { flashcard ->
            flashcardMap[flashcard.flashcardId] = flashcard
        }
        _flashcardCache.value = flashcardMap
        
        updateTimestamp("flashcards_$categoryId")
    }
    
    /**
     * Get cached flashcards for category if valid
     */
    fun getCachedFlashcards(categoryId: Long): List<Flashcard>? {
        return if (isCacheValid("flashcards_$categoryId")) {
            _flashcardsCache.value[categoryId]
        } else {
            null
        }
    }
    
    /**
     * Cache single flashcard
     */
    fun cacheFlashcard(flashcard: Flashcard) {
        val currentCache = _flashcardCache.value.toMutableMap()
        currentCache[flashcard.flashcardId] = flashcard
        _flashcardCache.value = currentCache
        updateTimestamp("flashcard_${flashcard.flashcardId}")
    }
    
    /**
     * Get cached flashcard if valid
     */
    fun getCachedFlashcard(flashcardId: Long): Flashcard? {
        return if (isCacheValid("flashcard_$flashcardId")) {
            _flashcardCache.value[flashcardId]
        } else {
            null
        }
    }
    
    /**
     * Cache category details
     */
    fun cacheCategory(category: Category) {
        val currentCache = _categoryDetailsCache.value.toMutableMap()
        currentCache[category.categoryId] = category
        _categoryDetailsCache.value = currentCache
        updateTimestamp("category_${category.categoryId}")
    }
    
    /**
     * Get cached category if valid
     */
    fun getCachedCategory(categoryId: Long): Category? {
        return if (isCacheValid("category_$categoryId")) {
            _categoryDetailsCache.value[categoryId]
        } else {
            null
        }
    }
    
    /**
     * Invalidate specific cache entry
     */
    fun invalidateCache(key: String) {
        val currentTimestamps = _lastUpdated.value.toMutableMap()
        currentTimestamps.remove(key)
        _lastUpdated.value = currentTimestamps
    }
    
    /**
     * Invalidate all flashcard-related cache
     */
    fun invalidateFlashcardCache() {
        _flashcardsCache.value = emptyMap()
        _flashcardCache.value = emptyMap()
        val currentTimestamps = _lastUpdated.value.toMutableMap()
        currentTimestamps.keys.removeAll { it.startsWith("flashcard") }
        _lastUpdated.value = currentTimestamps
    }
    
    /**
     * Invalidate all category-related cache
     */
    fun invalidateCategoryCache() {
        _categoriesCache.value = emptyMap()
        _categoryDetailsCache.value = emptyMap()
        val currentTimestamps = _lastUpdated.value.toMutableMap()
        currentTimestamps.keys.removeAll { it.startsWith("categor") }
        _lastUpdated.value = currentTimestamps
    }
    
    /**
     * Clear all cache
     */
    fun clearCache() {
        _categoriesCache.value = emptyMap()
        _flashcardsCache.value = emptyMap()
        _flashcardCache.value = emptyMap()
        _categoryDetailsCache.value = emptyMap()
        _lastUpdated.value = emptyMap()
    }
    
    private fun updateTimestamp(key: String) {
        val currentTimestamps = _lastUpdated.value.toMutableMap()
        currentTimestamps[key] = System.currentTimeMillis()
        _lastUpdated.value = currentTimestamps
    }
    
    private fun isCacheValid(key: String): Boolean {
        val timestamp = _lastUpdated.value[key] ?: return false
        return (System.currentTimeMillis() - timestamp) < cacheTimeout
    }
}

/**
 * Cache-aware repository wrapper
 */
@Singleton
class CachedFlashcardRepository @Inject constructor(
    private val cache: FlashcardCache
) {
    
    /**
     * Get categories with cache-first approach
     */
    fun getCategoriesWithCache(): List<CategoryDao.CategoryWithLearningStats>? {
        return cache.getCachedCategories()
    }
    
    /**
     * Store categories in cache
     */
    fun storeCategories(categories: List<CategoryDao.CategoryWithLearningStats>) {
        cache.cacheCategories(categories)
    }
    
    /**
     * Get flashcards with cache-first approach
     */
    fun getFlashcardsWithCache(categoryId: Long): List<Flashcard>? {
        return cache.getCachedFlashcards(categoryId)
    }
    
    /**
     * Store flashcards in cache
     */
    fun storeFlashcards(categoryId: Long, flashcards: List<Flashcard>) {
        cache.cacheFlashcards(categoryId, flashcards)
    }
    
    /**
     * Invalidate cache when data changes
     */
    fun invalidateOnDataChange() {
        cache.invalidateFlashcardCache()
        cache.invalidateCategoryCache()
    }
}

package com.an.intelligence.flashyfishki.domain.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.an.intelligence.flashyfishki.domain.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: Category): Long
    
    @Update
    suspend fun updateCategory(category: Category)
    
    @Delete
    suspend fun deleteCategory(category: Category)
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?
    
    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?
    
    @Query("SELECT EXISTS(SELECT 1 FROM flashcards WHERE category_id = :categoryId LIMIT 1)")
    suspend fun hasCategoryFlashcards(categoryId: Long): Boolean
    
    @Query("""
        SELECT c.*, COUNT(f.id) as flashcardCount
        FROM categories c
        LEFT JOIN flashcards f ON c.id = f.category_id
        GROUP BY c.id
        ORDER BY c.name ASC
    """)
    fun getCategoriesWithFlashcardCount(): Flow<List<CategoryWithCount>>
    
    @Query("""
        SELECT c.*, COUNT(f.id) as flashcardCount
        FROM categories c
        JOIN flashcards f ON c.id = f.category_id
        WHERE f.user_id = :userId
        GROUP BY c.id
        ORDER BY c.name ASC
    """)
    fun getUserCategoriesWithFlashcardCount(userId: Long): Flow<List<CategoryWithCount>>
    
    @Query("""
        SELECT c.*, 
               COUNT(f.id) as flashcardCount,
               SUM(CASE WHEN f.learning_status = 0 THEN 1 ELSE 0 END) as newCount,
               SUM(CASE WHEN f.learning_status = 1 THEN 1 ELSE 0 END) as firstRepeatCount,
               SUM(CASE WHEN f.learning_status = 2 THEN 1 ELSE 0 END) as secondRepeatCount,
               SUM(CASE WHEN f.learning_status = 3 THEN 1 ELSE 0 END) as learnedCount
        FROM categories c
        JOIN flashcards f ON c.id = f.category_id
        WHERE f.user_id = :userId
        GROUP BY c.id
        ORDER BY c.name ASC
    """)
    fun getUserCategoriesWithLearningStats(userId: Long): Flow<List<CategoryWithLearningStats>>
    
    @Transaction
    suspend fun safeDeleteCategory(categoryId: Long): Boolean {
        return if (!hasCategoryFlashcards(categoryId)) {
            val category = getCategoryById(categoryId)
            if (category != null) {
                deleteCategory(category)
                true
            } else {
                false
            }
        } else {
            false
        }
    }
    
    data class CategoryWithCount(
        val categoryId: Long,
        val name: String,
        val flashcardCount: Int
    )
    
    data class CategoryWithLearningStats(
        val categoryId: Long,
        val name: String,
        val flashcardCount: Int,
        val newCount: Int,
        val firstRepeatCount: Int,
        val secondRepeatCount: Int,
        val learnedCount: Int
    )
}

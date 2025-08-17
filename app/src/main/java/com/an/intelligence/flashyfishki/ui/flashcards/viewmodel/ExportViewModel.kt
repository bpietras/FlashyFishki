package com.an.intelligence.flashyfishki.ui.flashcards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.domain.dao.FlashcardDao
import com.an.intelligence.flashyfishki.domain.model.Category
import com.an.intelligence.flashyfishki.domain.model.Flashcard
import com.an.intelligence.flashyfishki.domain.repository.AuthRepository
import com.an.intelligence.flashyfishki.ui.flashcards.model.ExportProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val flashcardDao: FlashcardDao,
    private val categoryDao: CategoryDao,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _exportProgress = MutableStateFlow(ExportProgress())
    val exportProgress: StateFlow<ExportProgress> = _exportProgress.asStateFlow()

    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category.asStateFlow()

    private val _flashcards = MutableStateFlow<List<Flashcard>>(emptyList())
    val flashcards: StateFlow<List<Flashcard>> = _flashcards.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Get current user ID from auth repository
    private val currentUserId: StateFlow<Long?> = authRepository.currentUser
        .map { it?.userId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Load export data for category
     */
    fun loadExportData(categoryId: Long) {
        viewModelScope.launch {
            try {
                _error.value = null

                // Wait for current user to be available
                val userId = authRepository.currentUser.value?.userId
                if (userId == null) {
                    _error.value = "User not authenticated"
                    return@launch
                }

                // Load category
                val category = categoryDao.getCategoryById(categoryId)
                if (category == null) {
                    _error.value = "Category not found"
                    return@launch
                }
                _category.value = category

                // Load flashcards for this category
                flashcardDao.getUserFlashcardsByCategory(userId, categoryId)
                    .catch { e ->
                        _error.value = "Failed to load flashcards: ${e.message}"
                    }
                    .collect { flashcards ->
                        _flashcards.value = flashcards
                        _exportProgress.value = _exportProgress.value.copy(
                            totalCount = flashcards.size
                        )
                    }

            } catch (e: Exception) {
                _error.value = "Failed to load export data: ${e.message}"
            }
        }
    }

    /**
     * Start export process
     */
    fun startExport() {
        viewModelScope.launch {
            try {
                val category = _category.value
                val flashcards = _flashcards.value

                if (category == null) {
                    _error.value = "No category selected for export"
                    return@launch
                }

                if (flashcards.isEmpty()) {
                    _error.value = "No flashcards to export"
                    return@launch
                }

                _exportProgress.value = ExportProgress(
                    isExporting = true,
                    progress = 0f,
                    exportedCount = 0,
                    totalCount = flashcards.size,
                    isComplete = false,
                    error = null
                )

                // Generate markdown content
                val markdownContent = generateMarkdown(category, flashcards)

                // Simulate progress during content generation
                _exportProgress.value = _exportProgress.value.copy(
                    progress = 0.5f,
                    exportedCount = flashcards.size / 2
                )

                delay(500) // Simulate processing time

                // Save to file
                val filePath = saveToFile(category.name, markdownContent)

                _exportProgress.value = _exportProgress.value.copy(
                    isExporting = false,
                    progress = 1f,
                    exportedCount = flashcards.size,
                    isComplete = true,
                    filePath = filePath
                )

            } catch (e: Exception) {
                _exportProgress.value = _exportProgress.value.copy(
                    isExporting = false,
                    error = "Export failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Cancel export process
     */
    fun cancelExport() {
        _exportProgress.value = ExportProgress()
    }

    /**
     * Generate markdown content from flashcards
     */
    private suspend fun generateMarkdown(category: Category, flashcards: List<Flashcard>): String = withContext(Dispatchers.Default) {
        val dateFormatter = SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        val currentDate = Date()

        buildString {
            // Header
            appendLine("# ${category.name}")
            appendLine()
            appendLine("**Exported on:** ${dateFormatter.format(currentDate)}")
            appendLine("**Total flashcards:** ${flashcards.size}")
            appendLine()
            appendLine("---")
            appendLine()

            // Table of contents
            appendLine("## Table of Contents")
            appendLine()
            flashcards.forEachIndexed { index, flashcard ->
                val questionPreview = flashcard.question.take(50) + if (flashcard.question.length > 50) "..." else ""
                appendLine("${index + 1}. [${questionPreview}](#flashcard-${index + 1})")
            }
            appendLine()
            appendLine("---")
            appendLine()

            // Flashcards
            appendLine("## Flashcards")
            appendLine()

            flashcards.forEachIndexed { index, flashcard ->
                val learningStatusText = when (flashcard.learningStatus) {
                    0 -> "New"
                    1 -> "First Repeat"
                    2 -> "Second Repeat"
                    3 -> "Learned"
                    else -> "Unknown"
                }

                appendLine("### Flashcard ${index + 1}")
                appendLine()
                appendLine("**Question:**")
                appendLine(flashcard.question)
                appendLine()
                appendLine("**Answer:**")
                appendLine(flashcard.answer)
                appendLine()
                appendLine("**Metadata:**")
                appendLine("- **Difficulty Level:** ${flashcard.difficultyLevel}/5")
                appendLine("- **Learning Status:** $learningStatusText")
                appendLine("- **Public:** ${if (flashcard.isPublic) "Yes" else "No"}")
                appendLine("- **Created:** ${dateFormatter.format(flashcard.createdAt)}")
                if (flashcard.updatedAt != flashcard.createdAt) {
                    appendLine("- **Updated:** ${dateFormatter.format(flashcard.updatedAt)}")
                }
                if (flashcard.isPublic && flashcard.copiesCount > 0) {
                    appendLine("- **Copied by others:** ${flashcard.copiesCount} times")
                }
                appendLine()
                appendLine("---")
                appendLine()

                // Update progress
                val progress = (index + 1).toFloat() / flashcards.size
                _exportProgress.value = _exportProgress.value.copy(
                    progress = progress * 0.8f, // Reserve 20% for file saving
                    exportedCount = index + 1
                )

                // Small delay to show progress
                delay(50)
            }

            // Footer
            appendLine("## Export Information")
            appendLine()
            appendLine("This file was generated by FlashyFishki app.")
            appendLine("Category: ${category.name}")
            appendLine("Export date: ${dateFormatter.format(currentDate)}")
            appendLine("Total flashcards: ${flashcards.size}")
        }
    }

    /**
     * Save markdown content to file
     */
    private suspend fun saveToFile(categoryName: String, content: String): String = withContext(Dispatchers.IO) {
        try {
            // Create filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val sanitizedCategoryName = categoryName.replace(Regex("[^a-zA-Z0-9\\s]"), "").replace("\\s+".toRegex(), "_")
            val fileName = "flashcards_${sanitizedCategoryName}_$timestamp.md"

            // For Android, we'll save to the app's external files directory
            // In a real implementation, you'd want to use Android's Storage Access Framework
            val externalDir = File("/sdcard/Download") // This is a simplified approach
            if (!externalDir.exists()) {
                externalDir.mkdirs()
            }

            val file = File(externalDir, fileName)
            file.writeText(content)

            file.absolutePath
        } catch (e: Exception) {
            throw Exception("Failed to save file: ${e.message}")
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Reset export state
     */
    fun resetExport() {
        _exportProgress.value = ExportProgress()
    }
}

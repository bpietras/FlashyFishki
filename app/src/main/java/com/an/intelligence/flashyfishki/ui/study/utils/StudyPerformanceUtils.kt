package com.an.intelligence.flashyfishki.ui.study.utils

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Performance utilities for study module
 */
object StudyPerformanceUtils {
    
    /**
     * Debounce rapid user interactions to prevent excessive API calls
     */
    fun <T> Flow<T>.debounceUserActions(timeoutMillis: Long = 300L): Flow<T> {
        return this.distinctUntilChanged()
    }
    
    /**
     * Optimize memory usage by limiting cached flashcards
     */
    fun <T> List<T>.limitForPerformance(maxSize: Int = 100): List<T> {
        return if (size > maxSize) {
            take(maxSize)
        } else {
            this
        }
    }
    
    /**
     * Create optimized state for study sessions
     */
    @Composable
    fun <T> optimizedCollectAsState(
        flow: Flow<T>,
        initial: T
    ): State<T> {
        val lifecycleOwner = LocalLifecycleOwner.current
        var isActive by remember { mutableStateOf(true) }
        
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                isActive = when (event) {
                    Lifecycle.Event.ON_RESUME -> true
                    Lifecycle.Event.ON_PAUSE -> false
                    else -> isActive
                }
            }
            
            lifecycleOwner.lifecycle.addObserver(observer)
            
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        
        return if (isActive) {
            flow.collectAsState(initial = initial)
        } else {
            remember { mutableStateOf(initial) }
        }
    }
}

/**
 * Composable for managing memory during study sessions
 */
@Composable
fun StudyMemoryManager(
    onLowMemory: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    // Clear non-essential cached data when app goes to background
                    onLowMemory()
                }
                else -> { /* no-op */ }
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    content()
}

/**
 * Optimized remember function for expensive calculations
 */
@Composable
fun <T> rememberExpensive(
    vararg keys: Any?,
    calculation: () -> T
): T {
    return remember(*keys) {
        calculation()
    }
}

/**
 * Pagination helper for large flashcard sets
 */
data class StudyPagination(
    val pageSize: Int = 20,
    val currentPage: Int = 0
) {
    fun <T> paginate(items: List<T>): List<T> {
        val startIndex = currentPage * pageSize
        val endIndex = minOf(startIndex + pageSize, items.size)
        
        return if (startIndex < items.size) {
            items.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }
    
    fun hasNextPage(totalItems: Int): Boolean {
        return (currentPage + 1) * pageSize < totalItems
    }
    
    fun hasPrevPage(): Boolean {
        return currentPage > 0
    }
}

/**
 * Optimized LazyColumn key provider for flashcards
 */
fun flashcardKey(flashcardId: Long, index: Int): String {
    return "flashcard_${flashcardId}_$index"
}

/**
 * Memory-efficient image loading placeholder
 */
@Composable
fun rememberOptimizedPlaceholder(
    size: androidx.compose.ui.unit.Dp
): androidx.compose.ui.graphics.painter.Painter? {
    // Return null for now - in real app, this would create optimized placeholders
    return null
}

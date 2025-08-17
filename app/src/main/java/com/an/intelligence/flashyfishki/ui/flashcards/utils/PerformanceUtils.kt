package com.an.intelligence.flashyfishki.ui.flashcards.utils

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

/**
 * Performance utilities for optimizing Compose UI
 */

/**
 * Remember value with lifecycle awareness to prevent memory leaks
 */
@Composable
fun <T> rememberLifecycleAware(
    key: Any? = null,
    calculation: @DisallowComposableCalls () -> T
): T {
    val lifecycleOwner = LocalLifecycleOwner.current
    
    return remember(key, lifecycleOwner) {
        calculation()
    }
}

/**
 * Collect flow only when lifecycle is at least STARTED
 */
@Composable
fun <T> Flow<T>.collectAsStateWithLifecycleOptimized(
    initial: T,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): State<T> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember { mutableStateOf(initial) }
    
    DisposableEffect(this, lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        var isActive = lifecycle.currentState.isAtLeast(minActiveState)
        
        val observer = LifecycleEventObserver { _, event ->
            isActive = when (event) {
                Lifecycle.Event.ON_START -> true
                Lifecycle.Event.ON_STOP -> false
                else -> isActive
            }
        }
        
        lifecycle.addObserver(observer)
        
        val job = kotlinx.coroutines.CoroutineScope(
            kotlinx.coroutines.Dispatchers.Main.immediate
        ).launch {
            if (isActive) {
                collectLatest { value ->
                    if (isActive) {
                        state.value = value
                    }
                }
            }
        }
        
        onDispose {
            lifecycle.removeObserver(observer)
            job.cancel()
        }
    }
    
    return state
}

/**
 * Stable wrapper for lists to prevent unnecessary recompositions
 */
@Stable
class StableList<T>(
    private val list: List<T>
) : List<T> by list {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StableList<*>) return false
        return list == other.list
    }
    
    override fun hashCode(): Int = list.hashCode()
}

/**
 * Convert list to stable wrapper
 */
fun <T> List<T>.toStableList(): StableList<T> = StableList(this)

/**
 * Remember stable list to prevent recomposition
 */
@Composable
fun <T> rememberStableList(list: List<T>): StableList<T> {
    return remember(list) { list.toStableList() }
}

/**
 * Optimized string formatter for repeated use
 */
object StringFormatters {
    private val dateFormatter by lazy {
        java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    }
    
    private val shortDateFormatter by lazy {
        java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
    }
    
    private val timeFormatter by lazy {
        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    }
    
    fun formatDate(date: java.util.Date): String = dateFormatter.format(date)
    fun formatShortDate(date: java.util.Date): String = shortDateFormatter.format(date)
    fun formatTime(date: java.util.Date): String = timeFormatter.format(date)
}

/**
 * Debounced state for expensive operations
 */
@Composable
fun <T> rememberDebouncedState(
    value: T,
    delayMs: Long = 300L
): State<T> {
    val debouncedValue = remember { mutableStateOf(value) }
    
    LaunchedEffect(value) {
        kotlinx.coroutines.delay(delayMs)
        debouncedValue.value = value
    }
    
    return debouncedValue
}

/**
 * Memoized computation with key
 */
@Composable
fun <T, R> rememberMemoized(
    key: T,
    computation: (T) -> R
): R {
    return remember(key) { computation(key) }
}

/**
 * Lazy computation that only runs when needed
 */
@Composable
fun <T> rememberLazyComputation(
    trigger: Boolean,
    computation: () -> T
): T? {
    var result by remember { mutableStateOf<T?>(null) }
    
    LaunchedEffect(trigger) {
        if (trigger && result == null) {
            result = computation()
        }
    }
    
    return result
}

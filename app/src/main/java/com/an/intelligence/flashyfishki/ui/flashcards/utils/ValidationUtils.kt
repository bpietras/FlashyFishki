package com.an.intelligence.flashyfishki.ui.flashcards.utils

import androidx.compose.runtime.*
import kotlinx.coroutines.delay

/**
 * Debounced validation hook for form fields
 */
@Composable
fun rememberDebouncedValidation(
    value: String,
    delayMs: Long = 300L,
    validator: (String) -> String?
): String? {
    var validationError by remember { mutableStateOf<String?>(null) }
    var isValidating by remember { mutableStateOf(false) }
    
    LaunchedEffect(value) {
        isValidating = true
        delay(delayMs)
        validationError = validator(value)
        isValidating = false
    }
    
    return if (isValidating) null else validationError
}

/**
 * Optimized state for form fields with debounced validation
 */
@Stable
class DebouncedFormField(
    initialValue: String = "",
    private val validator: (String) -> String? = { null }
) {
    var value by mutableStateOf(initialValue)
        private set
    
    var error by mutableStateOf<String?>(null)
    
    var isValidating by mutableStateOf(false)
    
    fun updateValue(newValue: String) {
        value = newValue
        // Validation will be triggered by LaunchedEffect in composable
    }
    
    fun validate() {
        error = validator(value)
    }
    

    

}

/**
 * Remember a debounced form field
 */
@Composable
fun rememberDebouncedFormField(
    initialValue: String = "",
    delayMs: Long = 300L,
    validator: (String) -> String?
): DebouncedFormField {
    val field = remember { DebouncedFormField(initialValue, validator) }
    
    LaunchedEffect(field.value) {
        if (field.value.isNotEmpty()) {
            field.isValidating = true
            delay(delayMs)
            field.validate()
            field.isValidating = false
        } else {
            field.error = null
        }
    }
    
    return field
}

/**
 * Validation functions for flashcard forms
 */
object FlashcardValidators {
    fun questionValidator(question: String): String? {
        return when {
            question.isBlank() -> "Question cannot be empty"
            question.length > 500 -> "Question cannot exceed 500 characters"
            else -> null
        }
    }
    
    fun answerValidator(answer: String): String? {
        return when {
            answer.isBlank() -> "Answer cannot be empty"
            answer.length > 1000 -> "Answer cannot exceed 1000 characters"
            else -> null
        }
    }
    
    fun categoryNameValidator(name: String): String? {
        return when {
            name.isBlank() -> "Category name cannot be empty"
            name.length > 100 -> "Category name cannot exceed 100 characters"
            else -> null
        }
    }
}

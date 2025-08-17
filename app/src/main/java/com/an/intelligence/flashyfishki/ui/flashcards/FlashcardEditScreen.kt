package com.an.intelligence.flashyfishki.ui.flashcards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.an.intelligence.flashyfishki.domain.model.User
import com.an.intelligence.flashyfishki.ui.flashcards.components.FlashcardForm
import com.an.intelligence.flashyfishki.ui.flashcards.viewmodel.FlashcardEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardEditScreen(
    flashcardId: Long?,
    categoryId: Long?,
    currentUser: User,
    onSaveSuccess: () -> Unit,
    onCancel: () -> Unit,
    viewModel: FlashcardEditViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()

    // Initialize screen based on mode
    LaunchedEffect(flashcardId, categoryId) {
        if (flashcardId != null) {
            viewModel.initializeForEditFlashcard(flashcardId)
        } else {
            viewModel.initializeForNewFlashcard(categoryId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isEditMode) "Edit Flashcard" else "New Flashcard",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveFlashcard(onSaveSuccess)
                        },
                        enabled = formState.isValid && !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Show loading state
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Show error if any
                    error?.let { errorMessage ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Error",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(
                                    onClick = { viewModel.clearError() }
                                ) {
                                    Text("Dismiss")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Flashcard Form
                    FlashcardForm(
                        formState = formState,
                        categories = categories,
                        onQuestionChange = { question ->
                            viewModel.updateFormField(question = question)
                        },
                        onAnswerChange = { answer ->
                            viewModel.updateFormField(answer = answer)
                        },
                        onCategoryChange = { categoryId ->
                            viewModel.updateFormField(categoryId = categoryId)
                        },
                        onDifficultyChange = { difficulty ->
                            viewModel.updateFormField(difficultyLevel = difficulty)
                        },
                        onPublicChange = { isPublic ->
                            viewModel.updateFormField(isPublic = isPublic)
                        },
                        questionRemainingChars = viewModel.getQuestionRemainingChars(),
                        answerRemainingChars = viewModel.getAnswerRemainingChars()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                            enabled = !isSaving
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                viewModel.saveFlashcard(onSaveSuccess)
                            },
                            modifier = Modifier.weight(1f),
                            enabled = formState.isValid && !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (isEditMode) "Update" else "Create")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

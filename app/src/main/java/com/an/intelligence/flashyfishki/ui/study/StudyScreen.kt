package com.an.intelligence.flashyfishki.ui.study

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.an.intelligence.flashyfishki.domain.model.User
import com.an.intelligence.flashyfishki.ui.study.components.FlashcardStudyCard
import com.an.intelligence.flashyfishki.ui.study.components.StudyControlsSection
import com.an.intelligence.flashyfishki.ui.study.model.StudyAction
import com.an.intelligence.flashyfishki.ui.study.model.StudySessionStats
import com.an.intelligence.flashyfishki.ui.study.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    categoryId: Long,
    currentUser: User,
    onNavigateToSummary: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StudyViewModel = hiltViewModel()
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val completedSessionStats by viewModel.completedSessionStats.collectAsStateWithLifecycle()
    
    // Start study session when screen is first displayed
    LaunchedEffect(categoryId, currentUser.userId) {
        viewModel.startStudySession(categoryId, currentUser.userId)
    }
    
    // Navigate to summary when session ends
    LaunchedEffect(completedSessionStats) {
        completedSessionStats?.let {
            onNavigateToSummary(categoryId)
            viewModel.clearCompletedSessionStats()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Study Session",
                            fontWeight = FontWeight.Bold
                        )
                        sessionState?.let { state ->
                            Text(
                                text = "Card ${state.currentIndex + 1} of ${state.flashcards.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    LoadingState()
                }
                
                error != null -> {
                    ErrorState(
                        error = error!!,
                        onDismiss = { viewModel.clearError() },
                        onNavigateBack = onNavigateBack
                    )
                }
                
                sessionState != null -> {
                    StudySessionContent(
                        sessionState = sessionState!!,
                        onShowAnswer = { viewModel.handleAction(StudyAction.ShowAnswer) },
                        onCorrectAnswer = { viewModel.handleAction(StudyAction.CorrectAnswer) },
                        onIncorrectAnswer = { viewModel.handleAction(StudyAction.IncorrectAnswer) },
                        onEndSession = { viewModel.handleAction(StudyAction.EndSession) },
                        canEvaluate = viewModel.canEvaluateAnswer(),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Preparing study session...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onDismiss: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Study Session Error",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dismiss")
                }
                
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Go Back")
                }
            }
        }
    }
}

@Composable
private fun StudySessionContent(
    sessionState: com.an.intelligence.flashyfishki.ui.study.model.StudySessionState,
    onShowAnswer: () -> Unit,
    onCorrectAnswer: () -> Unit,
    onIncorrectAnswer: () -> Unit,
    onEndSession: () -> Unit,
    canEvaluate: Boolean,
    modifier: Modifier = Modifier
) {
    val currentFlashcard = sessionState.flashcards.getOrNull(sessionState.currentIndex)
    
    if (currentFlashcard == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No more flashcards in this session",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        return
    }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Progress indicator
        StudyProgressIndicator(
            progress = (sessionState.currentIndex + 1).toFloat() / sessionState.flashcards.size.toFloat(),
            currentCard = sessionState.currentIndex + 1,
            totalCards = sessionState.flashcards.size
        )
        
        // Flashcard
        FlashcardStudyCard(
            flashcard = currentFlashcard,
            isAnswerVisible = sessionState.isAnswerVisible,
            onShowAnswer = onShowAnswer,
            modifier = Modifier.weight(1f)
        )
        
        // Controls
        StudyControlsSection(
            canEvaluate = canEvaluate,
            onCorrectAnswer = onCorrectAnswer,
            onIncorrectAnswer = onIncorrectAnswer,
            onEndSession = onEndSession
        )
    }
}

@Composable
private fun StudyProgressIndicator(
    progress: Float,
    currentCard: Int,
    totalCards: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$currentCard / $totalCards",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

package com.an.intelligence.flashyfishki.ui.study

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.an.intelligence.flashyfishki.ui.study.components.StudyStatsCard
import com.an.intelligence.flashyfishki.ui.study.model.StudySessionStats
import com.an.intelligence.flashyfishki.ui.study.viewmodel.StudySummaryViewModel
import com.an.intelligence.flashyfishki.ui.study.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySummaryScreen(
    categoryId: Long,
    onReturnToStudy: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    summaryViewModel: StudySummaryViewModel = hiltViewModel(),
    studyViewModel: StudyViewModel = hiltViewModel()
) {
    val isLoading by summaryViewModel.isLoading.collectAsStateWithLifecycle()
    val error by summaryViewModel.error.collectAsStateWithLifecycle()
    val sessionStats by summaryViewModel.sessionStats.collectAsStateWithLifecycle()
    val completedSessionStats by studyViewModel.completedSessionStats.collectAsStateWithLifecycle()
    
    // Initialize ViewModel with session stats from StudyViewModel
    LaunchedEffect(completedSessionStats) {
        completedSessionStats?.let { stats ->
            summaryViewModel.initializeWithSessionStats(stats)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Session Complete!",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
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
            val currentSessionStats = sessionStats
            if (currentSessionStats == null) {
                EmptySessionState(onFinish = onFinish)
            } else if (currentSessionStats.completedCards <= 0) {
                EmptySessionState(onFinish = onFinish)
            } else {
                StudySummaryContent(
                    sessionStats = currentSessionStats,
                    onReturnToStudy = onReturnToStudy,
                    onFinish = onFinish,
                    isLoading = isLoading,
                    error = error,
                    onClearError = { summaryViewModel.clearError() },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptySessionState(
    onFinish: () -> Unit,
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
                text = "No cards reviewed",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "The study session ended without reviewing any cards.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Return to Home")
            }
        }
    }
}

@Composable
private fun StudySummaryContent(
    sessionStats: StudySessionStats,
    onReturnToStudy: () -> Unit,
    onFinish: () -> Unit,
    isLoading: Boolean,
    error: String?,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            // Congratulations header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎉 Well Done!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = "You've completed your study session",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        item {
            // Session statistics
            StudyStatsCard(stats = sessionStats)
        }
        
        item {
            // Performance insights
            PerformanceInsights(sessionStats = sessionStats)
        }
        
        item {
            // Error display
            error?.let { errorMessage ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Error saving session",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = onClearError,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
        
        item {
            // Action buttons
            StudyActionButtons(
                onReturnToStudy = onReturnToStudy,
                onFinish = onFinish,
                isLoading = isLoading
            )
        }
    }
}

@Composable
private fun PerformanceInsights(
    sessionStats: StudySessionStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Performance Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            val insights = mutableListOf<String>()
            
            when {
                sessionStats.accuracyPercentage >= 90 -> {
                    insights.add("🌟 Excellent! You have mastered this material.")
                }
                sessionStats.accuracyPercentage >= 75 -> {
                    insights.add("✅ Great work! You're doing very well.")
                }
                sessionStats.accuracyPercentage >= 50 -> {
                    insights.add("📚 Good effort! Consider reviewing these cards again.")
                }
                else -> {
                    insights.add("💪 Keep practicing! These cards will come back for review.")
                }
            }
            
            if (sessionStats.sessionDurationMinutes < 5) {
                insights.add("⚡ Quick session! Consider longer study periods for better retention.")
            }
            
            if (sessionStats.correctAnswers == sessionStats.completedCards) {
                insights.add("🎯 Perfect score! All answers were correct.")
            }
            
            insights.forEach { insight ->
                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StudyActionButtons(
    onReturnToStudy: () -> Unit,
    onFinish: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Return to study button
        Button(
            onClick = onReturnToStudy,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Study More Categories",
                style = MaterialTheme.typography.labelLarge
            )
        }
        
        // Finish button
        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Return to Home",
                style = MaterialTheme.typography.labelLarge
            )
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Text(
                        text = "Saving session...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

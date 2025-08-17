package com.an.intelligence.flashyfishki.ui.flashcards.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.an.intelligence.flashyfishki.domain.model.Flashcard
import com.an.intelligence.flashyfishki.ui.flashcards.model.LearningStatus
import com.an.intelligence.flashyfishki.ui.flashcards.theme.getDifficultyColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AnimatedFlashcardCard(
    flashcard: Flashcard,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    
    // Animations
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "cardScale"
    )
    
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 2f else 4f,
        animationSpec = tween(100),
        label = "cardElevation"
    )
    
    val cardColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200),
        label = "cardColor"
    )
    
    val learningStatus = LearningStatus.fromValue(flashcard.learningStatus)
    val difficultyColor = getDifficultyColor(flashcard.difficultyLevel)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with animated status indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Learning status indicator with pulse animation
                AnimatedStatusBadge(
                    text = learningStatus.displayName,
                    color = learningStatus.color
                )
                
                // Difficulty and public indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Difficulty indicator
                    Box(
                        modifier = Modifier
                            .background(
                                color = difficultyColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "★".repeat(flashcard.difficultyLevel),
                            style = MaterialTheme.typography.labelSmall,
                            color = difficultyColor
                        )
                    }
                    
                    if (flashcard.isPublic) {
                        PublicBadge()
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Question with fade-in animation
            Text(
                text = flashcard.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.graphicsLayer {
                    alpha = if (isPressed) 0.7f else 1f
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Answer preview
            Text(
                text = flashcard.answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.graphicsLayer {
                    alpha = if (isPressed) 0.7f else 1f
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer with creation date
            Text(
                text = "Created: ${formatDate(flashcard.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // Handle press state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun AnimatedStatusBadge(
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    var isPulsing by remember { mutableStateOf(false) }
    
    val badgeScale by animateFloatAsState(
        targetValue = if (isPulsing) 1.1f else 1f,
        animationSpec = tween(300),
        label = "badgeScale"
    )
    
    Box(
        modifier = Modifier
            .scale(badgeScale)
            .background(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
    
    LaunchedEffect(Unit) {
        isPulsing = true
        kotlinx.coroutines.delay(300)
        isPulsing = false
    }
}

@Composable
private fun PublicBadge() {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Public",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDate(date: java.util.Date): String {
    val formatter = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
    return formatter.format(date)
}

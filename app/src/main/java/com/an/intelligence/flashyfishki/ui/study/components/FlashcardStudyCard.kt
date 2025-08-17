package com.an.intelligence.flashyfishki.ui.study.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.an.intelligence.flashyfishki.domain.model.Flashcard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardStudyCard(
    flashcard: Flashcard,
    isAnswerVisible: Boolean,
    onShowAnswer: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Validation
    if (flashcard.question.isBlank() || flashcard.answer.isBlank()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = "Invalid flashcard data",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
        return
    }
    
    // Animation for card flip
    val rotationY by animateFloatAsState(
        targetValue = if (isAnswerVisible) 180f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "cardFlip"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 12f * density
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rotationY <= 90f) {
                // Front side - Question
                FlashcardFront(
                    question = flashcard.question,
                    onShowAnswer = onShowAnswer,
                    canShowAnswer = !isAnswerVisible
                )
            } else {
                // Back side - Answer (flipped)
                FlashcardBack(
                    answer = flashcard.answer,
                    modifier = Modifier.graphicsLayer { 
                        this.rotationY = 180f 
                    }
                )
            }
        }
    }
}

@Composable
private fun FlashcardFront(
    question: String,
    onShowAnswer: () -> Unit,
    canShowAnswer: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Question label
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "QUESTION",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        // Question text
        Text(
            text = question,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f).wrapContentHeight(Alignment.CenterVertically)
        )
        
        // Show answer button
        Button(
            onClick = onShowAnswer,
            enabled = canShowAnswer,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Show Answer",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun FlashcardBack(
    answer: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Answer label
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = "ANSWER",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        
        // Answer text
        Text(
            text = answer,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight(Alignment.CenterVertically)
        )
        
        // Info text
        Text(
            text = "Rate your answer below",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

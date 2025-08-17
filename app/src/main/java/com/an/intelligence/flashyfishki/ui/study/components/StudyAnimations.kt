package com.an.intelligence.flashyfishki.ui.study.components

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp

/**
 * Animated card transition for flashcard changes
 */
@Composable
fun AnimatedFlashcardTransition(
    visible: Boolean,
    onAnimationComplete: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    LaunchedEffect(visible) {
        if (visible) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            animationSpec = tween(300)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeOut(
            animationSpec = tween(300)
        ),
        content = { content() }
    )
    
    LaunchedEffect(visible) {
        if (!visible) {
            kotlinx.coroutines.delay(600) // Wait for animation to complete
            onAnimationComplete()
        }
    }
}

/**
 * Pulsing animation for correct/incorrect feedback
 */
@Composable
fun AnimatedFeedbackButton(
    isCorrect: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "buttonScale"
    )
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            if (isCorrect) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            kotlinx.coroutines.delay(150)
            isPressed = false
            onClick()
        }
    }
    
    Box(
        modifier = modifier
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { isPressed = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

/**
 * Loading skeleton for study cards
 */
@Composable
fun StudyCardSkeleton(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header skeleton
            Surface(
                modifier = Modifier
                    .width(120.dp)
                    .height(32.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {}
            
            // Content skeleton
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    ) {}
                }
            }
            
            // Button skeleton
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {}
        }
    }
}

/**
 * Progress animation with celebration effect
 */
@Composable
fun AnimatedProgressIndicator(
    progress: Float,
    isComplete: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "progress"
    )
    
    val haptic = LocalHapticFeedback.current
    
    LaunchedEffect(isComplete) {
        if (isComplete) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    LinearProgressIndicator(
        progress = animatedProgress,
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp),
        color = if (isComplete) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.primary
        },
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

package com.an.intelligence.flashyfishki.ui.flashcards.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.ui.flashcards.model.LearningStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedCategoryCard(
    categoryWithStats: CategoryDao.CategoryWithLearningStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    
    // Animation for card entrance
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 50f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "slideOffset"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "alpha"
    )
    
    // Press animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pressScale"
    )
    
    // Progress calculation
    val totalCards = categoryWithStats.flashcardCount
    val learnedCards = categoryWithStats.learnedCount
    val progress = if (totalCards > 0) learnedCards.toFloat() / totalCards else 0f
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) progress else 0f,
        animationSpec = tween(1000, delayMillis = 300),
        label = "progress"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                onClick = {
                    isPressed = true
                    onClick()
                }
            )
            .offset(y = slideOffset.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // Header with category name and progress indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = categoryWithStats.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${categoryWithStats.flashcardCount} cards",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Progress circle
                AnimatedProgressIndicator(
                    progress = animatedProgress,
                    totalCards = totalCards,
                    learnedCards = learnedCards
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Learning status indicators with animations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AnimatedLearningStatusIndicator(
                    status = LearningStatus.NEW,
                    count = categoryWithStats.newCount,
                    isVisible = isVisible
                )
                AnimatedLearningStatusIndicator(
                    status = LearningStatus.FIRST_REPEAT,
                    count = categoryWithStats.firstRepeatCount,
                    isVisible = isVisible
                )
                AnimatedLearningStatusIndicator(
                    status = LearningStatus.SECOND_REPEAT,
                    count = categoryWithStats.secondRepeatCount,
                    isVisible = isVisible
                )
                AnimatedLearningStatusIndicator(
                    status = LearningStatus.LEARNED,
                    count = categoryWithStats.learnedCount,
                    isVisible = isVisible
                )
            }
        }
    }
    
    // Trigger entrance animation
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        isVisible = true
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
private fun AnimatedProgressIndicator(
    progress: Float,
    totalCards: Int,
    learnedCards: Int
) {
    val progressColor by animateColorAsState(
        targetValue = when {
            progress >= 0.8f -> Color(0xFF10B981) // Green
            progress >= 0.5f -> Color(0xFFF59E0B) // Amber
            progress >= 0.2f -> Color(0xFF3B82F6) // Blue
            else -> Color(0xFF6B7280) // Gray
        },
        animationSpec = tween(500),
        label = "progressColor"
    )
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(64.dp)
    ) {
        // Background circle
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
        )
        
        // Progress circle
        Canvas(modifier = Modifier.size(56.dp)) { 
            val strokeWidth = 6.dp.toPx()
            drawCircle(
                color = progressColor,
                radius = (size.minDimension - strokeWidth) / 2,
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
            )
        }
        
        // Progress text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
            if (totalCards > 0) {
                Text(
                    text = "$learnedCards/$totalCards",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AnimatedLearningStatusIndicator(
    status: LearningStatus,
    count: Int,
    isVisible: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isVisible && count > 0) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "statusScale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (count > 0) 1f else 0.6f,
        animationSpec = tween(400),
        label = "statusAlpha"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    brush = if (count > 0) {
                        Brush.radialGradient(
                            colors = listOf(
                                status.color.copy(alpha = 0.3f),
                                status.color.copy(alpha = 0.1f)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Gray.copy(alpha = 0.1f),
                                Color.Gray.copy(alpha = 0.05f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (count > 0) status.color else Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = status.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
private fun Canvas(
    modifier: Modifier,
    onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
) {
    androidx.compose.foundation.Canvas(modifier = modifier, onDraw = onDraw)
}

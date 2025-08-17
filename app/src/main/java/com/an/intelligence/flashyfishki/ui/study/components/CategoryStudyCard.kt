package com.an.intelligence.flashyfishki.ui.study.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.an.intelligence.flashyfishki.ui.study.model.CategoryWithStudyStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryStudyCard(
    category: CategoryWithStudyStats,
    onStartStudy: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category name
            Text(
                text = category.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Statistics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total cards info
                Column {
                    Text(
                        text = "Total cards",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = category.totalFlashcards.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Cards to review info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "To review",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = category.flashcardsToReview.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (category.flashcardsToReview > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            // Breakdown of review types if there are cards to review
            if (category.flashcardsToReview > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (category.newFlashcards > 0) {
                        StudyStatChip(
                            label = "New",
                            count = category.newFlashcards,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (category.reviewFlashcards > 0) {
                        StudyStatChip(
                            label = "Review",
                            count = category.reviewFlashcards,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
            
            // Start study button
            Button(
                onClick = { 
                    if (category.categoryId > 0 && category.flashcardsToReview > 0) {
                        onStartStudy(category.categoryId)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = category.flashcardsToReview > 0,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (category.flashcardsToReview > 0) {
                        "Start Learning"
                    } else {
                        "No cards to review"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun StudyStatChip(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

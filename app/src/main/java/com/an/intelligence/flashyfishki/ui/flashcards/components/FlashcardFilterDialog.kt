package com.an.intelligence.flashyfishki.ui.flashcards.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.an.intelligence.flashyfishki.ui.flashcards.model.FlashcardFilter
import com.an.intelligence.flashyfishki.ui.flashcards.model.LearningStatus
import com.an.intelligence.flashyfishki.ui.flashcards.model.SortBy

@Composable
fun FlashcardFilterDialog(
    currentFilter: FlashcardFilter,
    onFilterChange: (FlashcardFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLearningStatus by remember { mutableStateOf(currentFilter.learningStatus) }
    var selectedDifficulty by remember { mutableStateOf(currentFilter.difficultyLevel) }
    var selectedSortBy by remember { mutableStateOf(currentFilter.sortBy) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Filter Flashcards",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Learning Status Filter
                Text(
                    text = "Learning Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedLearningStatus == null,
                        onClick = { selectedLearningStatus = null },
                        label = { Text("All") }
                    )
                    LearningStatus.values().forEach { status ->
                        FilterChip(
                            selected = selectedLearningStatus == status.value,
                            onClick = { selectedLearningStatus = status.value },
                            label = { Text(status.displayName) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Difficulty Level Filter
                Text(
                    text = "Difficulty Level",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedDifficulty == null,
                        onClick = { selectedDifficulty = null },
                        label = { Text("All") }
                    )
                    (1..5).forEach { level ->
                        FilterChip(
                            selected = selectedDifficulty == level,
                            onClick = { selectedDifficulty = level },
                            label = { Text(level.toString()) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sort By
                Text(
                    text = "Sort By",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Column {
                    val sortOptions = listOf(
                        SortBy.CREATED_DATE_DESC to "Newest First",
                        SortBy.CREATED_DATE_ASC to "Oldest First",
                        SortBy.LEARNING_STATUS to "Learning Status",
                        SortBy.DIFFICULTY_LEVEL to "Difficulty Level"
                    )
                    
                    sortOptions.forEach { (sortBy, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedSortBy == sortBy,
                                    onClick = { selectedSortBy = sortBy }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSortBy == sortBy,
                                onClick = { selectedSortBy = sortBy }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onFilterChange(
                        FlashcardFilter(
                            learningStatus = selectedLearningStatus,
                            difficultyLevel = selectedDifficulty,
                            sortBy = selectedSortBy
                        )
                    )
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

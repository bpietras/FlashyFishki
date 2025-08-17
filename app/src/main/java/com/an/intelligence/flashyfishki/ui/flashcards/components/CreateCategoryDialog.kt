package com.an.intelligence.flashyfishki.ui.flashcards.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.an.intelligence.flashyfishki.ui.flashcards.model.CategoryFormState

@Composable
fun CreateCategoryDialog(
    categoryFormState: CategoryFormState,
    onNameChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create New Category",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = categoryFormState.name,
                    onValueChange = onNameChange,
                    label = { Text("Category Name") },
                    placeholder = { Text("Enter category name") },
                    isError = categoryFormState.nameError != null,
                    supportingText = categoryFormState.nameError?.let { error ->
                        { Text(error, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Character counter
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${categoryFormState.name.length}/100",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (categoryFormState.name.length > 100) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(categoryFormState.name) },
                enabled = categoryFormState.isValid
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

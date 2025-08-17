package com.an.intelligence.flashyfishki.ui.study.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class ToastType {
    SUCCESS, ERROR, INFO
}

data class ToastMessage(
    val message: String,
    val type: ToastType,
    val duration: Long = 3000L
)

@Composable
fun StudyToastManager(
    toastMessage: ToastMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(toastMessage.duration)
            onDismiss()
        }
    }
    
    toastMessage?.let { toast ->
        StudyToast(
            message = toast.message,
            type = toast.type,
            onDismiss = onDismiss,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudyToast(
    message: String,
    type: ToastType,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector
    val containerColor: androidx.compose.ui.graphics.Color
    val contentColor: androidx.compose.ui.graphics.Color
    
    when (type) {
        ToastType.SUCCESS -> {
            icon = Icons.Default.CheckCircle
            containerColor = MaterialTheme.colorScheme.primaryContainer
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        }
        ToastType.ERROR -> {
            icon = Icons.Default.Error
            containerColor = MaterialTheme.colorScheme.errorContainer
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        }
        ToastType.INFO -> {
            icon = Icons.Default.Info
            containerColor = MaterialTheme.colorScheme.secondaryContainer
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = onDismiss
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Composable for managing study-specific toast messages
 */
@Composable
fun rememberStudyToastState(): StudyToastState {
    return remember { StudyToastState() }
}

class StudyToastState {
    private val _currentToast = mutableStateOf<ToastMessage?>(null)
    val currentToast: State<ToastMessage?> = _currentToast
    
    fun showSuccess(message: String, duration: Long = 2000L) {
        _currentToast.value = ToastMessage(message, ToastType.SUCCESS, duration)
    }
    
    fun showError(message: String, duration: Long = 4000L) {
        _currentToast.value = ToastMessage(message, ToastType.ERROR, duration)
    }
    
    fun showInfo(message: String, duration: Long = 3000L) {
        _currentToast.value = ToastMessage(message, ToastType.INFO, duration)
    }
    
    fun dismiss() {
        _currentToast.value = null
    }
}

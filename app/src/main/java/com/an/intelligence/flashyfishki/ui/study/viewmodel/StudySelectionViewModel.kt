package com.an.intelligence.flashyfishki.ui.study.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.intelligence.flashyfishki.domain.repository.StudyRepository
import com.an.intelligence.flashyfishki.ui.study.model.StudyAction
import com.an.intelligence.flashyfishki.ui.study.model.StudyUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudySelectionViewModel @Inject constructor(
    private val studyRepository: StudyRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()
    
    fun handleAction(action: StudyAction, userId: Long) {
        when (action) {
            is StudyAction.LoadCategories -> loadCategories(userId)
            else -> {
                // Other actions are handled by different ViewModels
            }
        }
    }
    
    fun loadCategories(userId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                studyRepository.getCategoriesWithStudyStats(userId)
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load categories: ${exception.message}"
                        )
                    }
                    .collect { categories ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            categories = categories,
                            error = null
                        )
                    }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load categories: ${exception.message}"
                )
            }
        }
    }
    
    fun retryLoadingCategories(userId: Long) {
        loadCategories(userId)
    }
}

package com.an.intelligence.flashyfishki.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.intelligence.flashyfishki.domain.model.User
import com.an.intelligence.flashyfishki.domain.repository.AuthRepository
import com.an.intelligence.flashyfishki.navigation.AuthRoute
import com.an.intelligence.flashyfishki.navigation.HomeRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NavigationState(
    val isLoading: Boolean = true,
    val startDestination: Any = AuthRoute,
    val currentUser: User? = null
)

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _navigationState = MutableStateFlow(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    
    init {
        checkInitialDestination()
    }
    
    private fun checkInitialDestination() {
        viewModelScope.launch {
            val user = authRepository.checkAutoLogin()
            
            _navigationState.value = if (user != null) {
                NavigationState(
                    isLoading = false,
                    startDestination = HomeRoute,
                    currentUser = user
                )
            } else {
                NavigationState(
                    isLoading = false,
                    startDestination = AuthRoute,
                    currentUser = null
                )
            }
        }
    }
}

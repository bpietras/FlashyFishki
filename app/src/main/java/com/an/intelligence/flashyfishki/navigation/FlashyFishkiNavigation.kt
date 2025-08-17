package com.an.intelligence.flashyfishki.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.an.intelligence.flashyfishki.ui.auth.AuthScreen
import com.an.intelligence.flashyfishki.ui.home.HomeScreen
import com.an.intelligence.flashyfishki.ui.navigation.NavigationViewModel

@Composable
fun FlashyFishkiNavigation(
    navController: NavHostController = rememberNavController(),
    navigationViewModel: NavigationViewModel = hiltViewModel()
) {
    val navigationState by navigationViewModel.navigationState.collectAsStateWithLifecycle()
    
    if (navigationState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    NavHost(
        navController = navController,
        startDestination = navigationState.startDestination
    ) {
        composable<AuthRoute> {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(HomeRoute) {
                        popUpTo(AuthRoute) { inclusive = true }
                    }
                }
            )
        }
        
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToAuth = {
                    navController.navigate(AuthRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                }
            )
        }
        
        // Future routes will be added here
        // composable<CategoriesRoute> { ... }
        // composable<FlashcardsRoute> { ... }
        // composable<LearningRoute> { ... }
        // composable<StatisticsRoute> { ... }
        // composable<ProfileRoute> { ... }
    }
}

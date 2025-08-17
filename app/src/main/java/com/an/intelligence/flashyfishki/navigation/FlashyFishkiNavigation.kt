package com.an.intelligence.flashyfishki.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.an.intelligence.flashyfishki.ui.auth.AuthScreen
import com.an.intelligence.flashyfishki.ui.home.HomeScreen

@Composable
fun FlashyFishkiNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AuthRoute
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

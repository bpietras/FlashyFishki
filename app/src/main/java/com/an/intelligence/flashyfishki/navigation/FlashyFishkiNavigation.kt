package com.an.intelligence.flashyfishki.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.an.intelligence.flashyfishki.domain.model.User
import com.an.intelligence.flashyfishki.ui.auth.AuthScreen
import com.an.intelligence.flashyfishki.ui.home.HomeScreen

@Composable
fun FlashyFishkiNavigation(
    navController: NavHostController = rememberNavController()
) {
    var currentUser by remember { mutableStateOf<User?>(null) }
    
    NavHost(
        navController = navController,
        startDestination = AuthRoute
    ) {
        composable<AuthRoute> {
            AuthScreen(
                onAuthSuccess = { user ->
                    currentUser = user
                    navController.navigate(HomeRoute) {
                        popUpTo(AuthRoute) { inclusive = true }
                    }
                }
            )
        }
        
        composable<HomeRoute> {
            HomeScreen(
                currentUser = currentUser,
                onNavigateToAuth = {
                    currentUser = null
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

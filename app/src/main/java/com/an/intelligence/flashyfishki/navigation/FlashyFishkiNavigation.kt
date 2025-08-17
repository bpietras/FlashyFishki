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
        
        // Flashcards module routes
        composable<CategoriesRoute> {
            com.an.intelligence.flashyfishki.ui.flashcards.CategoriesListScreen(
                currentUser = currentUser!!,
                onNavigateToCategory = { categoryId ->
                    navController.navigate(FlashcardsRoute(categoryId))
                },
                onNavigateToNewFlashcard = {
                    navController.navigate(FlashcardEditRoute())
                }
            )
        }
        
        composable<FlashcardsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<FlashcardsRoute>()
            com.an.intelligence.flashyfishki.ui.flashcards.CategoryFlashcardsScreen(
                categoryId = route.categoryId,
                currentUser = currentUser!!,
                onNavigateToFlashcard = { flashcardId ->
                    navController.navigate(FlashcardDetailsRoute(flashcardId))
                },
                onNavigateToEdit = { flashcardId ->
                    navController.navigate(FlashcardEditRoute(flashcardId = flashcardId))
                },
                onNavigateToExport = { categoryId ->
                    navController.navigate(ExportRoute(categoryId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable<FlashcardDetailsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<FlashcardDetailsRoute>()
            com.an.intelligence.flashyfishki.ui.flashcards.FlashcardDetailsScreen(
                flashcardId = route.flashcardId,
                currentUser = currentUser!!,
                onNavigateToEdit = { flashcardId ->
                    navController.navigate(FlashcardEditRoute(flashcardId = flashcardId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable<FlashcardEditRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<FlashcardEditRoute>()
            com.an.intelligence.flashyfishki.ui.flashcards.FlashcardEditScreen(
                flashcardId = route.flashcardId,
                categoryId = route.categoryId,
                currentUser = currentUser!!,
                onSaveSuccess = {
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
        
        composable<ExportRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ExportRoute>()
            com.an.intelligence.flashyfishki.ui.flashcards.ExportScreen(
                categoryId = route.categoryId,
                currentUser = currentUser!!,
                onExportComplete = { filePath ->
                    // Show success message or handle file
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        // Future routes will be added here
        // composable<LearningRoute> { ... }
        // composable<StatisticsRoute> { ... }
        // composable<ProfileRoute> { ... }
    }
}

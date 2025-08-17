package com.an.intelligence.flashyfishki.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        composable("categories") {
            com.an.intelligence.flashyfishki.ui.flashcards.CategoriesListScreen(
                currentUser = currentUser!!,
                onNavigateToCategory = { categoryId ->
                    navController.navigate("flashcards/$categoryId")
                },
                onNavigateToNewFlashcard = {
                    navController.navigate("flashcard_new")
                }
            )
        }
        
        composable(
            route = "flashcards/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
            com.an.intelligence.flashyfishki.ui.flashcards.CategoryFlashcardsScreen(
                categoryId = categoryId,
                currentUser = currentUser!!,
                onNavigateToFlashcard = { flashcardId ->
                    navController.navigate("flashcard_details/$flashcardId")
                },
                onNavigateToEdit = { flashcardId ->
                    if (flashcardId != null) {
                        navController.navigate("flashcard_edit/$flashcardId")
                    } else {
                        navController.navigate("flashcard_new?categoryId=$categoryId")
                    }
                },
                onNavigateToExport = { categoryId ->
                    navController.navigate("export/$categoryId")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "flashcard_details/{flashcardId}",
            arguments = listOf(navArgument("flashcardId") { type = NavType.LongType })
        ) { backStackEntry ->
            val flashcardId = backStackEntry.arguments?.getLong("flashcardId") ?: 0L
            com.an.intelligence.flashyfishki.ui.flashcards.FlashcardDetailsScreen(
                flashcardId = flashcardId,
                currentUser = currentUser!!,
                onNavigateToEdit = { flashcardId ->
                    navController.navigate("flashcard_edit/$flashcardId")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "flashcard_edit/{flashcardId}",
            arguments = listOf(navArgument("flashcardId") { type = NavType.LongType })
        ) { backStackEntry ->
            val flashcardId = backStackEntry.arguments?.getLong("flashcardId") ?: 0L
            com.an.intelligence.flashyfishki.ui.flashcards.FlashcardEditScreen(
                flashcardId = if (flashcardId > 0) flashcardId else null,
                categoryId = null,
                currentUser = currentUser!!,
                onSaveSuccess = {
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("flashcard_new") { 
            com.an.intelligence.flashyfishki.ui.flashcards.FlashcardEditScreen(
                flashcardId = null,
                categoryId = null,
                currentUser = currentUser!!,
                onSaveSuccess = {
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "flashcard_new?categoryId={categoryId}",
            arguments = listOf(navArgument("categoryId") { 
                type = NavType.LongType
                defaultValue = 0L
            })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId")?.takeIf { it > 0 }
            com.an.intelligence.flashyfishki.ui.flashcards.FlashcardEditScreen(
                flashcardId = null,
                categoryId = categoryId,
                currentUser = currentUser!!,
                onSaveSuccess = {
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "export/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
            com.an.intelligence.flashyfishki.ui.flashcards.ExportScreen(
                categoryId = categoryId,
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

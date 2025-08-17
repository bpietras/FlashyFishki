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
import androidx.navigation.toRoute
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.an.intelligence.flashyfishki.domain.model.User
import com.an.intelligence.flashyfishki.domain.repository.AuthRepository
import com.an.intelligence.flashyfishki.ui.auth.AuthScreen
import com.an.intelligence.flashyfishki.ui.flashcards.CategoriesListScreen
import com.an.intelligence.flashyfishki.ui.home.HomeScreen
import javax.inject.Inject

@Composable
fun FlashyFishkiNavigation(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository
) {
    val currentUser by authRepository.currentUser.collectAsStateWithLifecycle()
    
    NavHost(
        navController = navController,
        startDestination = AuthRoute
    ) {
        composable<AuthRoute> {
            AuthScreen(
                onAuthSuccess = { user ->
                    authRepository.setCurrentUser(user)
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
                    authRepository.setCurrentUser(null)
                    navController.navigate(AuthRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                },
                onNavigateToCategories = {
                    navController.navigate("categories")
                },
                onNavigateToStudy = {
                    navController.navigate(StudySelectionRoute)
                }
            )
        }
        
        // Flashcards module routes
        composable("categories") {
            CategoriesListScreen(
                currentUser = currentUser!!,
                onNavigateToCategory = { categoryId ->
                    navController.navigate("flashcards/$categoryId")
                },
                onNavigateToNewFlashcard = {
                    navController.navigate("flashcard_new")
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToStudy = {
                    navController.navigate(StudySelectionRoute)
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

        // Study Module Routes
        composable<StudySelectionRoute> {
            com.an.intelligence.flashyfishki.ui.study.StudySelectionScreen(
                currentUser = currentUser!!,
                onNavigateToStudy = { categoryId ->
                    navController.navigate(StudyRoute(categoryId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable<StudyRoute> { backStackEntry ->
            val studyRoute = backStackEntry.toRoute<StudyRoute>()
            com.an.intelligence.flashyfishki.ui.study.StudyScreen(
                categoryId = studyRoute.categoryId,
                currentUser = currentUser!!,
                onNavigateToSummary = { categoryId ->
                    navController.navigate(
                        StudySummaryRoute(categoryId = categoryId)
                    )
                    // Don't popUpTo StudyRoute - keep it in backstack for shared ViewModel
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable<StudySummaryRoute> { backStackEntry ->
            val summaryRoute = backStackEntry.toRoute<StudySummaryRoute>()
            
            // Get StudyViewModel from the study nav graph to share data
            val studyBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry<StudyRoute>()
            }
            val sharedStudyViewModel: com.an.intelligence.flashyfishki.ui.study.viewmodel.StudyViewModel = 
                hiltViewModel(studyBackStackEntry)
            
            com.an.intelligence.flashyfishki.ui.study.StudySummaryScreen(
                categoryId = summaryRoute.categoryId,
                onReturnToStudy = {
                    navController.navigate(StudySelectionRoute) {
                        popUpTo<StudyRoute> { inclusive = true }
                    }
                },
                onFinish = {
                    navController.navigate(HomeRoute) {
                        popUpTo<StudyRoute> { inclusive = true }
                    }
                },
                studyViewModel = sharedStudyViewModel
            )
        }

        // Future routes will be added here
        // composable<LearningRoute> { ... }
        // composable<StatisticsRoute> { ... }
        // composable<ProfileRoute> { ... }
    }
}

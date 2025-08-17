package com.an.intelligence.flashyfishki.ui.flashcards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.an.intelligence.flashyfishki.domain.dao.CategoryDao
import com.an.intelligence.flashyfishki.domain.model.User
import com.an.intelligence.flashyfishki.ui.flashcards.components.AnimatedCategoryCard
import com.an.intelligence.flashyfishki.ui.flashcards.components.CreateCategoryDialog
import com.an.intelligence.flashyfishki.ui.flashcards.model.LearningStatus
import com.an.intelligence.flashyfishki.ui.flashcards.viewmodel.CategoriesViewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoriesListScreen(
    currentUser: User,
    onNavigateToCategory: (Long) -> Unit,
    onNavigateToNewFlashcard: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToStudy: () -> Unit = {},
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val categoriesWithStats by viewModel.categoriesWithStats.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val categoryFormState by viewModel.categoryFormState.collectAsStateWithLifecycle()

    var showCreateCategoryDialog by remember { mutableStateOf(false) }

    // Load categories on screen start
    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "My Flashcards",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToStudy) {
                        Icon(
                            Icons.Default.School, 
                            contentDescription = "Start Study Session",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateCategoryDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { paddingValues ->
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Show loading state
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Show error state
            error?.let { errorMessage ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { 
                                    viewModel.clearError()
                                    viewModel.loadCategories()
                                }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }

            // Show empty state or categories
            if (!isLoading && error == null) {
                if (categoriesWithStats.isEmpty()) {
                    item {
                        EmptyCategoriesCard(
                            onCreateCategory = { showCreateCategoryDialog = true }
                        )
                    }
                } else {
                    itemsIndexed(categoriesWithStats) { index, categoryWithStats ->
                        LaunchedEffect(index) {
                            delay(index * 100L) // Stagger animations
                        }
                        
                        AnimatedCategoryCard(
                            categoryWithStats = categoryWithStats,
                            onClick = { onNavigateToCategory(categoryWithStats.categoryId) },
                            modifier = Modifier
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
            }
        }
    }

    // Create Category Dialog
    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            categoryFormState = categoryFormState,
            onNameChange = { viewModel.updateCategoryFormField(it) },
            onConfirm = { name ->
                viewModel.createCategory(name)
                showCreateCategoryDialog = false
            },
            onDismiss = {
                showCreateCategoryDialog = false
                viewModel.resetCategoryForm()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryCard(
    categoryWithStats: CategoryDao.CategoryWithLearningStats,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Category name and total count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryWithStats.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "${categoryWithStats.flashcardCount} cards",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Learning status indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LearningStatusIndicator(
                    status = LearningStatus.NEW,
                    count = categoryWithStats.newCount
                )
                LearningStatusIndicator(
                    status = LearningStatus.FIRST_REPEAT,
                    count = categoryWithStats.firstRepeatCount
                )
                LearningStatusIndicator(
                    status = LearningStatus.SECOND_REPEAT,
                    count = categoryWithStats.secondRepeatCount
                )
                LearningStatusIndicator(
                    status = LearningStatus.LEARNED,
                    count = categoryWithStats.learnedCount
                )
            }
        }
    }
}

@Composable
private fun LearningStatusIndicator(
    status: LearningStatus,
    count: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = status.color.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = status.color
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = status.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyCategoriesCard(
    onCreateCategory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No categories yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create your first category to start organizing your flashcards",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCreateCategory
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Category")
            }
        }
    }
}

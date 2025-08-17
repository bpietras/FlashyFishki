package com.an.intelligence.flashyfishki.ui.flashcards.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Optimized LazyColumn with pagination support
 */
@Composable
fun <T> OptimizedLazyColumn(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    pageSize: Int = 20,
    loadMoreThreshold: Int = 5,
    isLoading: Boolean = false,
    hasMoreItems: Boolean = true,
    onLoadMore: () -> Unit = {},
    itemKey: ((index: Int, item: T) -> Any)? = null,
    itemContent: @Composable (index: Int, item: T) -> Unit
) {
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            
            hasMoreItems && !isLoading && lastVisibleItemIndex > (totalItemsNumber - loadMoreThreshold)
        }
    }
    
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            onLoadMore()
        }
    }
    
    LazyColumn(
        modifier = modifier,
        state = state,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        itemsIndexed(
            items = items,
            key = itemKey
        ) { index, item ->
            itemContent(index, item)
        }
        
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        if (!hasMoreItems && items.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No more items",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Space for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Performance-optimized flashcard list
 */
@Composable
fun OptimizedFlashcardList(
    flashcards: List<com.an.intelligence.flashyfishki.domain.model.Flashcard>,
    onFlashcardClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val listState = rememberLazyListState()
    
    OptimizedLazyColumn(
        items = flashcards,
        state = listState,
        modifier = modifier,
        isLoading = isLoading,
        hasMoreItems = false, // We load all flashcards at once for now
        itemKey = { _, flashcard -> flashcard.flashcardId }
    ) { index, flashcard ->
        // Use AnimatedFlashcardCard with proper key for recomposition optimization
        key(flashcard.flashcardId) {
            AnimatedFlashcardCard(
                flashcard = flashcard,
                onClick = { onFlashcardClick(flashcard.flashcardId) },
                modifier = Modifier
            )
        }
    }
}

/**
 * Performance-optimized category list
 */
@Composable
fun OptimizedCategoryList(
    categories: List<com.an.intelligence.flashyfishki.domain.dao.CategoryDao.CategoryWithLearningStats>,
    onCategoryClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val listState = rememberLazyListState()
    
    OptimizedLazyColumn(
        items = categories,
        state = listState,
        modifier = modifier,
        isLoading = isLoading,
        hasMoreItems = false,
        itemKey = { _, category -> category.categoryId }
    ) { index, category ->
        // Stagger animations for better UX
        LaunchedEffect(index) {
            kotlinx.coroutines.delay(index * 50L)
        }
        
        key(category.categoryId) {
            AnimatedCategoryCard(
                categoryWithStats = category,
                onClick = { onCategoryClick(category.categoryId) },
                modifier = Modifier
            )
        }
    }
}

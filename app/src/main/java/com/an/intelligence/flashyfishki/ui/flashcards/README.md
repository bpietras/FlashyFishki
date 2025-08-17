# Moduł Moje Fiszki - Dokumentacja

## Przegląd

Moduł "Moje Fiszki" umożliwia użytkownikom zarządzanie własnymi fiszkami edukacyjnymi w aplikacji FlashyFishki. Implementuje pełny cykl CRUD dla fiszek i kategorii z zaawansowanymi funkcjami UX i optymalizacjami wydajności.

## Architektura

### Struktura katalogów
```
ui/flashcards/
├── cache/           # System cachowania danych
├── components/      # Reużywalne komponenty UI
├── model/          # Modele danych i stany
├── theme/          # Kolory i styling
├── utils/          # Utility functions i walidacja
├── viewmodel/      # ViewModels z logiką biznesową
└── [Screens].kt    # Główne ekrany aplikacji
```

### Główne ekrany

#### 1. CategoriesListScreen
- **Ścieżka**: `main/categories`
- **Funkcje**: Lista kategorii ze statystykami nauki, tworzenie kategorii
- **Komponenty**: `AnimatedCategoryCard`, `CreateCategoryDialog`

#### 2. CategoryFlashcardsScreen  
- **Ścieżka**: `main/categories/{categoryId}/flashcards`
- **Funkcje**: Lista fiszek w kategorii, filtrowanie, eksport
- **Komponenty**: `AnimatedFlashcardCard`, `FlashcardFilterDialog`

#### 3. FlashcardDetailsScreen
- **Ścieżka**: `main/flashcards/{flashcardId}`
- **Funkcje**: Szczegóły fiszki, edycja, usuwanie, przywracanie do nauki
- **Komponenty**: `FlashcardInfoCard`, `DeleteConfirmationDialog`

#### 4. FlashcardEditScreen
- **Ścieżka**: `main/flashcards/new`, `main/flashcards/{flashcardId}/edit`
- **Funkcje**: Tworzenie/edycja fiszki z walidacją
- **Komponenty**: `FlashcardForm` z pełną walidacją

#### 5. ExportScreen
- **Ścieżka**: `main/categories/{categoryId}/export`
- **Funkcje**: Eksport fiszek do formatu Markdown
- **Komponenty**: `ExportProgressIndicator`, `ExportInfoCard`

## ViewModels

### CategoriesViewModel
```kotlin
// Zarządzanie listą kategorii
- categoriesWithStats: StateFlow<List<CategoryWithLearningStats>>
- categoryFormState: StateFlow<CategoryFormState>
- loadCategories(), createCategory(), validateCategoryName()
```

### CategoryFlashcardsViewModel
```kotlin
// Zarządzanie fiszkami w kategorii
- flashcards: StateFlow<List<Flashcard>>
- filter: StateFlow<FlashcardFilter>
- loadFlashcards(), applyFilter(), loadCategory()
```

### FlashcardEditViewModel
```kotlin
// Tworzenie/edycja fiszek
- formState: StateFlow<FlashcardFormState>
- categories: StateFlow<List<Category>>
- initializeForNewFlashcard(), saveFlashcard(), validateForm()
```

### FlashcardDetailsViewModel
```kotlin
// Szczegóły i operacje na fiszce
- flashcard: StateFlow<Flashcard?>
- deleteFlashcard(), restoreToLearning()
```

### ExportViewModel
```kotlin
// Eksport do Markdown
- exportProgress: StateFlow<ExportProgress>
- startExport(), generateMarkdown(), saveToFile()
```

## Komponenty UI

### Animowane karty
- **AnimatedCategoryCard**: Karty kategorii z animacjami wejścia i progress indicators
- **AnimatedFlashcardCard**: Karty fiszek z animacjami naciśnięcia i hover effects

### Formularze
- **FlashcardForm**: Kompleksowy formularz z walidacją w czasie rzeczywistym
- **CreateCategoryDialog**: Dialog tworzenia kategorii z walidacją

### Optymalizacja
- **OptimizedLazyColumn**: LazyColumn z paginacją i optymalizacjami
- **OptimizedFlashcardList**: Lista fiszek z cache-aware rendering

## System cachowania

### FlashcardCache
```kotlin
// In-memory cache z timeout (5 min)
- categoriesCache: Map<Long, CategoryWithLearningStats>
- flashcardsCache: Map<Long, List<Flashcard>>
- flashcardCache: Map<Long, Flashcard>
```

### CachedFlashcardRepository
```kotlin
// Cache-first approach
- getCategoriesWithCache(), storeCategories()
- getFlashcardsWithCache(), storeFlashcards()
- invalidateOnDataChange()
```

## Walidacja

### FlashcardValidators
```kotlin
- questionValidator(): String 0-500 znaków
- answerValidator(): String 0-1000 znaków  
- categoryNameValidator(): String 0-100 znaków
```

### Debounced Validation
```kotlin
// Walidacja z opóźnieniem 300ms
rememberDebouncedValidation(value, delayMs, validator)
rememberDebouncedFormField(initialValue, delayMs, validator)
```

## Styling i kolory

### FlashcardColors
```kotlin
// Statusy nauki
- NewStatus: Gray (#6B7280)
- FirstRepeatStatus: Amber (#F59E0B)
- SecondRepeatStatus: Blue (#3B82F6) 
- LearnedStatus: Emerald (#10B981)

// Poziomy trudności
- DifficultyEasy: Green (1-2)
- DifficultyMedium: Amber (3)
- DifficultyHard: Red (4-5)
```

### Animacje
- **Entrance animations**: Staggered dla list, slide + fade
- **Press animations**: Scale down (0.95x) z spring animation
- **Progress animations**: Linear progress z color transitions
- **Item placement**: `animateItemPlacement()` dla LazyColumn

## Optymalizacje wydajności

### LazyColumn optimizations
- **Staggered animations**: Delay 50-100ms między elementami
- **Key functions**: Stable keys dla recomposition
- **Item placement**: `animateItemPlacement()` modifier

### Memory optimizations
- **StableList**: Wrapper zapobiegający recomposition
- **Lifecycle-aware**: Collection tylko gdy STARTED
- **Debounced state**: Expensive operations z delay
- **Memoized computations**: `remember()` z proper keys

### Cache strategy
- **5-minute timeout**: Automatyczne invalidation
- **Cache-first**: Sprawdzenie cache przed DAO
- **Selective invalidation**: Per-category/flashcard keys

## Testowanie

### Unit tests
- **CategoriesViewModelTest**: 8 test cases, walidacja i CRUD
- **FlashcardEditViewModelTest**: 15 test cases, formularze i limits
- **ExportViewModelTest**: 8 test cases, export logic

### Test utilities
```kotlin
// MockK dla DAO mocking
// Coroutines test dla async operations
// StandardTestDispatcher dla deterministic timing
```

## Użycie

### Nawigacja do modułu
```kotlin
// Z HomeScreen
navController.navigate("categories")
```

### Tworzenie nowej fiszki
```kotlin
// Z CategoriesListScreen -> CategoryFlashcardsScreen
navController.navigate("flashcard_new?categoryId=$categoryId")
```

### Eksport kategorii
```kotlin
// Z CategoryFlashcardsScreen
navController.navigate("export/$categoryId")
```

## Metryki wydajności

### Cold start optimization
- Lazy initialization komponentów
- Cache pre-warming w tle
- Minimal initial composition

### Memory usage
- Weak references w cache
- Lifecycle-aware collection
- Automatic cleanup w onDispose

### Network efficiency
- Local-first architecture
- Batch operations
- Offline-capable

## Przyszłe rozszerzenia

### Planowane funkcje
1. **Sync z cloud**: Synchronizacja między urządzeniami
2. **Advanced search**: Full-text search w fiszkich
3. **Statistics**: Detailowe statystyki nauki
4. **Sharing**: Udostępnianie kategorii między użytkownikami
5. **Import/Export**: Więcej formatów (CSV, JSON, Anki)

### Optymalizacje
1. **Pagination**: Dla dużych kolekcji (>1000 fiszek)
2. **Virtual scrolling**: Dla performance
3. **Background sync**: Automatic data refresh
4. **Prefetching**: Predictive loading

## API Reference

### Główne komponenty
```kotlin
@Composable
fun CategoriesListScreen(
    currentUser: User,
    onNavigateToCategory: (Long) -> Unit,
    onNavigateToNewFlashcard: () -> Unit
)

@Composable  
fun FlashcardEditScreen(
    flashcardId: Long?,
    categoryId: Long?,
    currentUser: User,
    onSaveSuccess: () -> Unit,
    onCancel: () -> Unit
)
```

### Utility functions
```kotlin
// Walidacja
fun rememberDebouncedValidation(value: String, delayMs: Long, validator: (String) -> String?): String?

// Performance
fun <T> List<T>.toStableList(): StableList<T>
fun <T> rememberStableList(list: List<T>): StableList<T>

// Cache
suspend fun FlashcardCache.cacheFlashcards(categoryId: Long, flashcards: List<Flashcard>)
```

## Troubleshooting

### Często występujące problemy

1. **Slow list scrolling**
   - Sprawdź czy używasz `key()` w LazyColumn
   - Upewnij się że `AnimatedFlashcardCard` ma `animateItemPlacement()`

2. **Memory leaks**
   - Sprawdź czy flow collection jest lifecycle-aware
   - Użyj `DisposableEffect` dla cleanup

3. **Cache invalidation**
   - Wywołaj `invalidateOnDataChange()` po modyfikacjach
   - Sprawdź timeout (5 min) w `FlashcardCache`

4. **Validation performance**
   - Użyj debounced validation (300ms delay)
   - Unikaj frequent recomposition w formach

### Debug tips
```kotlin
// Logging cache hits/misses
Log.d("FlashcardCache", "Cache hit for key: $key")

// Performance monitoring
@Composable
fun PerformanceLogger(tag: String, content: @Composable () -> Unit) {
    val time = remember { System.currentTimeMillis() }
    Log.d("Performance", "$tag composition time: ${System.currentTimeMillis() - time}ms")
    content()
}
```

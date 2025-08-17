# Plan implementacji widoku Moduł: Moje Fiszki

## 1. Przegląd

Moduł "Moje Fiszki" umożliwia użytkownikom zarządzanie własnymi fiszkami edukacyjnymi w aplikacji FlashyFishki. Moduł składa się z pięciu głównych ekranów: lista kategorii ze statystykami nauki, lista fiszek w kategorii, szczegóły pojedynczej fiszki, formularz tworzenia/edycji fiszek oraz eksport fiszek do formatu markdown. System wykorzystuje algorytm powtórek 3-5-7 z czterema statusami nauki (0-3) oznaczonymi kolorami, obsługuje kategoryzację, poziomy trudności i oznaczanie fiszek jako publiczne.

## 2. Routing widoku

Moduł wykorzystuje następujące ścieżki nawigacyjne:
- `main/categories` - CategoriesListScreen (lista kategorii)
- `main/categories/{categoryId}/flashcards` - CategoryFlashcardsScreen (fiszki w kategorii) 
- `main/flashcards/{flashcardId}` - FlashcardDetailsScreen (szczegóły fiszki)
- `main/flashcards/new` - FlashcardEditScreen (nowa fiszka)
- `main/flashcards/{flashcardId}/edit` - FlashcardEditScreen (edycja fiszki)
- `main/categories/{categoryId}/export` - ExportScreen (eksport kategorii)

## 3. Struktura komponentów

```
CategoriesListScreen (główny ekran modułu)
├── CategoryCard (pojedyncza kategoria ze statystykami)
├── CreateCategoryDialog (dialog tworzenia kategorii)
└── CategoryFlashcardsScreen
    ├── FlashcardCard (pojedyncza fiszka na liście)
    ├── CategoryTopAppBar (pasek z opcjami menu)
    ├── FlashcardDetailsScreen
    │   ├── FlashcardInfoCard (wyświetlanie treści)
    │   ├── FlashcardActionsRow (przyciski akcji)
    │   └── DeleteConfirmationDialog (potwierdzenie usunięcia)
    ├── FlashcardEditScreen
    │   ├── FlashcardForm (formularz pytania/odpowiedzi)
    │   ├── CategorySelector (dropdown kategorii)
    │   ├── DifficultySlider (suwak poziomu trudności)
    │   └── PublicToggle (przełącznik publiczny)
    └── ExportScreen
        ├── ExportInfoCard (informacje o eksporcie)
        └── ExportProgressIndicator (wskaźnik postępu)
```

## 4. Szczegóły komponentów

### CategoriesListScreen
- **Opis komponentu**: Główny ekran modułu wyświetlający listę kategorii użytkownika ze statystykami fiszek w każdym statusie nauki
- **Główne elementy**: LazyColumn z CategoryCard dla każdej kategorii, FloatingActionButton do dodawania kategorii, TopAppBar z tytułem
- **Obsługiwane interakcje**: Kliknięcie kategorii (nawigacja do fiszek), kliknięcie FAB (dialog nowej kategorii), długie naciśnięcie kategorii (opcje kontekstowe)
- **Obsługiwana walidacja**: Sprawdzenie unikalności nazwy kategorii, wymagana nazwa kategorii (minimum 1 znak)
- **Typy**: `CategoryWithLearningStats`, `Category`, `CategoryFormState`
- **Propsy**: `currentUser: User`, `onNavigateToCategory: (Long) -> Unit`, `onNavigateToNewFlashcard: () -> Unit`

### CategoryFlashcardsScreen
- **Opis komponentu**: Ekran wyświetlający listę fiszek z wybranej kategorii z opcjami filtrowania i eksportu
- **Główne elementy**: LazyColumn z FlashcardCard, TopAppBar z nazwą kategorii i menu opcji, FloatingActionButton do dodawania fiszki
- **Obsługiwane interakcje**: Kliknięcie fiszki (nawigacja do szczegółów), menu eksport (nawigacja do eksportu), filtrowanie według statusu nauki
- **Obsługiwana walidacja**: Sprawdzenie istnienia kategorii przed ładowaniem fiszek
- **Typy**: `Flashcard`, `Category`, `FlashcardFilter`
- **Propsy**: `categoryId: Long`, `currentUser: User`, `onNavigateToFlashcard: (Long) -> Unit`, `onNavigateToEdit: (Long?) -> Unit`, `onNavigateToExport: (Long) -> Unit`

### FlashcardDetailsScreen
- **Opis komponentu**: Ekran szczegółów pojedynczej fiszki z możliwością edycji, usuwania i przywracania do nauki
- **Główne elementy**: FlashcardInfoCard z treścią, FlashcardActionsRow z przyciskami, informacje o kategorii i poziomie trudności
- **Obsługiwane interakcje**: Edycja fiszki (nawigacja do formularza), usunięcie (dialog potwierdzenia), przywrócenie do nauki (zmiana statusu)
- **Obsługiwana walidacja**: Sprawdzenie uprawnień użytkownika do fiszki, potwierdzenie przed usunięciem
- **Typy**: `Flashcard`, `Category`, `DeleteConfirmationState`
- **Propsy**: `flashcardId: Long`, `currentUser: User`, `onNavigateToEdit: (Long) -> Unit`, `onNavigateBack: () -> Unit`

### FlashcardEditScreen
- **Opis komponentu**: Formularz tworzenia nowej lub edycji istniejącej fiszki z walidacją wszystkich pól
- **Główne elementy**: TextField dla pytania i odpowiedzi z licznikami znaków, CategorySelector, DifficultySlider, PublicToggle, przyciski Zapisz/Anuluj
- **Obsługiwane interakcje**: Zmiana wartości pól, wybór kategorii z dropdown, ustawienie poziomu trudności, przełączenie publiczny, zapisanie lub anulowanie
- **Obsługiwana walidacja**: Długość pytania (≤500 znaków), długość odpowiedzi (≤1000 znaków), wymagane pytanie i odpowiedź, wybrana kategoria, sprawdzenie limitu 1000 fiszek na użytkownika
- **Typy**: `Flashcard`, `Category`, `FlashcardFormState`, `ValidationResult`
- **Propsy**: `flashcardId: Long?`, `categoryId: Long?`, `currentUser: User`, `onSaveSuccess: () -> Unit`, `onCancel: () -> Unit`

### ExportScreen
- **Opis komponentu**: Ekran eksportu fiszek z wybranej kategorii do formatu markdown z wskaźnikiem postępu
- **Główne elementy**: ExportInfoCard z informacjami o kategorii, przycisk "Eksportuj", ExportProgressIndicator podczas eksportu
- **Obsługiwane interakcje**: Rozpoczęcie eksportu (generowanie pliku .md), anulowanie eksportu, powrót po zakończeniu
- **Obsługiwana walidacja**: Sprawdzenie istnienia fiszek do eksportu, sprawdzenie uprawnień do zapisu plików
- **Typy**: `Category`, `Flashcard`, `ExportProgress`, `ExportResult`
- **Propsy**: `categoryId: Long`, `currentUser: User`, `onExportComplete: (String) -> Unit`, `onCancel: () -> Unit`

## 5. Typy

### FlashcardFormState
```kotlin
data class FlashcardFormState(
    val question: String = "",
    val answer: String = "",
    val categoryId: Long = 0L,
    val difficultyLevel: Int = 1,
    val isPublic: Boolean = false,
    val questionError: String? = null,
    val answerError: String? = null,
    val categoryError: String? = null,
    val isValid: Boolean = false
)
```

### CategoryFormState
```kotlin
data class CategoryFormState(
    val name: String = "",
    val nameError: String? = null,
    val isValid: Boolean = false
)
```

### ExportProgress
```kotlin
data class ExportProgress(
    val isExporting: Boolean = false,
    val progress: Float = 0f,
    val exportedCount: Int = 0,
    val totalCount: Int = 0,
    val isComplete: Boolean = false,
    val error: String? = null,
    val filePath: String? = null
)
```

### FlashcardFilter
```kotlin
data class FlashcardFilter(
    val learningStatus: Int? = null,
    val difficultyLevel: Int? = null,
    val sortBy: SortBy = SortBy.CREATED_DATE_DESC
)

enum class SortBy {
    CREATED_DATE_DESC,
    CREATED_DATE_ASC,
    LEARNING_STATUS,
    DIFFICULTY_LEVEL
}
```

### ValidationResult
```kotlin
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)
```

### LearningStatus (enum dla statusów nauki)
```kotlin
enum class LearningStatus(val value: Int, val displayName: String, val color: Color) {
    NEW(0, "Nowe", Color.Gray),
    FIRST_REPEAT(1, "Pierwsza powtórka", Color.Yellow),
    SECOND_REPEAT(2, "Druga powtórka", Color.Blue),
    LEARNED(3, "Nauczone", Color.Green)
}
```

## 6. Zarządzanie stanem

### CategoriesViewModel
- **StateFlow**: `categoriesWithStats`, `isLoading`, `error`, `categoryFormState`
- **Funkcje**: `loadCategories()`, `createCategory(name: String)`, `validateCategoryName(name: String)`
- **Dependencies**: `CategoryDao`, `UserRepository`

### CategoryFlashcardsViewModel
- **StateFlow**: `flashcards`, `category`, `isLoading`, `filter`, `error`
- **Funkcje**: `loadFlashcards(categoryId: Long)`, `applyFilter(filter: FlashcardFilter)`, `loadCategory(categoryId: Long)`
- **Dependencies**: `FlashcardDao`, `CategoryDao`

### FlashcardEditViewModel
- **StateFlow**: `formState`, `categories`, `isLoading`, `isSaving`, `error`
- **Funkcje**: `loadFlashcard(flashcardId: Long)`, `updateFormField()`, `validateForm()`, `saveFlashcard()`, `loadCategories()`
- **Dependencies**: `FlashcardDao`, `CategoryDao`, `UserRepository`

### FlashcardDetailsViewModel
- **StateFlow**: `flashcard`, `category`, `isLoading`, `error`, `deleteConfirmationState`
- **Funkcje**: `loadFlashcard(flashcardId: Long)`, `deleteFlashcard()`, `restoreToLearning()`, `loadCategory()`
- **Dependencies**: `FlashcardDao`, `CategoryDao`

### ExportViewModel
- **StateFlow**: `exportProgress`, `category`, `flashcards`, `error`
- **Funkcje**: `loadExportData(categoryId: Long)`, `startExport()`, `cancelExport()`
- **Dependencies**: `FlashcardDao`, `CategoryDao`, `FileManager`

Wymagany jest customowy hook `useFormValidation` do zarządzania walidacją formularzy w czasie rzeczywistym z debounce.

## 7. Integracja z bazą

### Wykorzystywane DAO metody:

**CategoryDao:**
- `getUserCategoriesWithLearningStats(userId: Long): Flow<List<CategoryWithLearningStats>>` - pobieranie kategorii ze statystykami dla CategoriesListScreen
- `insertCategory(category: Category): Long` - tworzenie nowej kategorii
- `getCategoryByName(name: String): Category?` - walidacja unikalności nazwy
- `getCategoryById(categoryId: Long): Category?` - szczegóły kategorii

**FlashcardDao:**
- `getUserFlashcardsByCategory(userId: Long, categoryId: Long): Flow<List<Flashcard>>` - lista fiszek w kategorii
- `getFlashcardById(flashcardId: Long): Flashcard?` - szczegóły pojedynczej fiszki
- `insertFlashcard(flashcard: Flashcard): Long` - tworzenie nowej fiszki
- `updateFlashcard(flashcard: Flashcard)` - edycja istniejącej fiszki
- `deleteFlashcard(flashcard: Flashcard)` - usuwanie fiszki
- `countUserFlashcards(userId: Long): Int` - sprawdzenie limitu fiszek
- `updateFlashcardLearningStatus(flashcardId: Long, newStatus: Int, nextReviewDate: Date?, updateTime: Date)` - przywracanie do nauki

**Typy żądań i odpowiedzi:**
- **Żądania**: zawsze zawierają `userId` dla autoryzacji, opcjonalnie parametry filtrowania
- **Odpowiedzi**: `Flow<List<T>>` dla list danych, `T?` dla pojedynczych obiektów, `Long` dla ID nowo utworzonych rekordów

## 8. Interakcje użytkownika

### CategoriesListScreen
1. **Wyświetlenie listy**: Automatyczne ładowanie kategorii przy wejściu na ekran
2. **Kliknięcie kategorii**: Nawigacja do `CategoryFlashcardsScreen` z `categoryId`
3. **FAB dodaj kategorię**: Wyświetlenie dialogu z polem tekstowym
4. **Utworzenie kategorii**: Walidacja → zapis → odświeżenie listy

### CategoryFlashcardsScreen
1. **Wyświetlenie fiszek**: Automatyczne ładowanie dla `categoryId`
2. **Kliknięcie fiszki**: Nawigacja do `FlashcardDetailsScreen`
3. **FAB dodaj fiszkę**: Nawigacja do `FlashcardEditScreen` z preselected `categoryId`
4. **Menu eksport**: Nawigacja do `ExportScreen`
5. **Filtrowanie**: Aktualizacja listy według wybranych kryteriów

### FlashcardDetailsScreen
1. **Wyświetlenie szczegółów**: Ładowanie fiszki i kategorii
2. **Przycisk edytuj**: Nawigacja do `FlashcardEditScreen`
3. **Przycisk usuń**: Dialog potwierdzenia → usunięcie → nawigacja wstecz
4. **Przywróć do nauki**: Zmiana statusu na 0 → aktualizacja

### FlashcardEditScreen
1. **Wypełnianie formularza**: Walidacja w czasie rzeczywistym z debounce
2. **Wybór kategorii**: Dropdown z listą kategorii użytkownika
3. **Ustawienie trudności**: Slider 1-5
4. **Zapisz**: Walidacja → zapis → nawigacja wstecz
5. **Anuluj**: Nawigacja wstecz bez zapisu

### ExportScreen
1. **Wyświetlenie informacji**: Ładowanie kategorii i liczby fiszek
2. **Eksportuj**: Generowanie pliku .md → progress → komunikat o sukcesie
3. **Anuluj**: Zatrzymanie eksportu → nawigacja wstecz

## 9. Warunki i walidacja

### Walidacja formularza fiszki (FlashcardEditScreen):
- **Pytanie**: Nie może być puste, maksymalnie 500 znaków
- **Odpowiedź**: Nie może być pusta, maksymalnie 1000 znaków  
- **Kategoria**: Musi być wybrana z dostępnych kategorii
- **Poziom trudności**: Wartość 1-5 (kontrolowana przez slider)
- **Limit fiszek**: Sprawdzenie czy użytkownik nie przekracza limitu 1000 fiszek

### Walidacja kategorii (CreateCategoryDialog):
- **Nazwa**: Nie może być pusta, musi być unikalna dla użytkownika
- **Długość**: Maksymalnie 100 znaków

### Walidacja uprawnień:
- **Dostęp do fiszki**: Sprawdzenie czy `flashcard.userId == currentUser.userId`
- **Dostęp do kategorii**: Weryfikacja czy kategoria należy do użytkownika
- **Operacje CRUD**: Autoryzacja przed każdą operacją modyfikującą

### Walidacja biznesowa:
- **Usuwanie kategorii**: Sprawdzenie czy nie zawiera fiszek
- **Przywracanie do nauki**: Tylko dla fiszek ze statusem 3 (nauczone)
- **Oznaczanie jako publiczne**: Sprawdzenie kompletności danych fiszki

## 10. Obsługa błędów

### Błędy walidacji:
- **Przekroczenie limitów**: Wyświetlenie komunikatu z licznikiem pozostałych znaków
- **Puste pola**: Podświetlenie pól z błędami i komunikaty
- **Duplikaty**: Komunikat o nieunikalnej nazwie kategorii

### Błędy bazy danych:
- **Brak połączenia**: Komunikat "Błąd połączenia z bazą danych" + przycisk retry
- **Constraint violations**: Komunikaty o naruszeniu ograniczeń (foreign key, unique)
- **Timeout**: Komunikat o timeout + możliwość ponowienia

### Błędy nawigacji:
- **Nieistniejące rekordy**: Automatyczny powrót do poprzedniego ekranu + komunikat
- **Brak uprawnień**: Komunikat "Brak dostępu" + nawigacja do home

### Błędy eksportu:
- **Brak uprawnień do zapisu**: Komunikat o uprawnieniach storage + link do ustawień
- **Brak miejsca**: Komunikat o braku miejsca na dysku
- **Błąd generowania**: Komunikat ogólny + możliwość ponowienia

### Błędy sieciowe (przyszłe rozszerzenia):
- **Brak internetu**: Komunikat + tryb offline
- **Błędy synchronizacji**: Kolejka operacji do ponowienia

## 11. Kroki implementacji

1. **Przygotowanie routingu**
   - Dodanie nowych route do `Routes.kt`
   - Rozszerzenie `FlashyFishkiNavigation.kt` o nowe ekrany
   - Konfiguracja Navigation Compose z przekazywaniem parametrów

2. **Implementacja ViewModels**
   - `CategoriesViewModel` z `getUserCategoriesWithLearningStats`
   - `CategoryFlashcardsViewModel` z filtrowaniem
   - `FlashcardEditViewModel` z walidacją formularza
   - `FlashcardDetailsViewModel` z operacjami CRUD
   - `ExportViewModel` z generowaniem markdown

3. **Tworzenie podstawowych ekranów**
   - `CategoriesListScreen` z LazyColumn i FAB
   - `CategoryFlashcardsScreen` z TopAppBar i menu
   - `FlashcardDetailsScreen` z kartami informacji
   - `FlashcardEditScreen` z formularzem i walidacją
   - `ExportScreen` z progress indicator

4. **Implementacja komponentów UI**
   - `CategoryCard` z kolorowymi statusami nauki
   - `FlashcardCard` z podglądem treści
   - `CreateCategoryDialog` z walidacją
   - `FlashcardForm` z licznikami znaków
   - `DeleteConfirmationDialog`

5. **Dodanie walidacji i obsługi błędów**
   - Walidacja w czasie rzeczywistym z debounce
   - Obsługa komunikatów błędów
   - Loading states dla wszystkich operacji async
   - Error states z możliwością retry

6. **Implementacja eksportu do markdown**
   - Generator plików .md z formatowaniem
   - Progress tracking podczas eksportu
   - File operations z obsługą uprawnień
   - Komunikaty o statusie eksportu

7. **Styling i UX**
   - Kolorowe oznaczenia statusów nauki (szary/żółty/niebieski/zielony)
   - Spójne spacing i typography
   - Material Design 3 components
   - Animacje przejść między ekranami

8. **Testowanie**
   - Unit testy ViewModels
   - Integration testy DAO operations
   - UI testy dla głównych flow
   - Testy walidacji formularzy

9. **Optymalizacja wydajności**
   - LazyColumn optimization
   - Debounce dla walidacji
   - Caching kategorii
   - Pagination jeśli potrzeba

10. **Dokumentacja i finalizacja**
    - Dokumentacja API
    - Code review
    - Performance testing
    - User acceptance testing

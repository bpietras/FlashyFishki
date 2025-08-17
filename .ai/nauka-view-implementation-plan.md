# Plan implementacji widoku Modułu Nauki

## 1. Przegląd

Moduł Nauki składa się z trzech głównych ekranów: wyboru kategorii do nauki (StudySelectionScreen), głównego ekranu nauki z fiszkami (StudyScreen) oraz podsumowania sesji nauki (StudySummaryScreen). Moduł implementuje algorytm powtórek 3-5-7, umożliwiając użytkownikom efektywną naukę poprzez system spaced repetition. Użytkownicy mogą wybierać kategorie z fiszkami o statusach 0-2, przeglądać fiszki w formie interaktywnych kart, oceniać swoje odpowiedzi i przeglądać szczegółowe statystyki sesji.

## 2. Routing widoku

```
main/study - StudySelectionScreen (wybór kategorii)
main/study/{categoryId} - StudyScreen (sesja nauki)
main/study/{categoryId}/summary - StudySummaryScreen (podsumowanie)
```

Dodatkowe routes w Routes.kt:
- `StudySelectionRoute` - object dla wyboru kategorii
- `StudyRoute(categoryId: Long)` - data class dla sesji nauki
- `StudySummaryRoute(categoryId: Long, sessionStats: StudySessionStats)` - data class dla podsumowania

## 3. Struktura komponentów

```
StudyModule/
├── StudySelectionScreen
│   ├── CategoryStudyCard (×N)
│   └── EmptyStudyState
├── StudyScreen
│   ├── StudyTopBar
│   ├── StudyProgressIndicator
│   ├── FlashcardStudyCard
│   └── StudyControlsSection
└── StudySummaryScreen
    ├── StudyStatsCard
    ├── StudyChartsSection
    └── StudyActionButtons
```

## 4. Szczegóły komponentów

### StudySelectionScreen
- **Opis komponentu**: Główny ekran wyboru kategorii do nauki, wyświetla listę kategorii z liczbą fiszek gotowych do powtórki
- **Główne elementy**: LazyColumn z kartami kategorii, każda karta zawiera nazwę kategorii, liczbę fiszek do powtórki i przycisk "Rozpocznij naukę"
- **Obsługiwane interakcje**: 
  - Wybór kategorii do nauki
  - Rozpoczęcie sesji nauki dla kategorii
  - Nawigacja powrotna
- **Obsługiwana walidacja**: 
  - Sprawdzenie czy kategoria ma fiszki do powtórki (status 0-2)
  - Blokowanie przycisku rozpoczęcia nauki dla kategorii bez fiszek do powtórki
  - Walidacja istnienia użytkownika
- **Typy**: List<CategoryWithStudyStats>, StudyUiState, StudyAction
- **Propsy**: currentUser: User, onNavigateToStudy: (Long) -> Unit, onNavigateBack: () -> Unit

### CategoryStudyCard
- **Opis komponentu**: Karta reprezentująca pojedynczą kategorię z informacjami o friszkach do nauki
- **Główne elementy**: Card z nazwą kategorii, wskaźnikiem liczby fiszek do powtórki, przyciskiem "Rozpocznij naukę"
- **Obsługiwane interakcje**: Kliknięcie w przycisk rozpoczęcia nauki
- **Obsługiwana walidacja**: Sprawdzenie czy categoriId > 0 i flashcardsToReview > 0
- **Typy**: CategoryWithStudyStats
- **Propsy**: category: CategoryWithStudyStats, onStartStudy: (Long) -> Unit

### StudyScreen
- **Opis komponentu**: Główny ekran sesji nauki z prezentacją fiszek i oceną odpowiedzi
- **Główne elementy**: TopBar z postępem, Card z fiszką (swipeable), przyciski "Pokaż odpowiedź", "Dobrze", "Źle", przycisk "Zakończ sesję"
- **Obsługiwane interakcje**:
  - Wyświetlenie odpowiedzi na fiszkę
  - Ocena odpowiedzi jako poprawna/błędna
  - Przechodzenie do następnej fiszki
  - Zakończenie sesji w dowolnym momencie
- **Obsługiwana walidacja**:
  - Sprawdzenie czy currentFlashcardIndex < flashcards.size
  - Walidacja flashcardId przed oceną
  - Sprawdzenie czy użytkownik może ocenić fiszkę (fiszka należy do użytkownika)
- **Typy**: StudySessionState, Flashcard, StudyAction
- **Propsy**: categoryId: Long, currentUser: User, onNavigateToSummary: (StudySessionStats) -> Unit, onNavigateBack: () -> Unit

### FlashcardStudyCard
- **Opis komponentu**: Interaktywna karta fiszki z animacją obrotu między pytaniem a odpowiedzią
- **Główne elementy**: Card z animowanym przejściem, tekst pytania/odpowiedzi, przycisk "Pokaż odpowiedź"
- **Obsługiwane interakcje**: Obrót karty po kliknięciu "Pokaż odpowiedź", swipe gestures
- **Obsługiwana walidacja**: Sprawdzenie czy question.isNotBlank() i answer.isNotBlank()
- **Typy**: Flashcard, Boolean (isAnswerVisible)
- **Propsy**: flashcard: Flashcard, isAnswerVisible: Boolean, onShowAnswer: () -> Unit

### StudyControlsSection
- **Opis komponentu**: Sekcja z przyciskami do oceny odpowiedzi i kontroli sesji
- **Główne elementy**: Row z przyciskami "Dobrze", "Źle", "Zakończ sesję"
- **Obsługiwane interakcje**: Ocena odpowiedzi, zakończenie sesji
- **Obsługiwana walidacja**: Sprawdzenie czy można ocenić fiszkę (odpowiedź jest widoczna)
- **Typy**: Boolean (canEvaluate)
- **Propsy**: canEvaluate: Boolean, onCorrectAnswer: () -> Unit, onIncorrectAnswer: () -> Unit, onEndSession: () -> Unit

### StudySummaryScreen
- **Opis komponentu**: Ekran podsumowania zakończonej sesji nauki z statystykami i wykresami
- **Główne elementy**: LazyColumn ze statystykami sesji, wykresami kołowymi/słupkowymi, przyciskami akcji
- **Obsługiwane interakcje**: Powrót do nauki, zakończenie i powrót do głównego ekranu
- **Obsługiwana walidacja**: Sprawdzenie czy sessionStats.totalCards > 0
- **Typy**: StudySessionStats
- **Propsy**: categoryId: Long, sessionStats: StudySessionStats, onReturnToStudy: () -> Unit, onFinish: () -> Unit

### StudyStatsCard
- **Opis komponentu**: Karta z podstawowymi statystykami sesji nauki
- **Główne elementy**: Card z liczbą przerobionych fiszek, poprawnych/błędnych odpowiedzi, czasem sesji
- **Obsługiwane interakcje**: Brak
- **Obsługiwana walidacja**: Sprawdzenie czy wszystkie wartości >= 0
- **Typy**: StudySessionStats
- **Propsy**: stats: StudySessionStats

## 5. Typy

```kotlin
data class CategoryWithStudyStats(
    val categoryId: Long,
    val name: String,
    val totalFlashcards: Int,
    val flashcardsToReview: Int, // statusy 0-2
    val newFlashcards: Int, // status 0
    val reviewFlashcards: Int // statusy 1-2
)

data class StudySessionState(
    val categoryId: Long,
    val flashcards: List<Flashcard>,
    val currentIndex: Int = 0,
    val isAnswerVisible: Boolean = false,
    val sessionStats: StudySessionStats = StudySessionStats()
)

data class StudySessionStats(
    val totalCards: Int = 0,
    val completedCards: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val sessionStartTime: Long = System.currentTimeMillis(),
    val sessionEndTime: Long? = null
) {
    val sessionDurationMinutes: Int
        get() = ((sessionEndTime ?: System.currentTimeMillis()) - sessionStartTime) / (1000 * 60)
            .toInt()
    
    val accuracyPercentage: Float
        get() = if (completedCards > 0) (correctAnswers.toFloat() / completedCards) * 100 else 0f
}

data class StudyUiState(
    val isLoading: Boolean = false,
    val categories: List<CategoryWithStudyStats> = emptyList(),
    val error: String? = null
)

sealed class StudyAction {
    object LoadCategories : StudyAction()
    data class StartStudy(val categoryId: Long) : StudyAction()
    object ShowAnswer : StudyAction()
    object CorrectAnswer : StudyAction()
    object IncorrectAnswer : StudyAction()
    object NextFlashcard : StudyAction()
    object EndSession : StudyAction()
}
```

## 6. Zarządzanie stanem

Każdy ekran będzie miał dedykowany ViewModel:

### StudySelectionViewModel
- **Stan**: `StudyUiState` z listą kategorii i statystykami nauki
- **Metody**: 
  - `loadCategories()` - pobiera kategorie z liczbą fiszek do powtórki
  - `getCategoryStudyStats(categoryId)` - oblicza statystyki fiszek dla kategorii

### StudyViewModel  
- **Stan**: `StudySessionState` z aktualną sesją nauki
- **Metody**:
  - `startStudySession(categoryId)` - inicjalizuje sesję nauki
  - `showAnswer()` - pokazuje odpowiedź na fiszkę
  - `evaluateAnswer(isCorrect)` - ocenia odpowiedź i aktualizuje status fiszki
  - `nextFlashcard()` - przechodzi do następnej fiszki
  - `endSession()` - kończy sesję i zapisuje statystyki

### StudySummaryViewModel
- **Stan**: `StudySessionStats` ze statystykami zakończonej sesji
- **Metody**: `saveLearningStatistics()` - zapisuje statystyki sesji do bazy

## 7. Integracja z bazą

### Wymagane wywołania DAO:

**FlashcardDao:**
- `getFlashcardsForReviewByCategory(userId, categoryId, currentDate)` - pobieranie fiszek do powtórki
- `updateFlashcardLearningStatus(flashcardId, newStatus, nextReviewDate, updateTime)` - aktualizacja statusu fiszki

**CategoryDao:**
- `getUserCategoriesWithLearningStats(userId)` - pobieranie kategorii ze statystykami nauki

**UserDao:**
- `incrementTotalCardsReviewed(userId)` - inkrementacja liczby przejrzanych fiszek  
- `incrementCorrectAnswers(userId)` / `incrementIncorrectAnswers(userId)` - aktualizacja statystyk

**LearningStatisticsDao (nowy):**
- `insertLearningSession(session)` - zapis statystyk sesji nauki

### Typy żądań i odpowiedzi:
- **Żądanie**: `userId: Long, categoryId: Long, currentDate: Date`
- **Odpowiedź**: `Flow<List<Flashcard>>` - fiszki gotowe do powtórki
- **Aktualizacja statusu**: na podstawie algorytmu 3-5-7 (poprawna +1, błędna reset do 0)

## 8. Interakcje użytkownika

### StudySelectionScreen:
1. Użytkownik widzi listę kategorii z liczbą fiszek do powtórki
2. Kliknięcie w "Rozpocznij naukę" → nawigacja do StudyScreen z categoryId
3. Kategorie bez fiszek do powtórki mają zablokowany przycisk

### StudyScreen:
1. Użytkownik widzi pytanie z fiszki
2. Kliknięcie "Pokaż odpowiedź" → animacja obrotu karty, pojawia się odpowiedź
3. Kliknięcie "Dobrze" → status fiszki +1, przejście do następnej fiszki
4. Kliknięcie "Źle" → status fiszki reset do 0, przejście do następnej fiszki
5. Kliknięcie "Zakończ sesję" → nawigacja do StudySummaryScreen
6. Po przejrzeniu wszystkich fiszek → automatyczne przejście do podsumowania

### StudySummaryScreen:
1. Użytkownik widzi statystyki sesji z wykresami
2. Kliknięcie "Wróć do nauki" → powrót do StudySelectionScreen
3. Kliknięcie "Zakończ" → nawigacja do głównego ekranu

## 9. Warunki i walidacja

### StudySelectionScreen:
- **Warunek**: `flashcardsToReview > 0` - kategoria ma fiszki do powtórki
- **Walidacja**: Sprawdzanie czy użytkownik jest zalogowany (`currentUser != null`)
- **Efekt UI**: Przycisk "Rozpocznij naukę" jest nieaktywny dla kategorii bez fiszek

### StudyScreen:
- **Warunek**: `currentIndex < flashcards.size` - istnieją fiszki do nauki
- **Warunek**: `isAnswerVisible` - użytkownik może ocenić odpowiedź tylko po jej wyświetleniu
- **Walidacja**: Sprawdzanie czy fiszka należy do użytkownika przed aktualizacją statusu
- **Efekt UI**: Przyciski oceny są aktywne tylko po pokazaniu odpowiedzi

### StudySummaryScreen:
- **Warunek**: `sessionStats.completedCards > 0` - sesja zawiera przejrzane fiszki
- **Walidacja**: Sprawdzanie poprawności statystyk przed wyświetleniem
- **Efekt UI**: Wykresy są wyświetlane tylko przy danych > 0

## 10. Obsługa błędów

### Potencjalne błędy i rozwiązania:

1. **Brak fiszek do powtórki**: 
   - Wyświetlenie EmptyStudyState z informacją o braku fiszek
   - Sugestia dodania nowych fiszek lub przywrócenia nauczonych

2. **Błąd ładowania kategorii**:
   - Wyświetlenie komunikatu błędu z przyciskiem "Spróbuj ponownie"
   - Logowanie błędu do systemu

3. **Błąd podczas aktualizacji statusu fiszki**:
   - Rollback lokalnego stanu
   - Wyświetlenie Toast z informacją o błędzie
   - Możliwość ponowienia operacji

4. **Utrata połączenia z bazą danych**:
   - Buforowanie zmian lokalnie
   - Automatyczna synchronizacja po przywróceniu połączenia
   - Informowanie użytkownika o stanie offline

5. **Nieprawidłowe dane sesji**:
   - Walidacja danych przed zapisem
   - Domyślne wartości dla brakujących statystyk
   - Logowanie anomalii do debugowania

## 11. Kroki implementacji

1. **Przygotowanie struktur danych**
   - Utworzenie modeli: CategoryWithStudyStats, StudySessionState, StudySessionStats
   - Dodanie nowych routes do Routes.kt
   - Rozszerzenie FlashyFishkiNavigation.kt o routes modułu nauki

2. **Implementacja logiki biznesowej**
   - Utworzenie StudyRepository z metodami do zarządzania sesjami nauki
   - Implementacja algorytmu aktualizacji statusów fiszek (3-5-7)
   - Dodanie metod do obliczania statystyk nauki

3. **Implementacja ViewModels**
   - StudySelectionViewModel - zarządzanie wyborem kategorii
   - StudyViewModel - zarządzanie sesją nauki  
   - StudySummaryViewModel - zarządzanie podsumowaniem

4. **Implementacja komponentów UI**
   - CategoryStudyCard - karta kategorii z informacjami o nauce
   - FlashcardStudyCard - interaktywna karta fiszki z animacjami
   - StudyControlsSection - przyciski sterowania sesją
   - StudyStatsCard - wyświetlanie statystyk

5. **Implementacja ekranów głównych**
   - StudySelectionScreen - lista kategorii do nauki
   - StudyScreen - główna sesja nauki z fiszkami
   - StudySummaryScreen - podsumowanie z wykresami i statystykami

6. **Integracja z nawigacją**
   - Dodanie composable dla każdego ekranu w FlashyFishkiNavigation
   - Konfiguracja przechodzenia między ekranami z odpowiednimi parametrami
   - Obsługa Back Stack i popUpTo dla właściwego flow nawigacji

7. **Dodanie animacji i UX**
   - Animacja obrotu karty przy pokazywaniu odpowiedzi
   - Płynne przejścia między fiszkami
   - Progress indicator dla sesji nauki
   - Loading states i error handling

8. **Testowanie i optymalizacja**
   - Testy jednostkowe ViewModels
   - Testy UI komponentów
   - Testy integracyjne flow nauki
   - Optymalizacja wydajności dla dużych zestawów fiszek

9. **Integracja z istniejącymi ekranami**
   - Dodanie przycisków nawigacji do modułu nauki w HomeScreen
   - Linki do rozpoczęcia nauki z CategoriesListScreen
   - Integracja statystyk nauki z przyszłym modułem raportów

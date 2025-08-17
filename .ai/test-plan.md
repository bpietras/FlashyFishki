<analiza_projektu>

**1. Kluczowe komponenty projektu:**
- **Aplikacja mobilna edukacyjna Android** wykorzystująca fiszki do nauki z algorytmem powtórek rozłożonych w czasie (3-5-7)
- **Warstwa danych**: Room database z SQLite, zawiera encje User, Category, Flashcard, LearningStatistics
- **Warstwa UI**: Jetpack Compose z Navigation Compose, zawiera moduły: Auth, Home, Flashcards, Study, Reports
- **Architektura**: Clean Architecture z MVI pattern, ViewModels z Hilt DI
- **Repozytoria**: AuthRepository, StudyRepository do zarządzania danymi
- **System uwierzytelniania**: email/hasło z hashowaniem

**2. Specyfika stosu technologicznego i wpływ na strategię testowania:**
- **Kotlin 2.0.0**: Współrutyny wymagają testowania asynchronicznego z kotlinx-coroutines-test
- **Jetpack Compose**: Wymaga testów UI z Compose Testing oraz testów snapshot
- **Room Database**: Potrzebne testy DAOs z in-memory database
- **Hilt DI**: Wymaga testowania z TestHiltRunner i mockowaniem zależności
- **Navigation Compose**: Testy nawigacji między ekranami
- **Spaced Repetition Algorithm**: Krytyczne testy logiki algorytmu 3-5-7

**3. Priorytety testowe bazujące na strukturze repozytorium:**
1. **Wysoki priorytet**: AuthRepository (bezpieczeństwo), StudyRepository (core logic), ViewModels (business logic)
2. **Średni priorytet**: DAOs (data access), Navigation (user flow), UI Screens (user experience)
3. **Niski priorytet**: Utils (helper functions), Theme/Styling, Animations

**4. Potencjalne obszary ryzyka wymagające szczególnej uwagi:**
- **Bezpieczeństwo**: Hashowanie haseł, walidacja danych wejściowych
- **Algorytm nauki**: Logika spaced repetition 3-5-7, aktualizacja statusów fiszek
- **Persistence danych**: Integralność bazy danych, migracje
- **Asynchroniczność**: Race conditions w ViewModels, obsługa Flow/StateFlow
- **Performance**: Lazy loading fiszek, cache'owanie danych
- **UI State Management**: Synchronizacja stanu między komponentami

</analiza_projektu>

<plan_testów>

# Plan Testów dla Aplikacji FlashyFishki

## 1. Wprowadzenie i cele testowania

### 1.1 Cel dokumentu
Niniejszy dokument określa kompleksową strategię testowania dla aplikacji FlashyFishki - mobilnej aplikacji edukacyjnej wykorzystującej system fiszek z algorytmem powtórek rozłożonych w czasie.

### 1.2 Cele testowania
- **Zapewnienie jakości**: Weryfikacja poprawności funkcjonalności zgodnie z wymaganiami
- **Bezpieczeństwo**: Walidacja mechanizmów uwierzytelniania i hashowania haseł
- **Stabilność**: Testowanie algorytmu spaced repetition i integralności danych
- **Performance**: Sprawdzenie wydajności ładowania i nawigacji
- **UX**: Weryfikacja płynności interfejsu użytkownika w Jetpack Compose

## 2. Zakres testów

### 2.1 W zakresie testów
- Wszystkie moduły UI (Auth, Home, Flashcards, Study, Reports)
- Warstwa danych (DAOs, Repository, Database)
- ViewModels z logiką biznesową
- Algorytm spaced repetition 3-5-7
- System uwierzytelniania i bezpieczeństwa
- Nawigacja między ekranami
- Walidacja danych wejściowych

### 2.2 Poza zakresem testów (MVP)
- Testy wydajności pod obciążeniem
- Testy bezpieczeństwa penetracyjne
- Testy dostępności (accessibility)
- Testy na różnych wersjach Androida poniżej API 34

## 3. Typy testów do przeprowadzenia

### 3.1 Testy jednostkowe (Unit Tests)
**Zakres**: 70% pokrycia testami
- ViewModels (AuthViewModel, CategoriesViewModel, StudyViewModel)
- Repository classes (AuthRepository, StudyRepository)
- Utility classes (PasswordUtils, ValidationUtils)
- Business logic (spaced repetition algorithm)

**Narzędzia**: JUnit 4, MockK, kotlinx-coroutines-test

### 3.2 Testy integracyjne (Integration Tests)
**Zakres**: Integracja między warstwami
- Room Database + DAOs
- Repository + DAO integration
- ViewModel + Repository integration
- Navigation flow tests

**Narzędzia**: AndroidX Test, Room Testing, Hilt Testing

### 3.3 Testy UI (UI Tests)
**Zakres**: Kluczowe scenariusze użytkownika
- Authentication flow
- Flashcard creation and management
- Study session flow
- Navigation between screens

**Narzędzia**: Compose Testing, Espresso Core, AndroidX JUnit

### 3.4 Testy End-to-End (E2E)
**Zakres**: Pełne przepływy użytkownika
- Complete user registration and login
- Full study session workflow
- Data persistence across app restarts

## 4. Scenariusze testowe dla kluczowych funkcjonalności

### 4.1 Moduł uwierzytelniania
**Scenariusze pozytywne:**
- Rejestracja nowego użytkownika z poprawnymi danymi
- Logowanie z istniejącymi poprawnymi danymi
- Wylogowanie użytkownika

**Scenariusze negatywne:**
- Rejestracja z istniejącym adresem email
- Logowanie z nieprawidłowym hasłem
- Rejestracja z niewłaściwym formatem email
- Rejestracja ze słabym hasłem

**Scenariusze graniczne:**
- Maksymalna długość hasła
- Specjalne znaki w haśle
- Wielokrotne próby logowania

### 4.2 Zarządzanie fiszkami
**Scenariusze pozytywne:**
- Tworzenie nowej kategorii
- Dodawanie fiszki do kategorii
- Edycja istniejącej fiszki
- Usuwanie fiszki
- Eksport fiszek do Markdown

**Scenariusze negatywne:**
- Tworzenie kategorii z istniejącą nazwą
- Dodawanie fiszki z pustymi polami
- Usuwanie kategorii z istniejącymi fiszkami

**Scenariusze graniczne:**
- Maksymalna długość pytania i odpowiedzi
- Limit 1000 fiszek na użytkownika
- Filtrowanie dużej ilości fiszek

### 4.3 System nauki (Spaced Repetition)
**Scenariusze pozytywne:**
- Rozpoczęcie sesji nauki
- Poprawna odpowiedź na fiszkę
- Niepoprawna odpowiedź na fiszkę
- Ukończenie sesji nauki

**Scenariusze algorytmu 3-5-7:**
- Pierwsza powtórka po 3 dniach
- Druga powtórka po 5 dniach
- Trzecia powtórka po 7 dniach
- Reset algorytmu przy błędnej odpowiedzi

**Scenariusze graniczne:**
- Nauka bez dostępnych fiszek
- Wszystkie fiszki nauczone
- Sesja nauki przerwana

### 4.4 Nawigacja i UI
**Scenariusze nawigacji:**
- Przejście z ekranu logowania do głównego
- Nawigacja między modułami przez Bottom Navigation
- Powrót z detali fiszki do listy
- Deep linking do konkretnej fiszki

**Scenariusze UI:**
- Responsywność interfejsu
- Animacje przejść
- Obsługa gestów
- Zachowanie przy obracaniu ekranu

## 5. Środowisko testowe

### 5.1 Konfiguracja środowiska
- **Emulator**: Android API 34 (Android 14)
- **Urządzenia fizyczne**: Minimum 2 różne modele
- **Database**: In-memory database dla testów
- **Dependencies**: Test doubles dla zewnętrznych zależności

### 5.2 Dane testowe
- **Test users**: Przygotowane konta testowe
- **Sample flashcards**: Zestaw fiszek testowych
- **Categories**: Różne kategorie do testowania
- **Learning states**: Fiszki w różnych stanach nauki

## 6. Narzędzia do testowania

### 6.1 Narzędzia automatyczne
- **JUnit 4**: Framework testów jednostkowych
- **MockK**: Mockowanie obiektów Kotlin
- **kotlinx-coroutines-test**: Testowanie współrutyn
- **AndroidX Test**: Testy integracyjne Android
- **Compose Testing**: Testowanie UI Compose
- **Espresso Core**: Testy UI instrumentowane
- **Hilt Testing**: Testowanie z dependency injection

### 6.2 Narzędzia CI/CD
- **GitHub Actions**: Automatyczne uruchamianie testów
- **Jacoco**: Pomiar pokrycia kodu
- **Gradle**: Build i uruchamianie testów

### 6.3 Narzędzia manualne
- **Android Studio**: IDE do developmentu i debugowania
- **ADB**: Android Debug Bridge
- **Scrcpy**: Screen mirroring dla testów manualnych

## 7. Harmonogram testów

### 7.1 Faza 1: Testy jednostkowe (Tygodnie 1-2)
- Implementacja testów dla wszystkich ViewModels
- Testy Repository classes
- Testy utility functions
- Testy walidacji

### 7.2 Faza 2: Testy integracyjne (Tygodnie 3-4)
- Testy Room Database
- Testy integracji Repository-DAO
- Testy Hilt DI configuration
- Testy Navigation

### 7.3 Faza 3: Testy UI (Tygodnie 5-6)
- Testy Compose UI components
- Testy user flows
- Testy responsywności
- Testy accessibility podstawowe

### 7.4 Faza 4: Testy E2E i optymalizacja (Tydzień 7)
- Pełne scenariusze użytkownika
- Performance testing
- Bug fixing i optymalizacja
- Dokumentacja wyników

## 8. Kryteria akceptacji testów

### 8.1 Pokrycie kodu
- **Unit tests**: Minimum 80% pokrycia dla warstwy logiki biznesowej
- **Integration tests**: 100% krytycznych ścieżek danych
- **UI tests**: 100% głównych user flows

### 8.2 Kryteria funkcjonalne
- Wszystkie scenariusze pozytywne przechodzą
- Wszystkie scenariusze graniczne obsłużone
- Proper error handling dla scenariuszy negatywnych
- Performance requirements spełnione (cold start < 5s, warm start < 2s)

### 8.3 Kryteria jakości
- Zero błędów krytycznych (crash, data corruption)
- Maksymalnie 3 błędy średnie na moduł
- Zgodność z Material 3 guidelines
- Proper accessibility labels

## 9. Role i odpowiedzialności w procesie testowania

### 9.1 QA Engineer (Lead)
- **Odpowiedzialności**:
    - Planowanie i koordynacja testów
    - Tworzenie scenariuszy testowych
    - Wykonywanie testów manualnych
    - Raportowanie i śledzenie błędów
    - Akceptacja deliverables

### 9.2 Android Developer
- **Odpowiedzialności**:
    - Implementacja testów jednostkowych
    - Implementacja testów integracyjnych
    - Code review testów
    - Fixing zgłoszonych błędów
    - Dokumentacja techniczna testów

### 9.3 DevOps Engineer
- **Odpowiedzialności**:
    - Konfiguracja CI/CD pipeline
    - Automatyzacja uruchamiania testów
    - Monitoring pokrycia kodu
    - Deployment środowisk testowych

### 9.4 Product Owner
- **Odpowiedzialności**:
    - Definicja kryteriów akceptacji
    - Priorytetyzacja scenariuszy testowych
    - Akceptacja funkcjonalności
    - Decyzje o release readiness

## 10. Procedury raportowania błędów

### 10.1 Klasyfikacja błędów
**Krytyczne (P1)**:
- Crash aplikacji
- Utrata danych użytkownika
- Niemożność logowania
- Bezpieczeństwo danych

**Wysokie (P2)**:
- Funkcjonalność nie działa zgodnie z wymaganiami
- Algorytm spaced repetition niepoprawny
- Problemy z nawigacją

**Średnie (P3)**:
- Problemy UI/UX
- Drobne błędy walidacji
- Performance issues

**Niskie (P4)**:
- Kosmetyczne błędy UI
- Małe inconsistencies

### 10.2 Template raportu błędu
```markdown
**ID błędu**: BUG-YYYY-MM-DD-XXX
**Tytuł**: [Krótki opis błędu]
**Priorytet**: P1/P2/P3/P4
**Moduł**: Auth/Flashcards/Study/Home/Reports
**Środowisko**: Emulator/Device, Android version
**Kroki reprodukcji**:
1. [Krok 1]
2. [Krok 2]
3. [Krok 3]
**Oczekiwany rezultat**: [Co powinno się stać]
**Aktualny rezultat**: [Co się rzeczywiście stało]
**Screenshoty/Logi**: [Załączniki]
**Dodatkowe informacje**: [Jeśli potrzebne]
```

### 10.3 Workflow obsługi błędów
1. **Zgłoszenie**: QA Engineer tworzy ticket w systemie
2. **Triaging**: Team Lead przypisuje priorytet i developera
3. **Investigation**: Developer analizuje i estymuje effort
4. **Development**: Implementacja fix'a
5. **Testing**: QA weryfikuje naprawę
6. **Regression**: Sprawdzenie czy fix nie złamał niczego innego
7. **Closure**: Zamknięcie ticketu po akceptacji

### 10.4 SLA dla błędów
- **P1 (Krytyczne)**: 24 godziny
- **P2 (Wysokie)**: 3 dni robocze
- **P3 (Średnie)**: 1 tydzień
- **P4 (Niskie)**: Następny sprint

### 10.5 Narzędzia
- **Bug tracking**: GitHub Issues z labels
- **Test management**: GitHub Projects
- **Communication**: Slack/Teams dla urgent issues
- **Documentation**: Confluence/Wiki dla procedur

---

*Plan testów zostanie aktualizowany w miarę rozwoju projektu i pojawiania się nowych wymagań.*

</plan_testów>
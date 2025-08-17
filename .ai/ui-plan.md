# Architektura UI dla FlashyFishki

## 1. Przegląd struktury UI

Architektura UI aplikacji FlashyFishki jest zorganizowana wokół dwóch głównych grafów nawigacyjnych: grafu autentykacji oraz grafu głównego. Graf autentykacji odpowiada za logowanie i rejestrację użytkowników, natomiast graf główny zawiera pięć kluczowych modułów funkcjonalnych dostępnych przez Bottom Navigation Bar: Profil, Moje Fiszki, Nauka, Biblioteka Publiczna i Raporty.

Architektura jest zbudowana w oparciu o Jetpack Compose, wykorzystując reaktywne podejście do UI oparte na stanie aplikacji. Wykorzystuje wzorzec ViewModel do zarządzania stanem i komunikacji z warstwą repozytoriów, które z kolei komunikują się z DAO dla operacji bazodanowych. Aplikacja implementuje Material 3 Design dla spójnego i nowoczesnego wyglądu interfejsu.

## 2. Lista widoków

### Graf autentykacji

#### Ekran logowania (LoginScreen)
- **Ścieżka widoku**: auth/login
- **Główny cel**: Umożliwienie użytkownikowi zalogowania się do aplikacji
- **Kluczowe informacje**: Pola do wprowadzenia adresu e-mail i hasła, opcja przejścia do ekranu rejestracji
- **Kluczowe komponenty**: 
  - TextField dla adresu e-mail z walidacją
  - PasswordField dla hasła
  - Button "Zaloguj się"
  - TextButton "Zarejestruj się"
- **Względy UX/dostępność/bezpieczeństwo**: Walidacja wprowadzanych danych, obsługa błędów logowania, widoczne informacje o błędach

#### Ekran rejestracji (RegisterScreen)
- **Ścieżka widoku**: auth/register
- **Główny cel**: Umożliwienie utworzenia nowego konta użytkownika
- **Kluczowe informacje**: Pola do wprowadzenia adresu e-mail i hasła, walidacja siły hasła
- **Kluczowe komponenty**: 
  - TextField dla adresu e-mail z walidacją
  - PasswordField z wskaźnikiem siły hasła
  - Button "Zarejestruj się"
  - TextButton "Wróć do logowania"
- **Względy UX/dostępność/bezpieczeństwo**: Walidacja formatu e-maila i siły hasła, wyraźne komunikaty o błędach

### Graf główny

#### Moduł: Profil

##### Profil użytkownika (ProfileScreen)
- **Ścieżka widoku**: main/profile
- **Główny cel**: Wyświetlanie informacji o koncie użytkownika i dostępie do ustawień
- **Kluczowe informacje**: E-mail użytkownika, data ostatniego logowania, wskaźnik wykorzystania limitu fiszek
- **Kluczowe komponenty**: 
  - Card z informacjami o użytkowniku
  - LinearProgressIndicator pokazujący wykorzystanie limitu 1000 fiszek
  - Button "Wyloguj się"
  - Statystyki użytkownika
- **Względy UX/dostępność/bezpieczeństwo**: Wyraźne przedstawienie limitu fiszek, czytelna opcja wylogowania

#### Moduł: Moje Fiszki

##### Lista kategorii (CategoriesListScreen)
- **Ścieżka widoku**: main/categories
- **Główny cel**: Wyświetlanie listy kategorii fiszek utworzonych przez użytkownika
- **Kluczowe informacje**: Nazwy kategorii, liczba fiszek w każdej kategorii z podziałem na statusy nauki
- **Kluczowe komponenty**: 
  - LazyColumn z Card dla każdej kategorii
  - Ikona i licznik fiszek dla każdego statusu nauki
  - FloatingActionButton do dodawania nowej kategorii
- **Względy UX/dostępność/bezpieczeństwo**: Kolorowe oznaczenia statusów nauki (szary - nowe, żółty - pierwsza powtórka, niebieski - druga powtórka, zielony - nauczone)

##### Lista fiszek w kategorii (CategoryFlashcardsScreen)
- **Ścieżka widoku**: main/categories/{categoryId}/flashcards
- **Główny cel**: Wyświetlanie fiszek z wybranej kategorii
- **Kluczowe informacje**: Pytania, fragmenty odpowiedzi, status nauki, poziom trudności
- **Kluczowe komponenty**: 
  - LazyColumn z Card dla każdej fiszki
  - Kolorowe oznaczenia statusów nauki
  - FloatingActionButton do dodawania nowej fiszki
  - TopAppBar z nazwą kategorii i opcjami menu (eksport, filtrowanie)
- **Względy UX/dostępność/bezpieczeństwo**: Sortowanie według daty utworzenia, wyraźne oznaczenia statusów kolorami

##### Szczegóły fiszki (FlashcardDetailsScreen)
- **Ścieżka widoku**: main/flashcards/{flashcardId}
- **Główny cel**: Wyświetlanie pełnych informacji o fiszce
- **Kluczowe informacje**: Pytanie, odpowiedź, kategoria, poziom trudności, status nauki
- **Kluczowe komponenty**: 
  - Card z pełną treścią fiszki
  - Informacje o kategorii i poziomie trudności
  - Przyciski do edycji i usunięcia
  - Opcja przywrócenia do nauki dla nauczonych fiszek (status 3)
- **Względy UX/dostępność/bezpieczeństwo**: Potwierdzenie przed usunięciem fiszki

##### Tworzenie/edycja fiszki (FlashcardEditScreen)
- **Ścieżka widoku**: main/flashcards/new, main/flashcards/{flashcardId}/edit
- **Główny cel**: Tworzenie nowej lub edycja istniejącej fiszki
- **Kluczowe informacje**: Pola do wprowadzenia pytania i odpowiedzi, wybór kategorii, ustawienie trudności
- **Kluczowe komponenty**: 
  - TextField dla pytania z licznikiem znaków (max 500)
  - TextField dla odpowiedzi z licznikiem znaków (max 1000)
  - Dropdown do wyboru kategorii
  - Slider do ustawienia poziomu trudności (1-5)
  - Switch do oznaczenia fiszki jako publicznej
  - Przyciski "Zapisz" i "Anuluj"
- **Względy UX/dostępność/bezpieczeństwo**: Walidacja długości tekstu, wyświetlanie licznika pozostałych znaków

##### Eksport fiszek (ExportScreen)
- **Ścieżka widoku**: main/categories/{categoryId}/export
- **Główny cel**: Eksport fiszek z wybranej kategorii do formatu .md
- **Kluczowe informacje**: Kategoria do eksportu, liczba fiszek
- **Kluczowe komponenty**: 
  - Informacje o wybranej kategorii
  - Button "Eksportuj"
  - Wskaźnik postępu podczas eksportu
- **Względy UX/dostępność/bezpieczeństwo**: Informacja o postępie eksportu, komunikat o zakończeniu

#### Moduł: Nauka

##### Wybór kategorii do nauki (StudySelectionScreen)
- **Ścieżka widoku**: main/study
- **Główny cel**: Wybór kategorii fiszek do nauki
- **Kluczowe informacje**: Lista kategorii, liczba fiszek do powtórki w każdej kategorii
- **Kluczowe komponenty**: 
  - LazyColumn z Card dla każdej kategorii
  - Liczba fiszek do powtórki dla każdej kategorii
  - Button "Rozpocznij naukę" dla każdej kategorii
- **Względy UX/dostępność/bezpieczeństwo**: Wyraźne oznaczenie kategorii bez fiszek do nauki, blokowanie rozpoczęcia nauki dla pustych kategorii

##### Ekran nauki (StudyScreen)
- **Ścieżka widoku**: main/study/{categoryId}
- **Główny cel**: Prezentacja fiszek do nauki i ocena odpowiedzi
- **Kluczowe informacje**: Pytanie, odpowiedź (po obróceniu karty), opcje oceny
- **Kluczowe komponenty**: 
  - Swipeable Card z animacją obrotu między pytaniem a odpowiedzią
  - Button "Pokaż odpowiedź"
  - Przyciski "Dobrze" i "Źle" do oceny odpowiedzi
  - LinearProgressIndicator pokazujący postęp sesji
  - Button "Zakończ sesję"
- **Względy UX/dostępność/bezpieczeństwo**: Animacja obrotu karty, wyraźne przyciski oceny

##### Podsumowanie sesji nauki (StudySummaryScreen)
- **Ścieżka widoku**: main/study/{categoryId}/summary
- **Główny cel**: Prezentacja wyników zakończonej sesji nauki
- **Kluczowe informacje**: Liczba przerobionych fiszek, liczba poprawnych i błędnych odpowiedzi
- **Kluczowe komponenty**: 
  - Statystyki sesji
  - Wykresy słupkowe i kołowe dla poprawnych/błędnych odpowiedzi
  - Button "Wróć do nauki" i "Zakończ"
- **Względy UX/dostępność/bezpieczeństwo**: Czytelna prezentacja statystyk, intuicyjne opcje dalszych działań

#### Moduł: Biblioteka Publiczna

##### Lista publicznych fiszek (PublicLibraryScreen)
- **Ścieżka widoku**: main/public-library
- **Główny cel**: Przeglądanie fiszek udostępnionych przez innych użytkowników
- **Kluczowe informacje**: Pytania, kategorie, poziomy trudności
- **Kluczowe komponenty**: 
  - LazyColumn z Card dla każdej publicznej fiszki
  - Informacje o kategorii i poziomie trudności
  - Button "Kopiuj" do skopiowania fiszki
  - Oznaczenie już skopiowanych fiszek
- **Względy UX/dostępność/bezpieczeństwo**: Wyraźne oznaczenie fiszek już skopiowanych

##### Filtrowanie i sortowanie publicznych fiszek (PublicLibraryFiltersScreen)
- **Ścieżka widoku**: main/public-library/filters
- **Główny cel**: Zawężanie listy publicznych fiszek według kryteriów
- **Kluczowe informacje**: Dostępne kategorie, poziomy trudności, opcje sortowania
- **Kluczowe komponenty**: 
  - ChipGroup dla filtrów kategorii
  - Slider do filtrowania po poziomie trudności
  - Dropdown do sortowania według daty
  - Button "Zastosuj filtry" i "Wyczyść filtry"
- **Względy UX/dostępność/bezpieczeństwo**: Intuicyjne filtry, przejrzysty interfejs

#### Moduł: Raporty

##### Raporty tygodniowe (WeeklyReportsScreen)
- **Ścieżka widoku**: main/reports/weekly
- **Główny cel**: Wyświetlanie statystyk nauki z ostatnich 7 dni
- **Kluczowe informacje**: Liczba przeglądanych fiszek, liczba poprawnych i błędnych odpowiedzi
- **Kluczowe komponenty**: 
  - BarChart dla statystyk przeglądanych fiszek
  - PieChart dla statystyk poprawnych/błędnych odpowiedzi
  - Tabs do przełączania między różnymi typami statystyk
- **Względy UX/dostępność/bezpieczeństwo**: Czytelne wykresy, intuicyjne przełączanie między widokami

##### Szczegółowe statystyki kategorii (CategoryStatisticsScreen)
- **Ścieżka widoku**: main/reports/categories/{categoryId}
- **Główny cel**: Wyświetlanie szczegółowych statystyk dla wybranej kategorii
- **Kluczowe informacje**: Statystyki dla wybranej kategorii, rozkład statusów nauki
- **Kluczowe komponenty**: 
  - BarChart dla statystyk w kategorii
  - PieChart dla rozkładu statusów nauki
  - Dropdown do wyboru innej kategorii
- **Względy UX/dostępność/bezpieczeństwo**: Przejrzysta prezentacja danych, intuicyjne przełączanie między kategoriami

### Dialogi i komponenty modalne

#### Dialog potwierdzenia usunięcia (DeleteConfirmationDialog)
- **Główny cel**: Potwierdzenie chęci usunięcia fiszki lub kategorii
- **Kluczowe komponenty**: 
  - AlertDialog z ostrzeżeniem
  - Przyciski "Usuń" i "Anuluj"
- **Względy UX/dostępność/bezpieczeństwo**: Wyraźne ostrzeżenie o konsekwencjach, rozróżnione kolorami przyciski

#### Komunikat o błędzie (ErrorMessage)
- **Główny cel**: Informowanie użytkownika o wystąpieniu błędu
- **Kluczowe komponenty**: 
  - Snackbar lub AlertDialog z informacją o błędzie
  - Button "OK" do zamknięcia
- **Względy UX/dostępność/bezpieczeństwo**: Jasny opis błędu, sugestie rozwiązania problemu

## 3. Mapa podróży użytkownika

### Podróż 1: Rejestracja i logowanie
1. Użytkownik uruchamia aplikację i widzi ekran logowania (LoginScreen)
2. Wybiera opcję rejestracji i przechodzi do ekranu rejestracji (RegisterScreen)
3. Wprowadza adres e-mail i hasło
4. Po pomyślnej rejestracji zostaje automatycznie zalogowany i przeniesiony do ekranu profilu (ProfileScreen)

### Podróż 2: Tworzenie i zarządzanie fiszkami
1. Użytkownik loguje się do aplikacji (LoginScreen)
2. Przechodzi do modułu "Moje Fiszki" przez Bottom Navigation Bar
3. Na ekranie listy kategorii (CategoriesListScreen) tworzy nową kategorię lub wybiera istniejącą
4. Po wybraniu kategorii przechodzi do listy fiszek w danej kategorii (CategoryFlashcardsScreen)
5. Tworzy nową fiszkę wybierając przycisk "+" (FloatingActionButton)
6. Na ekranie tworzenia fiszki (FlashcardEditScreen) wprowadza pytanie, odpowiedź, określa poziom trudności i ewentualnie oznacza fiszkę jako publiczną
7. Po zapisaniu fiszki wraca do listy fiszek w kategorii
8. Może edytować fiszkę wybierając ją z listy i przechodząc do ekranu szczegółów (FlashcardDetailsScreen), a następnie wybierając opcję edycji

### Podróż 3: Proces nauki
1. Użytkownik loguje się do aplikacji (LoginScreen)
2. Przechodzi do modułu "Nauka" przez Bottom Navigation Bar
3. Na ekranie wyboru kategorii do nauki (StudySelectionScreen) wybiera kategorię
4. Rozpoczyna sesję nauki na ekranie nauki (StudyScreen)
5. Przegląda pytanie na fiszce
6. Obraca kartę, aby zobaczyć odpowiedź
7. Ocenia swoją odpowiedź jako "Dobrze" lub "Źle"
8. Po zakończeniu sesji przegląda podsumowanie (StudySummaryScreen)

### Podróż 4: Korzystanie z biblioteki publicznej
1. Użytkownik loguje się do aplikacji (LoginScreen)
2. Przechodzi do modułu "Biblioteka Publiczna" przez Bottom Navigation Bar
3. Na ekranie biblioteki publicznej (PublicLibraryScreen) przegląda dostępne fiszki
4. Korzysta z filtrów (PublicLibraryFiltersScreen) aby znaleźć interesujące materiały
5. Kopiuje wybrane fiszki do własnej kolekcji

### Podróż 5: Analiza postępów nauki
1. Użytkownik loguje się do aplikacji (LoginScreen)
2. Przechodzi do modułu "Raporty" przez Bottom Navigation Bar
3. Na ekranie raportów tygodniowych (WeeklyReportsScreen) przegląda ogólne statystyki z ostatnich 7 dni
4. Wybiera konkretną kategorię, aby zobaczyć szczegółowe statystyki (CategoryStatisticsScreen)

## 4. Układ i struktura nawigacji

Architektura nawigacji aplikacji FlashyFishki jest oparta na Navigation Compose i podzielona na dwa główne grafy:

### Graf autentykacji
Graf autentykacji zawiera ekrany logowania i rejestracji. Jest to pierwszy graf wyświetlany użytkownikowi, który nie jest jeszcze zalogowany.

```
AuthGraph
├── LoginScreen (ekran startowy)
└── RegisterScreen
```

### Graf główny
Graf główny jest dostępny po zalogowaniu i zawiera 5 głównych modułów, do których użytkownik ma dostęp przez Bottom Navigation Bar.

```
MainGraph
├── ProfileModule
│   └── ProfileScreen
│
├── MyFlashcardsModule
│   ├── CategoriesListScreen
│   ├── CategoryFlashcardsScreen
│   ├── FlashcardDetailsScreen
│   ├── FlashcardEditScreen
│   └── ExportScreen
│
├── StudyModule
│   ├── StudySelectionScreen
│   ├── StudyScreen
│   └── StudySummaryScreen
│
├── PublicLibraryModule
│   ├── PublicLibraryScreen
│   └── PublicLibraryFiltersScreen
│
└── ReportsModule
    ├── WeeklyReportsScreen
    └── CategoryStatisticsScreen
```

Struktura nawigacji wykorzystuje:
- `NavHost` jako kontener dla każdego grafu
- `NavController` do zarządzania nawigacją między ekranami
- Bottom Navigation Bar do nawigacji między głównymi modułami
- TopAppBar z przyciskiem powrotu dla nawigacji wewnątrz modułów

## 5. Kluczowe komponenty

### FlashcardCard
- **Opis**: Komponent reprezentujący fiszkę w formie karty
- **Zastosowanie**: Lista fiszek, ekran nauki
- **Funkcjonalność**: Wyświetlanie pytania i odpowiedzi, oznaczenie statusu nauki, poziom trudności

### CategoryCard
- **Opis**: Komponent reprezentujący kategorię fiszek
- **Zastosowanie**: Lista kategorii, wybór kategorii do nauki
- **Funkcjonalność**: Wyświetlanie nazwy kategorii, liczby fiszek, statystyk nauki

### FlashcardForm
- **Opis**: Formularz do tworzenia i edycji fiszki
- **Zastosowanie**: Ekrany tworzenia i edycji fiszek
- **Funkcjonalność**: Pola dla pytania i odpowiedzi z licznikami znaków, wybór kategorii, ustawienie trudności

### LearningStatusIndicator
- **Opis**: Wskaźnik statusu nauki fiszki
- **Zastosowanie**: Lista fiszek, szczegóły fiszki
- **Funkcjonalność**: Kolorowe oznaczenie statusu nauki (szary, żółty, niebieski, zielony)

### FlippableCard
- **Opis**: Karta z animacją obrotu
- **Zastosowanie**: Ekran nauki
- **Funkcjonalność**: Animowana prezentacja pytania i odpowiedzi

### StatisticsChart
- **Opis**: Komponent wykresu do prezentacji statystyk
- **Zastosowanie**: Raporty, podsumowanie sesji nauki
- **Funkcjonalność**: Wyświetlanie wykresów słupkowych i kołowych dla statystyk nauki

### FilterChipGroup
- **Opis**: Grupa elementów wyboru dla filtrów
- **Zastosowanie**: Biblioteka publiczna, filtrowanie fiszek
- **Funkcjonalność**: Wybór i zastosowanie filtrów kategorii i trudności

### ConfirmationDialog
- **Opis**: Dialog potwierdzenia akcji
- **Zastosowanie**: Usuwanie fiszek, kategorii, wylogowanie
- **Funkcjonalność**: Prezentacja ostrzeżenia i opcji potwierdzenia/anulowania

### ErrorDisplay
- **Opis**: Komponent wyświetlający błędy
- **Zastosowanie**: Cała aplikacja
- **Funkcjonalność**: Prezentacja komunikatów o błędach w spójny sposób

### ProgressButton
- **Opis**: Przycisk z wskaźnikiem postępu
- **Zastosowanie**: Logowanie, rejestracja, eksport
- **Funkcjonalność**: Wyświetlanie aktywności podczas wykonywania operacji

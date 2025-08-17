# Plan implementacji widoków rejestracji i logowania

## 1. Przegląd

Widoki rejestracji i logowania stanowią punkt wejścia do aplikacji FlashyFishki. Umożliwiają użytkownikom utworzenie nowego konta oraz zalogowanie się do aplikacji w celu uzyskania dostępu do funkcjonalności związanych z fiszkami edukacyjnymi. Implementacja obejmuje dwa główne ekrany: LoginScreen (auth/login) i RegisterScreen (auth/register) z pełną walidacją danych, obsługą błędów i automatycznym logowaniem po rejestracji.

## 2. Routing widoku

- **LoginScreen**: `auth/login` - domyślny ekran przy pierwszym uruchomieniu aplikacji
- **RegisterScreen**: `auth/register` - dostępny z LoginScreen poprzez przycisk nawigacyjny
- **Nawigacja**: Navigation Compose z argumentami przekazywanymi między ekranami
- **Auth Graph**: Osobny graf nawigacyjny dla widoków autentykacji

## 3. Struktura komponentów

```
AuthNavigation (NavGraph)
├── LoginScreen (@Composable)
│   ├── AuthHeader (logo + tytuł)
│   ├── LoginForm
│   │   ├── EmailTextField
│   │   ├── PasswordTextField
│   │   └── LoginButton
│   ├── NavigationSection
│   │   └── RegisterNavigationButton
│   └── ErrorMessage (conditional)
└── RegisterScreen (@Composable)
    ├── AuthHeader (logo + tytuł)
    ├── RegisterForm
    │   ├── EmailTextField
    │   ├── PasswordTextField
    │   ├── PasswordStrengthIndicator
    │   └── RegisterButton
    ├── NavigationSection
    │   └── LoginNavigationButton
    └── ErrorMessage (conditional)
```

## 4. Szczegóły komponentów

### LoginScreen
- **Opis komponentu**: Główny ekran logowania umożliwiający wprowadzenie danych uwierzytelniających
- **Główne elementy**: Column z polami email/hasło, przycisk logowania, link do rejestracji
- **Obsługiwane interakcje**: wprowadzanie danych, walidacja, przesyłanie formularza, nawigacja do rejestracji
- **Obsługiwana walidacja**: format e-maila (regex), niepuste pola, długość hasła minimum 6 znaków
- **Typy**: LoginUiState, NavController, AuthViewModel
- **Propsy**: navController: NavController, authViewModel: AuthViewModel

### RegisterScreen
- **Opis komponentu**: Ekran rejestracji z rozszerzoną walidacją i wskaźnikiem siły hasła
- **Główne elementy**: Column z polami email/hasło, wskaźnik siły hasła, przycisk rejestracji, link do logowania
- **Obsługiwane interakcje**: wprowadzanie danych, walidacja w czasie rzeczywistym, przesyłanie formularza, nawigacja do logowania
- **Obsługiwana walidacja**: format e-maila (regex), siła hasła (długość, znaki specjalne, cyfry), unikalność e-maila
- **Typy**: RegisterUiState, PasswordStrength, NavController, AuthViewModel
- **Propsy**: navController: NavController, authViewModel: AuthViewModel

### EmailTextField
- **Opis komponentu**: Komponenent wielokrotnego użytku dla wprowadzania adresu e-mail z walidacją
- **Główne elementy**: OutlinedTextField z ikoną e-mail, error state, helper text
- **Obsługiwane interakcje**: onValueChange, onFocusChange dla walidacji
- **Obsługiwana walidacja**: regex dla formatu e-mail `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$`, niepuste pole
- **Typy**: String (value), Boolean (isError), String? (errorMessage)
- **Propsy**: value: String, onValueChange: (String) -> Unit, isError: Boolean, errorMessage: String?, modifier: Modifier

### PasswordTextField
- **Opis komponentu**: Komponenent wielokrotnego użytku dla wprowadzania hasła z opcją pokazywania/ukrywania
- **Główne elementy**: OutlinedTextField z ikoną hasła, ikona toggle visibility, error state
- **Obsługiwane interakcje**: onValueChange, onVisibilityToggle
- **Obsługiwana walidacja**: minimalna długość 6 znaków, dla rejestracji dodatkowa walidacja siły
- **Typy**: String (value), Boolean (isVisible, isError), String? (errorMessage)
- **Propsy**: value: String, onValueChange: (String) -> Unit, isVisible: Boolean, onVisibilityToggle: () -> Unit, isError: Boolean, errorMessage: String?, modifier: Modifier

### PasswordStrengthIndicator
- **Opis komponentu**: Wizualny wskaźnik siły hasła wyświetlany tylko w RegisterScreen
- **Główne elementy**: LinearProgressIndicator z kolorową skalą, tekst opisowy
- **Obsługiwane interakcje**: brak (tylko wyświetlanie)
- **Obsługiwana walidacja**: analiza siły hasła na podstawie długości, różnorodności znaków
- **Typy**: PasswordStrength enum
- **Propsy**: strength: PasswordStrength, modifier: Modifier

### AuthViewModel
- **Opis komponentu**: ViewModel zarządzający stanem autentykacji i logiką biznesową
- **Główne elementy**: StateFlow dla UI state, suspend functions dla operacji DAO
- **Obsługiwane interakcje**: login(), register(), updateEmail(), updatePassword(), clearErrors()
- **Obsługiwana walidacja**: koordynacja walidacji z komponentami UI
- **Typy**: LoginUiState, RegisterUiState, User?, AuthResult
- **Propsy**: userDao: UserDao (wstrzyknięte przez Hilt)

## 5. Typy

```kotlin
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmailError: Boolean = false,
    val isPasswordError: Boolean = false
)

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val passwordStrength: PasswordStrength = PasswordStrength.WEAK,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmailError: Boolean = false,
    val isPasswordError: Boolean = false
)

data class LoginCredentials(
    val email: String,
    val password: String
)

data class RegisterCredentials(
    val email: String,
    val password: String
)

enum class PasswordStrength(val label: String, val color: Color) {
    WEAK("Słabe", Color.Red),
    MEDIUM("Średnie", Color.Orange),
    STRONG("Silne", Color.Green)
}

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

data class AuthState(
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null
)
```

## 6. Zarządzanie stanem

Stan zarządzany jest przez **AuthViewModel** wykorzystujący:

- **StateFlow** dla reaktywnego UI state
- **MutableStateFlow** dla wewnętrznych aktualizacji stanu
- **Hilt Dependency Injection** dla wstrzykiwania UserDao
- **Coroutines** dla asynchronicznych operacji bazodanowych

**Główne metody AuthViewModel:**
- `loginUiState: StateFlow<LoginUiState>` - stan ekranu logowania
- `registerUiState: StateFlow<RegisterUiState>` - stan ekranu rejestracji  
- `authState: StateFlow<AuthState>` - globalny stan autentykacji
- `suspend fun login(credentials: LoginCredentials)` - logika logowania
- `suspend fun register(credentials: RegisterCredentials)` - logika rejestracji
- `fun updateLoginEmail(email: String)` - aktualizacja email w formularzu logowania
- `fun updateLoginPassword(password: String)` - aktualizacja hasła w formularzu logowania
- `fun updateRegisterEmail(email: String)` - aktualizacja email w formularzu rejestracji
- `fun updateRegisterPassword(password: String)` - aktualizacja hasła z kalkulacją siły
- `fun clearErrors()` - czyszczenie komunikatów błędów

## 7. Integracja z bazą

**Operacje UserDao wykorzystywane w autentykacji:**

### Rejestracja (register):
- **Żądanie**: RegisterCredentials
- **Operacje DAO**: 
  1. `getUserByEmail(email: String): User?` - sprawdzenie unikalności e-maila
  2. `insertUser(user: User): Long` - utworzenie nowego użytkownika
- **Odpowiedź**: AuthResult.Success(user) lub AuthResult.Error(message)

### Logowanie (login):
- **Żądanie**: LoginCredentials  
- **Operacje DAO**:
  1. `getUserByEmail(email: String): User?` - pobranie danych użytkownika
  2. `updateLastLoginDate(userId: Long, loginDate: Date)` - aktualizacja czasu logowania
- **Odpowiedź**: AuthResult.Success(user) lub AuthResult.Error(message)

**Haszowanie hasła**: Wykorzystanie `BCrypt` lub `MessageDigest` z solą dla bezpiecznego przechowywania.

## 8. Interakcje użytkownika

### LoginScreen:
1. **Wprowadzanie e-maila**: walidacja formatu w czasie rzeczywistym, wyświetlanie błędu pod polem
2. **Wprowadzanie hasła**: sprawdzanie minimalnej długości, toggle widoczności
3. **Kliknięcie "Zaloguj się"**: 
   - Walidacja wszystkich pól
   - Wyświetlenie loading state
   - Próba logowania przez ViewModel
   - Przekierowanie do głównego ekranu lub wyświetlenie błędu
4. **Kliknięcie "Zarejestruj się"**: nawigacja do RegisterScreen

### RegisterScreen:
1. **Wprowadzanie e-maila**: walidacja formatu, sprawdzanie unikalności przy blur
2. **Wprowadzanie hasła**: 
   - Aktualizacja wskaźnika siły hasła w czasie rzeczywistym
   - Walidacja minimalnych wymagań
3. **Kliknięcie "Zarejestruj się"**:
   - Walidacja wszystkich pól
   - Sprawdzenie siły hasła (minimum MEDIUM)
   - Próba rejestracji przez ViewModel
   - Automatyczne logowanie przy sukcesie
   - Przekierowanie do głównego ekranu lub wyświetlenie błędu
4. **Kliknięcie "Wróć do logowania"**: nawigacja do LoginScreen

## 9. Warunki i walidacja

### Walidacja e-maila (EmailTextField):
- **Warunek**: Format zgodny z regex `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$`
- **Komponenty**: Wykorzystywane w LoginScreen i RegisterScreen
- **Wpływ na UI**: Czerwona ramka pola, komunikat błędu pod polem, zablokowanie przycisku submit

### Walidacja hasła (PasswordTextField):
- **Warunki**: 
  - Minimalna długość 6 znaków
  - Dla rejestracji: co najmniej jedna cyfra, jedna litera
- **Komponenty**: LoginScreen (podstawowa), RegisterScreen (rozszerzona)
- **Wpływ na UI**: Komunikat błędu, wskaźnik siły hasła (tylko rejestracja)

### Walidacja unikalności e-maila (RegisterScreen):
- **Warunek**: E-mail nie istnieje w bazie danych
- **Sprawdzanie**: Przy próbie rejestracji w AuthViewModel
- **Wpływ na UI**: Komunikat błędu "E-mail już istnieje", focus na pole e-mail

### Walidacja siły hasła (RegisterScreen):
- **Warunki**:
  - WEAK: < 6 znaków lub tylko litery/cyfry
  - MEDIUM: 6-8 znaków z literami i cyframi
  - STRONG: > 8 znaków z literami, cyframi i znakami specjalnymi
- **Wpływ na UI**: Kolorowy wskaźnik, tekst opisowy, ostrzeżenie dla słabych haseł

## 10. Obsługa błędów

### Błędy walidacji:
- **E-mail**: "Nieprawidłowy format adresu e-mail"
- **Hasło**: "Hasło musi mieć co najmniej 6 znaków"
- **Słabe hasło**: "Hasło jest zbyt słabe. Dodaj cyfry i znaki specjalne"

### Błędy autentykacji:
- **Nieprawidłowe logowanie**: "Nieprawidłowy e-mail lub hasło"
- **E-mail już istnieje**: "Konto z tym adresem e-mail już istnieje"
- **Błąd połączenia z bazą**: "Wystąpił błąd. Spróbuj ponownie"

### Obsługa błędów w UI:
- **Snackbar** dla błędów globalnych (problemy z bazą danych)
- **Inline errors** pod polami formularza
- **Loading states** z możliwością anulowania
- **Retry mechanisms** dla błędów przejściowych

## 11. Kroki implementacji

1. **Utworzenie struktury pakietów**:
   - `ui/auth/` - komponenty autentykacji
   - `ui/auth/components/` - komponenty wielokrotnego użytku
   - `ui/auth/viewmodel/` - AuthViewModel
   - `ui/auth/navigation/` - nawigacja auth

2. **Implementacja typów danych**:
   - Utworzenie data classes dla UiState
   - Implementacja enum PasswordStrength
   - Definicja sealed class AuthResult

3. **Utworzenie komponentów wielokrotnego użytku**:
   - EmailTextField z walidacją
   - PasswordTextField z toggle visibility
   - PasswordStrengthIndicator

4. **Implementacja AuthViewModel**:
   - Definicja StateFlow dla UI states
   - Implementacja funkcji login() i register()
   - Integracja z UserDao przez Hilt
   - Obsługa haszowania hasła

5. **Utworzenie ekranów**:
   - LoginScreen z formularzem i walidacją
   - RegisterScreen z rozszerzoną walidacją
   - Integracja z AuthViewModel

6. **Konfiguracja nawigacji**:
   - Definicja auth navigation graph
   - Konfiguracja deep links
   - Integracja z główną nawigacją aplikacji

7. **Implementacja logiki bezpieczeństwa**:
   - Haszowanie hasła (BCrypt lub podobne)
   - Walidacja siły hasła
   - Zabezpieczenie przed atakami brute force

8. **Testowanie i walidacja**:
   - Unit testy dla AuthViewModel
   - UI testy dla komponentów
   - Integration testy dla pełnego flow
   - Testy wydajnościowe dla walidacji

9. **Optymalizacja i finalizacja**:
   - Optymalizacja re-compositions
   - Implementacja proper error handling
   - Dodanie accessibility features
   - Code review i refactoring

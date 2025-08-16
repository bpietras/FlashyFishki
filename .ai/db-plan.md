```markdown
# Schemat bazy danych SQLite dla aplikacji FlashLearn

## Tabele

### User
| Kolumna             | Typ      | Ograniczenia                  | Opis                                   |
|---------------------|----------|-------------------------------|----------------------------------------|
| id                  | INTEGER  | PRIMARY KEY AUTOINCREMENT     | Unikalny identyfikator użytkownika     |
| email               | TEXT     | NOT NULL UNIQUE               | Adres email użytkownika (login)        |
| password_hash       | TEXT     | NOT NULL                      | Zahaszowane hasło użytkownika          |
| last_login_date     | INTEGER  | NULL                          | Timestamp ostatniego logowania         |
| total_cards_reviewed| INTEGER  | NOT NULL DEFAULT 0            | Łączna liczba przejrzanych fiszek      |
| correct_answers     | INTEGER  | NOT NULL DEFAULT 0            | Liczba poprawnych odpowiedzi           |
| incorrect_answers   | INTEGER  | NOT NULL DEFAULT 0            | Liczba niepoprawnych odpowiedzi        |
| created_at          | INTEGER  | NOT NULL                      | Timestamp utworzenia konta             |

### Category
| Kolumna  | Typ      | Ograniczenia              | Opis                             |
|----------|----------|--------------------------|---------------------------------|
| id       | INTEGER  | PRIMARY KEY AUTOINCREMENT | Unikalny identyfikator kategorii |
| name     | TEXT     | NOT NULL UNIQUE          | Nazwa kategorii (dziedziny)      |

### Flashcard
| Kolumna             | Typ      | Ograniczenia                                      | Opis                                       |
|---------------------|----------|--------------------------------------------------|-------------------------------------------|
| id                  | INTEGER  | PRIMARY KEY AUTOINCREMENT                         | Unikalny identyfikator fiszki              |
| user_id             | INTEGER  | NOT NULL REFERENCES User(id) ON DELETE CASCADE    | Identyfikator właściciela fiszki           |
| category_id         | INTEGER  | NOT NULL REFERENCES Category(id) ON DELETE RESTRICT | Identyfikator kategorii fiszki             |
| question            | TEXT     | NOT NULL CHECK(length(question) <= 500)           | Pytanie na fiszce (max 500 znaków)         |
| answer              | TEXT     | NOT NULL CHECK(length(answer) <= 1000)            | Odpowiedź na fiszce (max 1000 znaków)      |
| difficulty_level    | INTEGER  | NOT NULL CHECK(difficulty_level BETWEEN 1 AND 5)  | Poziom trudności (1-5)                     |
| learning_status     | INTEGER  | NOT NULL DEFAULT 0 CHECK(learning_status BETWEEN 0 AND 3) | Status nauki (0-3)                  |
| next_review_date    | INTEGER  | NULL                                              | Timestamp następnej planowanej powtórki     |
| is_public           | INTEGER  | NOT NULL DEFAULT 0                                | Flaga określająca, czy fiszka jest publiczna|
| original_flashcard_id| INTEGER  | NULL REFERENCES Flashcard(id) ON DELETE SET NULL  | ID oryginalnej fiszki, jeśli to kopia      |
| copies_count        | INTEGER  | NOT NULL DEFAULT 0                                | Licznik kopii fiszki                       |
| created_at          | INTEGER  | NOT NULL                                          | Timestamp utworzenia fiszki                |
| updated_at          | INTEGER  | NOT NULL                                          | Timestamp ostatniej aktualizacji fiszki    |

### LearningStatistics
| Kolumna               | Typ      | Ograniczenia                                          | Opis                                      |
|-----------------------|----------|------------------------------------------------------|-------------------------------------------|
| id                    | INTEGER  | PRIMARY KEY AUTOINCREMENT                             | Unikalny identyfikator statystyk          |
| flashcard_id          | INTEGER  | NOT NULL UNIQUE REFERENCES Flashcard(id) ON DELETE CASCADE | Identyfikator powiązanej fiszki       |
| correct_answers_count | INTEGER  | NOT NULL DEFAULT 0                                    | Liczba poprawnych odpowiedzi              |
| incorrect_answers_count| INTEGER | NOT NULL DEFAULT 0                                    | Liczba niepoprawnych odpowiedzi           |
| last_updated          | INTEGER  | NOT NULL                                              | Timestamp ostatniej aktualizacji statystyk|

## Relacje

1. **User** (1) -> (n) **Flashcard**
   - Jeden użytkownik może posiadać wiele fiszek
   - Implementacja: klucz obcy `user_id` w tabeli `Flashcard` odnosi się do `id` w tabeli `User`
   - Ograniczenie `ON DELETE CASCADE` - usunięcie użytkownika powoduje usunięcie wszystkich jego fiszek

2. **Category** (1) -> (n) **Flashcard**
   - Jedna kategoria może być przypisana do wielu fiszek
   - Implementacja: klucz obcy `category_id` w tabeli `Flashcard` odnosi się do `id` w tabeli `Category`
   - Ograniczenie `ON DELETE RESTRICT` - nie można usunąć kategorii, jeśli istnieją przypisane do niej fiszki

3. **Flashcard** (1) -> (n) **Flashcard**
   - Oryginalna fiszka może mieć wiele kopii
   - Implementacja: samoreferencyjny klucz obcy `original_flashcard_id` w tabeli `Flashcard`
   - Ograniczenie `ON DELETE SET NULL` - usunięcie oryginalnej fiszki nie usuwa kopii, ale ustawia `original_flashcard_id` na NULL

4. **Flashcard** (1) -> (1) **LearningStatistics**
   - Każda fiszka ma jeden zestaw statystyk nauki
   - Implementacja: klucz obcy `flashcard_id` w tabeli `LearningStatistics` odnosi się do `id` w tabeli `Flashcard`
   - Ograniczenie `ON DELETE CASCADE` - usunięcie fiszki powoduje usunięcie jej statystyk nauki

## Indeksy

```sql
-- Indeks dla szybkiego wyszukiwania fiszek użytkownika
CREATE INDEX idx_flashcard_user_id ON Flashcard(user_id);

-- Indeks dla szybkiego filtrowania fiszek po kategoriach
CREATE INDEX idx_flashcard_category_id ON Flashcard(category_id);

-- Indeks dla szybkiego wyszukiwania w bibliotece publicznej
CREATE INDEX idx_flashcard_public ON Flashcard(is_public, category_id, difficulty_level);

-- Indeks dla szybkiego sortowania w bibliotece publicznej
CREATE INDEX idx_flashcard_public_date ON Flashcard(is_public, created_at);

-- Indeks dla szybkiego wyszukiwania kopii fiszek
CREATE INDEX idx_flashcard_original_id ON Flashcard(original_flashcard_id);

-- Indeks dla szybkiego wyszukiwania fiszek do powtórki
CREATE INDEX idx_flashcard_review ON Flashcard(user_id, learning_status, next_review_date);
```

## Implementacja w Room ORM

Schemat bazy danych zostanie zaimplementowany przy użyciu Room ORM. Poniżej znajdują się wskazówki implementacyjne:

### Encje

1. **@Entity(tableName = "users")**
2. **@Entity(tableName = "categories")**
3. **@Entity(tableName = "flashcards")**
4. **@Entity(tableName = "learning_statistics")**

### DAO (Data Access Object)

Należy utworzyć interfejsy DAO dla każdej encji, zapewniające metody CRUD oraz złożone zapytania, takie jak:

- Pobieranie fiszek do nauki dla danego użytkownika
- Filtrowanie fiszek w bibliotece publicznej
- Generowanie raportów na żądanie

### Migracje

Początkową migrację należy utworzyć zgodnie z powyższym schematem. Przyszłe migracje powinny być planowane w miarę rozwoju aplikacji.

## Uwagi implementacyjne

1. **Limit fiszek**: Limit 1000 fiszek na użytkownika będzie egzekwowany na poziomie aplikacji, a nie bazy danych.

2. **Algorytm powtórek 3-5-7**: Implementacja poprzez pola `learning_status` i `next_review_date` w tabeli `Flashcard`.

3. **Walidacja długości tekstu**: Ograniczenia CHECK dla długości pytań (500 znaków) i odpowiedzi (1000 znaków).

4. **Bezpieczeństwo**: Przechowywanie hashów haseł zamiast zwykłego tekstu.

5. **Wydajność**: Indeksy dla najczęściej wykonywanych zapytań w celu optymalizacji wydajności.

6. **Raporty**: Generowanie na żądanie z danych w tabelach `Flashcard` i `LearningStatistics`.

7. **Eksport do formatu .md**: Funkcja realizowana na poziomie aplikacji, bez specjalnych struktur w bazie danych.
```
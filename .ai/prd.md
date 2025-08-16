# Dokument wymagań produktu (PRD) - FlashLearn

## 1. Przegląd produktu

FlashLearn to mobilna aplikacja edukacyjna na platformę Android, która umożliwia efektywną naukę poprzez system fiszek z wykorzystaniem metody powtórek rozłożonych w czasie. Aplikacja pozwala użytkownikom na tworzenie własnych fiszek edukacyjnych, ich kategoryzowanie, udostępnianie innym użytkownikom oraz korzystanie z fiszek udostępnionych przez społeczność. 

Główną wartością aplikacji jest oszczędność czasu przy tworzeniu materiałów do nauki dzięki możliwości korzystania z fiszek stworzonych przez innych użytkowników oraz efektywne przyswajanie wiedzy dzięki zaimplementowanemu algorytmowi powtórek 3-5-7.

Aplikacja skierowana jest przede wszystkim do uczniów, studentów i nauczycieli, którzy chcą efektywnie przyswajać i utrwalać wiedzę z różnych dziedzin.

## 2. Problem użytkownika

Manualne tworzenie wysokiej jakości fiszek edukacyjnych jest czasochłonne, co zniechęca do korzystania z efektywnej metody nauki jaką jest spaced repetition (powtórki rozłożone w czasie). Użytkownicy często rezygnują z tej metody nauki, mimo jej udowodnionej skuteczności, ze względu na:

1. Konieczność poświęcenia znacznej ilości czasu na przygotowanie materiałów
2. Trudności w systematycznym organizowaniu powtórek
3. Brak możliwości łatwego dzielenia się materiałami z innymi osobami uczącymi się tych samych zagadnień

FlashLearn rozwiązuje te problemy poprzez:
- Umożliwienie korzystania z fiszek stworzonych przez innych użytkowników
- Implementację algorytmu powtórek 3-5-7, który automatycznie zarządza procesem nauki
- Stworzenie prostego systemu udostępniania własnych materiałów edukacyjnych

## 3. Wymagania funkcjonalne

### 3.1 System kont użytkowników
- Rejestracja użytkownika za pomocą adresu e-mail i hasła
- Logowanie do aplikacji za pomocą adresu e-mail i hasła
- Przechowywanie informacji o czasie ostatniego logowania
- Możliwość wylogowania się z aplikacji

### 3.2 Zarządzanie fiszkami
- Tworzenie fiszek zawierających pytanie (do 500 znaków) i odpowiedź (do 1000 znaków)
- Edycja istniejących fiszek
- Usuwanie fiszek
- Oznaczanie fiszek jako gotowe do udostępnienia
- Kategoryzacja fiszek według dziedzin tworzonych przez użytkowników
- Określanie poziomu trudności fiszek
- Limit 1000 fiszek na użytkownika
- Eksport fiszek do formatu .md

### 3.3 System powtórek
- Implementacja algorytmu powtórek 3-5-7
- Oznaczanie fiszek statusami (0-3) określającymi etap nauki:
  - Status 0: Nowa fiszka (Dzień 1)
  - Status 1: Pierwsza powtórka (Dzień 3)
  - Status 2: Druga powtórka (Dzień 5)
  - Status 3: Trzecia powtórka (Dzień 7, fiszka uznana za nauczoną)
- Samoocena poprawności odpowiedzi (dobrze/źle)
- Możliwość przywrócenia nauczonych fiszek (status 3) do powtórek

### 3.4 Udostępnianie fiszek
- Publiczna biblioteka fiszek
- Filtrowanie fiszek po dziedzinie i poziomie trudności
- Sortowanie fiszek po dacie utworzenia
- Oznaczenie czy fiszka została już skopiowana przez użytkownika
- Kopiowanie fiszek od innych użytkowników do własnej kolekcji

### 3.5 Raportowanie
- Cotygodniowe raporty z postępów nauki
- Statystyki dotyczące:
  - Liczby fiszek przejrzanych w ciągu ostatnich 7 dni
  - Liczby poprawnych i błędnych odpowiedzi
  - Podziału statystyk na poszczególne dziedziny

## 4. Granice produktu

### 4.1 Co NIE wchodzi w zakres MVP:
- Zaawansowany algorytm powtórek (jak SuperMemo, Anki)
- Import fiszek z różnych formatów (PDF, DOCX, itp.)
- Integracje z innymi platformami edukacyjnymi
- Aplikacje webowe (na początek tylko mobilne)
- Mechanizmy weryfikacji jakości fiszek
- Mechanizmy gamifikacji
- System rekomendacji fiszek
- Mechanizmy zabezpieczające przed utratą danych
- System zgłaszania nieodpowiednich treści

### 4.2 Ograniczenia techniczne:
- Aplikacja dostępna tylko na platformie Android (API 34, Android 14)
- Aplikacja korzysta z lokalnej bazy danych
- Aplikacja działa w trybie offline
- Limit 1000 fiszek na użytkownika
- Fiszki zawierają tylko tekst (bez obrazów, dźwięków, filmów)

### 4.3 Wymagania wydajnościowe:
- Cold start: mniej niż 5 sekund
- Warm start: mniej niż 2 sekundy
- W przypadku wolniejszego działania: implementacja stronicowania

## 5. Historyjki użytkowników

### System kont użytkowników

#### US-001: Rejestracja nowego użytkownika
- Jako nowy użytkownik, chcę zarejestrować się w aplikacji, aby móc korzystać z jej funkcjonalności.
- Kryteria akceptacji:
  1. Użytkownik może wprowadzić adres e-mail i hasło
  2. System weryfikuje poprawność adresu e-mail
  3. System weryfikuje siłę hasła
  4. System tworzy nowe konto użytkownika
  5. System automatycznie loguje użytkownika po rejestracji

#### US-002: Logowanie do aplikacji
- Jako zarejestrowany użytkownik, chcę zalogować się do aplikacji, aby uzyskać dostęp do moich fiszek.
- Kryteria akceptacji:
  1. Użytkownik może wprowadzić adres e-mail i hasło
  2. System weryfikuje poprawność danych
  3. System umożliwia dostęp do konta użytkownika
  4. System zapisuje czas ostatniego logowania

#### US-003: Wylogowanie z aplikacji
- Jako zalogowany użytkownik, chcę wylogować się z aplikacji, aby zabezpieczyć moje konto.
- Kryteria akceptacji:
  1. Użytkownik może wybrać opcję wylogowania
  2. System kończy sesję użytkownika
  3. System przenosi użytkownika do ekranu logowania

### Zarządzanie fiszkami

#### US-004: Tworzenie nowej fiszki
- Jako zalogowany użytkownik, chcę utworzyć nową fiszkę, aby zapisać informacje do nauki.
- Kryteria akceptacji:
  1. Użytkownik może wprowadzić pytanie (do 500 znaków)
  2. Użytkownik może wprowadzić odpowiedź (do 1000 znaków)
  3. Użytkownik może wybrać dziedzinę (istniejącą lub utworzyć nową)
  4. Użytkownik może określić poziom trudności fiszki
  5. Użytkownik może oznaczyć fiszkę jako gotową do udostępnienia
  6. System zapisuje fiszkę w bazie danych
  7. System przypisuje fiszce status 0 (nowa fiszka)

#### US-005: Edycja istniejącej fiszki
- Jako zalogowany użytkownik, chcę edytować istniejącą fiszkę, aby zaktualizować jej zawartość.
- Kryteria akceptacji:
  1. Użytkownik może wybrać fiszkę do edycji
  2. Użytkownik może modyfikować pytanie, odpowiedź, dziedzinę, poziom trudności i status udostępnienia
  3. System zapisuje zmiany w bazie danych
  4. Edycja nie wpływa na kopie fiszki pobrane przez innych użytkowników

#### US-006: Usuwanie fiszki
- Jako zalogowany użytkownik, chcę usunąć niepotrzebną fiszkę, aby utrzymać porządek w mojej kolekcji.
- Kryteria akceptacji:
  1. Użytkownik może wybrać fiszkę do usunięcia
  2. System wyświetla prośbę o potwierdzenie usunięcia
  3. System usuwa fiszkę z bazy danych użytkownika
  4. Usunięcie nie wpływa na kopie fiszki pobrane przez innych użytkowników

#### US-007: Tworzenie nowej dziedziny
- Jako zalogowany użytkownik, chcę utworzyć nową dziedzinę, aby lepiej kategoryzować moje fiszki.
- Kryteria akceptacji:
  1. Użytkownik może wprowadzić nazwę nowej dziedziny
  2. System weryfikuje unikalność nazwy dziedziny dla danego użytkownika
  3. System zapisuje nową dziedzinę w bazie danych

#### US-008: Eksport fiszek do formatu .md
- Jako zalogowany użytkownik, chcę wyeksportować moje fiszki do formatu .md, aby móc korzystać z nich poza aplikacją.
- Kryteria akceptacji:
  1. Użytkownik może wybrać fiszki do eksportu
  2. System generuje plik .md zawierający wybrane fiszki
  3. System umożliwia zapisanie pliku na urządzeniu użytkownika

### System powtórek

#### US-009: Rozpoczęcie sesji nauki
- Jako zalogowany użytkownik, chcę rozpocząć sesję nauki, aby uczyć się z moich fiszek.
- Kryteria akceptacji:
  1. Użytkownik może wybrać dziedzinę do nauki
  2. System wyświetla fiszki w kolejności od statusu 0 do 2
  3. System nie wyświetla fiszek ze statusem 3 (nauczone)

#### US-010: Przeglądanie fiszki podczas nauki
- Jako uczący się użytkownik, chcę przeglądać fiszki jedna po drugiej, aby efektywnie się uczyć.
- Kryteria akceptacji:
  1. System wyświetla pytanie z fiszki
  2. Użytkownik może wyświetlić odpowiedź po zastanowieniu się
  3. Użytkownik może oznaczyć swoją odpowiedź jako poprawną lub błędną
  4. System aktualizuje status fiszki na podstawie odpowiedzi użytkownika:
     - Poprawna odpowiedź: status fiszki rośnie o 1
     - Błędna odpowiedź: status fiszki spada do 0

#### US-011: Zakończenie sesji nauki
- Jako uczący się użytkownik, chcę zakończyć sesję nauki, aby wrócić do głównego ekranu aplikacji.
- Kryteria akceptacji:
  1. Użytkownik może zakończyć sesję w dowolnym momencie
  2. System zapisuje postępy nauki
  3. System przenosi użytkownika do głównego ekranu aplikacji

#### US-012: Przywrócenie nauczonych fiszek do powtórek
- Jako zalogowany użytkownik, chcę przywrócić nauczone fiszki do powtórek, aby utrwalić wiedzę.
- Kryteria akceptacji:
  1. Użytkownik może wybrać fiszki ze statusem 3 (nauczone)
  2. Użytkownik może zmienić status wybranych fiszek
  3. System aktualizuje status fiszek w bazie danych

### Udostępnianie fiszek

#### US-013: Przeglądanie publicznej biblioteki fiszek
- Jako zalogowany użytkownik, chcę przeglądać fiszki udostępnione przez innych użytkowników, aby znaleźć przydatne materiały do nauki.
- Kryteria akceptacji:
  1. Użytkownik może przeglądać publiczną bibliotekę fiszek
  2. System wyświetla fiszki w formie tabeli
  3. System oznacza fiszki, które użytkownik już skopiował

#### US-014: Filtrowanie fiszek w publicznej bibliotece
- Jako zalogowany użytkownik, chcę filtrować fiszki w publicznej bibliotece, aby łatwiej znaleźć interesujące mnie materiały.
- Kryteria akceptacji:
  1. Użytkownik może filtrować fiszki według dziedziny
  2. Użytkownik może filtrować fiszki według poziomu trudności
  3. System wyświetla tylko fiszki spełniające kryteria filtrowania

#### US-015: Sortowanie fiszek w publicznej bibliotece
- Jako zalogowany użytkownik, chcę sortować fiszki w publicznej bibliotece, aby zobaczyć najnowsze materiały.
- Kryteria akceptacji:
  1. Użytkownik może sortować fiszki według daty utworzenia
  2. System wyświetla fiszki w wybranej kolejności

#### US-016: Kopiowanie fiszki z publicznej biblioteki
- Jako zalogowany użytkownik, chcę skopiować fiszkę z publicznej biblioteki, aby dodać ją do mojej kolekcji.
- Kryteria akceptacji:
  1. Użytkownik może wybrać fiszkę do skopiowania
  2. System tworzy kopię fiszki w kolekcji użytkownika
  3. System oznacza fiszkę jako skopiowaną z publicznej biblioteki
  4. System przypisuje fiszce status 0 (nowa fiszka)

#### US-017: Oznaczanie fiszki jako gotowej do udostępnienia
- Jako zalogowany użytkownik, chcę oznaczyć moją fiszkę jako gotową do udostępnienia, aby podzielić się nią z innymi użytkownikami.
- Kryteria akceptacji:
  1. Użytkownik może oznaczyć fiszkę jako gotową do udostępnienia
  2. System dodaje fiszkę do publicznej biblioteki
  3. System aktualizuje status udostępnienia fiszki w bazie danych

### Raportowanie

#### US-018: Przeglądanie cotygodniowego raportu
- Jako zalogowany użytkownik, chcę przeglądać cotygodniowy raport z moich postępów, aby śledzić efektywność nauki.
- Kryteria akceptacji:
  1. Użytkownik może wyświetlić raport z ostatnich 7 dni
  2. System wyświetla liczbę przejrzanych fiszek
  3. System wyświetla liczbę poprawnych i błędnych odpowiedzi
  4. System wyświetla statystyki w podziale na dziedziny

## 6. Metryki sukcesu

### 6.1 Udostępnianie fiszek
- 50% fiszek użytkownika pochodzi od innych użytkowników
- 75% fiszek użytkownika jest udostępnianych innym użytkownikom
- Pomiar: cotygodniowe raporty zawierające statystyki dotyczące pochodzenia fiszek

### 6.2 Wydajność aplikacji
- Cold start: mniej niż 5 sekund
- Warm start: mniej niż 2 sekundy
- W przypadku wolniejszego działania: implementacja stronicowania
- Pomiar: automatyczne testy wydajności podczas procesu rozwoju aplikacji

### 6.3 Efektywność nauki
- Raportowanie liczby fiszek przejrzanych w ciągu tygodnia
- Statystyki poprawnych i błędnych odpowiedzi w podziale na dziedziny
- Pomiar: cotygodniowe raporty generowane dla każdego użytkownika

### 6.4 Zaangażowanie użytkowników
- Średni czas spędzony w aplikacji dziennie
- Częstotliwość korzystania z aplikacji w ciągu tygodnia
- Pomiar: analiza czasu logowania i aktywności użytkowników

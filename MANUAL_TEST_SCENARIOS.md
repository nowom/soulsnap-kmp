# Scenariusze Testów Manualnych - SoulSnaps

## Przegląd Systemu Scopów Użytkowników

Aplikacja SoulSnaps implementuje system scopów użytkowników z następującymi rolami i planami:

### Role Użytkowników
- **FREE_USER** - Darmowy użytkownik
- **PREMIUM_USER** - Płatny użytkownik  
- **FAMILY_USER** - Rodzinny plan
- **ENTERPRISE_USER** - Firmowy plan
- **ADMIN** - Administrator
- **MODERATOR** - Moderator

### Plany Subskrypcji
- **FREE** - Darmowy plan
- **BASIC** - Podstawowy plan płatny
- **PREMIUM** - Premium plan
- **FAMILY** - Rodzinny plan
- **ENTERPRISE** - Firmowy plan
- **LIFETIME** - Dożywotni dostęp

---

## 1. FREE_USER (Darmowy Użytkownik)

### Limity:
- Maksymalnie 50 wspomnień
- 1GB miejsca na dane
- 5 analiz dziennie
- 0 współpracowników
- 0 eksportów miesięcznie
- 0 backupów
- 30 dni przechowywania danych

### Scenariusze Testowe:

#### 1.1 Rejestracja i Logowanie
**Cel:** Sprawdzenie procesu rejestracji i logowania dla darmowego użytkownika

**Kroki:**
1. Otwórz aplikację SoulSnaps
2. Wybierz "Zarejestruj się"
3. Wprowadź prawidłowy email i hasło
4. Potwierdź rejestrację
5. Zaloguj się z utworzonym kontem

**Oczekiwany rezultat:**
- Użytkownik zostaje zarejestrowany z planem FREE
- Automatyczne przypisanie scopu FREE_USER
- Dostęp do podstawowych funkcji aplikacji

#### 1.2 Tworzenie Wspomnień (SoulSnaps)
**Cel:** Sprawdzenie możliwości tworzenia wspomnień w ramach limitu

**Kroki:**
1. Zaloguj się jako FREE_USER
2. Przejdź do sekcji "SoulSnaps"
3. Kliknij "Dodaj Snap"
4. Dodaj zdjęcie, opis i emocję
5. Zapisz wspomnienie
6. Powtórz proces do osiągnięcia limitu 50 wspomnień

**Oczekiwany rezultat:**
- Możliwość tworzenia wspomnień do limitu 50
- Po przekroczeniu limitu wyświetlenie komunikatu o konieczności uaktualnienia planu

#### 1.3 Analiza Wspomnień
**Cel:** Sprawdzenie funkcji analizy w ramach dziennego limitu

**Kroki:**
1. Zaloguj się jako FREE_USER
2. Przejdź do sekcji "SoulSnaps"
3. Wybierz wspomnienie
4. Kliknij "Analizuj"
5. Powtórz proces 5 razy w ciągu dnia

**Oczekiwany rezultat:**
- Możliwość analizy do 5 wspomnień dziennie
- Po przekroczeniu limitu wyświetlenie komunikatu o wyczerpaniu dziennego limitu

#### 1.4 Ograniczenia Funkcji Premium
**Cel:** Sprawdzenie blokowania funkcji niedostępnych dla darmowego planu

**Kroki:**
1. Zaloguj się jako FREE_USER
2. Przejdź do sekcji "Wykrywanie wzorców"
3. Przejdź do sekcji "Udostępnianie"
4. Przejdź do sekcji "Eksport"
5. Przejdź do sekcji "Backup"

**Oczekiwany rezultat:**
- Funkcje niedostępne są zablokowane
- Wyświetlenie komunikatów o konieczności uaktualnienia planu
- Przekierowanie do ekranu uaktualnienia

#### 1.5 Personalizacja (Podstawowa)
**Cel:** Sprawdzenie dostępnych opcji personalizacji

**Kroki:**
1. Zaloguj się jako FREE_USER
2. Przejdź do "Ustawienia"
3. Sprawdź dostępne opcje personalizacji
4. Zmień podstawowe ustawienia (motyw, powiadomienia)

**Oczekiwany rezultat:**
- Dostęp do podstawowych opcji personalizacji
- Brak dostępu do zaawansowanych opcji

---

## 2. PREMIUM_USER (Płatny Użytkownik)

### Limity:
- Maksymalnie 1000 wspomnień
- 10GB miejsca na dane
- 100 analiz dziennie
- 5 współpracowników
- 10 eksportów miesięcznie
- 5 backupów
- 365 dni przechowywania danych

### Scenariusze Testowe:

#### 2.1 Uaktualnienie z Planu FREE
**Cel:** Sprawdzenie procesu uaktualnienia do planu Premium

**Kroki:**
1. Zaloguj się jako FREE_USER
2. Przejdź do "Ustawienia" → "Uaktualnij plan"
3. Wybierz plan Premium
4. Wykonaj płatność
5. Sprawdź nowe limity i funkcje

**Oczekiwany rezultat:**
- Pomyślne uaktualnienie do planu Premium
- Zwiększenie limitów zgodnie z nowym planem
- Odblokowanie funkcji Premium

#### 2.2 Zaawansowana Analiza Wspomnień
**Cel:** Sprawdzenie rozszerzonych funkcji analizy

**Kroki:**
1. Zaloguj się jako PREMIUM_USER
2. Przejdź do sekcji "SoulSnaps"
3. Wybierz wspomnienie
4. Kliknij "Analizuj"
5. Sprawdź dostępne opcje analizy (wzorce, insights)
6. Wykonaj analizę wzorców

**Oczekiwany rezultat:**
- Dostęp do zaawansowanych funkcji analizy
- Możliwość wykrywania wzorców
- Szczegółowe insights

#### 2.3 Udostępnianie Wspomnień
**Cel:** Sprawdzenie funkcji udostępniania

**Kroki:**
1. Zaloguj się jako PREMIUM_USER
2. Przejdź do sekcji "SoulSnaps"
3. Wybierz wspomnienie
4. Kliknij "Udostępnij"
5. Wybierz typ udostępnienia (prywatne, publiczne)
6. Wyślij link udostępnienia

**Oczekiwany rezultat:**
- Możliwość udostępniania wspomnień
- Różne opcje udostępniania
- Generowanie linków udostępniania

#### 2.4 Eksport Danych
**Cel:** Sprawdzenie funkcji eksportu

**Kroki:**
1. Zaloguj się jako PREMIUM_USER
2. Przejdź do "Ustawienia" → "Eksport"
3. Wybierz format eksportu (PDF, JSON)
4. Wybierz wspomnienia do eksportu
5. Wykonaj eksport
6. Sprawdź limit eksportów miesięcznie

**Oczekiwany rezultat:**
- Możliwość eksportu w różnych formatach
- Limit 10 eksportów miesięcznie
- Po przekroczeniu limitu odpowiedni komunikat

#### 2.5 Backup i Synchronizacja
**Cel:** Sprawdzenie funkcji backupu

**Kroki:**
1. Zaloguj się jako PREMIUM_USER
2. Przejdź do "Ustawienia" → "Backup"
3. Skonfiguruj automatyczny backup
4. Wykonaj ręczny backup
5. Sprawdź limit backupów (5)

**Oczekiwany rezultat:**
- Możliwość konfiguracji backupu
- Limit 5 backupów
- Automatyczna synchronizacja

---

## 3. FAMILY_USER (Rodzinny Plan)

### Limity:
- Maksymalnie 5000 wspomnień
- 25GB miejsca na dane
- 200 analiz dziennie
- 10 współpracowników
- 25 eksportów miesięcznie
- 10 backupów
- 730 dni przechowywania danych

### Scenariusze Testowe:

#### 3.1 Zarządzanie Rodziną
**Cel:** Sprawdzenie funkcji zarządzania członkami rodziny

**Kroki:**
1. Zaloguj się jako FAMILY_USER
2. Przejdź do "Ustawienia" → "Zarządzanie rodziną"
3. Dodaj członka rodziny (email)
4. Przypisz uprawnienia
5. Sprawdź limit współpracowników (10)

**Oczekiwany rezultat:**
- Możliwość dodawania członków rodziny
- Przypisywanie uprawnień
- Limit 10 współpracowników

#### 3.2 Współdzielone Wspomnienia
**Cel:** Sprawdzenie funkcji współdzielenia wspomnień w rodzinie

**Kroki:**
1. Zaloguj się jako FAMILY_USER
2. Utwórz wspomnienie
3. Oznacz jako "Rodzinne"
4. Zapros członka rodziny do współpracy
5. Sprawdź dostępność wspomnienia dla innych członków

**Oczekiwany rezultat:**
- Możliwość oznaczania wspomnień jako rodzinne
- Współdzielenie z członkami rodziny
- Kontrola dostępu

#### 3.3 Zaawansowane Udostępnianie
**Cel:** Sprawdzenie rozszerzonych funkcji udostępniania

**Kroki:**
1. Zaloguj się jako FAMILY_USER
2. Przejdź do sekcji "SoulSnaps"
3. Wybierz wspomnienie
4. Kliknij "Udostępnij"
5. Wybierz "Rodzina" jako odbiorców
6. Skonfiguruj uprawnienia dostępu

**Oczekiwany rezultat:**
- Opcje udostępniania dla rodziny
- Kontrola uprawnień dostępu
- Grupowe udostępnianie

---

## 4. ENTERPRISE_USER (Firmowy Plan)

### Limity:
- Nieograniczona liczba wspomnień
- 100GB+ miejsca na dane
- Nieograniczona liczba analiz
- Nieograniczona liczba współpracowników
- Nieograniczona liczba eksportów
- Nieograniczona liczba backupów
- Nieograniczone przechowywanie danych

### Scenariusze Testowe:

#### 4.1 Zarządzanie Zespołem
**Cel:** Sprawdzenie funkcji zarządzania zespołem

**Kroki:**
1. Zaloguj się jako ENTERPRISE_USER
2. Przejdź do "Zarządzanie zespołem"
3. Dodaj członków zespołu
4. Przypisz role i uprawnienia
5. Skonfiguruj polityki dostępu

**Oczekiwany rezultat:**
- Pełne zarządzanie zespołem
- Przypisywanie ról
- Konfiguracja polityk

#### 4.2 Zaawansowane Funkcje AI
**Cel:** Sprawdzenie dostępności zaawansowanych funkcji AI

**Kroki:**
1. Zaloguj się jako ENTERPRISE_USER
2. Przejdź do sekcji "AI Insights"
3. Sprawdź dostępne funkcje AI
4. Wykonaj zaawansowaną analizę
5. Sprawdź dashboard analityczny

**Oczekiwany rezultat:**
- Dostęp do wszystkich funkcji AI
- Zaawansowane analizy
- Dashboard analityczny

#### 4.3 API Access
**Cel:** Sprawdzenie dostępu do API

**Kroki:**
1. Zaloguj się jako ENTERPRISE_USER
2. Przejdź do "Ustawienia" → "API"
3. Wygeneruj klucze API
4. Sprawdź dokumentację API
5. Przetestuj dostęp do endpointów

**Oczekiwany rezultat:**
- Dostęp do API
- Generowanie kluczy API
- Dokumentacja API

#### 4.4 Nieograniczone Limity
**Cel:** Sprawdzenie braku ograniczeń w planie Enterprise

**Kroki:**
1. Zaloguj się jako ENTERPRISE_USER
2. Utwórz dużą liczbę wspomnień (>1000)
3. Wykonaj wiele analiz w ciągu dnia (>100)
4. Wykonaj wiele eksportów (>25)
5. Sprawdź brak komunikatów o limitach

**Oczekiwany rezultat:**
- Brak ograniczeń ilościowych
- Wszystkie funkcje dostępne
- Brak komunikatów o limitach

---

## 5. ADMIN (Administrator)

### Scenariusze Testowe:

#### 5.1 Zarządzanie Użytkownikami
**Cel:** Sprawdzenie funkcji administracyjnych

**Kroki:**
1. Zaloguj się jako ADMIN
2. Przejdź do "Panel administracyjny"
3. Sprawdź listę użytkowników
4. Zmień uprawnienia użytkownika
5. Zablokuj/odblokuj użytkownika

**Oczekiwany rezultat:**
- Pełny dostęp do zarządzania użytkownikami
- Możliwość zmiany uprawnień
- Kontrola dostępu użytkowników

#### 5.2 Zarządzanie Systemem
**Cel:** Sprawdzenie funkcji zarządzania systemem

**Kroki:**
1. Zaloguj się jako ADMIN
2. Przejdź do "Ustawienia systemu"
3. Sprawdź statystyki systemu
4. Skonfiguruj globalne ustawienia
5. Sprawdź logi systemu

**Oczekiwany rezultat:**
- Dostęp do ustawień systemu
- Statystyki i logi
- Konfiguracja globalna

---

## 6. MODERATOR (Moderator)

### Scenariusze Testowe:

#### 6.1 Moderacja Treści
**Cel:** Sprawdzenie funkcji moderacji

**Kroki:**
1. Zaloguj się jako MODERATOR
2. Przejdź do "Moderacja treści"
3. Sprawdź zgłoszone treści
4. Zatwierdź/odrzuć treści
5. Sprawdź historię moderacji

**Oczekiwany rezultat:**
- Dostęp do funkcji moderacji
- Kontrola treści
- Historia moderacji

---

## 7. Scenariusze Testowe - Przekraczanie Limitów

### 7.1 Przekroczenie Limitu Wspomnień
**Cel:** Sprawdzenie zachowania przy przekroczeniu limitu

**Kroki:**
1. Zaloguj się jako użytkownik z limitem wspomnień
2. Utwórz wspomnienia do osiągnięcia limitu
3. Spróbuj utworzyć kolejne wspomnienie
4. Sprawdź wyświetlony komunikat

**Oczekiwany rezultat:**
- Blokada tworzenia nowych wspomnień
- Komunikat o przekroczeniu limitu
- Sugestia uaktualnienia planu

### 7.2 Przekroczenie Dziennego Limitu Analiz
**Cel:** Sprawdzenie zachowania przy przekroczeniu dziennego limitu analiz

**Kroki:**
1. Zaloguj się jako użytkownik z limitem analiz
2. Wykonaj analizy do osiągnięcia dziennego limitu
3. Spróbuj wykonać kolejną analizę
4. Sprawdź wyświetlony komunikat

**Oczekiwany rezultat:**
- Blokada wykonywania analiz
- Komunikat o wyczerpaniu dziennego limitu
- Informacja o resetowaniu limitu następnego dnia

### 7.3 Przekroczenie Limitu Eksportów
**Cel:** Sprawdzenie zachowania przy przekroczeniu miesięcznego limitu eksportów

**Kroki:**
1. Zaloguj się jako użytkownik z limitem eksportów
2. Wykonaj eksporty do osiągnięcia miesięcznego limitu
3. Spróbuj wykonać kolejny eksport
4. Sprawdź wyświetlony komunikat

**Oczekiwany rezultat:**
- Blokada wykonywania eksportów
- Komunikat o przekroczeniu miesięcznego limitu
- Sugestia uaktualnienia planu

---

## 8. Scenariusze Testowe - Wygaśnięcie Subskrypcji

### 8.1 Wygaśnięcie Planu Premium
**Cel:** Sprawdzenie zachowania po wygaśnięciu płatnego planu

**Kroki:**
1. Symuluj wygaśnięcie planu Premium
2. Zaloguj się jako użytkownik
3. Sprawdź dostępne funkcje
4. Sprawdź komunikaty o wygaśnięciu

**Oczekiwany rezultat:**
- Automatyczne przejście na plan FREE
- Ograniczenie funkcji do poziomu FREE
- Komunikaty o wygaśnięciu subskrypcji

### 8.2 Odnowienie Subskrypcji
**Cel:** Sprawdzenie procesu odnowienia subskrypcji

**Kroki:**
1. Zaloguj się jako użytkownik z wygasłą subskrypcją
2. Przejdź do "Uaktualnij plan"
3. Wybierz plan do odnowienia
4. Wykonaj płatność
5. Sprawdź przywrócenie funkcji

**Oczekiwany rezultat:**
- Możliwość odnowienia subskrypcji
- Przywrócenie funkcji po płatności
- Aktualizacja limitów

---

## 9. Scenariusze Testowe - Błędy i Wyjątki

### 9.1 Błąd Płatności
**Cel:** Sprawdzenie zachowania przy błędzie płatności

**Kroki:**
1. Przejdź do uaktualnienia planu
2. Wprowadź nieprawidłowe dane płatności
3. Spróbuj wykonać płatność
4. Sprawdź wyświetlony komunikat błędu

**Oczekiwany rezultat:**
- Wyświetlenie komunikatu o błędzie płatności
- Możliwość ponowienia płatności
- Zachowanie aktualnego planu

### 9.2 Błąd Sieci
**Cel:** Sprawdzenie zachowania przy braku połączenia internetowego

**Kroki:**
1. Wyłącz połączenie internetowe
2. Spróbuj wykonać akcję wymagającą sieci
3. Sprawdź wyświetlony komunikat
4. Włącz połączenie i spróbuj ponownie

**Oczekiwany rezultat:**
- Komunikat o braku połączenia
- Możliwość ponowienia po przywróceniu połączenia
- Zachowanie danych lokalnych

---

## 10. Scenariusze Testowe - Bezpieczeństwo

### 10.1 Nieautoryzowany Dostęp
**Cel:** Sprawdzenie zabezpieczeń przed nieautoryzowanym dostępem

**Kroki:**
1. Spróbuj uzyskać dostęp do funkcji bez logowania
2. Spróbuj uzyskać dostęp do funkcji innego użytkownika
3. Sprawdź zachowanie aplikacji

**Oczekiwany rezultat:**
- Przekierowanie do ekranu logowania
- Blokada dostępu do danych innych użytkowników
- Komunikaty o braku uprawnień

### 10.2 Walidacja Uprawnień
**Cel:** Sprawdzenie walidacji uprawnień na poziomie API

**Kroki:**
1. Wykonaj żądanie API bez odpowiednich uprawnień
2. Wykonaj żądanie API z nieprawidłowym tokenem
3. Sprawdź odpowiedzi API

**Oczekiwany rezultat:**
- Odpowiedzi HTTP 401/403
- Komunikaty o braku uprawnień
- Brak dostępu do chronionych danych

---

## Podsumowanie

Dokument zawiera kompleksowe scenariusze testowe dla wszystkich scopów użytkowników w aplikacji SoulSnaps. Każdy scenariusz obejmuje:

- **Cel testu** - co ma być przetestowane
- **Kroki** - szczegółowe instrukcje wykonania
- **Oczekiwany rezultat** - co powinno się wydarzyć

Scenariusze pokrywają:
- Podstawowe funkcjonalności dla każdego scopu
- Przekraczanie limitów
- Wygaśnięcie subskrypcji
- Obsługę błędów
- Bezpieczeństwo

Te scenariusze powinny być wykonane przed każdym wydaniem aplikacji, aby zapewnić prawidłowe działanie systemu scopów użytkowników.


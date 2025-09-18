# Supabase Deployment Guide for SoulSnaps

## Wdrożenie tabel do Supabase

### 1. Przygotowanie środowiska

1. Zaloguj się do [Supabase Dashboard](https://supabase.com/dashboard)
2. Wybierz swój projekt lub utwórz nowy
3. Przejdź do sekcji **SQL Editor**

### 2. Wdrożenie schematu bazy danych

1. Otwórz plik `supabase_schema_updated.sql`
2. Skopiuj całą zawartość
3. Wklej do SQL Editor w Supabase
4. Kliknij **Run** aby wykonać skrypt

### 3. Weryfikacja wdrożenia

Po wykonaniu skryptu sprawdź czy zostały utworzone:

#### Tabele:
- `memories` - główna tabela do przechowywania snapów
- `user_profiles` - profile użytkowników

#### Buckety Storage:
- `snap-images` - do przechowywania zdjęć
- `snap-audio` - do przechowywania nagrań audio

#### Widoki:
- `memory_stats` - statystyki pamięci użytkowników

### 4. Konfiguracja Storage

1. Przejdź do **Storage** w Supabase Dashboard
2. Sprawdź czy utworzone zostały buckety:
   - `snap-images`
   - `snap-audio`
3. Ustaw odpowiednie uprawnienia dla każdego bucketu

### 5. Testowanie połączenia

Użyj następującego zapytania SQL do testowania:

```sql
-- Sprawdź czy tabele zostały utworzone
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('memories', 'user_profiles');

-- Sprawdź czy buckety storage zostały utworzone
SELECT name, public 
FROM storage.buckets 
WHERE name IN ('snap-images', 'snap-audio');

-- Sprawdź czy indeksy zostały utworzone
SELECT indexname, tablename 
FROM pg_indexes 
WHERE tablename IN ('memories', 'user_profiles');
```

### 6. Konfiguracja aplikacji

Po wdrożeniu schematu, zaktualizuj konfigurację aplikacji:

1. **URL Supabase** - znajdź w Settings > API
2. **Anon Key** - znajdź w Settings > API
3. **Service Role Key** - znajdź w Settings > API (dla operacji serwerowych)

### 7. Struktura tabeli memories

```sql
CREATE TABLE memories (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    mood_type TEXT CHECK (mood_type IN ('HAPPY', 'SAD', 'NEUTRAL', 'EXCITED', 'RELAXED')),
    photo_uri TEXT, -- Ścieżka do zdjęcia w storage
    audio_uri TEXT, -- Ścieżka do audio w storage
    image_url TEXT, -- URL do przetworzonego obrazu
    location_lat DECIMAL(10, 8),
    location_lng DECIMAL(11, 8),
    location_name TEXT,
    affirmation TEXT,
    is_favorite BOOLEAN DEFAULT FALSE,
    is_synced BOOLEAN DEFAULT FALSE, -- Status synchronizacji
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

### 8. Bezpieczeństwo

Schemat zawiera:
- **Row Level Security (RLS)** - użytkownicy widzą tylko swoje dane
- **Storage policies** - użytkownicy mogą zarządzać tylko swoimi plikami
- **Automatic cleanup** - usuwanie plików przy usuwaniu pamięci
- **Proper indexing** - optymalizacja zapytań

### 9. Funkcje pomocnicze

Schemat zawiera funkcje:
- `generate_snap_storage_path()` - generowanie ścieżek dla plików
- `cleanup_memory_files()` - czyszczenie plików przy usuwaniu
- `get_memory_by_id()` - bezpieczne pobieranie pamięci
- `update_updated_at_column()` - automatyczne aktualizowanie timestamp

### 10. Monitoring

Użyj widoku `memory_stats` do monitorowania:
```sql
SELECT * FROM memory_stats WHERE user_id = 'your-user-id';
```

## Troubleshooting

### Problem: Błędy podczas wykonywania skryptu
**Rozwiązanie:** Sprawdź czy masz odpowiednie uprawnienia w Supabase

### Problem: Buckety storage nie zostały utworzone
**Rozwiązanie:** Sprawdź czy masz włączone Storage w projekcie

### Problem: RLS blokuje zapytania
**Rozwiązanie:** Upewnij się, że użytkownik jest zalogowany i ma odpowiednie uprawnienia

## Następne kroki

1. Zaktualizuj konfigurację aplikacji z nowymi danymi Supabase
2. Przetestuj zapisywanie i pobieranie snapów
3. Skonfiguruj backup bazy danych
4. Ustaw monitoring i alerty

-- SoulSnaps Test Queries
-- Użyj tych zapytań do testowania funkcjonalności bazy danych

-- 1. Sprawdzenie struktury tabel
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns 
WHERE table_name = 'memories' 
ORDER BY ordinal_position;

-- 2. Sprawdzenie indeksów
SELECT 
    indexname, 
    tablename, 
    indexdef
FROM pg_indexes 
WHERE tablename IN ('memories', 'user_profiles')
ORDER BY tablename, indexname;

-- 3. Sprawdzenie polityk RLS
SELECT 
    schemaname, 
    tablename, 
    policyname, 
    permissive, 
    roles, 
    cmd, 
    qual
FROM pg_policies 
WHERE tablename IN ('memories', 'user_profiles')
ORDER BY tablename, policyname;

-- 4. Sprawdzenie buckietów storage
SELECT 
    name, 
    public, 
    file_size_limit, 
    allowed_mime_types
FROM storage.buckets 
WHERE name IN ('snap-images', 'snap-audio');

-- 5. Test wstawiania przykładowej pamięci (wymaga zalogowanego użytkownika)
INSERT INTO memories (
    title, 
    description, 
    mood_type, 
    photo_uri, 
    location_name,
    affirmation
) VALUES (
    'Test Memory',
    'This is a test memory for SoulSnaps',
    'HAPPY',
    'test-user-id/snap-images/test-photo.jpg',
    'Test Location',
    'You are doing great!'
);

-- 6. Pobranie wszystkich pamięci użytkownika
SELECT 
    id,
    title,
    description,
    mood_type,
    photo_uri,
    audio_uri,
    location_name,
    affirmation,
    is_favorite,
    is_synced,
    created_at
FROM memories 
WHERE user_id = auth.uid()
ORDER BY created_at DESC;

-- 7. Pobranie pamięci z określonym nastrojem
SELECT 
    id,
    title,
    description,
    mood_type,
    created_at
FROM memories 
WHERE user_id = auth.uid() 
AND mood_type = 'HAPPY'
ORDER BY created_at DESC;

-- 8. Pobranie ulubionych pamięci
SELECT 
    id,
    title,
    description,
    mood_type,
    created_at
FROM memories 
WHERE user_id = auth.uid() 
AND is_favorite = true
ORDER BY created_at DESC;

-- 9. Pobranie pamięci z lokalizacją
SELECT 
    id,
    title,
    location_name,
    location_lat,
    location_lng,
    created_at
FROM memories 
WHERE user_id = auth.uid() 
AND location_lat IS NOT NULL 
AND location_lng IS NOT NULL
ORDER BY created_at DESC;

-- 10. Statystyki użytkownika
SELECT 
    total_memories,
    favorite_memories,
    memories_with_photos,
    memories_with_audio,
    memories_with_location,
    first_memory_date,
    last_memory_date
FROM memory_stats 
WHERE user_id = auth.uid();

-- 11. Pobranie pamięci z określonego zakresu dat
SELECT 
    id,
    title,
    description,
    mood_type,
    created_at
FROM memories 
WHERE user_id = auth.uid() 
AND created_at >= '2024-01-01'::timestamp
AND created_at < '2024-12-31'::timestamp
ORDER BY created_at DESC;

-- 12. Wyszukiwanie w tytułach i opisach
SELECT 
    id,
    title,
    description,
    mood_type,
    created_at
FROM memories 
WHERE user_id = auth.uid() 
AND (
    title ILIKE '%test%' 
    OR description ILIKE '%test%'
)
ORDER BY created_at DESC;

-- 13. Aktualizacja pamięci
UPDATE memories 
SET 
    title = 'Updated Test Memory',
    description = 'This memory has been updated',
    is_favorite = true,
    updated_at = NOW()
WHERE id = 'your-memory-id' 
AND user_id = auth.uid();

-- 14. Usunięcie pamięci
DELETE FROM memories 
WHERE id = 'your-memory-id' 
AND user_id = auth.uid();

-- 15. Test funkcji get_memory_by_id
SELECT * FROM get_memory_by_id('your-memory-id');

-- 16. Sprawdzenie triggerów
SELECT 
    trigger_name,
    event_manipulation,
    action_timing,
    action_statement
FROM information_schema.triggers 
WHERE event_object_table IN ('memories', 'user_profiles')
ORDER BY event_object_table, trigger_name;

-- 17. Sprawdzenie funkcji
SELECT 
    routine_name,
    routine_type,
    data_type
FROM information_schema.routines 
WHERE routine_schema = 'public' 
AND routine_name IN (
    'update_updated_at_column',
    'handle_new_user',
    'generate_snap_storage_path',
    'cleanup_memory_files',
    'get_memory_by_id'
)
ORDER BY routine_name;

-- 18. Test generowania ścieżki storage
SELECT generate_snap_storage_path(
    auth.uid(),
    'photo',
    'jpg'
) as photo_path;

SELECT generate_snap_storage_path(
    auth.uid(),
    'audio',
    'mp3'
) as audio_path;

-- 19. Sprawdzenie uprawnień storage
SELECT 
    bucket_id,
    name,
    owner,
    created_at
FROM storage.objects 
WHERE bucket_id IN ('snap-images', 'snap-audio')
LIMIT 10;

-- 20. Test widoku memory_stats
SELECT 
    user_id,
    total_memories,
    favorite_memories,
    memories_with_photos,
    memories_with_audio,
    memories_with_location,
    first_memory_date,
    last_memory_date
FROM memory_stats 
ORDER BY total_memories DESC;

-- 21. Sprawdzenie logów (jeśli włączone)
SELECT 
    timestamp,
    level,
    message
FROM postgres_logs 
WHERE message ILIKE '%memories%'
ORDER BY timestamp DESC
LIMIT 10;

-- 22. Test wydajności - sprawdzenie planu wykonania
EXPLAIN ANALYZE
SELECT 
    id,
    title,
    mood_type,
    created_at
FROM memories 
WHERE user_id = auth.uid() 
AND created_at >= NOW() - INTERVAL '30 days'
ORDER BY created_at DESC;

-- 23. Sprawdzenie rozmiaru tabel
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables 
WHERE tablename IN ('memories', 'user_profiles')
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- 24. Sprawdzenie aktywnych połączeń
SELECT 
    pid,
    usename,
    application_name,
    client_addr,
    state,
    query_start,
    query
FROM pg_stat_activity 
WHERE datname = current_database()
AND state = 'active';

-- 25. Test backup (przykład)
-- pg_dump -h your-host -U your-user -d your-database --schema-only > backup_schema.sql
-- pg_dump -h your-host -U your-user -d your-database --data-only > backup_data.sql

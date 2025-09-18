# SoulSnaps Sync Flow Documentation

## Przepływ synchronizacji snapów z serwerem

### 1. **UI: Dodawanie snapu**
```
[CaptureMomentScreen] 
    ↓ użytkownik klika "Dodaj"
[CaptureMomentViewModel.saveMemory()]
```

### 2. **ViewModel: Zbieranie danych**
```kotlin
val result = createSnapUseCase(
    title = state.value.title,
    description = state.value.description,
    mood = state.value.selectedMood,
    photoUri = state.value.photoUri,
    audioUri = state.value.audioUri,
    locationName = state.value.location,
    latitude = null,
    longitude = null
)
```

### 3. **CreateSnapUseCase: Local-first approach**
```kotlin
// 1) Tworzenie lokalnego draftu
val draftId = createLocalDraft(...)

// 2) Kolejkowanie synchronizacji
syncEngine.enqueueSync(draftId)

// 3) Generowanie afirmacji (opcjonalnie)
val affirmation = generateAffirmationIfNeeded(description, mood)
```

### 4. **SyncEngine: Synchronizacja**
```kotlin
// 1) Sprawdzenie połączenia
if (!networkMonitor.isOnline()) return

// 2) Upload plików do Supabase Storage
val photoResult = storageService.uploadPhoto(draftId, photoUri)
val audioResult = storageService.uploadAudio(draftId, audioUri)

// 3) Tworzenie obiektu RemoteSnap
val remoteSnap = RemoteSnap(...)

// 4) Wysłanie do serwera
val createResult = remoteApiService.createSnap(remoteSnap)

// 5) Aktualizacja statusu lokalnego
draftRepository.updateDraftSyncState(
    draftId = draftId,
    syncState = SyncState.SYNCED,
    remoteId = createResult.remoteId
)
```

### 5. **WorkManager: Background sync**
```kotlin
// SyncWorker - wykonywany w tle
class SyncWorker : CoroutineWorker {
    override suspend fun doWork(): Result {
        val result = syncEngine.syncPendingDrafts()
        return if (result.success) Result.success() else Result.retry()
    }
}
```

### 6. **Affirmation Flow: Po stworzeniu snapu**
```kotlin
// Generowanie afirmacji
val affirmation = affirmationUseCase(
    description = description,
    emotion = mood.name
)

// Aktualizacja draftu
updateDraftWithAffirmation(draftId, affirmation)
```

## Struktura danych

### SnapDraft (lokalny)
```kotlin
data class SnapDraft(
    val id: String,                    // UUID
    val title: String?,
    val description: String?,
    val mood: String?,
    val photoUri: String?,             // Lokalna ścieżka
    val audioUri: String?,             // Lokalna ścieżka
    val locationName: String?,
    val latitude: Double?,
    val longitude: Double?,
    val affirmation: String?,
    val isFavorite: Boolean = false,
    val syncState: SyncState = PENDING,
    val remoteId: String? = null,      // ID z serwera
    val createdAt: Long,
    val updatedAt: Long,
    val retryCount: Int = 0,
    val errorMessage: String? = null
)
```

### RemoteSnap (serwer)
```kotlin
data class RemoteSnap(
    val id: String? = null,
    val title: String?,
    val description: String?,
    val mood_type: String?,
    val photo_uri: String?,            // Ścieżka w Supabase Storage
    val audio_uri: String?,            // Ścieżka w Supabase Storage
    val photo_thumb_path: String?,
    val photo_medium_path: String?,
    val location_lat: Double?,
    val location_lng: Double?,
    val location_name: String?,
    val affirmation: String?,
    val is_favorite: Boolean = false,
    val created_at: String? = null,
    val updated_at: String? = null
)
```

## Stany synchronizacji

### SyncState
- **PENDING** - Oczekuje na synchronizację
- **SYNCING** - W trakcie synchronizacji
- **SYNCED** - Zsynchronizowany pomyślnie
- **FAILED** - Synchronizacja nieudana (będzie ponowiona)

## WorkManager Configuration

### Immediate Sync
```kotlin
val syncWork = OneTimeWorkRequestBuilder<SyncWorker>()
    .setConstraints(Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build())
    .addTag("sync_$draftId")
    .build()
```

### Periodic Sync
```kotlin
val periodicSyncWork = PeriodicWorkRequestBuilder<SyncWorker>(
    30, TimeUnit.MINUTES,  // Interval
    5, TimeUnit.MINUTES    // Flex interval
)
    .setConstraints(Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build())
    .addTag("periodic_sync")
    .build()
```

## Storage Paths

### Supabase Storage Structure
```
snap-images/
  └── {user_id}/
      └── images/
          └── {timestamp}/
              └── {draft_id}.{ext}

snap-audio/
  └── {user_id}/
      └── audio/
          └── {timestamp}/
              └── {draft_id}.{ext}
```

## Error Handling

### Retry Logic
- Maksymalnie 3 próby synchronizacji
- Exponential backoff: 5s, 25s, 125s
- Maksymalny delay: 5 minut

### Error States
- **Network Error** - Retry po przywróceniu połączenia
- **Server Error** - Retry z backoff
- **Storage Error** - Retry upload plików
- **Validation Error** - Nie retry, pokaż błąd użytkownikowi

## Monitoring & Analytics

### Sync Events
```kotlin
sealed class SyncEvent {
    data class SnapCreated(val remoteId: String) : SyncEvent()
    data class SnapUpdated(val remoteId: String) : SyncEvent()
    data class SnapDeleted(val remoteId: String) : SyncEvent()
    data class SyncFailed(val draftId: String, val error: String) : SyncEvent()
    data class SyncProgress(val draftId: String, val progress: Int) : SyncEvent()
}
```

### Metrics
- Liczba pending drafts
- Success rate synchronizacji
- Average sync time
- Error frequency by type

## Security

### Row Level Security (RLS)
- Użytkownicy widzą tylko swoje snapy
- Storage policies z prefiksem `{user_id}/`
- Wszystkie operacje wymagają autentykacji

### Data Privacy
- Lokalne dane szyfrowane
- Pliki uploadowane tylko gdy użytkownik jest zalogowany
- Możliwość usunięcia wszystkich danych użytkownika

## Performance

### Optimizations
- Batch sync (max 10 snapów na raz)
- Compressed image uploads
- Background sync tylko gdy urządzenie jest na ładowaniu
- Smart retry logic

### Caching
- Lokalne cache dla offline access
- Thumbnail generation dla szybkiego podglądu
- Lazy loading dla dużych list

## Testing

### Unit Tests
- SyncEngine logic
- Error handling
- Retry mechanisms

### Integration Tests
- End-to-end sync flow
- WorkManager scheduling
- Storage upload/download

### UI Tests
- Offline/online scenarios
- Error state handling
- Progress indicators

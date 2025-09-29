# Local Storage Management

## Overview
This module provides comprehensive local storage management for SoulSnaps app, including data clearing functionality for GDPR compliance and user privacy.

## Features
- ✅ **Complete Data Clearing** - Clear all local storage data
- ✅ **Selective Data Clearing** - Clear only user-specific or sensitive data
- ✅ **GDPR Compliance** - Full compliance with data protection regulations
- ✅ **Session Management** - Automatic cleanup on session expiration
- ✅ **Error Handling** - Comprehensive error handling and logging
- ✅ **Storage Statistics** - Get storage usage information
- ✅ **Cleanup Detection** - Check if cleanup is needed

## Components

### LocalStorageManager
Centralized manager for all local storage operations.

```kotlin
class LocalStorageManager(
    private val memoryRepository: MemoryRepository,
    private val affirmationRepository: AffirmationRepository,
    private val userPreferencesStorage: UserPreferencesStorage,
    private val sessionDataStore: SessionDataStore,
    private val accessGuard: AccessGuard,
    private val crashlyticsManager: CrashlyticsManager
)
```

### ClearUserDataUseCase
Use case for clearing user data with different levels of granularity.

```kotlin
class ClearUserDataUseCase(
    private val localStorageManager: LocalStorageManager,
    private val userSessionManager: UserSessionManager
)
```

## Usage

### Basic Data Clearing
```kotlin
// Inject the use case
val clearUserDataUseCase: ClearUserDataUseCase by inject()

// Clear all user data (full GDPR compliance)
clearUserDataUseCase()

// Clear only user-specific data (memories, preferences)
clearUserDataUseCase.clearUserDataOnly()

// Clear only sensitive data (memories, session)
clearUserDataUseCase.clearSensitiveDataOnly()
```

### Storage Statistics
```kotlin
// Get storage statistics
val stats = clearUserDataUseCase.getStorageStats()
println("Memories: ${stats.memoriesCount}")
println("Total size: ${stats.totalSize} bytes")

// Check if cleanup is needed
val needsCleanup = clearUserDataUseCase.isCleanupNeeded()
```

### Direct LocalStorageManager Usage
```kotlin
// Inject LocalStorageManager
val localStorageManager: LocalStorageManager by inject()

// Clear all data for specific user
localStorageManager.clearAllLocalData("user123")

// Clear only user data
localStorageManager.clearUserDataOnly("user123")

// Clear only sensitive data
localStorageManager.clearSensitiveDataOnly("user123")

// Get storage statistics
val stats = localStorageManager.getStorageStats()

// Check if cleanup is needed
val needsCleanup = localStorageManager.isCleanupNeeded()
```

## Data Types Cleared

### Complete Data Clearing (`clearAllLocalData`)
- **Session Data** - User session information
- **User Preferences** - App settings and preferences
- **Memories** - All user memories and photos
- **Affirmations** - Favorite affirmations
- **Quota Data** - User quota and usage data
- **Analytics Data** - Analytics and tracking data

### User Data Only (`clearUserDataOnly`)
- **Session Data** - User session information
- **User Preferences** - App settings and preferences
- **Memories** - All user memories and photos
- **Affirmations** - Favorite affirmations
- **Quota Data** - User quota and usage data

### Sensitive Data Only (`clearSensitiveDataOnly`)
- **Session Data** - User session information
- **Memories** - All user memories and photos
- **Affirmations** - Favorite affirmations
- **Quota Data** - User quota and usage data

## Automatic Cleanup

### Session Expiration
When a user session expires, the system automatically clears all local storage:

```kotlin
// In UserSessionManager.onSessionExpired()
fun onSessionExpired() {
    _currentUser.update { null }
    _sessionState.update { SessionState.SessionExpired }
    
    // Clear local storage when session expires
    coroutineScope.launch {
        try {
            val currentUserId = _currentUser.value?.userId
            localStorageManager.clearAllLocalData(currentUserId)
            crashlyticsManager.log("Local storage cleared due to session expiration")
        } catch (e: Exception) {
            crashlyticsManager.recordException(e)
            crashlyticsManager.log("Error clearing local storage on session expiration: ${e.message}")
        }
    }
}
```

### Sign Out
During sign out, all local data is cleared:

```kotlin
// In SignOutUseCase
suspend operator fun invoke() {
    val currentUser = userSessionManager.getCurrentUser()
    val userId = currentUser?.userId ?: "unknown"
    
    // Clear all local storage data
    localStorageManager.clearAllLocalData(userId)
    
    // Sign out from auth service
    authRepository.signOut()
    
    // Clear user session
    userSessionManager.onUserSignedOut()
}
```

## Error Handling

All operations include comprehensive error handling:

```kotlin
try {
    localStorageManager.clearAllLocalData(userId)
    crashlyticsManager.log("Local storage cleared successfully")
} catch (e: Exception) {
    crashlyticsManager.recordException(e)
    crashlyticsManager.log("Error clearing local storage: ${e.message}")
    throw e
}
```

## GDPR Compliance

### Right to Erasure
Users can request complete deletion of their data:

```kotlin
// Clear all data (GDPR Article 17)
clearUserDataUseCase()
```

### Data Portability
Users can export their data before deletion:

```kotlin
// Get storage statistics
val stats = clearUserDataUseCase.getStorageStats()

// Export data (implementation depends on requirements)
exportUserData(stats)
```

### Consent Withdrawal
Users can withdraw consent and clear data:

```kotlin
// Clear sensitive data only
clearUserDataUseCase.clearSensitiveDataOnly()
```

## Storage Statistics

Get detailed information about stored data:

```kotlin
data class StorageStats(
    val memoriesCount: Int = 0,
    val affirmationsCount: Int = 0,
    val preferencesSize: Long = 0,
    val sessionDataSize: Long = 0,
    val totalSize: Long = 0
)
```

## Testing

### Unit Tests
```kotlin
@Test
fun `should clear all local data`() = runTest {
    // Given
    val localStorageManager = LocalStorageManager(...)
    
    // When
    localStorageManager.clearAllLocalData("user123")
    
    // Then
    assertFalse(localStorageManager.isCleanupNeeded())
}
```

### Integration Tests
```kotlin
@Test
fun `should clear data on session expiration`() = runTest {
    // Given
    val userSessionManager = UserSessionManager(...)
    
    // When
    userSessionManager.onSessionExpired()
    
    // Then
    // Verify all data is cleared
}
```

## Best Practices

1. **Always use try-catch** - Wrap cleanup operations in error handling
2. **Log operations** - Log all cleanup operations for debugging
3. **User confirmation** - Ask user for confirmation before clearing data
4. **Backup important data** - Consider backing up data before clearing
5. **Test thoroughly** - Test all cleanup scenarios
6. **Monitor storage** - Monitor storage usage and cleanup operations
7. **GDPR compliance** - Ensure all operations comply with GDPR
8. **Performance** - Consider performance impact of large data clearing

## Security Considerations

1. **Secure deletion** - Ensure data is securely deleted from storage
2. **No recovery** - Make sure deleted data cannot be recovered
3. **Audit trail** - Keep audit trail of data clearing operations
4. **User consent** - Always get user consent before clearing data
5. **Data retention** - Follow data retention policies

## Troubleshooting

### Common Issues
- **Data not cleared** - Check if all repositories are properly injected
- **Performance issues** - Consider clearing data in background
- **Error during cleanup** - Check error logs and handle exceptions
- **Storage not updated** - Ensure all storage providers are updated

### Debug Information
```kotlin
// Check if cleanup is needed
val needsCleanup = localStorageManager.isCleanupNeeded()

// Get storage statistics
val stats = localStorageManager.getStorageStats()

// Check Crashlytics logs for cleanup operations
```

## Future Enhancements

1. **Selective memory deletion** - Allow users to delete specific memories
2. **Data export** - Export user data before deletion
3. **Scheduled cleanup** - Automatic cleanup of old data
4. **Storage optimization** - Optimize storage usage
5. **Cloud sync** - Sync cleanup operations with cloud storage
6. **User preferences** - Allow users to configure cleanup behavior


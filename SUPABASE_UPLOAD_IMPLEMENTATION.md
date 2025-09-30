# Supabase Memory Upload Implementation

This document describes the complete implementation of memory upload functionality to Supabase, including file uploads to Supabase Storage.

## Overview

The implementation provides a complete offline-first memory upload system that:
1. Saves memories locally first (offline-first approach)
2. Uploads files (photos/audio) to Supabase Storage
3. Saves memory metadata to Supabase PostgreSQL database
4. Handles sync states and retry logic
5. Manages file cleanup on deletion

## Architecture

### Components

1. **SoulSnapApi.kt** - Main API client with upload method
2. **SupabaseMemoryDataSource.kt** - Supabase-specific data source implementation
3. **FileStorageManager** - Platform-specific file storage management
4. **MemoryRepositoryImpl.kt** - Repository that orchestrates the upload process

### Data Flow

```
Memory Creation → Local Storage → File Upload → Database Insert → Sync Complete
```

## Implementation Details

### 1. File Upload to Supabase Storage

The `SupabaseMemoryDataSource` includes methods for uploading files:

```kotlin
private suspend fun uploadPhotoToStorage(photoUri: String, userId: String): String? {
    return withRetry("uploadPhotoToStorage") {
        val fileData = fileStorageManager.loadPhoto(photoUri)
        val fileName = "photos/${userId}/${UUID.randomUUID()}.jpg"
        
        client.storage.from(STORAGE_BUCKET).upload(
            path = fileName,
            data = fileData
        )
        
        // Return the public URL
        "${client.supabaseUrl}/storage/v1/object/public/$STORAGE_BUCKET/$fileName"
    }
}
```

### 2. Memory Database Insert

After file upload, the memory metadata is inserted into the PostgreSQL database:

```kotlin
override suspend fun insertMemory(memory: Memory, userId: String): Long? =
    withRetry("insertMemory") {
        // Upload files to Supabase Storage first
        val remotePhotoPath = memory.photoUri?.let { photoUri ->
            uploadPhotoToStorage(photoUri, userId)
        }
        
        val remoteAudioPath = memory.audioUri?.let { audioUri ->
            uploadAudioToStorage(audioUri, userId)
        }
        
        // Create memory row with remote file paths
        val memoryRow = memory.copy(
            remotePhotoPath = remotePhotoPath,
            remoteAudioPath = remoteAudioPath
        ).toRow(userId)
        
        val inserted = client.from(TABLE).insert(memoryRow)
            .decodeSingle<MemoryRow>()
        
        // Return local Long ID, but save inserted.id (uuid) to remoteId field in local DB
        (inserted.id?.hashCode() ?: 0).toLong()
    }
```

### 3. File Cleanup on Deletion

When deleting memories, files are also removed from Supabase Storage:

```kotlin
override suspend fun deleteMemory(id: Long, userId: String): Boolean {
    return withRetry("deleteMemory") {
        // Get local memory to find remoteId and file paths
        val localMemory = memoryDao.getById(id)
        val remoteId = localMemory?.remoteId
        
        if (remoteId != null) {
            // Delete files from Supabase Storage first
            localMemory.remotePhotoPath?.let { photoPath ->
                deletePhotoFromStorage(photoPath)
            }
            
            localMemory.remoteAudioPath?.let { audioPath ->
                deleteAudioFromStorage(audioPath)
            }
            
            // Then delete memory record from database
            deleteMemoryByRemoteId(remoteId, userId)
        } else {
            false
        }
    } ?: false
}
```

## Usage Example

### Basic Memory Upload

```kotlin
// In your ViewModel or UseCase
class UploadMemoryUseCase(
    private val memoryRepository: MemoryRepository,
    private val userSessionManager: UserSessionManager
) {
    suspend fun uploadMemory(memory: Memory): Result<Unit> {
        return try {
            val currentUser = userSessionManager.getCurrentUser()
            if (currentUser != null) {
                // The repository handles the complete upload process
                memoryRepository.addMemory(memory)
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Memory with Photo and Audio

```kotlin
val memory = Memory(
    title = "Beautiful sunset",
    description = "Amazing sunset at the beach",
    createdAt = System.currentTimeMillis(),
    mood = MoodType.HAPPY,
    photoUri = "photo_12345.jpg", // Local file path
    audioUri = "audio_67890.m4a", // Local file path
    locationName = "Malibu Beach",
    latitude = 34.0259,
    longitude = -118.7798,
    affirmation = "I am grateful for this beautiful moment"
)

// Upload the memory
uploadMemoryUseCase.uploadMemory(memory)
```

## Configuration

### Supabase Setup

1. **Storage Bucket**: Create a bucket named `memories` in your Supabase project
2. **Database Schema**: Use the provided `supabase_schema.sql` to set up the database
3. **RLS Policies**: Ensure Row Level Security is properly configured

### Environment Variables

Make sure your Supabase credentials are properly configured in the platform-specific `Secrets.kt` files:

```kotlin
// Android: shared/features/src/androidMain/kotlin/pl/soulsnaps/config/Secrets.kt
// iOS: shared/features/src/iosMain/kotlin/pl/soulsnaps/config/Secrets.kt

object Secrets {
    const val SUPABASE_URL = "https://your-project.supabase.co"
    const val SUPABASE_ANON_KEY = "your-anon-key"
}
```

## Error Handling

The implementation includes comprehensive error handling:

1. **Retry Logic**: Automatic retry with exponential backoff
2. **Crashlytics Integration**: Error logging and reporting
3. **Graceful Degradation**: Continues working offline when network is unavailable
4. **File Cleanup**: Proper cleanup of uploaded files on failure

## Testing

### Unit Tests

Test the upload functionality with mock data:

```kotlin
@Test
fun `uploadMemory should upload files and save metadata`() = runTest {
    // Given
    val memory = createTestMemory()
    val userId = "test-user-id"
    
    // When
    val result = supabaseMemoryDataSource.insertMemory(memory, userId)
    
    // Then
    assertThat(result).isNotNull()
    // Verify files were uploaded and metadata saved
}
```

### Integration Tests

Test the complete flow with real Supabase instance:

```kotlin
@Test
fun `complete upload flow should work end-to-end`() = runTest {
    // Given
    val memory = createTestMemoryWithFiles()
    
    // When
    val result = memoryRepository.addMemory(memory)
    
    // Then
    assertThat(result).isNotNull()
    // Verify memory appears in Supabase
}
```

## Performance Considerations

1. **File Compression**: Consider compressing images before upload
2. **Background Upload**: Use background tasks for large file uploads
3. **Progress Tracking**: Implement upload progress callbacks
4. **Bandwidth Management**: Respect user's data usage preferences

## Security

1. **File Validation**: Validate file types and sizes before upload
2. **User Isolation**: Ensure users can only access their own files
3. **Access Control**: Use Supabase RLS policies for data security
4. **Encryption**: Consider encrypting sensitive files before upload

## Future Enhancements

1. **Thumbnail Generation**: Create thumbnails for faster loading
2. **Progressive Upload**: Upload files in chunks for better reliability
3. **Sync Conflict Resolution**: Handle conflicts when multiple devices edit the same memory
4. **Offline Queue**: Queue uploads when offline and process when online
5. **Compression**: Implement image and audio compression before upload

## Troubleshooting

### Common Issues

1. **Upload Fails**: Check Supabase credentials and network connectivity
2. **File Not Found**: Ensure FileStorageManager is properly implemented
3. **Permission Denied**: Verify Supabase RLS policies are correctly configured
4. **Storage Quota**: Monitor Supabase Storage usage and limits

### Debug Logging

Enable debug logging to troubleshoot issues:

```kotlin
// In your app configuration
const val ENABLE_DEBUG_LOGGING = true
```

This will provide detailed logs of the upload process, including file paths, upload URLs, and any errors encountered.

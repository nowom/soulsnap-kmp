package pl.soulsnaps.data

import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import pl.soulsnaps.crashlytics.CrashlyticsManager
import pl.soulsnaps.database.dao.MemoryDao
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.storage.FileStorageManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlin.random.Random

class SupabaseMemoryDataSourceTest {

    private val mockClient = mockk<SupabaseClient>()
    private val mockCrashlyticsManager = mockk<CrashlyticsManager>()
    private val mockMemoryDao = mockk<MemoryDao>()
    private val mockUserSessionManager = mockk<UserSessionManager>()
    private val mockFileStorageManager = mockk<FileStorageManager>()
    
    private val dataSource = SupabaseMemoryDataSource(
        client = mockClient,
        crashlyticsManager = mockCrashlyticsManager,
        memoryDao = mockMemoryDao,
        userSessionManager = mockUserSessionManager,
        fileStorageManager = mockFileStorageManager
    )

    @Test
    fun `test upload photo to storage success`() = runTest {
        // Given
        val photoUri = "local_photo_path.jpg"
        val userId = "test_user_123"
        val fileData = "mock_photo_data".toByteArray()
        val fileName = "photos/$userId/${Random.nextLong()}.jpg"
        
        every { mockFileStorageManager.loadPhoto(photoUri) } returns fileData
        every { mockClient.storage } returns mockk<Storage>()
        every { mockClient.storage.from(any()) } returns mockk()
        every { mockClient.storage.from(any()).upload(any(), any()) } just Runs
        every { mockClient.supabaseUrl } returns "https://test.supabase.co"
        
        // When
        val result = dataSource.javaClass.getDeclaredMethod("uploadPhotoToStorage", String::class.java, String::class.java)
            .apply { isAccessible = true }
            .invoke(dataSource, photoUri, userId) as String?
        
        // Then
        assertNotNull(result)
        assertTrue(result!!.contains("https://test.supabase.co/storage/v1/object/public/memories/photos/"))
    }

    @Test
    fun `test upload photo to storage failure`() = runTest {
        // Given
        val photoUri = "invalid_photo_path.jpg"
        val userId = "test_user_123"
        
        every { mockFileStorageManager.loadPhoto(photoUri) } returns null
        every { mockCrashlyticsManager.log(any()) } just Runs
        
        // When
        val result = dataSource.javaClass.getDeclaredMethod("uploadPhotoToStorage", String::class.java, String::class.java)
            .apply { isAccessible = true }
            .invoke(dataSource, photoUri, userId) as String?
        
        // Then
        assertNull(result)
        verify { mockCrashlyticsManager.log(contains("Failed to load photo data")) }
    }

    @Test
    fun `test insert memory with files`() = runTest {
        // Given
        val memory = Memory(
            id = 1,
            title = "Test Memory",
            description = "Test Description",
            timestamp = System.currentTimeMillis(),
            mood = MoodType.HAPPY,
            photoUri = "local_photo.jpg",
            audioUri = "local_audio.m4a",
            locationName = "Test Location",
            latitude = 52.2297,
            longitude = 21.0122
        )
        val userId = "test_user_123"
        val fileData = "mock_file_data".toByteArray()
        
        every { mockFileStorageManager.loadPhoto(any()) } returns fileData
        every { mockFileStorageManager.loadAudio(any()) } returns fileData
        every { mockClient.storage } returns mockk<Storage>()
        every { mockClient.storage.from(any()) } returns mockk()
        every { mockClient.storage.from(any()).upload(any(), any()) } just Runs
        every { mockClient.supabaseUrl } returns "https://test.supabase.co"
        every { mockClient.from(any()) } returns mockk()
        every { mockClient.from(any()).insert(any()) } returns mockk()
        every { mockClient.from(any()).insert(any()).decodeSingle<Any>() } returns mockk()
        
        // When
        val result = dataSource.insertMemory(memory, userId)
        
        // Then
        assertNotNull(result)
        verify { mockFileStorageManager.loadPhoto("local_photo.jpg") }
        verify { mockFileStorageManager.loadAudio("local_audio.m4a") }
    }

    @Test
    fun `test delete memory with files`() = runTest {
        // Given
        val memoryId = 1L
        val userId = "test_user_123"
        val remoteId = "remote_123"
        val photoUrl = "https://test.supabase.co/storage/v1/object/public/memories/photos/test.jpg"
        val audioUrl = "https://test.supabase.co/storage/v1/object/public/memories/audio/test.m4a"
        
        val localMemory = mockk<pl.soulsnaps.database.model.Memory> {
            every { remoteId } returns remoteId
            every { remotePhotoPath } returns photoUrl
            every { remoteAudioPath } returns audioUrl
        }
        
        every { mockMemoryDao.getById(memoryId) } returns localMemory
        every { mockClient.storage } returns mockk<Storage>()
        every { mockClient.storage.from(any()) } returns mockk()
        every { mockClient.storage.from(any()).delete(any()) } just Runs
        every { mockClient.from(any()) } returns mockk()
        every { mockClient.from(any()).delete(any()) } returns mockk()
        
        // When
        val result = dataSource.deleteMemory(memoryId, userId)
        
        // Then
        assertTrue(result)
        verify { mockClient.storage.from("memories").delete("photos/test.jpg") }
        verify { mockClient.storage.from("memories").delete("audio/test.m4a") }
    }

    @Test
    fun `test retry mechanism on failure`() = runTest {
        // Given
        val photoUri = "test_photo.jpg"
        val userId = "test_user_123"
        val fileData = "mock_data".toByteArray()
        
        every { mockFileStorageManager.loadPhoto(photoUri) } returns fileData
        every { mockClient.storage } returns mockk<Storage>()
        every { mockClient.storage.from(any()) } returns mockk()
        every { mockClient.storage.from(any()).upload(any(), any()) } throws RuntimeException("Network error")
        every { mockCrashlyticsManager.log(any()) } just Runs
        every { mockCrashlyticsManager.recordException(any()) } just Runs
        
        // When
        val result = dataSource.javaClass.getDeclaredMethod("uploadPhotoToStorage", String::class.java, String::class.java)
            .apply { isAccessible = true }
            .invoke(dataSource, photoUri, userId) as String?
        
        // Then
        assertNull(result)
        verify(exactly = 3) { mockClient.storage.from(any()).upload(any(), any()) }
        verify { mockCrashlyticsManager.recordException(any()) }
    }
}
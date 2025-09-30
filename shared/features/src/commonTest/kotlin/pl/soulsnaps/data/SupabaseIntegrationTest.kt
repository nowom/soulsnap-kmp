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
import pl.soulsnaps.sync.offline.OfflineSyncQueue
import pl.soulsnaps.sync.offline.OfflineSyncProcessor
import pl.soulsnaps.sync.connectivity.ConnectivityMonitor
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SupabaseIntegrationTest {

    @Test
    fun `test end-to-end memory upload and download`() = runTest {
        // Given
        val mockClient = mockk<SupabaseClient>()
        val mockCrashlyticsManager = mockk<CrashlyticsManager>()
        val mockMemoryDao = mockk<MemoryDao>()
        val mockUserSessionManager = mockk<UserSessionManager>()
        val mockFileStorageManager = mockk<FileStorageManager>()
        val mockConnectivityMonitor = mockk<ConnectivityMonitor>()
        
        val dataSource = SupabaseMemoryDataSource(
            client = mockClient,
            crashlyticsManager = mockCrashlyticsManager,
            memoryDao = mockMemoryDao,
            userSessionManager = mockUserSessionManager,
            fileStorageManager = mockFileStorageManager
        )
        
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
        
        // Mock setup
        every { mockFileStorageManager.loadPhoto(any()) } returns fileData
        every { mockFileStorageManager.loadAudio(any()) } returns fileData
        every { mockClient.storage } returns mockk()
        every { mockClient.storage.from(any()) } returns mockk()
        every { mockClient.storage.from(any()).upload(any(), any()) } just Runs
        every { mockClient.supabaseUrl } returns "https://test.supabase.co"
        every { mockClient.from(any()) } returns mockk()
        every { mockClient.from(any()).insert(any()) } returns mockk()
        every { mockClient.from(any()).insert(any()).decodeSingle<Any>() } returns mockk()
        every { mockCrashlyticsManager.log(any()) } just Runs
        
        // When
        val result = dataSource.insertMemory(memory, userId)
        
        // Then
        assertNotNull(result)
        verify { mockFileStorageManager.loadPhoto("local_photo.jpg") }
        verify { mockFileStorageManager.loadAudio("local_audio.m4a") }
        verify { mockClient.storage.from("memories").upload(any(), any()) }
    }

    @Test
    fun `test offline sync queue operations`() = runTest {
        // Given
        val syncQueue = OfflineSyncQueue()
        val memory = Memory(
            id = 1,
            title = "Test Memory",
            description = "Test Description",
            timestamp = System.currentTimeMillis(),
            mood = MoodType.HAPPY
        )
        val userId = "test_user_123"
        
        // When
        syncQueue.addOperation(
            type = pl.soulsnaps.sync.offline.SyncOperationType.INSERT,
            memory = memory,
            userId = userId,
            priority = pl.soulsnaps.sync.offline.SyncPriority.HIGH
        )
        
        // Then
        assertEquals(1, syncQueue.getOperationsCount())
        assertFalse(syncQueue.isEmpty())
        
        val nextOperation = syncQueue.getNextOperation()
        assertNotNull(nextOperation)
        assertEquals(pl.soulsnaps.sync.offline.SyncOperationType.INSERT, nextOperation!!.type)
        assertEquals(pl.soulsnaps.sync.offline.SyncPriority.HIGH, nextOperation.priority)
        
        // Mark as completed
        syncQueue.markCompleted(nextOperation.id)
        assertEquals(0, syncQueue.getOperationsCount())
        assertTrue(syncQueue.isEmpty())
    }

    @Test
    fun `test offline sync processor with connectivity`() = runTest {
        // Given
        val syncQueue = OfflineSyncQueue()
        val mockOnlineDataSource = mockk<OnlineDataSource>()
        val mockConnectivityMonitor = mockk<ConnectivityMonitor>()
        val mockCrashlyticsManager = mockk<CrashlyticsManager>()
        
        val connectivityFlow = MutableStateFlow(true)
        every { mockConnectivityMonitor.connected } returns connectivityFlow
        every { mockCrashlyticsManager.log(any()) } just Runs
        
        val processor = OfflineSyncProcessor(
            syncQueue = syncQueue,
            onlineDataSource = mockOnlineDataSource,
            connectivityMonitor = mockConnectivityMonitor,
            crashlyticsManager = mockCrashlyticsManager
        )
        
        val memory = Memory(
            id = 1,
            title = "Test Memory",
            description = "Test Description",
            timestamp = System.currentTimeMillis(),
            mood = MoodType.HAPPY
        )
        
        // Add operation to queue
        syncQueue.addOperation(
            type = pl.soulsnaps.sync.offline.SyncOperationType.INSERT,
            memory = memory,
            userId = "test_user_123"
        )
        
        // Mock successful processing
        every { mockOnlineDataSource.insertMemory(any(), any()) } returns 1L
        
        // When
        processor.start()
        processor.processPendingOperations()
        
        // Then
        assertEquals(0, syncQueue.getOperationsCount())
        verify { mockOnlineDataSource.insertMemory(any(), any()) }
    }

    @Test
    fun `test error handling and retry mechanism`() = runTest {
        // Given
        val mockClient = mockk<SupabaseClient>()
        val mockCrashlyticsManager = mockk<CrashlyticsManager>()
        val mockMemoryDao = mockk<MemoryDao>()
        val mockUserSessionManager = mockk<UserSessionManager>()
        val mockFileStorageManager = mockk<FileStorageManager>()
        
        val dataSource = SupabaseMemoryDataSource(
            client = mockClient,
            crashlyticsManager = mockCrashlyticsManager,
            memoryDao = mockMemoryDao,
            userSessionManager = mockUserSessionManager,
            fileStorageManager = mockFileStorageManager
        )
        
        val photoUri = "test_photo.jpg"
        val userId = "test_user_123"
        val fileData = "mock_data".toByteArray()
        
        // Mock network error
        every { mockFileStorageManager.loadPhoto(photoUri) } returns fileData
        every { mockClient.storage } returns mockk<io.github.jan.supabase.storage.Storage>()
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

    @Test
    fun `test sync status monitoring`() = runTest {
        // Given
        val syncQueue = OfflineSyncQueue()
        val mockOnlineDataSource = mockk<OnlineDataSource>()
        val mockConnectivityMonitor = mockk<ConnectivityMonitor>()
        val mockCrashlyticsManager = mockk<CrashlyticsManager>()
        
        val connectivityFlow = MutableStateFlow(false)
        every { mockConnectivityMonitor.connected } returns connectivityFlow
        every { mockCrashlyticsManager.log(any()) } just Runs
        
        val processor = OfflineSyncProcessor(
            syncQueue = syncQueue,
            onlineDataSource = mockOnlineDataSource,
            connectivityMonitor = mockConnectivityMonitor,
            crashlyticsManager = mockCrashlyticsManager
        )
        
        // When
        val status = processor.getSyncStatus()
        
        // Then
        assertEquals(0, status.pendingOperations)
        assertEquals(0, status.failedOperations)
        assertFalse(status.isConnected)
        assertFalse(status.isProcessing)
        assertFalse(status.hasPendingWork)
        assertFalse(status.hasFailedWork)
        assertFalse(status.canSync)
    }
}

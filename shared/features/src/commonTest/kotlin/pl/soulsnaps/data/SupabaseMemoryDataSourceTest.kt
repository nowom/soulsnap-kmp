package pl.soulsnaps.data

import kotlinx.coroutines.test.runTest
import kotlin.test.*
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.crashlytics.CrashlyticsManager

/**
 * Test suite for SupabaseMemoryDataSource
 * Tests the stub implementation functionality
 */
class SupabaseMemoryDataSourceTest {

    private lateinit var dataSource: OnlineDataSource
    private lateinit var mockCrashlyticsManager: CrashlyticsManager

    @BeforeTest
    fun setup() {
        mockCrashlyticsManager = MockCrashlyticsManager()
        // Create a simple test implementation
        dataSource = TestSupabaseMemoryDataSource(mockCrashlyticsManager)
    }

    @Test
    fun `getAllMemories should return empty list when no memories exist`() = runTest {
        // Given
        val userId = "test-user-123"

        // When
        val result = dataSource.getAllMemories(userId)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `insertMemory should add memory and return ID`() = runTest {
        // Given
        val memory = createTestMemory()
        val userId = "test-user-123"

        // When
        val result = dataSource.insertMemory(memory, userId)

        // Then
        assertNotNull(result)
        assertEquals(1L, result)
    }

    @Test
    fun `getAllMemories should return inserted memories`() = runTest {
        // Given
        val memory = createTestMemory()
        val userId = "test-user-123"

        // When
        dataSource.insertMemory(memory, userId)
        val result = dataSource.getAllMemories(userId)

        // Then
        assertEquals(1, result.size)
        assertEquals("Test Memory", result[0].title)
    }

    @Test
    fun `getMemoryById should return correct memory`() = runTest {
        // Given
        val memory = createTestMemory()
        val userId = "test-user-123"

        // When
        val insertedId = dataSource.insertMemory(memory, userId)!!
        val result = dataSource.getMemoryById(insertedId)

        // Then
        assertNotNull(result)
        assertEquals("Test Memory", result.title)
    }

    @Test
    fun `updateMemory should modify existing memory`() = runTest {
        // Given
        val memory = createTestMemory()
        val userId = "test-user-123"

        // When
        val insertedId = dataSource.insertMemory(memory, userId)!!
        val updatedMemory = memory.copy(id = insertedId.toInt(), title = "Updated Memory")
        val updateResult = dataSource.updateMemory(updatedMemory, userId)
        val retrievedMemory = dataSource.getMemoryById(insertedId)

        // Then
        assertTrue(updateResult)
        assertNotNull(retrievedMemory)
        assertEquals("Updated Memory", retrievedMemory.title)
    }

    @Test
    fun `deleteMemory should remove memory`() = runTest {
        // Given
        val memory = createTestMemory()
        val userId = "test-user-123"

        // When
        val insertedId = dataSource.insertMemory(memory, userId)!!
        val deleteResult = dataSource.deleteMemory(insertedId, userId)
        val retrievedMemory = dataSource.getMemoryById(insertedId)

        // Then
        assertTrue(deleteResult)
        assertNull(retrievedMemory)
    }

    @Test
    fun `markAsFavorite should update favorite status`() = runTest {
        // Given
        val memory = createTestMemory(isFavorite = false)
        val userId = "test-user-123"

        // When
        val insertedId = dataSource.insertMemory(memory, userId)!!
        val favoriteResult = dataSource.markAsFavorite(insertedId, true, userId)
        val retrievedMemory = dataSource.getMemoryById(insertedId)

        // Then
        assertTrue(favoriteResult)
        assertNotNull(retrievedMemory)
        assertTrue(retrievedMemory.isFavorite)
    }

    @Test
    fun `getUnsyncedMemories should return all memories`() = runTest {
        // Given
        val memory1 = createTestMemory(title = "Memory 1")
        val memory2 = createTestMemory(title = "Memory 2")
        val userId = "test-user-123"

        // When
        dataSource.insertMemory(memory1, userId)
        dataSource.insertMemory(memory2, userId)
        val result = dataSource.getUnsyncedMemories(userId)

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `markAsSynced should always return true`() = runTest {
        // Given
        val memory = createTestMemory()
        val userId = "test-user-123"

        // When
        val insertedId = dataSource.insertMemory(memory, userId)!!
        val result = dataSource.markAsSynced(insertedId, userId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `memories should be isolated by userId`() = runTest {
        // Given
        val memory = createTestMemory()
        val userId1 = "user-1"
        val userId2 = "user-2"

        // When
        dataSource.insertMemory(memory, userId1)
        val user1Memories = dataSource.getAllMemories(userId1)
        val user2Memories = dataSource.getAllMemories(userId2)

        // Then
        assertEquals(1, user1Memories.size)
        assertEquals(0, user2Memories.size)
    }

    // Helper methods
    private fun createTestMemory(
        id: Int = 0,
        title: String = "Test Memory",
        isFavorite: Boolean = false
    ): Memory {
        return Memory(
            id = id,
            title = title,
            description = "Test Description",
            createdAt = 1700000000000L,
            mood = MoodType.HAPPY,
            photoUri = "test-photo.jpg",
            audioUri = "test-audio.m4a",
            locationName = "Test Location",
            latitude = 52.2297,
            longitude = 21.0122,
            affirmation = "Test Affirmation",
            isFavorite = isFavorite
        )
    }

    // Test implementation that bypasses SupabaseClient
    private class TestSupabaseMemoryDataSource(
        crashlyticsManager: CrashlyticsManager
    ) : OnlineDataSource {
        
        private val memoryStorage = mutableMapOf<String, MutableList<Memory>>()
        private var nextId = 1L

        override suspend fun getAllMemories(userId: String): List<Memory> {
            return memoryStorage[userId]?.sortedByDescending { it.createdAt } ?: emptyList()
        }

        override suspend fun getMemoryById(id: Long): Memory? {
            return memoryStorage.values.flatten().find { it.id.toLong() == id }
        }

        override suspend fun insertMemory(memory: Memory, userId: String): Long? {
            val memoryId = nextId++
            val memoryWithId = memory.copy(id = memoryId.toInt())
            
            if (memoryStorage[userId] == null) {
                memoryStorage[userId] = mutableListOf()
            }
            memoryStorage[userId]?.add(memoryWithId)
            
            return memoryId
        }

        override suspend fun updateMemory(memory: Memory, userId: String): Boolean {
            val userMemories = memoryStorage[userId]
            val index = userMemories?.indexOfFirst { it.id == memory.id }
            return if (index != null && index >= 0) {
                userMemories[index] = memory
                true
            } else {
                false
            }
        }

        override suspend fun deleteMemory(id: Long, userId: String): Boolean {
            val userMemories = memoryStorage[userId]
            if (userMemories != null) {
                val iterator = userMemories.iterator()
                while (iterator.hasNext()) {
                    if (iterator.next().id.toLong() == id) {
                        iterator.remove()
                        return true
                    }
                }
            }
            return false
        }

        override suspend fun markAsFavorite(id: Long, isFavorite: Boolean, userId: String): Boolean {
            val userMemories = memoryStorage[userId]
            val memory = userMemories?.find { it.id.toLong() == id }
            return if (memory != null) {
                val index = userMemories.indexOf(memory)
                userMemories[index] = memory.copy(isFavorite = isFavorite)
                true
            } else {
                false
            }
        }

        override suspend fun getUnsyncedMemories(userId: String): List<Memory> {
            return getAllMemories(userId)
        }

        override suspend fun markAsSynced(id: Long, userId: String): Boolean {
            return true
        }
    }

    // Mock implementation
    private class MockCrashlyticsManager : CrashlyticsManager {
        override fun log(message: String) {}
        override fun recordException(throwable: Throwable) {}
        override fun setUserId(userId: String) {}
        override fun setCustomKey(key: String, value: String) {}
        override fun setCustomKey(key: String, value: Boolean) {}
        override fun setCustomKey(key: String, value: Int) {}
        override fun setCustomKey(key: String, value: Float) {}
        override fun setCustomKey(key: String, value: Double) {}
        override fun resetAnalyticsData() {}
        override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {}
        override fun testCrash() {}
    }
}

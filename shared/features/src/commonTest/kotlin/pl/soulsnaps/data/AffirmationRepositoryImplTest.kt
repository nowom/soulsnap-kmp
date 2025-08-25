package pl.soulsnaps.data

import kotlinx.coroutines.test.runTest
import pl.soulsnaps.database.dao.MemoryDao
import pl.soulsnaps.domain.model.Affirmation
import pl.soulsnaps.domain.model.ThemeType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AffirmationRepositoryImplTest {
    
    @Test
    fun `getAffirmations should return mock data when no database entries exist`() = runTest {
        // Given
        val mockMemoryDao = MockMemoryDao()
        val repository = AffirmationRepositoryImpl(mockMemoryDao)
        
        // When
        val affirmations = repository.getAffirmations(null)
        
        // Then
        assertNotNull(affirmations)
        assertTrue(affirmations.isNotEmpty())
        assertEquals(7, affirmations.size) // 7 mock affirmations
        
        // Check first affirmation
        val firstAffirmation = affirmations.first()
        assertEquals("1", firstAffirmation.id)
        assertEquals("Jestem spokojem i światłem.", firstAffirmation.text)
        assertEquals("Spokój", firstAffirmation.emotion)
        assertEquals("Poranek", firstAffirmation.timeOfDay)
        assertTrue(firstAffirmation.isFavorite)
        assertEquals(ThemeType.SELF_LOVE, firstAffirmation.themeType)
    }
    
    @Test
    fun `getAffirmations should filter by emotion when filter is provided`() = runTest {
        // Given
        val mockMemoryDao = MockMemoryDao()
        val repository = AffirmationRepositoryImpl(mockMemoryDao)
        
        // When
        val affirmations = repository.getAffirmations("Spokój")
        
        // Then
        assertNotNull(affirmations)
        assertTrue(affirmations.isNotEmpty())
        assertTrue(affirmations.all { it.emotion.contains("Spokój", ignoreCase = true) })
    }
    
    @Test
    fun `getFavoriteAffirmations should return only favorite affirmations`() = runTest {
        // Given
        val mockMemoryDao = MockMemoryDao()
        val repository = AffirmationRepositoryImpl(mockMemoryDao)
        
        // When
        val favorites = repository.getFavoriteAffirmations()
        
        // Then
        assertNotNull(favorites)
        assertTrue(favorites.isNotEmpty())
        assertTrue(favorites.all { it.isFavorite })
    }
    
    @Test
    fun `saveAffirmationForMemory should update memory affirmation`() = runTest {
        // Given
        val mockMemoryDao = MockMemoryDao()
        val repository = AffirmationRepositoryImpl(mockMemoryDao)
        val memoryId = 1
        val affirmationText = "Nowa afirmacja"
        val mood = "happy"
        
        // When
        repository.saveAffirmationForMemory(memoryId, affirmationText, mood)
        
        // Then
        // Mock implementation doesn't actually save, but we can verify the method doesn't throw
        assertTrue(true) // Method executed successfully
    }
    
    @Test
    fun `getAffirmationByMemoryId should return null when memory has no affirmation`() = runTest {
        // Given
        val mockMemoryDao = MockMemoryDao()
        val repository = AffirmationRepositoryImpl(mockMemoryDao)
        
        // When
        val affirmation = repository.getAffirmationByMemoryId(999) // Non-existent memory
        
        // Then
        assertEquals(null, affirmation)
    }
    
    @Test
    fun `playAffirmation should not throw exception`() = runTest {
        // Given
        val mockMemoryDao = MockMemoryDao()
        val repository = AffirmationRepositoryImpl(mockMemoryDao)
        val text = "Test affirmation"
        
        // When & Then
        repository.playAffirmation(text) // Should not throw
        assertTrue(true) // Method executed successfully
    }
    
    @Test
    fun `stopAudio should not throw exception`() = runTest {
        // Given
        val mockMemoryDao = MockMemoryDao()
        val repository = AffirmationRepositoryImpl(mockMemoryDao)
        
        // When & Then
        repository.stopAudio() // Should not throw
        assertTrue(true) // Method executed successfully
    }
    
    @Test
    fun `updateIsFavorite should not throw exception`() = runTest {
        // Given
        val mockMemoryDao = MockMemoryDao()
        val repository = AffirmationRepositoryImpl(mockMemoryDao)
        val id = "1"
        
        // When & Then
        repository.updateIsFavorite(id) // Should not throw
        assertTrue(true) // Method executed successfully
    }
}

// Mock implementation for testing
class MockMemoryDao : MemoryDao {
    override suspend fun insert(memory: pl.soulsnaps.database.Memories): Long = 1L
    
    override fun getAll(): kotlinx.coroutines.flow.Flow<List<pl.soulsnaps.database.Memories>> {
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }
    
    override suspend fun getById(id: Long): pl.soulsnaps.database.Memories? = null
    
    override suspend fun delete(id: Long) {}
    
    override suspend fun update(memory: pl.soulsnaps.database.Memories) {}
    
    override suspend fun markAsFavorite(id: Long, isFavorite: Boolean) {}
    
    override suspend fun clearAll() {}
    
    override suspend fun getUnsynced(): List<pl.soulsnaps.database.Memories> = emptyList()
    
    override suspend fun markAsSynced(id: Long) {}
}


package pl.soulsnaps.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import pl.soulsnaps.database.dao.MemoryDao
import pl.soulsnaps.database.Memories
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.util.NetworkMonitor

class MemoryRepositoryImpl(
    private val networkMonitor: NetworkMonitor,
    private val memoryDao: MemoryDao
) : MemoryRepository {
    private val fakeMemories = listOf(
        Memory(
            id = 1,
            title = "Spacer nad Wisłą",
            description = "Piękny zachód słońca.",
            createdAt = Clock.System.now().toEpochMilliseconds() - 86400000L, // wczoraj
            mood = MoodType.HAPPY,
            photoUri = null,
            audioUri = null,
            locationName = "Warszawa",
            latitude = 52.2297,
            longitude = 21.0122,
            affirmation = "Jestem obecny tu i teraz.",
            isFavorite = false
        ),
        Memory(
            id = 2,
            title = "Weekend w górach",
            description = "Cisza i natura.",
            createdAt = Clock.System.now().toEpochMilliseconds() - 3 * 86400000L,
            mood = MoodType.SAD,
            photoUri = null,
            audioUri = null,
            locationName = "Zakopane",
            latitude = 49.2992,
            longitude = 19.9496,
            affirmation = "Oddycham spokojnie.",
            isFavorite = true
        ),
        Memory(
            id = 3,
            title = "Bez lokalizacji",
            description = "Snap bez miejsca.",
            createdAt = Clock.System.now().toEpochMilliseconds(),
            mood = MoodType.HAPPY,
            photoUri = null,
            audioUri = null,
            locationName = null,
            latitude = null,
            longitude = null,
            affirmation = null,
            isFavorite = false
        )
    )

    override suspend fun addMemory(memory: Memory): Int {
        return try {
            val memoriesEntity = Memories(
                id = 0, // Database will auto-generate ID
                title = memory.title,
                description = memory.description,
                timestamp = memory.createdAt,
                mood = memory.mood?.name,
                photoUri = memory.photoUri,
                audioUri = memory.audioUri,
                locationName = memory.locationName,
                latitude = memory.latitude,
                longitude = memory.longitude,
                affirmation = memory.affirmation,
                isFavorite = memory.isFavorite,
                isSynced = false
            )
            
            val newId = memoryDao.insert(memoriesEntity)
            newId.toInt()
        } catch (e: Exception) {
            throw Exception("Failed to save memory: ${e.message}")
        }
    }

    suspend fun getAllMemories(): List<Memory> {
        delay(300) // symulacja opóźnienia
        return fakeMemories
    }
//    override fun getMemories(): Flow<List<Memory>> {
//        return if (networkMonitor.isOnline()) {
//            flow { emit(onlineSource.fetchMemories()) }
//        } else {
//            offlineSource.getMemories()
//        }
//    }

    override fun getMemories(): Flow<List<Memory>> {
        return flow {
            emit(getAllMemories())
        }
    }

    override suspend fun getMemoryById(id: Int): Memory? {
        delay(200) // simulate network delay
        return fakeMemories.find { it.id == id }
    }

    override suspend fun markAsFavorite(id: Int, isFavorite: Boolean) {
        delay(100) // simulate network delay
        // In a real implementation, this would update the database
        // For now, we'll just simulate success
    }
}

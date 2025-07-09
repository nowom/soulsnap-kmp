package pl.soulsnaps.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.util.NetworkMonitor

class MemoryRepositoryImpl(
    private val networkMonitor: NetworkMonitor
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
        TODO("Not yet implemented")
    }
}

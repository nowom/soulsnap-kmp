package pl.soulsnaps.database.dao

import app.cash.sqldelight.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.soulsnaps.database.Memories
import pl.soulsnaps.database.SoulSnapDatabase

class MemoryDaoImpl(private val db: SoulSnapDatabase) : MemoryDao {
    private val queries = db.soulSnapDatabaseQueries

    override suspend fun insert(memory: Memories): Long {
        return queries.insertMemory(
            title = memory.title,
            description = memory.description,
            timestamp = memory.timestamp,
            mood = memory.mood,
            photoUri = memory.photoUri,
            audioUri = memory.audioUri,
            locationName = memory.locationName,
            latitude = memory.latitude,
            longitude = memory.longitude,
            affirmation = memory.affirmation,
            isFavorite = memory.isFavorite,
            isSynced = memory.isSynced
        ).value
    }

    override fun getAll(): Flow<List<Memories>> =
        queries.selectAll().asFlow().map { it.executeAsList() }

    override suspend fun getById(id: Long): Memories? =
        queries.selectById(id).executeAsOneOrNull()

    override suspend fun delete(id: Long) {
        queries.deleteMemoryById(id)
    }

    override suspend fun update(memory: Memories) {
        queries.updateMemory(
            title = memory.title,
            description = memory.description,
            timestamp = memory.timestamp,
            mood = memory.mood,
            photoUri = memory.photoUri,
            audioUri = memory.audioUri,
            locationName = memory.locationName,
            latitude = memory.latitude,
            longitude = memory.longitude,
            affirmation = memory.affirmation,
            isFavorite = memory.isFavorite,
            isSynced = memory.isSynced,
            id = memory.id
        )
    }

    override suspend fun markAsFavorite(id: Long, isFavorite: Boolean) {
        queries.markAsFavorite(isFavorite, id)
    }

    override suspend fun clearAll() {
        queries.clearAll()
    }

    override suspend fun getUnsynced(): List<Memories> =
        queries.getUnsyncedMemories().executeAsList()

    override suspend fun markAsSynced(id: Long) {
        queries.markAsSynced(id)
    }
}

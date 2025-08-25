package pl.soulsnaps.database.dao

import kotlinx.coroutines.flow.Flow
import pl.soulsnaps.database.Memories

interface MemoryDao {
    suspend fun insert(memory: Memories): Long
    fun getAll(): Flow<List<Memories>>
    suspend fun getById(id: Long): Memories?
    suspend fun delete(id: Long)
    suspend fun update(memory: Memories)
    suspend fun markAsFavorite(id: Long, isFavorite: Boolean)
    suspend fun clearAll()
    suspend fun getUnsynced(): List<Memories>
    suspend fun markAsSynced(id: Long)
    suspend fun deleteInvalidMemories(): Int
}

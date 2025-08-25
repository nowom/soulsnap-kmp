package pl.soulsnaps.domain

import kotlinx.coroutines.flow.Flow
import pl.soulsnaps.domain.model.Memory

interface MemoryRepository {
    fun getMemories(): Flow<List<Memory>>
    suspend fun getMemoryById(id: Int): Memory?
    suspend fun addMemory(memory: Memory): Int // Returns the ID of the created memory
//    suspend fun deleteMemory(id: Int)
//    suspend fun updateMemory(memory: Memory)
    suspend fun markAsFavorite(id: Int, isFavorite: Boolean)
    suspend fun cleanupInvalidMemories(): Int
}
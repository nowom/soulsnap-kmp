package pl.soulsnaps.data

import pl.soulsnaps.domain.model.Memory

/**
 * Interface for online data sources (Supabase, Firebase, etc.)
 * Provides abstraction for remote data operations
 */
interface OnlineDataSource {
    suspend fun getAllMemories(userId: String): List<Memory>
    suspend fun getMemoryById(id: Long): Memory?
    suspend fun insertMemory(memory: Memory, userId: String): Long?
    suspend fun updateMemory(memory: Memory, userId: String): Boolean
    suspend fun deleteMemory(id: Long, userId: String): Boolean
    suspend fun markAsFavorite(id: Long, isFavorite: Boolean, userId: String): Boolean
    suspend fun getUnsyncedMemories(userId: String): List<Memory>
    suspend fun markAsSynced(id: Long, userId: String): Boolean
}

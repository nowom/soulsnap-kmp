package pl.soulsnaps.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.model.*
import pl.soulsnaps.network.SupabaseDatabaseService
import pl.soulsnaps.network.SupabaseAuthService

class SupabaseMemoryRepository(
    private val databaseService: SupabaseDatabaseService,
    private val authService: SupabaseAuthService
) : MemoryRepository {
    
    override fun getMemories(): Flow<List<Memory>> = flow {
        try {
            val currentUser = authService.getCurrentUser()
            if (currentUser != null) {
                val memories = databaseService.getAllMemories(currentUser.userId)
                emit(memories)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            println("Error fetching memories: ${e.message}")
            emit(emptyList())
        }
    }
    
    override suspend fun getMemoryById(id: Int): Memory? {
        return try {
            val currentUser = authService.getCurrentUser()
            if (currentUser != null) {
                databaseService.getMemoryById(id.toLong())
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error fetching memory by ID: ${e.message}")
            null
        }
    }
    
    override suspend fun addMemory(memory: Memory): Int {
        return try {
            val currentUser = authService.getCurrentUser()
            if (currentUser != null) {
                val result = databaseService.insertMemory(memory, currentUser.userId)
                result?.toInt() ?: -1
            } else {
                throw Exception("User not authenticated")
            }
        } catch (e: Exception) {
            throw Exception("Failed to save memory: ${e.message}")
        }
    }

    override suspend fun markAsFavorite(id: Int, isFavorite: Boolean) {
        try {
            val currentUser = authService.getCurrentUser()
            if (currentUser != null) {
                databaseService.markAsFavorite(id.toLong(), isFavorite, currentUser.userId)
            }
        } catch (e: Exception) {
            println("Error toggling favorite: ${e.message}")
        }
    }

    override suspend fun cleanupInvalidMemories(): Int {
        // For Supabase, we don't need to clean up invalid memories
        // as they are stored remotely and validated
        println("DEBUG: SupabaseMemoryRepository.cleanupInvalidMemories() - no cleanup needed for remote storage")
        return 0
    }
    
    suspend fun updateMemory(memory: Memory): Boolean {
        return try {
            val currentUser = authService.getCurrentUser()
            if (currentUser != null) {
                databaseService.updateMemory(memory, currentUser.userId)
            } else {
                false
            }
        } catch (e: Exception) {
            println("Error updating memory: ${e.message}")
            false
        }
    }
    
    suspend fun deleteMemory(id: Int): Boolean {
        return try {
            val currentUser = authService.getCurrentUser()
            if (currentUser != null) {
                databaseService.deleteMemory(id.toLong(), currentUser.userId)
            } else {
                false
            }
        } catch (e: Exception) {
            println("Error deleting memory: ${e.message}")
            false
        }
    }
    
    suspend fun getUnsyncedMemories(): List<Memory> {
        return try {
            val currentUser = authService.getCurrentUser()
            if (currentUser != null) {
                databaseService.getUnsyncedMemories(currentUser.userId)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error fetching unsynced memories: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun markAsSynced(id: Int): Boolean {
        return try {
            val currentUser = authService.getCurrentUser()
            if (currentUser != null) {
                databaseService.markAsSynced(id.toLong(), currentUser.userId)
            } else {
                false
            }
        } catch (e: Exception) {
            println("Error marking memory as synced: ${e.message}")
            false
        }
    }
}

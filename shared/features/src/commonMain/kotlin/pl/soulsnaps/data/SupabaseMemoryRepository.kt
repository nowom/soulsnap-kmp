package pl.soulsnaps.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.soulsnaps.domain.MemoryRepository
import pl.soulsnaps.domain.model.*
import pl.soulsnaps.network.SupabaseDatabaseService

class SupabaseMemoryRepository(
    private val databaseService: SupabaseDatabaseService
) : MemoryRepository {
    
    override fun getMemories(): Flow<List<Memory>> = flow {
        try {
            val databaseMemories = databaseService.getAllMemories()
            val memories = databaseMemories.map { it.toMemory() }
            emit(memories)
        } catch (e: Exception) {
            println("Error fetching memories: ${e.message}")
            emit(emptyList())
        }
    }
    
    override suspend fun getMemoryById(id: Int): Memory? {
        return try {
            val databaseMemory = databaseService.getMemoryById(id.toString())
            databaseMemory?.toMemory()
        } catch (e: Exception) {
            println("Error fetching memory by ID: ${e.message}")
            null
        }
    }
    

    

    

    
    override suspend fun markAsFavorite(id: Int, isFavorite: Boolean) {
        try {
            databaseService.toggleMemoryFavorite(id.toString(), isFavorite)
        } catch (e: Exception) {
            println("Error toggling favorite: ${e.message}")
        }
    }
    

}

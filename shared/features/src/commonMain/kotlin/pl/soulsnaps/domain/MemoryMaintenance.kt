package pl.soulsnaps.domain

/**
 * Memory maintenance interface for cleanup operations
 * Follows Dependency Inversion Principle - domain layer defines interface
 */
interface MemoryMaintenance {
    /**
     * Clean up large memories that might cause performance issues
     * @return number of cleaned up items
     */
    suspend fun cleanupLargeMemories(): Int
    
    /**
     * Clean up orphaned files
     * @return number of cleaned up files
     */
    suspend fun cleanupOrphanedFiles(): Int
    
    /**
     * Check if maintenance is needed
     * @return true if maintenance should be performed
     */
    suspend fun isMaintenanceNeeded(): Boolean
}

/**
 * No-op implementation for when maintenance is not needed
 */
class NoOpMemoryMaintenance : MemoryMaintenance {
    override suspend fun cleanupLargeMemories(): Int = 0
    override suspend fun cleanupOrphanedFiles(): Int = 0
    override suspend fun isMaintenanceNeeded(): Boolean = false
}

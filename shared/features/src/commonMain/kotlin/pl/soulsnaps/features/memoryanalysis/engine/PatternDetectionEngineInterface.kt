package pl.soulsnaps.features.memoryanalysis.engine

import pl.soulsnaps.features.memoryanalysis.model.*
import pl.soulsnaps.domain.model.Memory

/**
 * Interface for PatternDetectionEngine - allows mocking in tests
 */
interface PatternDetectionEngineInterface {
    
    /**
     * Analyze all memories and detect patterns
     */
    suspend fun detectPatterns(memories: List<Memory>): MemoryPatterns
    
    /**
     * Generate insights from memories
     */
    suspend fun generateInsights(memories: List<Memory>): MemoryInsights
}

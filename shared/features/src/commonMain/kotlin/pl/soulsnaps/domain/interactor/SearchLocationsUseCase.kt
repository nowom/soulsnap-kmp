package pl.soulsnaps.domain.interactor

import pl.soulsnaps.features.location.LocationSearchService
import pl.soulsnaps.features.location.LocationSuggestion

/**
 * Use case for searching locations with autocomplete
 * Handles business logic and thread management
 */
class SearchLocationsUseCase(
    private val locationSearchService: LocationSearchService
) {
    
    /**
     * Search for locations based on query
     * @param query Search query (minimum 2 characters)
     * @return List of location suggestions
     */
    suspend operator fun invoke(query: String): List<LocationSuggestion> {
        println("DEBUG: SearchLocationsUseCase - searching for: '$query'")
        
        // Business logic: minimum query length
        if (query.length < 2) {
            println("DEBUG: SearchLocationsUseCase - query too short, returning empty list")
            return emptyList()
        }
        
        // Trim whitespace
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return emptyList()
        }
        
        return try {
            val results = locationSearchService.searchLocations(trimmedQuery)
            println("DEBUG: SearchLocationsUseCase - found ${results.size} results for '$trimmedQuery'")
            results
        } catch (e: Exception) {
            println("ERROR: SearchLocationsUseCase - search failed: ${e.message}")
            // Return empty list instead of throwing - UI can handle gracefully
            emptyList()
        }
    }
}



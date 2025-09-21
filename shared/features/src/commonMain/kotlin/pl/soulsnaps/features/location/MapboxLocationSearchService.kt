package pl.soulsnaps.features.location

import pl.soulsnaps.data.network.NetworkException
import pl.soulsnaps.data.network.SoulSnapApi

/**
 * Real Mapbox Geocoding API implementation using SoulSnapApi
 * Follows clean architecture with centralized API client
 */
class MapboxLocationSearchService(
    private val soulSnapApi: SoulSnapApi
) : LocationSearchService {
    
    override suspend fun searchLocations(query: String): List<LocationSuggestion> {
        if (query.length < 2) return emptyList()
        
        return try {
            println("DEBUG: MapboxLocationSearchService - delegating to SoulSnapApi for: '$query'")
            soulSnapApi.searchLocations(query)
            
        } catch (e: NetworkException) {
            println("ERROR: MapboxLocationSearchService - network error: ${e.message}")
            // Fallback to mock data if API fails
            MockLocationSearchService().searchLocations(query)
            
        } catch (e: Exception) {
            println("ERROR: MapboxLocationSearchService - unexpected error: ${e.message}")
            // Fallback to mock data for any other errors
            MockLocationSearchService().searchLocations(query)
        }
    }
}


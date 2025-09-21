package pl.soulsnaps.data.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import pl.soulsnaps.features.location.LocationSuggestion
import pl.soulsnaps.features.location.LocationType

/**
 * SoulSnap API client for handling all network requests
 * Centralized HTTP client management following clean architecture
 * 
 * Supports:
 * - Mapbox Geocoding API for location search
 * - Future APIs: Supabase, OpenAI, etc.
 */
class SoulSnapApi(
    private val httpClient: HttpClient = pl.soulsnaps.data.network.httpClient
) {
    
    companion object {
        // Mapbox configuration
        private const val MAPBOX_BASE_URL = "https://api.mapbox.com"
        private const val MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoibW5vd293IiwiYSI6ImNtOTl4NzB2ajA3MmMybnI0ZmYwMXZvdngifQ.94yPMCjP73IZdoT-4h7DgQ"
        
        // Future API endpoints
        private const val SUPABASE_BASE_URL = "https://your-supabase-project.supabase.co"
        private const val OPENAI_BASE_URL = "https://api.openai.com/v1"
    }
    
    // MARK: - Location/Geocoding APIs
    
    /**
     * Search locations using Mapbox Geocoding API
     * @param query Search query (e.g., "Kraków")
     * @param country Country filter (default: "PL" for Poland)
     * @param limit Maximum number of results (default: 5)
     * @param types Location types filter (default: "place,poi,address")
     * @param language Response language (default: "pl")
     * @return List of location suggestions
     */
    suspend fun searchLocations(
        query: String,
        country: String = "PL",
        limit: Int = 5,
        types: String = "place,poi,address",
        language: String = "pl"
    ): List<LocationSuggestion> {
        println("DEBUG: SoulSnapApi.searchLocations() - searching for: '$query'")
        
        return try {
            val response: MapboxGeocodingResponse = httpClient.get("$MAPBOX_BASE_URL/geocoding/v5/mapbox.places/${query}.json") {
                parameter("access_token", MAPBOX_ACCESS_TOKEN)
                parameter("country", country)
                parameter("types", types)
                parameter("limit", limit)
                parameter("language", language)
            }.body()
            
            println("DEBUG: SoulSnapApi.searchLocations() - received ${response.features.size} results")
            
            response.features.map { feature ->
                val coordinates = feature.center
                LocationSuggestion(
                    name = feature.text ?: feature.place_name,
                    fullAddress = feature.place_name,
                    latitude = coordinates[1], // Mapbox uses [lng, lat]
                    longitude = coordinates[0],
                    type = mapPlaceType(feature.place_type?.firstOrNull())
                )
            }
            
        } catch (e: Exception) {
            println("ERROR: SoulSnapApi.searchLocations() - failed: ${e.message}")
            throw NetworkException("Location search failed: ${e.message}", e)
        }
    }
    
    /**
     * Get location details by coordinates
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return Location suggestion with details
     */
    suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): LocationSuggestion? {
        println("DEBUG: SoulSnapApi.reverseGeocode() - lat: $latitude, lon: $longitude")
        
        return try {
            val response: MapboxGeocodingResponse = httpClient.get("$MAPBOX_BASE_URL/geocoding/v5/mapbox.places/$longitude,$latitude.json") {
                parameter("access_token", MAPBOX_ACCESS_TOKEN)
                parameter("types", "place,poi,address")
                parameter("language", "pl")
            }.body()
            
            response.features.firstOrNull()?.let { feature ->
                val coordinates = feature.center
                LocationSuggestion(
                    name = feature.text ?: feature.place_name,
                    fullAddress = feature.place_name,
                    latitude = coordinates[1],
                    longitude = coordinates[0],
                    type = mapPlaceType(feature.place_type?.firstOrNull())
                )
            }
            
        } catch (e: Exception) {
            println("ERROR: SoulSnapApi.reverseGeocode() - failed: ${e.message}")
            null
        }
    }
    
    // MARK: - Future API Methods
    
    /**
     * Upload memory to Supabase (placeholder)
     * TODO: Implement when Supabase integration is ready
     */
    suspend fun uploadMemory(memoryData: Any): Result<String> {
        return Result.failure(NotImplementedError("Supabase integration coming soon"))
    }
    
    /**
     * Generate AI insights using OpenAI (placeholder)
     * TODO: Implement when AI features are ready
     */
    suspend fun generateAIInsights(prompt: String): Result<String> {
        return Result.failure(NotImplementedError("AI integration coming soon"))
    }
    
    // MARK: - Helper Methods
    
    private fun mapPlaceType(placeType: String?): LocationType {
        return when (placeType) {
            "place" -> LocationType.PLACE
            "poi" -> LocationType.POI
            "address" -> LocationType.ADDRESS
            "region" -> LocationType.REGION
            else -> LocationType.PLACE
        }
    }
}

/**
 * Custom exception for network-related errors
 */
class NetworkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

// MARK: - Data Models

/**
 * Mapbox Geocoding API response model
 */
@Serializable
data class MapboxGeocodingResponse(
    val features: List<MapboxFeature>
)

/**
 * Mapbox feature model representing a single location result
 */
@Serializable
data class MapboxFeature(
    val place_name: String,
    val text: String? = null,
    val center: List<Double>, // [longitude, latitude]
    val place_type: List<String>? = null
)

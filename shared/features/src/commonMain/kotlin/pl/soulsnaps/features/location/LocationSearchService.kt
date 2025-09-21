package pl.soulsnaps.features.location

import kotlinx.coroutines.delay

/**
 * Service for location search and autocomplete
 * Uses Mapbox Geocoding API for real implementation
 */
interface LocationSearchService {
    suspend fun searchLocations(query: String): List<LocationSuggestion>
}

data class LocationSuggestion(
    val name: String,
    val fullAddress: String,
    val latitude: Double,
    val longitude: Double,
    val type: LocationType = LocationType.PLACE
)

enum class LocationType {
    PLACE,      // City, town
    ADDRESS,    // Street address
    POI,        // Point of interest
    REGION      // State, country
}

/**
 * Mock implementation for testing
 * In production, this would call Mapbox Geocoding API
 */
class MockLocationSearchService : LocationSearchService {
    
    private val mockLocations = listOf(
        LocationSuggestion("Kraków", "Kraków, Poland", 50.0647, 19.9450, LocationType.PLACE),
        LocationSuggestion("Kraków Old Town", "Old Town, Kraków, Poland", 50.0619, 19.9369, LocationType.POI),
        LocationSuggestion("Kraków Main Square", "Main Market Square, Kraków, Poland", 50.0616, 19.9373, LocationType.POI),
        LocationSuggestion("Warsaw", "Warsaw, Poland", 52.2297, 21.0122, LocationType.PLACE),
        LocationSuggestion("Warsaw Old Town", "Old Town, Warsaw, Poland", 52.2495, 21.0134, LocationType.POI),
        LocationSuggestion("Gdańsk", "Gdańsk, Poland", 54.3520, 18.6466, LocationType.PLACE),
        LocationSuggestion("Gdańsk Old Town", "Old Town, Gdańsk, Poland", 54.3477, 18.6535, LocationType.POI),
        LocationSuggestion("Zakopane", "Zakopane, Poland", 49.2992, 19.9496, LocationType.PLACE),
        LocationSuggestion("Wrocław", "Wrocław, Poland", 51.1079, 17.0385, LocationType.PLACE),
        LocationSuggestion("Wrocław Market Square", "Market Square, Wrocław, Poland", 51.1105, 17.0320, LocationType.POI),
        LocationSuggestion("Poznań", "Poznań, Poland", 52.4064, 16.9252, LocationType.PLACE),
        LocationSuggestion("Łódź", "Łódź, Poland", 51.7592, 19.4560, LocationType.PLACE),
        LocationSuggestion("Katowice", "Katowice, Poland", 50.2649, 19.0238, LocationType.PLACE),
        LocationSuggestion("Lublin", "Lublin, Poland", 51.2465, 22.5684, LocationType.PLACE),
        LocationSuggestion("Białystok", "Białystok, Poland", 53.1325, 23.1688, LocationType.PLACE),
        LocationSuggestion("Częstochowa", "Częstochowa, Poland", 50.7971, 19.1200, LocationType.PLACE),
        LocationSuggestion("Szczecin", "Szczecin, Poland", 53.4285, 14.5528, LocationType.PLACE),
        LocationSuggestion("Bydgoszcz", "Bydgoszcz, Poland", 53.1235, 18.0084, LocationType.PLACE),
        LocationSuggestion("Toruń", "Toruń, Poland", 53.0138, 18.5984, LocationType.PLACE),
        LocationSuggestion("Rzeszów", "Rzeszów, Poland", 50.0412, 21.9991, LocationType.PLACE)
    )
    
    override suspend fun searchLocations(query: String): List<LocationSuggestion> {
        // Simulate network delay
        delay(300)
        
        if (query.length < 2) return emptyList()
        
        return mockLocations.filter { location ->
            location.name.contains(query, ignoreCase = true) ||
            location.fullAddress.contains(query, ignoreCase = true)
        }.take(5) // Limit to 5 suggestions
    }
}



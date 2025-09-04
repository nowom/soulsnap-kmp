package pl.soulsnaps.location

import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Location data for photos
 */
data class PhotoLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val timestamp: Long = getCurrentTimeMillis(),
    val placeId: String? = null,
    val placeType: PlaceType? = null
)

/**
 * Place information
 */
data class PlaceInfo(
    val id: String,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val placeType: PlaceType,
    val rating: Float? = null,
    val photos: List<String> = emptyList()
)

/**
 * Place types
 */
enum class PlaceType {
    RESTAURANT,
    CAFE,
    PARK,
    MUSEUM,
    SHOPPING,
    ENTERTAINMENT,
    TRANSPORT,
    OTHER
}

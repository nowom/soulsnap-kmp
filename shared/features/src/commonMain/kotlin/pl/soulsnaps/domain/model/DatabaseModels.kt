package pl.soulsnaps.domain.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Serializable
data class DatabaseMemory(
    val id: String,
    val user_id: String,
    val title: String,
    val description: String?,
    val mood_type: String?,
    val photo_url: String?,
    val audio_url: String?,
    val location_lat: Double?,
    val location_lng: Double?,
    val location_name: String?,
    val affirmation: String?,
    val is_favorite: Boolean,
    val created_at: String,
    val updated_at: String
) {
    fun toMemory(): Memory {
        return Memory(
            id = id.toIntOrNull() ?: id.hashCode(),
            title = title,
            description = description ?: "",
            createdAt = try {
                Instant.parse(created_at).toEpochMilliseconds()
            } catch (e: Exception) {
                1700000000000L // Fixed timestamp instead of Clock.System.now()
            },
            mood = mood_type?.let { MoodType.valueOf(it.uppercase()) },
            photoUri = photo_url,
            audioUri = audio_url,
            locationName = location_name,
            latitude = location_lat,
            longitude = location_lng,
            affirmation = affirmation,
            isFavorite = is_favorite
        )
    }
}

fun Memory.toDatabaseMemory(userId: String): CreateMemoryRequest {
    return CreateMemoryRequest(
        user_id = userId,
        title = title,
        description = description,
        mood_type = mood?.name?.lowercase(),
        photo_url = photoUri,
        audio_url = audioUri,
        location_lat = latitude,
        location_lng = longitude,
        location_name = locationName,
        affirmation = affirmation,
        is_favorite = isFavorite
    )
}

fun Memory.toUpdateMemoryRequest(): UpdateMemoryRequest {
    return UpdateMemoryRequest(
        title = title,
        description = description,
        mood_type = mood?.name?.lowercase(),
        photo_url = photoUri,
        audio_url = audioUri,
        location_lat = latitude,
        location_lng = longitude,
        location_name = locationName,
        affirmation = affirmation,
        is_favorite = isFavorite
    )
}

@Serializable
data class CreateMemoryRequest(
    val user_id: String,
    val title: String,
    val description: String? = null,
    val mood_type: String? = null,
    val photo_url: String? = null,
    val audio_url: String? = null,
    val location_lat: Double? = null,
    val location_lng: Double? = null,
    val location_name: String? = null,
    val affirmation: String? = null,
    val is_favorite: Boolean = false
)

@Serializable
data class UpdateMemoryRequest(
    val title: String? = null,
    val description: String? = null,
    val mood_type: String? = null,
    val photo_url: String? = null,
    val audio_url: String? = null,
    val location_lat: Double? = null,
    val location_lng: Double? = null,
    val location_name: String? = null,
    val affirmation: String? = null,
    val is_favorite: Boolean? = null
)

@Serializable
data class DatabaseUserProfile(
    val id: String,
    val user_id: String,
    val display_name: String?,
    val bio: String?,
    val avatar_url: String?,
    val preferences: Map<String, String>?,
    val created_at: String,
    val updated_at: String
)

@Serializable
data class CreateUserProfileRequest(
    val display_name: String? = null,
    val bio: String? = null,
    val avatar_url: String? = null,
    val preferences: Map<String, String>? = null
)

@Serializable
data class UpdateUserProfileRequest(
    val display_name: String? = null,
    val bio: String? = null,
    val avatar_url: String? = null,
    val preferences: Map<String, String>? = null
)

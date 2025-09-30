package pl.soulsnaps.data.model

import kotlinx.serialization.Serializable
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.domain.model.SyncState
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@Serializable
data class MemoryRow(
    val id: String? = null,            // uuid
    val user_id: String,               // uuid
    val title: String? = null,
    val description: String? = null,
    val mood_type: String? = null,     // 'HAPPY'|'SAD'|'NEUTRAL'|'EXCITED'|'RELAXED'
    val photo_uri: String? = null,
    val audio_uri: String? = null,
    val photo_thumb_path: String? = null,
    val photo_medium_path: String? = null,
    val location_lat: Double? = null,
    val location_lng: Double? = null,
    val location_name: String? = null,
    val affirmation: String? = null,
    val is_favorite: Boolean = false,
    val is_synced: Boolean = false,
    val created_at: String? = null,
    val updated_at: String? = null
)

fun MemoryRow.toDomain(): Memory = Memory(
    id = id?.hashCode() ?: 0,                 // lokalny int (opcjonalny)
    title = title.orEmpty(),
    description = description.orEmpty(),
    createdAt = created_at?.tsToMillis() ?: 0L,
    updatedAt = updated_at?.tsToMillis() ?: (created_at?.tsToMillis() ?: 0L),
    mood = mood_type?.let { runCatching { MoodType.valueOf(it) }.getOrNull() },
    photoUri = null,                          // lokalna ścieżka (nie z bazy)
    audioUri = null,                          // lokalna ścieżka (nie z bazy)
    imageUrl = null,                          // deprecated u Ciebie
    locationName = location_name,
    latitude = location_lat,
    longitude = location_lng,
    affirmation = affirmation,
    isFavorite = is_favorite,
    remotePhotoPath = photo_uri,
    remoteAudioPath = audio_uri,
    remoteId = id,
    syncState = if (is_synced) SyncState.SYNCED else SyncState.PENDING,
    retryCount = 0,
    errorMessage = null
)

/**
 * Convert timestamp string to milliseconds
 */
@OptIn(ExperimentalTime::class)
fun String.tsToMillis(): Long {
    return try {
        Instant.parse(this).toEpochMilliseconds()
    } catch (e: Exception) {
        0L
    }
}

/**
 * Convert Memory to MemoryRow for Supabase
 */
@OptIn(ExperimentalTime::class)
fun Memory.toRow(userId: String): MemoryRow = MemoryRow(
    id = remoteId,
    user_id = userId,
    title = title,
    description = description,
    mood_type = mood?.name,
    photo_uri = remotePhotoPath,
    audio_uri = remoteAudioPath,
    photo_thumb_path = null, // TODO: implement thumbnail generation
    photo_medium_path = null, // TODO: implement medium size generation
    location_lat = latitude,
    location_lng = longitude,
    location_name = locationName,
    affirmation = affirmation,
    is_favorite = isFavorite,
    is_synced = syncState == SyncState.SYNCED,
    created_at = Instant.fromEpochMilliseconds(createdAt).toString(),
    updated_at = Instant.fromEpochMilliseconds(updatedAt).toString()
)
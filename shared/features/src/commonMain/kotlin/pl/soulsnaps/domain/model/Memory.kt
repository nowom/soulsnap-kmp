package pl.soulsnaps.domain.model

data class Memory(
    val id: Int = 0,
    val title: String,
    val description: String,
    val createdAt: Long,
    val mood: MoodType?,
    val photoUri: String?,
    val audioUri: String?,
    val locationName: String?,
    val latitude: Double?,
    val longitude: Double?,
    val affirmation: String? = null,
    val isFavorite: Boolean = false
)
package pl.soulsnaps.domain.model

data class Affirmation(
    val id: String = "UUID.randomUUID().toString()", //TODO
    val text: String,
    val audioUrl: String?, // local path or URL for TTS
    val emotion: String,
    val timeOfDay: String,
    val isFavorite: Boolean = false,
    val themeType: ThemeType,
)
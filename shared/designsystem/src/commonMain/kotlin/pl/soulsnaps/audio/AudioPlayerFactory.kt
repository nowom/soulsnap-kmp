package pl.soulsnaps.audio

/**
 * Factory for creating platform-specific AudioPlayer implementations
 */
expect class AudioPlayerFactory {
    fun create(): AudioPlayer
}

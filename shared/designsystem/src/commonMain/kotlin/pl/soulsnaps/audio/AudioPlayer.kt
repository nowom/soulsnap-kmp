package pl.soulsnaps.audio

import kotlinx.coroutines.flow.StateFlow

/**
 * Audio Player interface for playing affirmations and other audio content
 * Provides multiplatform audio playback capabilities
 */
interface AudioPlayer {
    val isPlaying: StateFlow<Boolean>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>
    val playbackState: StateFlow<PlaybackState>
    
    /**
     * Play audio from text using TTS
     * @param text Text to convert to speech
     * @param voiceType Type of voice to use (AI, User, Default)
     */
    suspend fun playText(text: String, voiceType: VoiceType = VoiceType.DEFAULT)
    
    /**
     * Play audio from URL or file path
     * @param audioUri URI or path to audio file
     */
    suspend fun playAudio(audioUri: String)
    
    /**
     * Pause current playback
     */
    suspend fun pause()
    
    /**
     * Resume paused playback
     */
    suspend fun resume()
    
    /**
     * Stop playback and reset position
     */
    suspend fun stop()
    
    /**
     * Seek to specific position in milliseconds
     */
    suspend fun seekTo(positionMs: Long)
    
    /**
     * Set playback speed (0.5x to 2.0x)
     */
    suspend fun setPlaybackSpeed(speed: Float)
    
    /**
     * Set volume (0.0 to 1.0)
     */
    suspend fun setVolume(volume: Float)
    
    /**
     * Release audio resources
     */
    suspend fun release()
}

/**
 * Voice types for TTS
 */
enum class VoiceType {
    DEFAULT,    // Platform default voice
    AI,         // AI-generated voice (ElevenLabs)
    USER        // User's recorded voice
}

/**
 * Audio playback states
 */
enum class PlaybackState {
    IDLE,       // No audio loaded
    LOADING,    // Loading audio
    READY,      // Audio loaded, ready to play
    PLAYING,    // Currently playing
    PAUSED,     // Paused
    STOPPED,    // Stopped
    ERROR       // Error occurred
}

/**
 * Audio configuration
 */
data class AudioConfig(
    val voiceType: VoiceType = VoiceType.DEFAULT,
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val enableBackgroundPlayback: Boolean = true,
    val enableHeadphoneControls: Boolean = true
)

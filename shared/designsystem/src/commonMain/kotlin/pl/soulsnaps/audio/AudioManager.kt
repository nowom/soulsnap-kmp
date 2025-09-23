package pl.soulsnaps.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Audio Manager - manages audio playback across the app
 * Handles multiple audio sources and provides centralized control
 */
class AudioManager(
    private val audioPlayer: AudioPlayer,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentAudio = MutableStateFlow<AudioItem?>(null)
    val currentAudio: StateFlow<AudioItem?> = _currentAudio.asStateFlow()
    
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    init {
        // Observe audio player state changes
        coroutineScope.launch {
            audioPlayer.isPlaying.collect { playing ->
                _isPlaying.value = playing
            }
        }
        
        coroutineScope.launch {
            audioPlayer.playbackState.collect { state ->
                _playbackState.value = state
            }
        }
    }
    
    /**
     * Play affirmation text
     */
    suspend fun playAffirmation(text: String, voiceType: VoiceType = VoiceType.DEFAULT) {
        println("🎵 AudioManager: playAffirmation called with text: $text")
        
        val audioItem = AudioItem(
            id = "affirmation_${getCurrentTimeMillis()}",
            text = text,
            type = AudioType.AFFIRMATION,
            voiceType = voiceType
        )
        
        _currentAudio.value = audioItem
        println("🎵 AudioManager: Calling audioPlayer.playText")
        audioPlayer.playText(text, voiceType)
        println("🎵 AudioManager: audioPlayer.playText completed")
    }
    
    /**
     * Play memory audio
     */
    suspend fun playMemoryAudio(audioUri: String, memoryId: String) {
        val audioItem = AudioItem(
            id = memoryId,
            text = null,
            audioUri = audioUri,
            type = AudioType.MEMORY
        )
        
        _currentAudio.value = audioItem
        audioPlayer.playAudio(audioUri)
    }
    
    /**
     * Play guided meditation or exercise
     */
    suspend fun playExercise(text: String, exerciseType: ExerciseType) {
        val audioItem = AudioItem(
            id = "exercise_${getCurrentTimeMillis()}",
            text = text,
            type = AudioType.EXERCISE,
            exerciseType = exerciseType
        )
        
        _currentAudio.value = audioItem
        audioPlayer.playText(text, VoiceType.DEFAULT)
    }
    
    /**
     * Pause current playback
     */
    suspend fun pause() {
        audioPlayer.pause()
    }
    
    /**
     * Resume playback
     */
    suspend fun resume() {
        audioPlayer.resume()
    }
    
    /**
     * Stop playback
     */
    suspend fun stop() {
        audioPlayer.stop()
        _currentAudio.value = null
    }
    
    /**
     * Toggle play/pause
     */
    suspend fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }
    
    /**
     * Set playback speed
     */
    suspend fun setPlaybackSpeed(speed: Float) {
        audioPlayer.setPlaybackSpeed(speed)
    }
    
    /**
     * Set volume
     */
    suspend fun setVolume(volume: Float) {
        audioPlayer.setVolume(volume)
    }
    
    /**
     * Release resources
     */
    suspend fun release() {
        audioPlayer.release()
    }
}

/**
 * Audio item data class
 */
data class AudioItem(
    val id: String,
    val text: String? = null,
    val audioUri: String? = null,
    val type: AudioType,
    val voiceType: VoiceType = VoiceType.DEFAULT,
    val exerciseType: ExerciseType? = null,
    val duration: Long = 0L
)

/**
 * Audio types
 */
enum class AudioType {
    AFFIRMATION,
    MEMORY,
    EXERCISE,
    MEDITATION
}

/**
 * Exercise types for audio
 */
enum class ExerciseType {
    BREATHING,
    GRATITUDE,
    MINDFULNESS,
    RELAXATION
}

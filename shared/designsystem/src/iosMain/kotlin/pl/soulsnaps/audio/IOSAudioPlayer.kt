package pl.soulsnaps.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFoundation.*
import platform.Foundation.*

/**
 * iOS implementation of AudioPlayer using AVFoundation
 */
class IOSAudioPlayer : AudioPlayer {
    
    private var audioPlayer: AVAudioPlayer? = null
    private var speechSynthesizer: AVSpeechSynthesizer? = null
    private var currentUtterance: AVSpeechUtterance? = null
    
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    override val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    override val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private var currentVoiceType: VoiceType = VoiceType.DEFAULT
    
    init {
        initializeAudioSession()
        initializeSpeechSynthesizer()
    }
    
    private fun initializeAudioSession() {
        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setCategoryWithOptions(
            AVAudioSessionCategoryPlayback,
            AVAudioSessionCategoryOptions.allowBluetooth or AVAudioSessionCategoryOptions.allowBluetoothA2DP
        )
        audioSession.setActive(true, null)
    }
    
    private fun initializeSpeechSynthesizer() {
        speechSynthesizer = AVSpeechSynthesizer()
    }
    
    override suspend fun playText(text: String, voiceType: VoiceType) {
        stop()
        currentVoiceType = voiceType
        
        _playbackState.value = PlaybackState.LOADING
        
        when (voiceType) {
            VoiceType.DEFAULT -> {
                playWithAVSpeechSynthesizer(text)
            }
            VoiceType.AI -> {
                // TODO: Implement AI voice (ElevenLabs integration)
                playWithAVSpeechSynthesizer(text) // Fallback to AVSpeechSynthesizer for now
            }
            VoiceType.USER -> {
                // TODO: Implement user voice playback
                playWithAVSpeechSynthesizer(text) // Fallback to AVSpeechSynthesizer for now
            }
        }
    }
    
    private suspend fun playWithAVSpeechSynthesizer(text: String) {
        val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
        
        // Configure voice
        val voices = AVSpeechSynthesisVoice.speechVoices()
        val voice = voices.firstOrNull { voice ->
            // Try to find a pleasant voice
            voice.language.startsWith("en") || voice.language.startsWith("pl")
        } ?: AVSpeechSynthesisVoice.speechVoices().firstOrNull()
        
        utterance.voice = voice
        utterance.rate = 0.5f // Slower rate for better comprehension
        utterance.pitchMultiplier = 1.0f
        utterance.volume = 1.0f
        
        currentUtterance = utterance
        
        speechSynthesizer?.speakUtterance(utterance)
        
        _playbackState.value = PlaybackState.READY
        _isPlaying.value = true
        _playbackState.value = PlaybackState.PLAYING
    }
    
    override suspend fun playAudio(audioUri: String) {
        stop()
        _playbackState.value = PlaybackState.LOADING
        
        try {
            val url = NSURL.URLWithString(audioUri) ?: NSURL.fileURLWithPath(audioUri)
            audioPlayer = AVAudioPlayer(contentsOfURL = url, error = null)
            
            audioPlayer?.let { player ->
                player.prepareToPlay()
                player.play()
                
                _duration.value = (player.duration * 1000).toLong() // Convert to milliseconds
                _playbackState.value = PlaybackState.READY
                _isPlaying.value = true
                _playbackState.value = PlaybackState.PLAYING
            }
        } catch (e: Exception) {
            _playbackState.value = PlaybackState.ERROR
        }
    }
    
    override suspend fun pause() {
        when {
            speechSynthesizer?.isSpeaking == true -> {
                speechSynthesizer?.pauseSpeakingAtBoundary(AVSpeechBoundaryImmediate)
                _isPlaying.value = false
                _playbackState.value = PlaybackState.PAUSED
            }
            audioPlayer?.isPlaying == true -> {
                audioPlayer?.pause()
                _isPlaying.value = false
                _playbackState.value = PlaybackState.PAUSED
            }
        }
    }
    
    override suspend fun resume() {
        when {
            speechSynthesizer?.isPaused == true -> {
                speechSynthesizer?.continueSpeaking()
                _isPlaying.value = true
                _playbackState.value = PlaybackState.PLAYING
            }
            audioPlayer != null -> {
                audioPlayer?.play()
                _isPlaying.value = true
                _playbackState.value = PlaybackState.PLAYING
            }
        }
    }
    
    override suspend fun stop() {
        speechSynthesizer?.stopSpeakingAtBoundary(AVSpeechBoundaryImmediate)
        audioPlayer?.stop()
        audioPlayer = null
        currentUtterance = null
        
        _isPlaying.value = false
        _playbackState.value = PlaybackState.STOPPED
        _currentPosition.value = 0L
        _duration.value = 0L
    }
    
    override suspend fun seekTo(positionMs: Long) {
        audioPlayer?.currentTime = positionMs / 1000.0 // Convert to seconds
        _currentPosition.value = positionMs
    }
    
    override suspend fun setPlaybackSpeed(speed: Float) {
        currentUtterance?.rate = speed * 0.5f // AVSpeechSynthesizer uses different scale
        audioPlayer?.rate = speed
    }
    
    override suspend fun setVolume(volume: Float) {
        currentUtterance?.volume = volume
        audioPlayer?.volume = volume
    }
    
    override suspend fun release() {
        stop()
        speechSynthesizer = null
        audioPlayer?.release()
        audioPlayer = null
    }
}

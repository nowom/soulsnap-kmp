package pl.soulsnaps.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager as AndroidAudioManager
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of AudioPlayer using MediaPlayer and TextToSpeech
 */
class AndroidAudioPlayer(
    private val context: Context
) : AudioPlayer {
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AndroidAudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var textToSpeech: TextToSpeech? = null
    
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    override val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    override val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private var audioFocusRequest: AudioFocusRequest? = null
    private var currentVoiceType: VoiceType = VoiceType.DEFAULT
    
    init {
        initializeTextToSpeech()
    }
    
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configure TTS settings
                textToSpeech?.setSpeechRate(1.0f)
                textToSpeech?.setPitch(1.0f)
                
                // Set up utterance progress listener
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        println("🎵 AndroidAudioPlayer: TTS onStart called for utteranceId: $utteranceId")
                        _playbackState.value = PlaybackState.PLAYING
                        _isPlaying.value = true
                        println("🎵 AndroidAudioPlayer: State set to PLAYING, isPlaying = true")
                    }
                    
                    override fun onDone(utteranceId: String?) {
                        println("🎵 AndroidAudioPlayer: TTS onDone called for utteranceId: $utteranceId")
                        _playbackState.value = PlaybackState.STOPPED
                        _isPlaying.value = false
                        println("🎵 AndroidAudioPlayer: State set to STOPPED, isPlaying = false")
                    }
                    
                    override fun onError(utteranceId: String?) {
                        println("🎵 AndroidAudioPlayer: TTS onError called for utteranceId: $utteranceId")
                        _playbackState.value = PlaybackState.ERROR
                        _isPlaying.value = false
                        println("🎵 AndroidAudioPlayer: State set to ERROR, isPlaying = false")
                    }
                })
            }
        }
    }
    
    override suspend fun playText(text: String, voiceType: VoiceType) {
        println("🎵 AndroidAudioPlayer: playText called with text: $text, voiceType: $voiceType")
        stop()
        currentVoiceType = voiceType
        
        _playbackState.value = PlaybackState.LOADING
        println("🎵 AndroidAudioPlayer: PlaybackState set to LOADING")
        
        when (voiceType) {
            VoiceType.DEFAULT -> {
                println("🎵 AndroidAudioPlayer: Using DEFAULT voice, calling playWithTTS")
                playWithTTS(text)
            }
            VoiceType.AI -> {
                println("🎵 AndroidAudioPlayer: Using AI voice (fallback to TTS)")
                // TODO: Implement AI voice (ElevenLabs integration)
                playWithTTS(text) // Fallback to TTS for now
            }
            VoiceType.USER -> {
                println("🎵 AndroidAudioPlayer: Using USER voice (fallback to TTS)")
                // TODO: Implement user voice playback
                playWithTTS(text) // Fallback to TTS for now
            }
        }
    }
    
    private suspend fun playWithTTS(text: String) {
        println("🎵 AndroidAudioPlayer: playWithTTS called with text: $text")
        suspendCancellableCoroutine<Unit> { continuation ->
            println("🎵 AndroidAudioPlayer: Requesting audio focus")
            requestAudioFocus()
            
            val utteranceId = "affirmation_${System.currentTimeMillis()}"
            println("🎵 AndroidAudioPlayer: Calling TTS.speak with utteranceId: $utteranceId")
            
            val result = textToSpeech?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId
            )
            
            println("🎵 AndroidAudioPlayer: TTS.speak result: $result")
            
            if (result == TextToSpeech.SUCCESS) {
                println("🎵 AndroidAudioPlayer: TTS SUCCESS, setting state to READY")
                _playbackState.value = PlaybackState.READY
                continuation.resume(Unit)
            } else {
                println("🎵 AndroidAudioPlayer: TTS ERROR, setting state to ERROR")
                _playbackState.value = PlaybackState.ERROR
                continuation.resume(Unit)
            }
        }
    }
    
    override suspend fun playAudio(audioUri: String) {
        stop()
        _playbackState.value = PlaybackState.LOADING
        
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                
                setOnPreparedListener { mp ->
                    _duration.value = mp.duration.toLong()
                    _playbackState.value = PlaybackState.READY
                    mp.start()
                    _isPlaying.value = true
                    _playbackState.value = PlaybackState.PLAYING
                }
                
                setOnCompletionListener {
                    _isPlaying.value = false
                    _playbackState.value = PlaybackState.STOPPED
                }
                
                setOnErrorListener { _, _, _ ->
                    _playbackState.value = PlaybackState.ERROR
                    _isPlaying.value = false
                    true
                }
                
                prepareAsync()
            }
            
            requestAudioFocus()
        } catch (e: Exception) {
            _playbackState.value = PlaybackState.ERROR
        }
    }
    
    override suspend fun pause() {
        when {
            textToSpeech?.isSpeaking == true -> {
                textToSpeech?.stop()
                _isPlaying.value = false
                _playbackState.value = PlaybackState.PAUSED
            }
            mediaPlayer?.isPlaying == true -> {
                mediaPlayer?.pause()
                _isPlaying.value = false
                _playbackState.value = PlaybackState.PAUSED
            }
        }
    }
    
    override suspend fun resume() {
        when {
            mediaPlayer != null -> {
                mediaPlayer?.start()
                _isPlaying.value = true
                _playbackState.value = PlaybackState.PLAYING
            }
            else -> {
                // For TTS, we need to restart since it doesn't support pause/resume
                // This is a limitation of Android TTS
            }
        }
    }
    
    override suspend fun stop() {
        textToSpeech?.stop()
        mediaPlayer?.apply {
            stop()
            reset()
        }
        mediaPlayer = null
        
        _isPlaying.value = false
        _playbackState.value = PlaybackState.STOPPED
        _currentPosition.value = 0L
        _duration.value = 0L
        
        abandonAudioFocus()
    }
    
    override suspend fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.toInt())
        _currentPosition.value = positionMs
    }
    
    override suspend fun setPlaybackSpeed(speed: Float) {
        textToSpeech?.setSpeechRate(speed)
        mediaPlayer?.let { player ->
            val params = player.playbackParams?.setSpeed(speed) ?: android.media.PlaybackParams().setSpeed(speed)
            player.setPlaybackParams(params)
        }
    }
    
    override suspend fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }
    
    override suspend fun release() {
        stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        mediaPlayer?.release()
        mediaPlayer = null
    }
    
    private fun requestAudioFocus() {
        // TODO: Implement proper audio focus handling for Android 8.0+
        // For now, just set the volume
    }
    
    private fun abandonAudioFocus() {
        // TODO: Implement proper audio focus handling
    }
}

package pl.soulsnaps.features.virtualmirror

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class VirtualMirrorViewModel : ViewModel() {
    private val _state = MutableStateFlow(VirtualMirrorState())
    val state: StateFlow<VirtualMirrorState> = _state.asStateFlow()

    private val emotions = listOf(
        DetectedEmotion("Radość", "😄", 0.95f),
        DetectedEmotion("Smutek", "😢", 0.88f),
        DetectedEmotion("Złość", "😠", 0.82f),
        DetectedEmotion("Zaskoczenie", "😮", 0.90f),
        DetectedEmotion("Spokój", "😌", 0.93f)
    )

    private val prompts = mapOf(
        "Radość" to "Co sprawiło, że dziś się uśmiechasz?",
        "Smutek" to "Co mogłoby poprawić Twój nastrój?",
        "Złość" to "Co wywołało Twoją złość? Jak możesz sobie z nią poradzić?",
        "Zaskoczenie" to "Co Cię dziś zaskoczyło?",
        "Spokój" to "Co daje Ci poczucie spokoju?"
    )

    fun handleIntent(intent: VirtualMirrorIntent) {
        when (intent) {
            is VirtualMirrorIntent.AnalyzeEmotion -> analyzeEmotion()
            is VirtualMirrorIntent.TryAgain -> reset()
            is VirtualMirrorIntent.Back -> stopCamera()
            else -> {}
        }
    }

    private fun analyzeEmotion() {
        _state.update { it.copy(isAnalyzing = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            val emotion = emotions.random()
            _state.update {
                it.copy(
                    isAnalyzing = false,
                    detectedEmotion = emotion,
                    reflectionPrompt = prompts[emotion.label]
                )
            }
        }
    }

    private fun reset() {
        _state.update {
            it.copy(
                isAnalyzing = false,
                detectedEmotion = null,
                reflectionPrompt = null,
                errorMessage = null
            )
        }
    }

    private fun stopCamera() {
        _state.update { it.copy(isCameraActive = false) }
    }
} 
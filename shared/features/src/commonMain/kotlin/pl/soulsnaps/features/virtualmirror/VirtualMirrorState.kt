package pl.soulsnaps.features.virtualmirror

data class VirtualMirrorState(
    val isCameraActive: Boolean = true,
    val isAnalyzing: Boolean = false,
    val detectedEmotion: DetectedEmotion? = null,
    val reflectionPrompt: String? = null,
    val errorMessage: String? = null
)

data class DetectedEmotion(
    val label: String,
    val emoji: String,
    val confidence: Float
)

sealed class VirtualMirrorIntent {
    object StartCamera : VirtualMirrorIntent()
    object StopCamera : VirtualMirrorIntent()
    object AnalyzeEmotion : VirtualMirrorIntent()
    data class EmotionDetected(val emotion: DetectedEmotion) : VirtualMirrorIntent()
    data class ShowPrompt(val prompt: String) : VirtualMirrorIntent()
    object TryAgain : VirtualMirrorIntent()
    object Back : VirtualMirrorIntent()
} 
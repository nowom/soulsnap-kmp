package pl.soulsnaps.features.onboarding

data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val voiceRecordingPath: String? = null,
    val selectedGoal: UserGoal? = null,
    val permissionsGranted: Set<Permission> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class OnboardingStep {
    WELCOME,
    VOICE_SETUP,
    GOALS,
    PERMISSIONS,
    GET_STARTED
}

enum class UserGoal(val title: String, val description: String, val emoji: String) {
    STRESS_MANAGEMENT("Zarządzanie stresem", "Naucz się technik relaksacji i radzenia sobie ze stresem", "😌"),
    EMOTIONAL_AWARENESS("Świadomość emocjonalna", "Lepiej poznaj i zrozum swoje emocje", "🧠"),
    SELF_LOVE("Miłość własna", "Buduj pozytywną relację z samym sobą", "❤️"),
    MINDFULNESS("Uważność", "Żyj bardziej świadomie i w teraźniejszości", "🧘"),
    GRATITUDE("Wdzięczność", "Rozwijaj wdzięczność za to, co masz", "🙏")
}

enum class Permission(val title: String, val description: String) {
    CAMERA("Kamera", "Do robienia zdjęć w SoulSnaps"),
    LOCATION("Lokalizacja", "Do zapisywania miejsc w Twoich wspomnieniach"),
    AUDIO("Mikrofon", "Do nagrywania głosu i dźwięków")
}

sealed class OnboardingIntent {
    object NextStep : OnboardingIntent()
    object PreviousStep : OnboardingIntent()
    object SkipVoiceSetup : OnboardingIntent()
    data class RecordVoice(val audioPath: String) : OnboardingIntent()
    data class SelectGoal(val goal: UserGoal) : OnboardingIntent()
    data class GrantPermission(val permission: Permission) : OnboardingIntent()
    object GetStarted : OnboardingIntent()
} 
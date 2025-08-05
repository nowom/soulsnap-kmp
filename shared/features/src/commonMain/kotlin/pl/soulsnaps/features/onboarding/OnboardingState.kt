package pl.soulsnaps.features.onboarding

data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val selectedFocus: UserFocus? = null,
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class OnboardingStep {
    WELCOME,
    APP_TOUR,
    PERSONALIZATION,
    AUTH,
    GET_STARTED
}

enum class UserFocus(val title: String, val description: String, val emoji: String) {
    STRESS_MANAGEMENT("Stress Management", "Learn relaxation techniques and stress coping strategies", "😌"),
    EMOTIONAL_AWARENESS("Emotional Awareness", "Better understand and recognize your emotions", "🧠"),
    SELF_LOVE("Self Love", "Build a positive relationship with yourself", "❤️"),
    MINDFULNESS("Mindfulness", "Live more consciously and in the present moment", "🧘"),
    GRATITUDE("Gratitude", "Develop gratitude for what you have", "🙏")
}

enum class AuthType {
    EMAIL,
    GOOGLE,
    FACEBOOK,
    ANONYMOUS
}

sealed class OnboardingIntent {
    object NextStep : OnboardingIntent()
    object PreviousStep : OnboardingIntent()
    object SkipTour : OnboardingIntent()
    data class SelectFocus(val focus: UserFocus) : OnboardingIntent()
    data class Authenticate(val authType: AuthType) : OnboardingIntent()
    data class UpdateEmail(val email: String) : OnboardingIntent()
    data class UpdatePassword(val password: String) : OnboardingIntent()
    object GetStarted : OnboardingIntent()
} 
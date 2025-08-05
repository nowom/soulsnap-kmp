package pl.soulsnaps.features.analytics

import kotlinx.datetime.Clock

sealed class AnalyticsEvent {
    data class OnboardingStarted(
        val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AnalyticsEvent()

    data class OnboardingStepCompleted(
        val step: String,
        val timeSpent: Long,
        val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AnalyticsEvent()

    data class OnboardingSkipped(
        val step: String,
        val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AnalyticsEvent()

    data class OnboardingCompleted(
        val totalTime: Long,
        val selectedFocus: String?,
        val authMethod: String?,
        val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AnalyticsEvent()

    data class AuthAttempted(
        val method: String,
        val success: Boolean,
        val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AnalyticsEvent()

    data class FeatureUsed(
        val feature: String,
        val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AnalyticsEvent()

    data class EmotionCaptured(
        val emotion: String,
        val intensity: String,
        val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AnalyticsEvent()

    data class ScreenViewed(
        val screen: String,
        val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AnalyticsEvent()

    data class ErrorOccurred(
        val error: String,
        val screen: String,
        val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ) : AnalyticsEvent()
} 
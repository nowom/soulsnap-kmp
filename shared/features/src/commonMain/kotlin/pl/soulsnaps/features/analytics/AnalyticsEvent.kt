package pl.soulsnaps.features.analytics

import pl.soulsnaps.utils.getCurrentTimeMillis

sealed class AnalyticsEvent {
    data class OnboardingStarted(
        val timestamp: Long = getCurrentTimeMillis()
    ) : AnalyticsEvent()
    
    data class OnboardingStepCompleted(
        val step: String,
        val timestamp: Long = getCurrentTimeMillis()
    ) : AnalyticsEvent()
    
    data class OnboardingCompleted(
        val totalTime: Long,
        val timestamp: Long = getCurrentTimeMillis()
    ) : AnalyticsEvent()
    
    data class FeatureUsed(
        val feature: String,
        val timestamp: Long = getCurrentTimeMillis()
    ) : AnalyticsEvent()
    
    data class ScreenViewed(
        val screen: String,
        val timestamp: Long = getCurrentTimeMillis()
    ) : AnalyticsEvent()
    
    data class ErrorOccurred(
        val error: String,
        val screen: String,
        val timestamp: Long = getCurrentTimeMillis()
    ) : AnalyticsEvent()
    
    data class UserAction(
        val action: String,
        val context: String,
        val timestamp: Long = getCurrentTimeMillis()
    ) : AnalyticsEvent()
    
    data class PerformanceMetric(
        val metric: String,
        val value: Long,
        val timestamp: Long = getCurrentTimeMillis()
    ) : AnalyticsEvent()
} 
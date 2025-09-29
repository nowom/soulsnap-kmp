package pl.soulsnaps.features.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import pl.soulsnaps.data.AnalyticsRepository
import pl.soulsnaps.utils.getCurrentTimeMillis
import pl.soulsnaps.crashlytics.CrashlyticsManager
import pl.soulsnaps.analytics.FirebaseAnalyticsManager

class AnalyticsManager(
    private val repository: AnalyticsRepository,
    private val crashlyticsManager: CrashlyticsManager,
    private val firebaseAnalytics: FirebaseAnalyticsManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _onboardingStartTime = MutableStateFlow<Long?>(null)
    private val _stepStartTimes = MutableStateFlow<Map<String, Long>>(emptyMap())
    
    private val _events = MutableStateFlow<List<AnalyticsEvent>>(emptyList())
    val events: StateFlow<List<AnalyticsEvent>> = _events.asStateFlow()

    init {
        coroutineScope.launch {
            repository.getEvents().collect { events ->
                _events.value = events
            }
        }
    }

    // Onboarding Analytics
    fun startOnboarding() {
        val startTime = getCurrentTimeMillis()
        _onboardingStartTime.value = startTime
        trackEvent(AnalyticsEvent.OnboardingStarted(startTime))
        
        // Firebase Analytics
        firebaseAnalytics.logEvent("onboarding_started", mapOf(
            "timestamp" to startTime
        ))
    }

    fun startStep(step: String) {
        _stepStartTimes.value = _stepStartTimes.value + (step to getCurrentTimeMillis())
        trackScreenView(step)
        
        // Firebase Analytics
        firebaseAnalytics.logScreenView(step)
    }

    fun completeStep(step: String) {
        val startTime = _stepStartTimes.value[step]
        val timeSpent = startTime?.let { getCurrentTimeMillis() - it } ?: 0L
        trackEvent(AnalyticsEvent.OnboardingStepCompleted(step, timeSpent))
        
        // Firebase Analytics
        firebaseAnalytics.logEvent("onboarding_step_completed", mapOf(
            "step" to step,
            "time_spent_ms" to timeSpent
        ))
    }

    fun skipStep(step: String) {
        trackEvent(AnalyticsEvent.UserAction("onboarding_skipped", step))
        
        // Firebase Analytics
        firebaseAnalytics.logEvent("onboarding_step_skipped", mapOf(
            "step" to step
        ))
    }

    fun completeOnboarding(selectedFocus: String?, authMethod: String?) {
        val startTime = _onboardingStartTime.value
        val totalTime = startTime?.let { getCurrentTimeMillis() - it } ?: 0L
        trackEvent(AnalyticsEvent.OnboardingCompleted(totalTime))
        
        // Firebase Analytics
        firebaseAnalytics.logEvent("onboarding_completed", mapOf(
            "total_time_ms" to totalTime,
            "selected_focus" to (selectedFocus ?: "none"),
            "auth_method" to (authMethod ?: "none")
        ))
    }

    // Auth Analytics
    fun trackAuthAttempt(method: String, success: Boolean) {
        trackEvent(AnalyticsEvent.UserAction("auth_attempted", "$method:$success"))
        
        // Firebase Analytics
        if (success) {
            firebaseAnalytics.logLogin(method)
        } else {
            firebaseAnalytics.logEvent("auth_failed", mapOf(
                "method" to method
            ))
        }
    }

    // Feature Analytics
    fun trackFeatureUsage(feature: String) {
        coroutineScope.launch {
            repository.trackFeatureUsage(feature)
        }
        
        // Firebase Analytics
        firebaseAnalytics.logEvent("feature_used", mapOf(
            "feature_name" to feature
        ))
    }

    // Emotion Analytics
    fun trackEmotionCapture(emotion: String, intensity: String) {
        trackEvent(AnalyticsEvent.UserAction("emotion_captured", "$emotion:$intensity"))
        
        // Firebase Analytics
        firebaseAnalytics.logEvent("emotion_captured", mapOf(
            "emotion" to emotion,
            "intensity" to intensity
        ))
    }

    // Screen Analytics
    fun trackScreenView(screen: String) {
        coroutineScope.launch {
            repository.trackScreenView(screen)
        }
        
        // Firebase Analytics
        firebaseAnalytics.logScreenView(screen)
    }

    // Error Analytics
    fun trackError(error: String, screen: String) {
        coroutineScope.launch {
            repository.trackError(error, screen)
        }
        
        // Firebase Analytics
        firebaseAnalytics.logEvent("error_occurred", mapOf(
            "error_message" to error,
            "screen" to screen
        ))
    }

    // Generic event tracking
    fun trackEvent(event: AnalyticsEvent) {
        coroutineScope.launch {
            repository.trackEvent(event)
        }
        
        // Firebase Analytics - log based on event type
        when (event) {
            is AnalyticsEvent.UserAction -> {
                firebaseAnalytics.logEvent("user_action", mapOf(
                    "action" to event.action,
                    "context" to event.context
                ))
            }
            is AnalyticsEvent.OnboardingStarted -> {
                firebaseAnalytics.logEvent("onboarding_started", mapOf(
                    "timestamp" to event.timestamp
                ))
            }
            is AnalyticsEvent.OnboardingStepCompleted -> {
                firebaseAnalytics.logEvent("onboarding_step_completed", mapOf(
                    "step" to event.step,
                    "timestamp" to event.timestamp
                ))
            }
            is AnalyticsEvent.OnboardingCompleted -> {
                firebaseAnalytics.logEvent("onboarding_completed", mapOf(
                    "total_time_ms" to event.totalTime
                ))
            }
            is AnalyticsEvent.ErrorOccurred -> {
                firebaseAnalytics.logEvent("error_occurred", mapOf(
                    "error_message" to event.error,
                    "screen" to event.screen
                ))
            }
            is AnalyticsEvent.FeatureUsed -> {
                firebaseAnalytics.logEvent("feature_used", mapOf(
                    "feature" to event.feature
                ))
            }
            is AnalyticsEvent.ScreenViewed -> {
                firebaseAnalytics.logEvent("screen_viewed", mapOf(
                    "screen" to event.screen
                ))
            }
            is AnalyticsEvent.PerformanceMetric -> {
                firebaseAnalytics.logEvent("performance_metric", mapOf(
                    "metric" to event.metric,
                    "value" to event.value
                ))
            }
        }
    }

    // Debug methods
    fun getAnalyticsSummary(): String {
        val currentEvents = events.value
        return buildString {
            appendLine("📊 Analytics Summary:")
            appendLine("Total Events: ${currentEvents.size}")
            appendLine("Events by Type:")
            currentEvents.groupBy { it::class.simpleName }
                .forEach { (type, events) ->
                    appendLine("  $type: ${events.size}")
                }
        }
    }

    fun clearAnalytics() {
        coroutineScope.launch {
            repository.clearEvents()
        }
    }
    
    // Crashlytics integration
    fun logError(error: Throwable, context: String = "") {
        crashlyticsManager.recordException(error)
        crashlyticsManager.log("Error in $context: ${error.message}")
        trackEvent(AnalyticsEvent.ErrorOccurred(error.message ?: "Unknown error", context))
    }
    
    fun setUserProperties(userId: String, properties: Map<String, String>) {
        crashlyticsManager.setUserId(userId)
        properties.forEach { (key, value) ->
            crashlyticsManager.setCustomKey(key, value)
        }
        
        // Firebase Analytics
        firebaseAnalytics.setUserId(userId)
        properties.forEach { (key, value) ->
            firebaseAnalytics.setUserProperty(key, value)
        }
    }
    
    fun testCrash() {
        crashlyticsManager.testCrash()
    }
} 
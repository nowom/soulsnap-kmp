package pl.soulsnaps.features.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import pl.soulsnaps.data.AnalyticsRepository
import pl.soulsnaps.utils.getCurrentTimeMillis

class AnalyticsManager(
    private val repository: AnalyticsRepository,
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
    }

    fun startStep(step: String) {
        _stepStartTimes.value = _stepStartTimes.value + (step to getCurrentTimeMillis())
        trackScreenView(step)
    }

    fun completeStep(step: String) {
        val startTime = _stepStartTimes.value[step]
        val timeSpent = startTime?.let { getCurrentTimeMillis() - it } ?: 0L
        trackEvent(AnalyticsEvent.OnboardingStepCompleted(step, timeSpent))
    }

    fun skipStep(step: String) {
        trackEvent(AnalyticsEvent.UserAction("onboarding_skipped", step))
    }

    fun completeOnboarding(selectedFocus: String?, authMethod: String?) {
        val startTime = _onboardingStartTime.value
        val totalTime = startTime?.let { getCurrentTimeMillis() - it } ?: 0L
        trackEvent(AnalyticsEvent.OnboardingCompleted(totalTime))
    }

    // Auth Analytics
    fun trackAuthAttempt(method: String, success: Boolean) {
        trackEvent(AnalyticsEvent.UserAction("auth_attempted", "$method:$success"))
    }

    // Feature Analytics
    fun trackFeatureUsage(feature: String) {
        coroutineScope.launch {
            repository.trackFeatureUsage(feature)
        }
    }

    // Emotion Analytics
    fun trackEmotionCapture(emotion: String, intensity: String) {
        trackEvent(AnalyticsEvent.UserAction("emotion_captured", "$emotion:$intensity"))
    }

    // Screen Analytics
    fun trackScreenView(screen: String) {
        coroutineScope.launch {
            repository.trackScreenView(screen)
        }
    }

    // Error Analytics
    fun trackError(error: String, screen: String) {
        coroutineScope.launch {
            repository.trackError(error, screen)
        }
    }

    // Generic event tracking
    fun trackEvent(event: AnalyticsEvent) {
        coroutineScope.launch {
            repository.trackEvent(event)
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
} 
package pl.soulsnaps.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.soulsnaps.features.analytics.AnalyticsEvent

class FakeAnalyticsRepository : AnalyticsRepository {
    private val events = MutableStateFlow<List<AnalyticsEvent>>(emptyList())
    
    override suspend fun trackEvent(event: AnalyticsEvent) {
        events.value = events.value + event
    }
    
    override suspend fun trackFeatureUsage(feature: String) {
        trackEvent(pl.soulsnaps.features.analytics.AnalyticsEvent.FeatureUsed(feature))
    }
    
    override suspend fun trackScreenView(screen: String) {
        trackEvent(pl.soulsnaps.features.analytics.AnalyticsEvent.ScreenViewed(screen))
    }
    
    override suspend fun trackError(error: String, screen: String) {
        trackEvent(pl.soulsnaps.features.analytics.AnalyticsEvent.ErrorOccurred(error, screen))
    }
    
    override fun getEvents(): Flow<List<AnalyticsEvent>> = events.asStateFlow()
    
    override suspend fun clearEvents() {
        events.value = emptyList()
    }
}

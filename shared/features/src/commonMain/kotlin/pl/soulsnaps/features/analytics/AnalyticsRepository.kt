package pl.soulsnaps.features.analytics

import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    suspend fun trackEvent(event: AnalyticsEvent)
    suspend fun trackScreenView(screen: String)
    suspend fun trackFeatureUsage(feature: String)
    suspend fun trackError(error: String, screen: String)
    fun getEvents(): Flow<List<AnalyticsEvent>>
    suspend fun clearEvents()
}

class FakeAnalyticsRepository : AnalyticsRepository {
    private val events = mutableListOf<AnalyticsEvent>()
    
    override suspend fun trackEvent(event: AnalyticsEvent) {
        events.add(event)
        println("📊 ANALYTICS: ${event::class.simpleName} - $event")
    }
    
    override suspend fun trackScreenView(screen: String) {
        trackEvent(AnalyticsEvent.ScreenViewed(screen))
    }
    
    override suspend fun trackFeatureUsage(feature: String) {
        trackEvent(AnalyticsEvent.FeatureUsed(feature))
    }
    
    override suspend fun trackError(error: String, screen: String) {
        trackEvent(AnalyticsEvent.ErrorOccurred(error, screen))
    }
    
    override fun getEvents(): Flow<List<AnalyticsEvent>> {
        return kotlinx.coroutines.flow.flowOf(events.toList())
    }
    
    override suspend fun clearEvents() {
        events.clear()
    }
} 
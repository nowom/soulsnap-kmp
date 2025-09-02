package pl.soulsnaps.data

import kotlinx.coroutines.flow.Flow
import pl.soulsnaps.features.analytics.AnalyticsEvent

interface AnalyticsRepository {
    suspend fun trackEvent(event: AnalyticsEvent)
    suspend fun trackFeatureUsage(feature: String)
    suspend fun trackScreenView(screen: String)
    suspend fun trackError(error: String, screen: String)
    fun getEvents(): Flow<List<AnalyticsEvent>>
    suspend fun clearEvents()
}

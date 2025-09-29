package pl.soulsnaps.crashlytics

/**
 * No-op implementation of CrashlyticsManager for common code
 * This is used when platform-specific implementation is not available
 */
class NoOpCrashlyticsManager : CrashlyticsManager {
    
    override fun setUserId(userId: String) {
        // No-op
    }
    
    override fun setCustomKey(key: String, value: String) {
        // No-op
    }
    
    override fun setCustomKey(key: String, value: Boolean) {
        // No-op
    }
    
    override fun setCustomKey(key: String, value: Int) {
        // No-op
    }
    
    override fun setCustomKey(key: String, value: Float) {
        // No-op
    }
    
    override fun setCustomKey(key: String, value: Double) {
        // No-op
    }
    
    override fun log(message: String) {
        // No-op
    }
    
    override fun recordException(throwable: Throwable) {
        // No-op
    }
    
    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        // No-op
    }
    
    override fun testCrash() {
        // No-op
    }
    
    override fun resetAnalyticsData() {
        // No-op
    }
}

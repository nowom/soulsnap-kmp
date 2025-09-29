package pl.soulsnaps.crashlytics

/**
 * iOS implementation of CrashlyticsManager (no-op for now)
 * TODO: Implement iOS crash reporting when needed
 */
class IosCrashlyticsManager : CrashlyticsManager {
    
    override fun setUserId(userId: String) {
        // No-op for iOS
    }
    
    override fun setCustomKey(key: String, value: String) {
        // No-op for iOS
    }
    
    override fun setCustomKey(key: String, value: Boolean) {
        // No-op for iOS
    }
    
    override fun setCustomKey(key: String, value: Int) {
        // No-op for iOS
    }
    
    override fun setCustomKey(key: String, value: Float) {
        // No-op for iOS
    }
    
    override fun setCustomKey(key: String, value: Double) {
        // No-op for iOS
    }
    
    override fun log(message: String) {
        // No-op for iOS
    }
    
    override fun recordException(throwable: Throwable) {
        // No-op for iOS
    }
    
    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        // No-op for iOS
    }
    
    override fun testCrash() {
        // No-op for iOS
    }
    
    override fun resetAnalyticsData() {
        // No-op for iOS
    }
}

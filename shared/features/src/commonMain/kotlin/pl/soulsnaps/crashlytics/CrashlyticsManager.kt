package pl.soulsnaps.crashlytics

/**
 * Interface for crash reporting functionality
 * This allows for platform-specific implementations
 */
interface CrashlyticsManager {
    /**
     * Set user identifier for crash reports
     */
    fun setUserId(userId: String)
    
    /**
     * Set custom key-value pair for crash reports
     */
    fun setCustomKey(key: String, value: String)
    
    /**
     * Set custom key-value pair for crash reports (Boolean)
     */
    fun setCustomKey(key: String, value: Boolean)
    
    /**
     * Set custom key-value pair for crash reports (Int)
     */
    fun setCustomKey(key: String, value: Int)
    
    /**
     * Set custom key-value pair for crash reports (Float)
     */
    fun setCustomKey(key: String, value: Float)
    
    /**
     * Set custom key-value pair for crash reports (Double)
     */
    fun setCustomKey(key: String, value: Double)
    
    /**
     * Log a non-fatal error
     */
    fun log(message: String)
    
    /**
     * Record a non-fatal exception
     */
    fun recordException(throwable: Throwable)
    
    /**
     * Enable or disable crash collection
     */
    fun setCrashlyticsCollectionEnabled(enabled: Boolean)
    
    /**
     * Test crash (for development only)
     */
    fun testCrash()
    
    /**
     * Reset analytics data (for GDPR compliance)
     */
    fun resetAnalyticsData()
}

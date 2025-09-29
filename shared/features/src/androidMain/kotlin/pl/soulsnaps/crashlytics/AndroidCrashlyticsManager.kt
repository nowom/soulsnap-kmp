package pl.soulsnaps.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Android implementation of CrashlyticsManager using Firebase Crashlytics
 */
class AndroidCrashlyticsManager : CrashlyticsManager {
    
    private val crashlytics = FirebaseCrashlytics.getInstance()
    
    override fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }
    
    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }
    
    override fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }
    
    override fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }
    
    override fun setCustomKey(key: String, value: Float) {
        crashlytics.setCustomKey(key, value)
    }
    
    override fun setCustomKey(key: String, value: Double) {
        crashlytics.setCustomKey(key, value)
    }
    
    override fun log(message: String) {
        crashlytics.log(message)
    }
    
    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
    
    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }
    
    override fun testCrash() {
        crashlytics.log("Test crash triggered")
        throw RuntimeException("Test crash for Crashlytics")
    }
    
    override fun resetAnalyticsData() {
        // Firebase Crashlytics doesn't have resetAnalyticsData method
        // This would be handled by Firebase Analytics if integrated
        crashlytics.log("Analytics data reset requested")
    }
}

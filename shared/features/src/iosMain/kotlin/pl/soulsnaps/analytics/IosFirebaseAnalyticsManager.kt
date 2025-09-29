package pl.soulsnaps.analytics

/**
 * iOS implementation of FirebaseAnalyticsManager (no-op for now)
 * TODO: Implement iOS Firebase Analytics when needed
 */
class IosFirebaseAnalyticsManager : FirebaseAnalyticsManager {
    
    override fun logEvent(eventName: String, parameters: Map<String, Any>) {
        // No-op for iOS
    }
    
    override fun setUserProperty(name: String, value: String) {
        // No-op for iOS
    }
    
    override fun setUserId(userId: String) {
        // No-op for iOS
    }
    
    override fun logScreenView(screenName: String, screenClass: String?) {
        // No-op for iOS
    }
    
    override fun logCustomEvent(eventName: String, vararg parameters: Pair<String, Any>) {
        // No-op for iOS
    }
    
    override fun setDefaultEventParameters(parameters: Map<String, Any>) {
        // No-op for iOS
    }
    
    override fun resetAnalyticsData() {
        // No-op for iOS
    }
    
    override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        // No-op for iOS
    }
    
    override fun logPurchase(
        transactionId: String,
        value: Double,
        currency: String,
        items: List<PurchaseItem>
    ) {
        // No-op for iOS
    }
    
    override fun logLogin(method: String) {
        // No-op for iOS
    }
    
    override fun logSignUp(method: String) {
        // No-op for iOS
    }
    
    override fun logSearch(searchTerm: String, category: String?) {
        // No-op for iOS
    }
    
    override fun logShare(contentType: String, itemId: String?) {
        // No-op for iOS
    }
    
    override fun logAppOpen() {
        // No-op for iOS
    }
    
    override fun logFirstOpen() {
        // No-op for iOS
    }
}


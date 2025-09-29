package pl.soulsnaps.analytics

/**
 * Interface for Firebase Analytics functionality
 * This allows for platform-specific implementations
 */
interface FirebaseAnalyticsManager {
    /**
     * Log a custom event
     */
    fun logEvent(eventName: String, parameters: Map<String, Any> = emptyMap())
    
    /**
     * Set user properties
     */
    fun setUserProperty(name: String, value: String)
    
    /**
     * Set user ID
     */
    fun setUserId(userId: String)
    
    /**
     * Log screen view
     */
    fun logScreenView(screenName: String, screenClass: String? = null)
    
    /**
     * Log custom event with parameters
     */
    fun logCustomEvent(eventName: String, vararg parameters: Pair<String, Any>)
    
    /**
     * Set default event parameters
     */
    fun setDefaultEventParameters(parameters: Map<String, Any>)
    
    /**
     * Reset analytics data
     */
    fun resetAnalyticsData()
    
    /**
     * Set analytics collection enabled
     */
    fun setAnalyticsCollectionEnabled(enabled: Boolean)
    
    /**
     * Log purchase event
     */
    fun logPurchase(
        transactionId: String,
        value: Double,
        currency: String,
        items: List<PurchaseItem> = emptyList()
    )
    
    /**
     * Log login event
     */
    fun logLogin(method: String)
    
    /**
     * Log sign up event
     */
    fun logSignUp(method: String)
    
    /**
     * Log search event
     */
    fun logSearch(searchTerm: String, category: String? = null)
    
    /**
     * Log share event
     */
    fun logShare(contentType: String, itemId: String? = null)
    
    /**
     * Log app open event
     */
    fun logAppOpen()
    
    /**
     * Log first open event
     */
    fun logFirstOpen()
}

/**
 * Data class for purchase items
 */
data class PurchaseItem(
    val itemId: String,
    val itemName: String,
    val itemCategory: String,
    val quantity: Int,
    val price: Double
)


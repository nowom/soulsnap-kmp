package pl.soulsnaps.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Android implementation of FirebaseAnalyticsManager using Firebase Analytics
 */
class AndroidFirebaseAnalyticsManager(
    private val context: Context
) : FirebaseAnalyticsManager {
    
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    
    override fun logEvent(eventName: String, parameters: Map<String, Any>) {
        val bundle = Bundle()
        parameters.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Long -> bundle.putLong(key, value)
                is Int -> bundle.putInt(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putFloat(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
    
    override fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }
    
    override fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
    }
    
    override fun logScreenView(screenName: String, screenClass: String?) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { putString(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
    
    override fun logCustomEvent(eventName: String, vararg parameters: Pair<String, Any>) {
        logEvent(eventName, parameters.toMap())
    }
    
    override fun setDefaultEventParameters(parameters: Map<String, Any>) {
        // Firebase Analytics doesn't have direct support for default parameters
        // This would need to be handled at the application level
    }
    
    override fun resetAnalyticsData() {
        firebaseAnalytics.resetAnalyticsData()
    }
    
    override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }
    
    override fun logPurchase(
        transactionId: String,
        value: Double,
        currency: String,
        items: List<PurchaseItem>
    ) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.TRANSACTION_ID, transactionId)
            putDouble(FirebaseAnalytics.Param.VALUE, value)
            putString(FirebaseAnalytics.Param.CURRENCY, currency)
            
            if (items.isNotEmpty()) {
                val itemList = items.map { item ->
                    Bundle().apply {
                        putString(FirebaseAnalytics.Param.ITEM_ID, item.itemId)
                        putString(FirebaseAnalytics.Param.ITEM_NAME, item.itemName)
                        putString(FirebaseAnalytics.Param.ITEM_CATEGORY, item.itemCategory)
                        putLong(FirebaseAnalytics.Param.QUANTITY, item.quantity.toLong())
                        putDouble(FirebaseAnalytics.Param.PRICE, item.price)
                    }
                }
                putParcelableArray(FirebaseAnalytics.Param.ITEMS, itemList.toTypedArray())
            }
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, bundle)
    }
    
    override fun logLogin(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }
    
    override fun logSignUp(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
    }
    
    override fun logSearch(searchTerm: String, category: String?) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, searchTerm)
            category?.let { putString("category", it) }
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
    }
    
    override fun logShare(contentType: String, itemId: String?) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            itemId?.let { putString(FirebaseAnalytics.Param.ITEM_ID, it) }
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
    }
    
    override fun logAppOpen() {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, Bundle())
    }
    
    override fun logFirstOpen() {
        firebaseAnalytics.logEvent("first_open", Bundle())
    }
}
package pl.soulsnaps.access.storage

import android.content.Context
import android.content.ContextWrapper
import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration

/**
 * Android implementacja UserPreferencesStorageFactory
 * 
 * Pobiera context z aktualnej aktywności lub aplikacji
 */
actual object UserPreferencesStorageFactory {
    
    private var applicationContext: Context? = null
    
    /**
     * Inicjalizuje factory z context aplikacji
     * Powinno być wywołane w Application.onCreate()
     */
    fun initialize(context: Context) {
        println("DEBUG: UserPreferencesStorageFactory.initialize() called with context: ${context.packageName}")
        applicationContext = context.applicationContext
    }
    
    /**
     * Pobiera context aplikacji
     */
    actual fun getContext(): Any {
        println("DEBUG: UserPreferencesStorageFactory.getContext() called")
        return applicationContext 
            ?: throw IllegalStateException("UserPreferencesStorageFactory not initialized. Call initialize() first.")
    }
    
    /**
     * Tworzy instancję UserPreferencesStorage z dostępnym context
     */
    actual fun create(): UserPreferencesStorage {
        return UserPreferencesStorage()
    }
}




// Android App Example
// File: androidApp/src/main/java/com/example/soulsnaps/AndroidApplication.kt

package com.example.soulsnaps

import android.app.Application
import pl.soulsnaps.di.AndroidKoinInitializer
import pl.soulsnaps.sync.manager.SyncManager
import org.koin.android.ext.android.inject

class AndroidApplication : Application() {
    
    private val syncManager: SyncManager by inject()
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin with platform module
        AndroidKoinInitializer.initialize()
        
        // Start sync manager
        syncManager.start()
        
        println("DEBUG: AndroidApplication.onCreate() - Koin initialized and sync started")
    }
    
    override fun onTerminate() {
        super.onTerminate()
        
        // Stop sync manager
        syncManager.stop()
        
        println("DEBUG: AndroidApplication.onTerminate() - sync stopped")
    }
}

// iOS App Example
// File: iosApp/iosApp/iOSApp.swift

import SwiftUI
import shared

@main
struct iOSApp: App {
    
    init() {
        // Initialize Koin with platform module
        IOSKoinInitializerKt.initialize()
        
        // Start sync manager
        let syncManager = KoinHelperKt.getSyncManager()
        syncManager.start()
        
        print("DEBUG: iOSApp.init() - Koin initialized and sync started")
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

// Helper for accessing Koin from Swift
class KoinHelper {
    static func getSyncManager() -> SyncManager {
        return KoinHelperKt.getSyncManager()
    }
}

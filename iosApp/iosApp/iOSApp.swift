import SwiftUI
import shared

@main
struct iOSApp: App {
    
    @StateObject private var networkMonitor = NetworkMonitor()
    @State private var backgroundTaskHandler: BackgroundTaskHandler?
    
    init() {
        // Initialize background task handler
        setupBackgroundTasks()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(networkMonitor)
                .onAppear {
                    setupSyncManager()
                }
        }
    }
    
    /**
     * Setup background tasks
     */
    private func setupBackgroundTasks() {
        print("DEBUG: iOSApp.setupBackgroundTasks() - setting up background tasks")
        
        // Register background task identifier in Info.plist
        // Add to Info.plist:
        // <key>BGTaskSchedulerPermittedIdentifiers</key>
        // <array>
        //     <string>com.soulsnaps.sync</string>
        // </array>
    }
    
    /**
     * Setup sync manager with network monitoring
     */
    private func setupSyncManager() {
        print("DEBUG: iOSApp.setupSyncManager() - setting up sync manager")
        
        // TODO: Initialize actual sync manager with dependencies
        // let syncManager = IOSSyncManager(...)
        // backgroundTaskHandler = BackgroundTaskHandler(syncManager: syncManager)
        // networkMonitor.setSyncManager(syncManager)
        
        // Schedule initial background task
        backgroundTaskHandler?.scheduleBackgroundTask()
    }
}
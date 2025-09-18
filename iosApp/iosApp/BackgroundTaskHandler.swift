import Foundation
import BackgroundTasks
import shared

/**
 * iOS Background Task Handler for SoulSnaps sync
 */
class BackgroundTaskHandler {
    
    private let syncManager: IOSSyncManager
    private let backgroundTaskIdentifier = "com.soulsnaps.sync"
    
    init(syncManager: IOSSyncManager) {
        self.syncManager = syncManager
        registerBackgroundTask()
    }
    
    /**
     * Register background task with iOS
     */
    private func registerBackgroundTask() {
        print("DEBUG: BackgroundTaskHandler.registerBackgroundTask() - registering background task")
        
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: backgroundTaskIdentifier,
            using: nil
        ) { [weak self] task in
            self?.handleBackgroundTask(task: task as! BGAppRefreshTask)
        }
    }
    
    /**
     * Handle background task execution
     */
    private func handleBackgroundTask(task: BGAppRefreshTask) {
        print("DEBUG: BackgroundTaskHandler.handleBackgroundTask() - executing background sync")
        
        // Schedule next background task
        scheduleBackgroundTask()
        
        // Set expiration handler
        task.expirationHandler = {
            print("DEBUG: BackgroundTaskHandler.handleBackgroundTask() - task expired")
            task.setTaskCompleted(success: false)
        }
        
        // Execute sync
        syncManager.handleBackgroundTask { success in
            print("DEBUG: BackgroundTaskHandler.handleBackgroundTask() - sync completed with success: \(success)")
            task.setTaskCompleted(success: success)
        }
    }
    
    /**
     * Schedule next background task
     */
    func scheduleBackgroundTask() {
        print("DEBUG: BackgroundTaskHandler.scheduleBackgroundTask() - scheduling next background task")
        
        let request = BGAppRefreshTaskRequest(identifier: backgroundTaskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 30 * 60) // 30 minutes
        
        do {
            try BGTaskScheduler.shared.submit(request)
            print("DEBUG: BackgroundTaskHandler.scheduleBackgroundTask() - background task scheduled successfully")
        } catch {
            print("ERROR: BackgroundTaskHandler.scheduleBackgroundTask() - failed to schedule background task: \(error)")
        }
    }
    
    /**
     * Cancel all background tasks
     */
    func cancelAllBackgroundTasks() {
        print("DEBUG: BackgroundTaskHandler.cancelAllBackgroundTasks() - cancelling all background tasks")
        BGTaskScheduler.shared.cancelAllTaskRequests()
    }
}

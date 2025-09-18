package pl.soulsnaps.sync.scheduler

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import pl.soulsnaps.sync.manager.PlatformScheduler
import pl.soulsnaps.sync.worker.AdvancedSyncWorker
import java.util.concurrent.TimeUnit

/**
 * Android PlatformScheduler using WorkManager
 */
class AndroidPlatformScheduler(
    private val context: Context
) : PlatformScheduler {
    
    private val workManager = WorkManager.getInstance(context)
    
    companion object {
        private const val SYNC_WORK_NAME = "advanced_sync"
        private const val PERIODIC_SYNC_WORK_NAME = "periodic_sync"
    }
    
    override fun ensureScheduled() {
        println("DEBUG: AndroidPlatformScheduler.ensureScheduled() - scheduling periodic sync")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        // Periodic sync every 30 minutes
        val periodicSyncWork = PeriodicWorkRequestBuilder<AdvancedSyncWorker>(
            30, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("periodic_sync")
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncWork
        )
        
        println("DEBUG: AndroidPlatformScheduler.ensureScheduled() - periodic sync scheduled")
    }
    
    override fun wakeUpNow() {
        println("DEBUG: AndroidPlatformScheduler.wakeUpNow() - triggering immediate sync")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val immediateSyncWork = OneTimeWorkRequestBuilder<AdvancedSyncWorker>()
            .setConstraints(constraints)
            .addTag("immediate_sync")
            .build()
        
        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            immediateSyncWork
        )
        
        println("DEBUG: AndroidPlatformScheduler.wakeUpNow() - immediate sync enqueued")
    }
    
    override fun cancel() {
        println("DEBUG: AndroidPlatformScheduler.cancel() - cancelling all sync work")
        
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
        workManager.cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
        workManager.cancelAllWorkByTag("periodic_sync")
        workManager.cancelAllWorkByTag("immediate_sync")
        
        println("DEBUG: AndroidPlatformScheduler.cancel() - all sync work cancelled")
    }
}

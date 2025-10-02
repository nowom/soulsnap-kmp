package pl.soulsnaps.sync.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import pl.soulsnaps.sync.connectivity.ConnectivityMonitor
import pl.soulsnaps.sync.events.AppEvent
import pl.soulsnaps.sync.events.GlobalEventBus
import pl.soulsnaps.sync.model.SyncConfig
import pl.soulsnaps.sync.model.SyncStatus
import pl.soulsnaps.sync.model.SyncTask
import pl.soulsnaps.sync.processor.SyncProcessor
import pl.soulsnaps.sync.queue.SyncQueue
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Advanced SyncManager with queue processing and per-entity mutex
 */
class AdvancedSyncManager(
    private val syncQueue: SyncQueue,
    private val syncProcessor: SyncProcessor,
    private val connectivityMonitor: ConnectivityMonitor,
    private val platformScheduler: PlatformScheduler,
    private val config: SyncConfig = SyncConfig()
) : SyncManager {
    
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _status = MutableStateFlow(SyncStatus(running = false, pendingCount = 0))
    
    // Per-entity mutex to prevent race conditions
    private val entityMutexes = mutableMapOf<Long, kotlinx.coroutines.sync.Mutex>()
    
    override val status: StateFlow<SyncStatus> = _status.asStateFlow()
    
    private var isRunning = false
    private var processingJob: kotlinx.coroutines.Job? = null
    
    override fun start() {
        if (isRunning) {
            //println("⚠️ AdvancedSyncManager.start() - already running, skipping")
            return
        }
        
        isRunning = true
        //println("========================================")
        //println("🚀 AdvancedSyncManager.start() - STARTING SYNC MANAGER")
        //println("========================================")
        
        // Start connectivity monitoring
        //println("📡 Starting connectivity monitor...")
        connectivityMonitor.start()
        //println("✅ Connectivity monitor started")
        
        // Start processing loop
        //println("🔄 Starting processing loop...")
        startProcessingLoop()
        //println("✅ Processing loop started")
        
        // Schedule periodic wake-ups
        //println("⏰ Scheduling periodic wake-ups...")
        platformScheduler.ensureScheduled()
        //println("✅ Scheduler configured")
        
        // Pull all data on startup if configured
        if (config.pullOnStartup) {
            //println("📥 Pull on startup enabled, enqueueing PullAll task")
            syncScope.launch {
                enqueue(pl.soulsnaps.sync.model.PullAll())
            }
        }
        
        //println("========================================")
        //println("✅ AdvancedSyncManager.start() - SYNC MANAGER RUNNING")
        //println("========================================")
    }
    
    override fun stop() {
        if (!isRunning) return
        
        isRunning = false
        //println("DEBUG: AdvancedSyncManager.stop() - stopping sync manager")
        
        // Stop processing
        processingJob?.cancel()
        processingJob = null
        
        // Stop connectivity monitoring
        connectivityMonitor.stop()
        
        // Cancel platform scheduler
        platformScheduler.cancel()
        
        // Cancel all coroutines
        syncScope.cancel()
    }
    
    override suspend fun triggerNow() {
        if (!isRunning) {
            //println("DEBUG: AdvancedSyncManager.triggerNow() - not running, ignoring trigger")
            return
        }
        
        //println("DEBUG: AdvancedSyncManager.triggerNow() - triggering immediate sync")
        processSyncQueue()
    }
    
    override suspend fun enqueue(task: SyncTask) {
        if (!isRunning) {
            //println("ERROR: AdvancedSyncManager.enqueue() - ❌ not running, ignoring task: ${task.id}")
            //println("ERROR: AdvancedSyncManager.enqueue() - ❌ You need to call syncManager.start() first!")
            return
        }
        
        //println("DEBUG: AdvancedSyncManager.enqueue() - ✅ enqueueing task: ${task.id}")
        //println("DEBUG: AdvancedSyncManager.enqueue() - task type: ${task::class.simpleName}")
        //println("DEBUG: AdvancedSyncManager.enqueue() - isOnline: ${connectivityMonitor.connected.value}")
        
        syncQueue.enqueue(task)
        
        // Update status
        updateStatus()
        
        val pendingCount = syncQueue.getPendingCount()
        //println("DEBUG: AdvancedSyncManager.enqueue() - queue size after enqueue: $pendingCount")
        
        // Trigger immediate processing if online
        if (connectivityMonitor.connected.value) {
            //println("DEBUG: AdvancedSyncManager.enqueue() - triggering immediate sync (online)")
            triggerNow()
        } else {
            //println("WARNING: AdvancedSyncManager.enqueue() - offline, sync will happen when connection is restored")
        }
    }
    
    private fun startProcessingLoop() {
        processingJob = syncScope.launch {
            while (isRunning) {
                try {
                    // Wait for connectivity or timeout
                    if (!connectivityMonitor.connected.value) {
                        delay(5000) // Check every 5 seconds when offline
                        continue
                    }
                    
                    // Process sync queue
                    processSyncQueue()
                    
                    // Update status
                    updateStatus()
                    
                    // Wait before next iteration
                    delay(1000) // Check every second when online
                } catch (e: Exception) {
                    //println("ERROR: AdvancedSyncManager.processingLoop() - error: ${e.message}")
                    delay(5000) // Wait longer on error
                }
            }
        }
    }
    
    private suspend fun processSyncQueue() {
        //println("DEBUG: AdvancedSyncManager.processSyncQueue() - 🔄 starting sync queue processing")
        
        if (!connectivityMonitor.connected.value) {
            //println("WARNING: AdvancedSyncManager.processSyncQueue() - ⚠️ offline, skipping processing")
            return
        }
        
        try {
            // Get due tasks
            val dueTasks = syncQueue.getDueTasks(config.maxParallelTasks)
            //println("DEBUG: AdvancedSyncManager.processSyncQueue() - found ${dueTasks.size} due tasks")
            
            if (dueTasks.isEmpty()) {
                //println("DEBUG: AdvancedSyncManager.processSyncQueue() - no due tasks, returning")
                return
            }
            
            //println("DEBUG: AdvancedSyncManager.processSyncQueue() - processing ${dueTasks.size} tasks")
            
            // Emit sync started event
            GlobalEventBus.emit(AppEvent.SyncStarted(dueTasks.size))
            
            // Process tasks in parallel (with per-entity mutex)
            val results = dueTasks.map { taskEntity ->
                syncScope.launch {
                    processTask(taskEntity)
                }
            }
            
            // Wait for all tasks to complete
            results.forEach { it.join() }
            
            // Count results
            val successCount = results.count { it.isCompleted && !it.isCancelled }
            val failureCount = results.size - successCount
            
            // Emit sync completed event
            GlobalEventBus.emit(AppEvent.SyncCompleted(successCount, failureCount))
            
            //println("DEBUG: AdvancedSyncManager.processSyncQueue() - completed: $successCount success, $failureCount failures")
            
        } catch (e: Exception) {
            //println("ERROR: AdvancedSyncManager.processSyncQueue() - error: ${e.message}")
            GlobalEventBus.emit(AppEvent.SyncFailed(e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun processTask(taskEntity: pl.soulsnaps.sync.queue.SyncTaskEntity) {
        val task = taskEntity.toSyncTask() ?: return
        
        // Get or create mutex for this entity
        val mutex = entityMutexes.getOrPut(task.localId) {
            kotlinx.coroutines.sync.Mutex()
        }
        
        mutex.withLock {
            try {
                // Mark task as running
                syncQueue.markRunning(taskEntity.id)
                
                // Process task
                val result = syncProcessor.run(task)
                
                if (result.isSuccess) {
                    // Mark as completed
                    syncQueue.markCompleted(taskEntity.id)
                    //println("DEBUG: AdvancedSyncManager.processTask() - task completed: ${task.id}")
                } else {
                    // Mark as failed and reschedule
                    syncQueue.markFailed(taskEntity.id, taskEntity.attemptCount + 1)
                    //println("ERROR: AdvancedSyncManager.processTask() - task failed: ${task.id}, error: ${result.exceptionOrNull()?.message}")
                }
                
            } catch (e: Exception) {
                // Mark as failed and reschedule
                syncQueue.markFailed(taskEntity.id, taskEntity.attemptCount + 1)
                //println("ERROR: AdvancedSyncManager.processTask() - task exception: ${task.id}, error: ${e.message}")
            }
        }
    }
    
    private suspend fun updateStatus() {
        val pendingCount = syncQueue.getPendingCount()
        val runningCount = syncQueue.getRunningCount()
        val running = runningCount > 0
        
        _status.value = SyncStatus(
            running = running,
            pendingCount = pendingCount,
            lastSyncTime = if (pendingCount == 0) getCurrentTimeMillis() else null
        )
    }
}

/**
 * SyncManager interface
 */
interface SyncManager {
    fun start()
    fun stop()
    suspend fun triggerNow()
    suspend fun enqueue(task: SyncTask)
    val status: StateFlow<SyncStatus>
}

/**
 * Platform scheduler interface
 */
interface PlatformScheduler {
    fun ensureScheduled()
    fun wakeUpNow()
    fun cancel()
}

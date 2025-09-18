package pl.soulsnaps.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import pl.soulsnaps.data.OnlineDataSource
import pl.soulsnaps.database.SoulSnapDatabase
import pl.soulsnaps.database.dao.MemoryDao
import pl.soulsnaps.features.auth.UserSessionManager
import pl.soulsnaps.sync.connectivity.ConnectivityMonitor
import pl.soulsnaps.sync.events.EventBus
import pl.soulsnaps.sync.manager.PlatformScheduler
import pl.soulsnaps.sync.manager.SyncManager
import pl.soulsnaps.sync.model.SyncConfig
import pl.soulsnaps.sync.processor.SyncProcessor
import pl.soulsnaps.sync.processor.SyncProcessorImpl
import pl.soulsnaps.sync.queue.SyncQueue
import pl.soulsnaps.sync.storage.StorageClient
import pl.soulsnaps.sync.file.ImagePipeline
import pl.soulsnaps.sync.file.LocalFileIO
import pl.soulsnaps.sync.storage.SupabaseStorageClient

/**
 * Common DI module for shared components
 * Defines interfaces and common implementations
 */
val commonModule = module {
    
    // Sync configuration
    single { 
        SyncConfig(
            maxParallelTasks = 3,
            backoffBaseMs = 15000L,
            backoffMaxMs = 3600000L,
            uploadCompression = true,
            pullOnStartup = true,
            retryOnMetered = false
        )
    }
    
    // Event bus (singleton)
    single<EventBus> { pl.soulsnaps.sync.events.GlobalEventBus }
    
    // Sync queue
    single {
        SyncQueue(
            database = get<SoulSnapDatabase>(),
            config = get<SyncConfig>()
        )
    }
    
    // Sync processor
    single<SyncProcessor> {
        SyncProcessorImpl(
            memoryDao = get<MemoryDao>(),
            onlineDataSource = get<OnlineDataSource>(),
            storageClient = get<StorageClient>(),
            userSessionManager = get<UserSessionManager>(),
            eventBus = get<EventBus>()
        )
    }
    
    // Sync manager
    single<SyncManager> {
        pl.soulsnaps.sync.manager.AdvancedSyncManager(
            syncQueue = get<SyncQueue>(),
            syncProcessor = get<SyncProcessor>(),
            connectivityMonitor = get<ConnectivityMonitor>(),
            platformScheduler = get<PlatformScheduler>(),
            config = get<SyncConfig>()
        )
    }

    // Supabase storage client with file processing
    single<StorageClient> {
        SupabaseStorageClient(
            supabaseClient = get(),
            imagePipeline = get<ImagePipeline>(),
            localFileIO = get<LocalFileIO>()
        )
    }
}
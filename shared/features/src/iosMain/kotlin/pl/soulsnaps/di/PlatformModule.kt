package pl.soulsnaps.di

import org.koin.core.module.Module
import org.koin.dsl.module
import pl.soulsnaps.audio.AudioPlayer
import pl.soulsnaps.audio.AudioPlayerFactory
import pl.soulsnaps.database.DatabaseDriverFactory
import pl.soulsnaps.sync.connectivity.ConnectivityMonitor
import pl.soulsnaps.sync.connectivity.IOSConnectivityMonitor
import pl.soulsnaps.sync.file.IOSImagePipeline
import pl.soulsnaps.sync.file.IOSLocalFileIO
import pl.soulsnaps.sync.file.ImagePipeline
import pl.soulsnaps.sync.file.LocalFileIO
import pl.soulsnaps.sync.manager.PlatformScheduler
import pl.soulsnaps.sync.scheduler.IOSPlatformScheduler
import pl.soulsnaps.sync.storage.StorageClient
import pl.soulsnaps.sync.storage.SupabaseStorageClient

/**
 * iOS-specific platform module
 * Provides platform-specific implementations
 */
actual val platformModule: Module = module {
    single {
        DatabaseDriverFactory().createDriver()
    }
    single<AudioPlayer> {
        AudioPlayerFactory().create()
    }

    // File I/O and image processing
    single<ImagePipeline> {
        IOSImagePipeline()
    }
    
    single<LocalFileIO> {
        IOSLocalFileIO()
    }
    
    // iOS connectivity monitor
    single<ConnectivityMonitor> {
        IOSConnectivityMonitor()
    }
    
    // iOS platform scheduler
    single<PlatformScheduler> {
        IOSPlatformScheduler()
    }
}

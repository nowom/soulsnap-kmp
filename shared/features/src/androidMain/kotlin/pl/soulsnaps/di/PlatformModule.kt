package pl.soulsnaps.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pl.soulsnaps.audio.AudioPlayer
import pl.soulsnaps.audio.AudioPlayerFactory
import pl.soulsnaps.database.DatabaseDriverFactory
import pl.soulsnaps.sync.connectivity.AndroidConnectivityMonitor
import pl.soulsnaps.sync.connectivity.ConnectivityMonitor
import pl.soulsnaps.sync.file.AndroidImagePipeline
import pl.soulsnaps.sync.file.AndroidLocalFileIO
import pl.soulsnaps.sync.file.ImagePipeline
import pl.soulsnaps.sync.file.LocalFileIO
import pl.soulsnaps.sync.manager.PlatformScheduler
import pl.soulsnaps.sync.scheduler.AndroidPlatformScheduler
import pl.soulsnaps.sync.storage.StorageClient
import pl.soulsnaps.sync.storage.SupabaseStorageClient
import pl.soulsnaps.util.ConnectivityManagerNetworkMonitor
import pl.soulsnaps.util.NetworkMonitor

/**
 * Android-specific platform module
 * Provides platform-specific implementations
 */
actual val platformModule: Module = module {
    single {
        DatabaseDriverFactory(context = androidContext()).createDriver()
    }
    single<AudioPlayer> {
        AudioPlayerFactory(androidContext()).create()
    }
    single<NetworkMonitor> { ConnectivityManagerNetworkMonitor(androidContext()) }

    single<ConnectivityMonitor> {
        AndroidConnectivityMonitor(androidContext())
    }

    // File I/O and image processing
    single<ImagePipeline> {
        AndroidImagePipeline(androidContext())
    }
    
    single<LocalFileIO> {
        AndroidLocalFileIO(androidContext())
    }
    
    // Android platform scheduler
    single<PlatformScheduler> {
        AndroidPlatformScheduler(androidContext())
    }
}

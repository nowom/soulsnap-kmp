package pl.soulsnaps.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.core.module.Module
import org.koin.dsl.module
import pl.soulsnaps.access.storage.createPreferencesDataStore
import pl.soulsnaps.audio.AudioPlayer
import pl.soulsnaps.audio.AudioPlayerFactory
import pl.soulsnaps.components.IOSSettingsNavigator
import pl.soulsnaps.components.SettingsNavigator
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
import pl.soulsnaps.crashlytics.CrashlyticsManager
import pl.soulsnaps.crashlytics.CrashlyticsManagerFactory
import pl.soulsnaps.storage.FileStorageManager
import pl.soulsnaps.storage.FileStorageManagerFactory

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

    single<DataStore<Preferences>> {
        createPreferencesDataStore()
    }

    // Crashlytics
    single<CrashlyticsManager> {
        CrashlyticsManagerFactory.create()
    }

    // Firebase Analytics
    single<pl.soulsnaps.analytics.FirebaseAnalyticsManager> {
        pl.soulsnaps.analytics.FirebaseAnalyticsManagerFactory.create()
    }

    single<SettingsNavigator> { IOSSettingsNavigator() }
    
    // FileStorageManager - iOS implementation
    single<FileStorageManager> {
        FileStorageManagerFactory.create()
    }
}

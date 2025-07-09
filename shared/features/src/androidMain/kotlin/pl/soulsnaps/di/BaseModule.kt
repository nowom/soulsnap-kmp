package pl.soulsnaps.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import pl.soulsnaps.database.DatabaseDriverFactory
import pl.soulsnaps.util.ConnectivityManagerNetworkMonitor
import pl.soulsnaps.util.NetworkMonitor

actual val platformModule: Module = module {
    single {
        DatabaseDriverFactory(context = androidContext()).createDriver()
    }
    single<NetworkMonitor> { ConnectivityManagerNetworkMonitor(androidContext()) }
}
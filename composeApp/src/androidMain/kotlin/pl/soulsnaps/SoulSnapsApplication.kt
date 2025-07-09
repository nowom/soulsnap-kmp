package pl.soulsnaps

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import pl.soulsnaps.di.initKoin

class SoulSnapsApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@SoulSnapsApplication)
            androidLogger()
            modules()
        }
    }
}
package pl.soulsnaps

import android.app.Application
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.soulsnaps.di.initKoin
import pl.soulsnaps.crashlytics.CrashlyticsManager

class SoulSnapsApplication: Application(), KoinComponent {

    private val crashlyticsManager: CrashlyticsManager by inject()

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        initKoin {
            androidContext(this@SoulSnapsApplication)
            androidLogger()
            modules()
        }
        
        // Initialize Crashlytics after Koin is ready
        crashlyticsManager.setCrashlyticsCollectionEnabled(true)
        crashlyticsManager.setUserId("anonymous_user")
        crashlyticsManager.log("SoulSnaps Application started")
    }
}
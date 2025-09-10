package pl.soulsnaps.di

import org.koin.core.module.Module
import org.koin.dsl.module
import pl.soulsnaps.audio.AudioPlayer
import pl.soulsnaps.audio.AudioPlayerFactory
import pl.soulsnaps.database.DatabaseDriverFactory

actual val platformModule: Module = module {
    single {
        DatabaseDriverFactory().createDriver()
    }
    single<AudioPlayer> {
        AudioPlayerFactory().create()
    }
}
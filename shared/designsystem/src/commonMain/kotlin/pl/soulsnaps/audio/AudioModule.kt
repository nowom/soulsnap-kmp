package pl.soulsnaps.audio

import org.koin.dsl.module

/**
 * Koin module for audio dependencies
 */
val audioModule = module {
    
    single<AudioManager> {
        AudioManager(get())
    }
}

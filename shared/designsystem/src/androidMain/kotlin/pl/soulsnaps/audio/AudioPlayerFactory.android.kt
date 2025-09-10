package pl.soulsnaps.audio

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class AudioPlayerFactory(private val context: Context) {
    actual fun create(): AudioPlayer {
        return AndroidAudioPlayer(context)
    }
}

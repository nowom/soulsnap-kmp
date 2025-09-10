package pl.soulsnaps.audio

actual class AudioPlayerFactory {
    actual fun create(): AudioPlayer {
        return IOSAudioPlayer()
    }
}

package pl.soulsnaps

import androidx.compose.ui.window.ComposeUIViewController
import pl.soulsnaps.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}
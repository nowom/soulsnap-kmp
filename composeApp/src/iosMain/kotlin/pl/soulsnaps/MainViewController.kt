package pl.soulsnaps

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.compose.getKoin
import pl.soulsnaps.di.initKoin
import pl.soulsnaps.features.startup.AppInitializer

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    val initializer = getKoin().get<AppInitializer>()
    initializer.initialize()
    App()
}
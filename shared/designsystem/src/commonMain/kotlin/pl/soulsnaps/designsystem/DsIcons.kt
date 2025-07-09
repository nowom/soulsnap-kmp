package pl.soulsnaps.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.painterResource
import soulsnaps.shared.designsystem.generated.resources.Res
import soulsnaps.shared.designsystem.generated.resources.compose_multiplatform
import soulsnaps.shared.designsystem.generated.resources.icon_photo_24

object DsIcons {
    @Composable
    fun Add(): Painter = painterResource(
        Res.drawable.icon_photo_24)

    // Dodaj więcej ikon wg potrzeby
}
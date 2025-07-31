package pl.soulsnaps.features.virtualmirror

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object VirtualMirrorRoute

fun NavController.navigateToVirtualMirror(navOptions: NavOptions? = null) =
    navigate(VirtualMirrorRoute, navOptions)

fun NavGraphBuilder.virtualMirrorScreen(
    onBack: () -> Unit
) {
    composable<VirtualMirrorRoute> {
        VirtualMirrorScreen(onBack = onBack)
    }
} 
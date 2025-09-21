package pl.soulsnaps.features.capturemoment

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object CaptureMomentRoute

fun NavController.navigateToCaptureMoment(navOptions: NavOptions? = null) =
    navigate(CaptureMomentRoute, navOptions)

fun NavGraphBuilder.captureMomentScreen() {
    composable<CaptureMomentRoute> { backStackEntry ->
        val viewModel: CaptureMomentViewModel = koinViewModel()
        
        // Location selection is now handled by LocationManager (no navigation needed)
        
        AddMemoryScreen(viewModel = viewModel)
    }
}
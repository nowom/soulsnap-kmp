package pl.soulsnaps.features.memoryhub.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object MemoryDetailsRoute

fun NavController.navigateToMemoryDetails(memoryId: Int, navOptions: NavOptions? = null) =
    navigate(MemoryDetailsRoute, navOptions)

fun NavGraphBuilder.memoryDetailsScreen(
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    composable<MemoryDetailsRoute> {
        val vm: MemoryDetailsViewModel = koinViewModel()
        
        // For now, we'll load a default memory (ID 1) for testing
        // In a real implementation, you'd pass the memory ID through navigation
        remember {
            vm.loadMemoryDetails(1)
        }
        
        val state by vm.state.collectAsStateWithLifecycle()
        
        MemoryDetailsScreen(
            state = state,
            onBack = onBack,
            onEdit = onEdit,
            onDelete = onDelete,
            onShare = onShare,
            onToggleFavorite = { vm.handleIntent(MemoryDetailsIntent.ToggleFavorite) }
        )
    }
}

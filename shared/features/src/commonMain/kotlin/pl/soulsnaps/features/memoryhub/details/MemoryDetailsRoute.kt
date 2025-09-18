package pl.soulsnaps.features.memoryhub.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data class MemoryDetailsRoute(val memoryId: Int)

fun NavController.navigateToMemoryDetails(memoryId: Int, navOptions: NavOptions? = null) =
    navigate(MemoryDetailsRoute(memoryId), navOptions)

fun NavGraphBuilder.memoryDetailsScreen(
    onBack: () -> Unit = {},
    onEdit: (Int) -> Unit = {},
    onDelete: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    composable<MemoryDetailsRoute> { backStackEntry ->
        val vm: MemoryDetailsViewModel = koinViewModel()
        val route = backStackEntry.toRoute<MemoryDetailsRoute>()
        
        // Load the correct memory using the ID from navigation
        remember(route.memoryId) {
            println("DEBUG: MemoryDetailsRoute - loading memory with ID: ${route.memoryId}")
            vm.loadMemoryDetails(route.memoryId)
        }
        
        val state by vm.state.collectAsStateWithLifecycle()
        
        // Handle successful deletion by navigating back
        LaunchedEffect(state.isDeleted) {
            if (state.isDeleted) {
                println("DEBUG: MemoryDetailsRoute - memory was deleted, navigating back")
                onBack()
            }
        }
        
        // Handle navigation to edit
        LaunchedEffect(state.navigateToEdit) {
            if (state.navigateToEdit) {
                println("DEBUG: MemoryDetailsRoute - navigating to edit for memory ID: ${route.memoryId}")
                vm.clearEditNavigation()
                onEdit(route.memoryId)
            }
        }
        
        MemoryDetailsScreen(
            state = state,
            onBack = onBack,
            onEdit = { vm.handleIntent(MemoryDetailsIntent.EditMemory) },
            onDelete = { vm.handleIntent(MemoryDetailsIntent.DeleteMemory) },
            onShare = onShare,
            onToggleFavorite = { vm.handleIntent(MemoryDetailsIntent.ToggleFavorite) },
            onConfirmDelete = { vm.handleIntent(MemoryDetailsIntent.ConfirmDelete) },
            onCancelDelete = { vm.handleIntent(MemoryDetailsIntent.CancelDelete) }
        )
    }
}

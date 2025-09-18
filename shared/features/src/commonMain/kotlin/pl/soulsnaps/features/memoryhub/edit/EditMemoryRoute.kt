package pl.soulsnaps.features.memoryhub.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data class EditMemoryRoute(val memoryId: Int)

fun NavController.navigateToEditMemory(memoryId: Int, navOptions: NavOptions? = null) =
    navigate(EditMemoryRoute(memoryId), navOptions)

fun NavGraphBuilder.editMemoryScreen(
    onBack: () -> Unit = {},
    onSaveComplete: () -> Unit = {}
) {
    composable<EditMemoryRoute> { backStackEntry ->
        val vm: EditMemoryViewModel = koinViewModel()
        val route = backStackEntry.toRoute<EditMemoryRoute>()
        
        // Load the memory to edit
        remember(route.memoryId) {
            println("DEBUG: EditMemoryRoute - loading memory for editing with ID: ${route.memoryId}")
            vm.loadMemoryForEdit(route.memoryId)
        }
        
        val state by vm.state.collectAsStateWithLifecycle()
        
        // Handle successful save by navigating back
        LaunchedEffect(state.isSaved) {
            if (state.isSaved) {
                println("DEBUG: EditMemoryRoute - memory was saved, navigating back")
                onSaveComplete()
            }
        }
        
        EditMemoryScreen(
            state = state,
            onBack = onBack,
            onIntent = vm::handleIntent
        )
    }
}

package pl.soulsnaps.features.capturemoment

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun EnhancedCaptureMomentRoute(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    EnhancedCaptureMomentScreen(
        onNavigateToPhotoEnhancement = { photoUri ->
            // TODO: Navigate to photo enhancement screen
            // navController.navigate("photo_enhancement/$photoUri")
        },
        onSaveMemory = { memoryData ->
            // TODO: Save memory and navigate back
            navController.popBackStack()
        },
        onBack = {
            navController.popBackStack()
        },
        modifier = modifier
    )
}

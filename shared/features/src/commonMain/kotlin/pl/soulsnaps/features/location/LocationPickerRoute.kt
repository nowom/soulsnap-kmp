package pl.soulsnaps.features.location

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
data class LocationPickerRoute(val currentLocation: String? = null)

fun NavController.navigateToLocationPicker(currentLocation: String? = null, navOptions: NavOptions? = null) =
    navigate(LocationPickerRoute(currentLocation), navOptions)

fun NavGraphBuilder.locationPickerScreen(
    onLocationSelected: (String?) -> Unit,
    onBack: () -> Unit
) {
    composable<LocationPickerRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<LocationPickerRoute>()
        
        LocationPickerScreen(
            currentLocation = route.currentLocation,
            onLocationSelected = onLocationSelected,
            onBack = onBack
        )
    }
}



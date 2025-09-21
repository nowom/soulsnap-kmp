package pl.soulsnaps.features.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * LocationManager - podobny pattern do CameraManager
 * Używa dialog z autocomplete zamiast navigation
 */
@Composable
fun rememberLocationManager(onResult: (String?) -> Unit): LocationManager {
    var showDialog by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<String?>(null) }
    
    // Show dialog when requested
    if (showDialog) {
        LocationPickerDialogWithAutocomplete(
            currentLocation = currentLocation,
            onLocationSelected = { location ->
                println("DEBUG: LocationManager - location selected: $location")
                onResult(location)
                showDialog = false
            },
            onDismiss = {
                println("DEBUG: LocationManager - dialog dismissed")
                showDialog = false
            }
        )
    }
    
    return remember {
        LocationManager(
            onLaunch = { location ->
                println("DEBUG: LocationManager - launching dialog with current location: $location")
                currentLocation = location
                showDialog = true
            }
        )
    }
}

class LocationManager(
    private val onLaunch: (String?) -> Unit
) {
    fun launch(currentLocation: String? = null) {
        onLaunch(currentLocation)
    }
}

package pl.soulsnaps.features.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import pl.soulsnaps.utils.getCurrentTimeMillis

/**
 * Android implementation of LocationPermissionManager
 * Uses ActivityResultContracts for permission requests
 */
actual class LocationPermissionManager(
    private val activity: ComponentActivity
) {
    
    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    
    actual suspend fun hasLocationPermission(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    actual suspend fun requestLocationPermission(): Boolean {
        println("DEBUG: LocationPermissionManager.Android - requesting location permissions")
        
        if (hasLocationPermission()) {
            println("DEBUG: LocationPermissionManager.Android - permissions already granted")
            return true
        }
        
        return suspendCancellableCoroutine { continuation ->
        
        val permissionLauncher = activity.activityResultRegistry.register(
            "location_permission_${getCurrentTimeMillis()}",
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            println("DEBUG: LocationPermissionManager.Android - permission result: $allGranted")
            println("DEBUG: LocationPermissionManager.Android - detailed results: $permissions")
            continuation.resume(allGranted)
        }
        
        continuation.invokeOnCancellation {
            permissionLauncher.unregister()
        }
        
        permissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }
    
    actual suspend fun shouldShowPermissionRationale(): Boolean {
        return REQUIRED_PERMISSIONS.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
    }
    
    actual suspend fun openAppSettings(): Boolean {
        return try {
            println("DEBUG: LocationPermissionManager.Android - opening app settings")
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activity.startActivity(intent)
            true
        } catch (e: Exception) {
            println("ERROR: LocationPermissionManager.Android - failed to open settings: ${e.message}")
            false
        }
    }
}

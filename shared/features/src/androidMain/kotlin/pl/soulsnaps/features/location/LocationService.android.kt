package pl.soulsnaps.features.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import pl.soulsnaps.data.network.SoulSnapApi
import kotlin.coroutines.resume

/**
 * Android implementation of LocationService using Google Play Services
 * Uses FusedLocationProviderClient for accurate location detection
 */
actual class LocationService(
    private val context: Context,
    private val soulSnapApi: SoulSnapApi,
    private val permissionManager: LocationPermissionManager
) {
    
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(5000)
        .setMaxUpdateDelayMillis(15000)
        .build()
    
    companion object {
        private const val LOCATION_TIMEOUT_MS = 15000L
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    
    actual suspend fun getCurrentLocation(): LocationResult {
        println("DEBUG: LocationService.Android - getting current location")
        
        // Check permissions first
        if (!hasLocationPermission()) {
            println("ERROR: LocationService.Android - location permission denied")
            return LocationResult.Error.PermissionDenied
        }
        
        // Check if location services are enabled
        if (!isLocationEnabled()) {
            println("ERROR: LocationService.Android - location services disabled")
            return LocationResult.Error.LocationDisabled
        }
        
        return try {
            val location = withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
                getCurrentLocationInternal()
            }
            
            if (location != null) {
                println("DEBUG: LocationService.Android - location obtained: lat=${location.latitude}, lng=${location.longitude}")
                
                // Try to get address using reverse geocoding
                val address = try {
                    val suggestion = soulSnapApi.reverseGeocode(location.latitude, location.longitude)
                    suggestion?.name ?: "Current Location"
                } catch (e: Exception) {
                    println("WARN: LocationService.Android - reverse geocoding failed: ${e.message}")
                    "Current Location"
                }
                
                LocationResult.Success(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    address = address
                )
            } else {
                println("ERROR: LocationService.Android - location timeout")
                LocationResult.Error.Timeout
            }
        } catch (e: SecurityException) {
            println("ERROR: LocationService.Android - security exception: ${e.message}")
            LocationResult.Error.PermissionDenied
        } catch (e: Exception) {
            println("ERROR: LocationService.Android - unknown error: ${e.message}")
            LocationResult.Error.Unknown(e.message ?: "Unknown location error")
        }
    }
    
    actual suspend fun hasLocationPermission(): Boolean {
        return permissionManager.hasLocationPermission()
    }
    
    actual suspend fun requestLocationPermission(): Boolean {
        return permissionManager.requestLocationPermission()
    }
    
    private suspend fun getCurrentLocationInternal(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            val cancellationTokenSource = CancellationTokenSource()
            
            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
            
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                continuation.resume(location)
            }.addOnFailureListener { exception ->
                println("ERROR: LocationService.Android - getCurrentLocation failed: ${exception.message}")
                continuation.resume(null)
            }
            
        } catch (e: Exception) {
            println("ERROR: LocationService.Android - getCurrentLocationInternal exception: ${e.message}")
            continuation.resume(null)
        }
    }
    
    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}


package pl.soulsnaps.features.location

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import pl.soulsnaps.data.network.SoulSnapApi
import platform.CoreLocation.*
import platform.Foundation.NSError
import kotlin.coroutines.resume

/**
 * iOS implementation of LocationService using Core Location
 * Uses CLLocationManager for native iOS location services
 */
actual class LocationService(
    private val soulSnapApi: SoulSnapApi,
    private val permissionManager: LocationPermissionManager
) {
    
    private val locationManager = CLLocationManager()
    
    companion object {
        private const val LOCATION_TIMEOUT_MS = 15000L
    }
    
    actual suspend fun getCurrentLocation(): LocationResult {
        println("DEBUG: LocationService.iOS - getting current location")
        
        // Check permissions first
        if (!hasLocationPermission()) {
            println("ERROR: LocationService.iOS - location permission denied")
            return LocationResult.Error.PermissionDenied
        }
        
        // Check if location services are enabled
        if (!CLLocationManager.locationServicesEnabled()) {
            println("ERROR: LocationService.iOS - location services disabled")
            return LocationResult.Error.LocationDisabled
        }
        
        return try {
            val location = withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
                getCurrentLocationInternal()
            }
            
            if (location != null) {
                println("DEBUG: LocationService.iOS - location obtained: lat=${location.first}, lng=${location.second}")
                
                // Try to get address using reverse geocoding
                val address = try {
                    val suggestion = soulSnapApi.reverseGeocode(location.first, location.second)
                    suggestion?.name ?: "Current Location"
                } catch (e: Exception) {
                    println("WARN: LocationService.iOS - reverse geocoding failed: ${e.message}")
                    "Current Location"
                }
                
                LocationResult.Success(
                    latitude = location.first,
                    longitude = location.second,
                    accuracy = location.third,
                    address = address
                )
            } else {
                println("ERROR: LocationService.iOS - location timeout")
                LocationResult.Error.Timeout
            }
        } catch (e: Exception) {
            println("ERROR: LocationService.iOS - unknown error: ${e.message}")
            LocationResult.Error.Unknown(e.message ?: "Unknown location error")
        }
    }
    
    actual suspend fun hasLocationPermission(): Boolean {
        return permissionManager.hasLocationPermission()
    }
    
    actual suspend fun requestLocationPermission(): Boolean {
        return permissionManager.requestLocationPermission()
    }
    
    private suspend fun getCurrentLocationInternal(): Triple<Double, Double, Float?>? = suspendCancellableCoroutine { continuation ->
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val locations = didUpdateLocations.filterIsInstance<CLLocation>()
                val location = locations.lastOrNull()
                
                if (location != null) {
                    val coordinate = location.coordinate
                    val accuracy = location.horizontalAccuracy.toFloat()
                    
                    continuation.resume(
                        Triple(
                            coordinate.latitude,
                            coordinate.longitude,
                            if (accuracy >= 0) accuracy else null
                        )
                    )
                    
                    // Stop location updates
                    manager.stopUpdatingLocation()
                } else {
                    continuation.resume(null)
                }
            }
            
            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                println("ERROR: LocationService.iOS - location failed: ${didFailWithError.localizedDescription}")
                continuation.resume(null)
                manager.stopUpdatingLocation()
            }
        }
        
        locationManager.delegate = delegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.requestLocation()
        
        continuation.invokeOnCancellation {
            locationManager.stopUpdatingLocation()
        }
    }
}


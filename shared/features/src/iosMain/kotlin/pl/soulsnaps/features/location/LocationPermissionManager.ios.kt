package pl.soulsnaps.features.location

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.*
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import kotlin.coroutines.resume

/**
 * iOS implementation of LocationPermissionManager
 * Uses Core Location framework for permission handling
 */
actual class LocationPermissionManager {
    
    private val locationManager = CLLocationManager()
    
    actual suspend fun hasLocationPermission(): Boolean {
        val authStatus = CLLocationManager.authorizationStatus()
        return authStatus == kCLAuthorizationStatusAuthorizedWhenInUse || 
               authStatus == kCLAuthorizationStatusAuthorizedAlways
    }
    
    actual suspend fun requestLocationPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        println("DEBUG: LocationPermissionManager.iOS - requesting location permissions")
        
        val currentStatus = CLLocationManager.authorizationStatus()
        
        when (currentStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> {
                println("DEBUG: LocationPermissionManager.iOS - permissions already granted")
                continuation.resume(true)
                return@suspendCancellableCoroutine
            }
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> {
                println("DEBUG: LocationPermissionManager.iOS - permissions denied/restricted")
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }
        }
        
        // Handle permission request for kCLAuthorizationStatusNotDetermined
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: CLAuthorizationStatus) {
                println("DEBUG: LocationPermissionManager.iOS - authorization status changed: $didChangeAuthorizationStatus")
                
                when (didChangeAuthorizationStatus) {
                    kCLAuthorizationStatusAuthorizedWhenInUse,
                    kCLAuthorizationStatusAuthorizedAlways -> {
                        continuation.resume(true)
                    }
                    kCLAuthorizationStatusDenied,
                    kCLAuthorizationStatusRestricted -> {
                        continuation.resume(false)
                    }
                    // Don't respond to kCLAuthorizationStatusNotDetermined - wait for final status
                }
            }
        }
        
        locationManager.delegate = delegate
        
        continuation.invokeOnCancellation {
            locationManager.delegate = null
        }
        
        // Request permission
        if (CLLocationManager.locationServicesEnabled()) {
            locationManager.requestWhenInUseAuthorization()
        } else {
            println("ERROR: LocationPermissionManager.iOS - location services not enabled")
            continuation.resume(false)
        }
    }
    
    actual suspend fun shouldShowPermissionRationale(): Boolean {
        // iOS doesn't have a concept of "show rationale" like Android
        // Always return false
        return false
    }
    
    actual suspend fun openAppSettings(): Boolean {
        return try {
            println("DEBUG: LocationPermissionManager.iOS - opening app settings")
            val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
            if (settingsUrl != null && UIApplication.sharedApplication.canOpenURL(settingsUrl)) {
                UIApplication.sharedApplication.openURL(settingsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            println("ERROR: LocationPermissionManager.iOS - failed to open settings: ${e.message}")
            false
        }
    }
}


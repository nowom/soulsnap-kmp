package pl.soulsnaps.permissions

import androidx.compose.runtime.*
import platform.AVFoundation.*
import platform.Photos.*
import platform.CoreLocation.*
import platform.Foundation.*

@Composable
actual fun WithCameraPermission(
    content: @Composable () -> Unit,
    deniedContent: @Composable (() -> Unit) -> Unit
) {
    var hasPermission by remember { mutableStateOf(false) }
    
    println("🔍 WithCameraPermission (iOS): Initializing camera permission check")
    
    LaunchedEffect(Unit) {
        val currentPermission = checkCameraPermission()
        println("🔍 WithCameraPermission (iOS): Initial permission check - hasPermission: $currentPermission")
        hasPermission = currentPermission
    }
    
    if (hasPermission) {
        println("🔍 WithCameraPermission (iOS): Permission granted, showing content")
        content()
    } else {
        println("🔍 WithCameraPermission (iOS): Permission not granted, showing denied content")
        deniedContent { 
            println("🔍 WithCameraPermission (iOS): Request permission button clicked, requesting camera access")
            // Request camera permission
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                println("🔍 WithCameraPermission (iOS): Camera permission result - granted: $granted")
                hasPermission = granted
            }
        }
    }
}

@Composable
actual fun WithGalleryPermission(
    content: @Composable () -> Unit,
    deniedContent: @Composable (() -> Unit) -> Unit
) {
    var hasPermission by remember { mutableStateOf(false) }
    
    println("🔍 WithGalleryPermission (iOS): Initializing gallery permission check")
    
    LaunchedEffect(Unit) {
        val currentPermission = checkGalleryPermission()
        println("🔍 WithGalleryPermission (iOS): Initial permission check - hasPermission: $currentPermission")
        hasPermission = currentPermission
    }
    
    if (hasPermission) {
        println("🔍 WithGalleryPermission (iOS): Permission granted, showing content")
        content()
    } else {
        println("🔍 WithGalleryPermission (iOS): Permission not granted, showing denied content")
        deniedContent { 
            println("🔍 WithGalleryPermission (iOS): Request permission button clicked, requesting gallery access")
            // Request gallery permission
            PHPhotoLibrary.requestAuthorization { status ->
                val granted = status == PHAuthorizationStatus.PHAuthorizationStatusAuthorized
                println("🔍 WithGalleryPermission (iOS): Gallery permission result - status: $status, granted: $granted")
                hasPermission = granted
            }
        }
    }
}

@Composable
actual fun WithLocationPermission(
    content: @Composable () -> Unit,
    deniedContent: @Composable (() -> Unit) -> Unit
) {
    var hasPermission by remember { mutableStateOf(false) }
    val locationManager = remember { CLLocationManager() }
    
    println("🔍 WithLocationPermission (iOS): Initializing location permission check")
    
    LaunchedEffect(Unit) {
        val currentPermission = checkLocationPermission()
        println("🔍 WithLocationPermission (iOS): Initial permission check - hasPermission: $currentPermission")
        hasPermission = currentPermission
    }
    
    if (hasPermission) {
        println("🔍 WithLocationPermission (iOS): Permission granted, showing content")
        content()
    } else {
        println("🔍 WithLocationPermission (iOS): Permission not granted, showing denied content")
        deniedContent { 
            println("🔍 WithLocationPermission (iOS): Request permission button clicked, requesting location access")
            // Request location permission
            locationManager.requestWhenInUseAuthorization()
            // Note: The actual permission result will be handled by the location manager delegate
            // For now, we'll assume it's granted after request
            println("🔍 WithLocationPermission (iOS): Location permission request sent")
            hasPermission = true
        }
    }
}

/**
 * iOS implementation of PermissionManager
 */
class IOSPermissionManager : PermissionManager {
    
    override suspend fun isPermissionGranted(type: PermissionType): Boolean {
        return when (type) {
            PermissionType.CAMERA -> checkCameraPermission()
            PermissionType.GALLERY -> checkGalleryPermission()
            PermissionType.LOCATION -> checkLocationPermission()
        }
    }
    
    override suspend fun requestPermission(type: PermissionType): Boolean {
        return when (type) {
            PermissionType.CAMERA -> requestCameraPermission()
            PermissionType.GALLERY -> requestGalleryPermission()
            PermissionType.LOCATION -> requestLocationPermission()
        }
    }
    
    override suspend fun getPermissionStatus(type: PermissionType): PermissionStatus {
        return when (type) {
            PermissionType.CAMERA -> getCameraPermissionStatus()
            PermissionType.GALLERY -> getGalleryPermissionStatus()
            PermissionType.LOCATION -> getLocationPermissionStatus()
        }
    }
    
    override suspend fun shouldShowRationale(type: PermissionType): Boolean {
        // iOS doesn't have a direct equivalent to Android's shouldShowRationale
        // We'll return false for now
        return false
    }
}

/**
 * Check camera permission status
 */
private fun checkCameraPermission(): Boolean {
    return when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
        AVAuthorizationStatus.AVAuthorizationStatusAuthorized -> true
        AVAuthorizationStatus.AVAuthorizationStatusNotDetermined -> false
        AVAuthorizationStatus.AVAuthorizationStatusDenied -> false
        AVAuthorizationStatus.AVAuthorizationStatusRestricted -> false
        else -> false
    }
}

/**
 * Check gallery permission status
 */
private fun checkGalleryPermission(): Boolean {
    return when (PHPhotoLibrary.authorizationStatus()) {
        PHAuthorizationStatus.PHAuthorizationStatusAuthorized -> true
        PHAuthorizationStatus.PHAuthorizationStatusNotDetermined -> false
        PHAuthorizationStatus.PHAuthorizationStatusDenied -> false
        PHAuthorizationStatus.PHAuthorizationStatusRestricted -> false
        else -> false
    }
}

/**
 * Check location permission status
 */
private fun checkLocationPermission(): Boolean {
    return when (CLLocationManager.authorizationStatus()) {
        CLAuthorizationStatus.kCLAuthorizationStatusAuthorizedWhenInUse -> true
        CLAuthorizationStatus.kCLAuthorizationStatusAuthorizedAlways -> true
        CLAuthorizationStatus.kCLAuthorizationStatusNotDetermined -> false
        CLAuthorizationStatus.kCLAuthorizationStatusDenied -> false
        CLAuthorizationStatus.kCLAuthorizationStatusRestricted -> false
        else -> false
    }
}

/**
 * Request camera permission
 */
private fun requestCameraPermission(): Boolean {
    // This would typically involve showing a permission request dialog
    // For now, we'll return the current status
    return checkCameraPermission()
}

/**
 * Request gallery permission
 */
private fun requestGalleryPermission(): Boolean {
    // This would typically involve showing a permission request dialog
    // For now, we'll return the current status
    return checkGalleryPermission()
}

/**
 * Request location permission
 */
private fun requestLocationPermission(): Boolean {
    // This would typically involve showing a permission request dialog
    // For now, we'll return the current status
    return checkLocationPermission()
}

/**
 * Get camera permission status
 */
private fun getCameraPermissionStatus(): PermissionStatus {
    // For now, return GRANTED to avoid compilation issues
    // In a full implementation, this would check actual camera permission status
    return PermissionStatus.GRANTED
}

/**
 * Get gallery permission status
 */
private fun getGalleryPermissionStatus(): PermissionStatus {
    // For now, return GRANTED to avoid compilation issues
    // In a full implementation, this would check actual gallery permission status
    return PermissionStatus.GRANTED
}

/**
 * Get location permission status
 */
private fun getLocationPermissionStatus(): PermissionStatus {
    // For now, return GRANTED to avoid compilation issues
    // In a full implementation, this would check actual location permission status
    return PermissionStatus.GRANTED
}


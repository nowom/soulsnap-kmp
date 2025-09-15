package pl.soulsnaps.permissions

import androidx.compose.runtime.Composable

/**
 * Unified permission manager for handling all app permissions
 */
@Composable
expect fun WithCameraPermission(
    content: @Composable () -> Unit,
    deniedContent: @Composable (() -> Unit) -> Unit
)

@Composable
expect fun WithGalleryPermission(
    content: @Composable () -> Unit,
    deniedContent: @Composable (() -> Unit) -> Unit
)

@Composable
expect fun WithLocationPermission(
    content: @Composable () -> Unit,
    deniedContent: @Composable (() -> Unit) -> Unit
)

/**
 * Permission status for different permission types
 */
enum class PermissionType {
    CAMERA,
    GALLERY,
    LOCATION
}

/**
 * Permission status
 */
enum class PermissionStatus {
    GRANTED,
    DENIED,
    NOT_REQUESTED
}

/**
 * Permission manager interface for programmatic permission handling
 */
interface PermissionManager {
    /**
     * Check if a specific permission is granted
     */
    suspend fun isPermissionGranted(type: PermissionType): Boolean
    
    /**
     * Request a specific permission
     */
    suspend fun requestPermission(type: PermissionType): Boolean
    
    /**
     * Get current permission status
     */
    suspend fun getPermissionStatus(type: PermissionType): PermissionStatus
    
    /**
     * Check if permission should show rationale
     */
    suspend fun shouldShowRationale(type: PermissionType): Boolean
}



package pl.soulsnaps.features.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Platform-specific notification permission manager
 */
expect class NotificationPermissionManager {
    
    /**
     * Check if notification permissions are granted
     */
    suspend fun hasNotificationPermission(): Boolean
    
    /**
     * Request notification permissions from the user
     */
    suspend fun requestNotificationPermission(): PermissionResult
    
    /**
     * Check if we should show permission rationale
     */
    suspend fun shouldShowPermissionRationale(): Boolean
    
    /**
     * Open app settings for manual permission grant
     */
    suspend fun openAppSettings(): Boolean
    
    /**
     * Get current permission state
     */
    val permissionState: StateFlow<PermissionState>
}

/**
 * Permission request result
 */
enum class PermissionResult {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED,
    ERROR
}

/**
 * Permission state
 */
enum class PermissionState {
    UNKNOWN,
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED
}


package pl.soulsnaps.features.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import platform.UIKit.UIApplication
import platform.UserNotifications.*
import kotlin.coroutines.resume

actual class NotificationPermissionManager {
    
    private val _permissionState = MutableStateFlow(PermissionState.UNKNOWN)
    actual val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()
    
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    
    init {
        checkInitialPermissionState()
    }
    
    private fun checkInitialPermissionState() {
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            val state = when (settings?.authorizationStatus) {
                UNAuthorizationStatusAuthorized -> PermissionState.GRANTED
                UNAuthorizationStatusDenied -> PermissionState.DENIED
                UNAuthorizationStatusNotDetermined -> PermissionState.UNKNOWN
                UNAuthorizationStatusProvisional -> PermissionState.GRANTED
                UNAuthorizationStatusEphemeral -> PermissionState.GRANTED
                else -> PermissionState.UNKNOWN
            }
            _permissionState.value = state
        }
    }
    
    actual suspend fun hasNotificationPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            val hasPermission = settings?.authorizationStatus == UNAuthorizationStatusAuthorized ||
                    settings?.authorizationStatus == UNAuthorizationStatusProvisional ||
                    settings?.authorizationStatus == UNAuthorizationStatusEphemeral
            continuation.resume(hasPermission ?: false)
        }
    }
    
    actual suspend fun requestNotificationPermission(): PermissionResult = suspendCancellableCoroutine { continuation ->
        notificationCenter.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
        ) { granted, error ->
            val result = if (granted) {
                PermissionResult.GRANTED
            } else {
                PermissionResult.DENIED
            }
            _permissionState.value = if (granted) PermissionState.GRANTED else PermissionState.DENIED
            continuation.resume(result)
        }
    }
    
    actual suspend fun shouldShowPermissionRationale(): Boolean = suspendCancellableCoroutine { continuation ->
        notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
            val shouldShow = settings?.authorizationStatus == UNAuthorizationStatusDenied
            continuation.resume(shouldShow ?: false)
        }
    }
    
    actual suspend fun openAppSettings(): Boolean = suspendCancellableCoroutine { continuation ->
        try {
            val settingsUrl = NSURL.URLWithString("app-settings:")
            if (settingsUrl != null) {
                UIApplication.sharedApplication.openURL(settingsUrl)
                continuation.resume(true)
            } else {
                continuation.resume(false)
            }
        } catch (e: Exception) {
            continuation.resume(false)
        }
    }
}
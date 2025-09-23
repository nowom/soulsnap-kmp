package pl.soulsnaps.features.notifications

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class NotificationPermissionManager(
    private val context: Context,
    private val activity: ComponentActivity
) {
    
    private val _permissionState = MutableStateFlow(PermissionState.UNKNOWN)
    actual val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()
    
    init {
        checkInitialPermissionState()
    }
    
    private fun checkInitialPermissionState() {
        val hasPermission = hasNotificationPermissionSync()
        _permissionState.value = if (hasPermission) PermissionState.GRANTED else PermissionState.DENIED
    }
    
    actual suspend fun hasNotificationPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        val hasPermission = hasNotificationPermissionSync()
        continuation.resume(hasPermission)
    }
    
    private fun hasNotificationPermissionSync(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For older versions, check if notifications are enabled
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.areNotificationsEnabled()
        }
    }
    
    actual suspend fun requestNotificationPermission(): PermissionResult = suspendCancellableCoroutine { continuation ->
        // For now, we'll just check the current permission state
        // In a real implementation, you'd need to handle permission requests differently
        val hasPermission = hasNotificationPermissionSync()
        val result = if (hasPermission) {
            PermissionResult.GRANTED
        } else {
            PermissionResult.DENIED
        }
        _permissionState.value = if (hasPermission) PermissionState.GRANTED else PermissionState.DENIED
        continuation.resume(result)
    }
    
    actual suspend fun shouldShowPermissionRationale(): Boolean = suspendCancellableCoroutine { continuation ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val shouldShow = activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
            continuation.resume(shouldShow)
        } else {
            // For older versions, we can't show rationale for notification permission
            continuation.resume(false)
        }
    }
    
    actual suspend fun openAppSettings(): Boolean = suspendCancellableCoroutine { continuation ->
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            continuation.resume(true)
        } catch (e: Exception) {
            continuation.resume(false)
        }
    }
}

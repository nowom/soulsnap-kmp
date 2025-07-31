package pl.soulsnaps.permissions

import androidx.compose.runtime.*
import platform.AVFoundation.*

@Composable
actual fun WithCameraPermission(
    content: @Composable () -> Unit,
    deniedContent: @Composable () -> Unit
) {
    var permissionGranted by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        when (status) {
            AVAuthorizationStatusAuthorized -> permissionGranted = true
            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                    permissionGranted = granted
                    checked = true
                }
            }
            else -> {
                permissionGranted = false
                checked = true
            }
        }
        checked = true
    }

    if (checked) {
        if (permissionGranted) content() else deniedContent()
    }
} 
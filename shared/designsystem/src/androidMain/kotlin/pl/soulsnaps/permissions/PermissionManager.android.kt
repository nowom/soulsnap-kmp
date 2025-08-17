package pl.soulsnaps.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun WithCameraPermission(
    content: @Composable () -> Unit,
    deniedContent: @Composable () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var shouldShowRationale by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            shouldShowRationale = context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
        }
    }
    
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    if (hasPermission) {
        content()
    } else {
        deniedContent()
    }
}

@Composable
actual fun WithGalleryPermission(
    content: @Composable () -> Unit,
    deniedContent: @Composable () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }
    
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    if (hasPermission) {
        content()
    } else {
        deniedContent()
    }
}

@Composable
actual fun WithLocationPermission(
    content: @Composable () -> Unit,
    deniedContent: @Composable () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }
    
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    if (hasPermission) {
        content()
    } else {
        deniedContent()
    }
}

/**
 * Android implementation of PermissionManager
 */
class AndroidPermissionManager(private val context: Context) : PermissionManager {
    
    override suspend fun isPermissionGranted(type: PermissionType): Boolean {
        val permission = when (type) {
            PermissionType.CAMERA -> Manifest.permission.CAMERA
            PermissionType.GALLERY -> Manifest.permission.READ_EXTERNAL_STORAGE
            PermissionType.LOCATION -> Manifest.permission.ACCESS_COARSE_LOCATION
        }
        
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    override suspend fun requestPermission(type: PermissionType): Boolean {
        // This would need to be implemented with a callback mechanism
        // For now, we'll return the current status
        return isPermissionGranted(type)
    }
    
    override suspend fun getPermissionStatus(type: PermissionType): PermissionStatus {
        return if (isPermissionGranted(type)) {
            PermissionStatus.GRANTED
        } else {
            PermissionStatus.DENIED
        }
    }
    
    override suspend fun shouldShowRationale(type: PermissionType): Boolean {
        // This would need to be implemented with Activity reference
        // For now, return false
        return false
    }
}

package pl.soulsnaps.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun WithCameraPermission(
    content: @Composable () -> Unit,
    deniedContent: @Composable (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var shouldShowRationale by remember { mutableStateOf(false) }
    
    Log.d("SoulSnaps", "🔍 WithCameraPermission: Initializing camera permission check")
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("SoulSnaps", "🔍 WithCameraPermission: Permission result received - isGranted: $isGranted")
        hasPermission = isGranted
        if (!isGranted) {
            // Check if we should show rationale (permission was denied before)
            shouldShowRationale = context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
            Log.d("SoulSnaps", "🔍 WithCameraPermission: Permission denied, shouldShowRationale: $shouldShowRationale")
        } else {
            Log.d("SoulSnaps", "🔍 WithCameraPermission: Permission granted successfully!")
        }
    }
    
    LaunchedEffect(Unit) {
        val currentPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        Log.d("SoulSnaps", "🔍 WithCameraPermission: Initial permission check - hasPermission: $currentPermission")
        hasPermission = currentPermission
    }
    
    if (hasPermission) {
        Log.d("SoulSnaps", "🔍 WithCameraPermission: Permission granted, showing content")
        content()
    } else {
        Log.d("SoulSnaps", "🔍 WithCameraPermission: Permission not granted, showing denied content")
        deniedContent { 
            Log.d("SoulSnaps", "🔍 WithCameraPermission: Request permission button clicked, launching permission request")
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}

@Composable
actual fun WithGalleryPermission(
    content: @Composable () -> Unit,
    deniedContent: @Composable (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    
    Log.d("SoulSnaps", "🔍 WithGalleryPermission: Initializing gallery permission check")
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasOldPermission = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        val hasNewPermission = permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: false
        val isGranted = hasOldPermission || hasNewPermission
        Log.d("SoulSnaps", "🔍 WithGalleryPermission: Permission result received - permissions: $permissions")
        Log.d("SoulSnaps", "🔍 WithGalleryPermission: hasOldPermission: $hasOldPermission, hasNewPermission: $hasNewPermission, isGranted: $isGranted")
        hasPermission = isGranted
        if (isGranted) {
            Log.d("SoulSnaps", "🔍 WithGalleryPermission: Permission granted successfully!")
        } else {
            Log.d("SoulSnaps", "🔍 WithGalleryPermission: Permission denied")
        }
    }
    
    LaunchedEffect(Unit) {
        // Check for both old and new gallery permissions
        val hasOldPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasNewPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
        val currentPermission = hasOldPermission || hasNewPermission
        
        val oldStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        val newStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
        
        Log.d("SoulSnaps", "🔍 WithGalleryPermission: Initial permission check - hasOldPermission: $hasOldPermission, hasNewPermission: $hasNewPermission, currentPermission: $currentPermission")
        Log.d("SoulSnaps", "🔍 WithGalleryPermission: Old status: $oldStatus, New status: $newStatus")
        hasPermission = currentPermission
    }
    
    if (hasPermission) {
        Log.d("SoulSnaps", "🔍 WithGalleryPermission: Permission granted, showing content")
        content()
    } else {
        Log.d("SoulSnaps", "🔍 WithGalleryPermission: Permission not granted, showing denied content")
        deniedContent { 
            val oldStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            val newStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
            Log.d("SoulSnaps", "🔍 WithGalleryPermission: Request permission button clicked, old status: $oldStatus, new status: $newStatus")
            Log.d("SoulSnaps", "🔍 WithGalleryPermission: Launching permission request for both gallery permissions")
            // Request both old and new gallery permissions
            permissionLauncher.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_MEDIA_IMAGES
            ))
        }
    }
}

@Composable
actual fun WithLocationPermission(
    content: @Composable () -> Unit,
    deniedContent: @Composable (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    
    Log.d("SoulSnaps", "🔍 WithLocationPermission: Initializing location permission check")
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("SoulSnaps", "🔍 WithLocationPermission: Permission result received - isGranted: $isGranted")
        hasPermission = isGranted
        if (isGranted) {
            Log.d("SoulSnaps", "🔍 WithLocationPermission: Permission granted successfully!")
        } else {
            Log.d("SoulSnaps", "🔍 WithLocationPermission: Permission denied")
        }
    }
    
    LaunchedEffect(Unit) {
        val currentPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        Log.d("SoulSnaps", "🔍 WithLocationPermission: Initial permission check - hasPermission: $currentPermission")
        hasPermission = currentPermission
    }
    
    if (hasPermission) {
        Log.d("SoulSnaps", "🔍 WithLocationPermission: Permission granted, showing content")
        content()
    } else {
        Log.d("SoulSnaps", "🔍 WithLocationPermission: Permission not granted, showing denied content")
        deniedContent { 
            Log.d("SoulSnaps", "🔍 WithLocationPermission: Request permission button clicked, launching permission request")
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
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

package pl.soulsnaps.features.capturemoment

import androidx.compose.foundation.Image
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import pl.soulsnaps.components.FullScreenCircularProgress
import pl.soulsnaps.components.PrimaryButton
import pl.soulsnaps.components.showPlatformDatePicker
import pl.soulsnaps.components.AnimatedErrorMessage
import pl.soulsnaps.components.AnimatedSuccessMessage
import pl.soulsnaps.features.location.LocationOptionItem
import pl.soulsnaps.features.location.rememberLocationManager
import pl.soulsnaps.components.DatePicker
import pl.soulsnaps.components.AffirmationToggle
import pl.soulsnaps.components.AffirmationSnackbar
import pl.soulsnaps.components.AffirmationErrorSnackbar
import pl.soulsnaps.components.PermissionRequiredDialog
import pl.soulsnaps.components.PermissionType
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.navigation.LocalNavController
import pl.soulsnaps.photo.rememberCameraManager
import pl.soulsnaps.photo.rememberGalleryManager
import pl.soulsnaps.permissions.WithCameraPermission
import pl.soulsnaps.permissions.WithGalleryPermission
import pl.soulsnaps.features.auth.ui.PaywallScreen
import pl.soulsnaps.features.analytics.CapacityAnalyticsScreen
import pl.soulsnaps.features.memoryhub.MemoryHubRoute
import pl.soulsnaps.utils.getCurrentTimeMillis


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemoryScreen(
    viewModel: CaptureMomentViewModel = koinViewModel(),
    userSessionManager: pl.soulsnaps.features.auth.UserSessionManager = koinInject()
) {
    val moods = MoodType.entries.toTypedArray()
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    var showGalleryPermissionDialog by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val navController = LocalNavController.current
    
    // Camera and Gallery managers
    val cameraManager = rememberCameraManager { sharedImage ->
        sharedImage?.let { image ->
            imageBitmap = image.toImageBitmap()
            // Save image data as Base64 string for now (temporary solution)
            val imageBytes = image.toByteArray()
            if (imageBytes != null) {
                @OptIn(ExperimentalEncodingApi::class)
                val base64Image = "data:image/jpeg;base64," + Base64.encode(imageBytes)
                viewModel.handleIntent(CaptureMomentIntent.ChangePhoto(base64Image))
            }
            showPhotoDialog = false
        }
    }
    
    val galleryManager = rememberGalleryManager { sharedImage ->
        sharedImage?.let { image ->
            imageBitmap = image.toImageBitmap()
            // Save image data as Base64 string for now (temporary solution)
            val imageBytes = image.toByteArray()
            if (imageBytes != null) {
                @OptIn(ExperimentalEncodingApi::class)
                val base64Image = "data:image/jpeg;base64," + Base64.encode(imageBytes)
                viewModel.handleIntent(CaptureMomentIntent.ChangePhoto(base64Image))
            }
            showPhotoDialog = false
        }
    }
    
    // Location Manager - launcher pattern like camera/gallery
    val locationManager = rememberLocationManager { selectedLocation ->
        println("DEBUG: AddMemoryScreen - location selected via manager: $selectedLocation")
        viewModel.handleIntent(CaptureMomentIntent.ChangeLocation(selectedLocation ?: ""))
    }

//    val mediaRecorder = mediaRecorderremember { MediaRecorder() }
//    var audioFilePath by remember { mutableStateOf<String?>(null) }

//    val recordAudioLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult(),
//        onResult = { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val data = result.data
//                val uri = data?.data
//                if (uri != null) {
//                    viewModel.handleIntent(CaptureMomentIntent.ChangeAudio(uri.toString()))
//                }
//            }
//            showPhotoDialog = false
//        })

    // Handle success navigation
    state.savedMemoryId?.let { memoryId ->
        println("DEBUG: CaptureMomentScreen - savedMemoryId detected: $memoryId, starting navigation")
        LaunchedEffect(memoryId) {
            println("DEBUG: CaptureMomentScreen - LaunchedEffect triggered for memoryId: $memoryId")
            delay(1500) // Show success message for 1.5 seconds
            println("DEBUG: CaptureMomentScreen - navigating to MemoryHubRoute")
            // Navigate to Memory Hub (Timeline)
            navController.navigate(MemoryHubRoute) {
                popUpTo("captureMoment") { inclusive = true }
            }
        }
    }

    if (state.isSaving) {
        FullScreenCircularProgress()
    }
    
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Add Memory", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Wstecz"
                    )
                }
            },
        )
    }, bottomBar = {
        PrimaryButton("Save", modifier = Modifier.padding(16.dp)) {
            viewModel.handleIntent(CaptureMomentIntent.SaveMemory)
        }
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {

            // Success/Error Messages
            AnimatedSuccessMessage(
                message = state.successMessage,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            AnimatedErrorMessage(
                message = state.errorMessage,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Zdjęcie
            PhotoPickerView(imageBitmap) {
                showPhotoDialog = true
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tytuł
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.handleIntent(CaptureMomentIntent.ChangeTitle(it)) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.isTitleValid == false,
                supportingText = {
                    if (state.isTitleValid == false) {
                        Text("Pole wymagane", color = MaterialTheme.colorScheme.error)
                    }
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Opis
            TextField(
                value = state.description,
                onValueChange = { viewModel.handleIntent(CaptureMomentIntent.ChangeDescription(it)) },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lokalizacja - Instagram-style option item
            LocationOptionItem(
                currentLocation = state.location,
                onLocationClick = {
                    println("DEBUG: Location option clicked - launching location manager")
                    locationManager.launch(state.location)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Data - nowy komponent
            DatePicker(
                selectedDateMillis = state.date,
                onDateSelected = { timestamp ->
                    viewModel.handleIntent(CaptureMomentIntent.ChangeDate(timestamp))
                },
                label = "Date",
                placeholder = "Select a date"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Audio recording (temporarily disabled)
            Button(
                onClick = {
//                    val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
//                    recordAudioLauncher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = false // Temporarily disabled
            ) {
                Text("Record Audio (Coming Soon)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Affirmation Toggle
            AffirmationToggle(
                isEnabled = state.affirmationRequested,
                onToggle = { enabled ->
                    viewModel.handleIntent(CaptureMomentIntent.ToggleAffirmationRequested(enabled))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mood Selection
            Text(
                text = "Select Mood:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moods.forEach { mood ->
                    Button(
                        onClick = { viewModel.handleIntent(CaptureMomentIntent.ChangeMood(mood)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.selectedMood == mood) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = mood.name,
                            color = if (state.selectedMood == mood) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
            
            // Extra spacing at bottom for better scrolling
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Paywall overlay
        if (state.showPaywall) {
            PaywallScreen(
                reason = state.paywallReason,
                recommendedPlan = state.recommendedPlan,
                onClose = { viewModel.hidePaywall() },
                onUpgrade = { selectedPlan ->
                    // TODO: Implement actual upgrade logic
                    println("Upgrading to plan: $selectedPlan")
                    viewModel.hidePaywall()
                    // Here you would typically navigate to payment screen or handle upgrade
                }
            )
        }
        
        // Analytics overlay
        if (state.showAnalytics) {
            CapacityAnalyticsScreen(
                analytics = viewModel.getAnalytics(),
                userId = userSessionManager.getCurrentUser()?.userId ?: "anonymous_user",
                onClose = { viewModel.hideAnalytics() }
            )
        }
        
        // Photo selection dialog
        if (showPhotoDialog) {
            PhotoSelectionDialog(
                onCameraClick = {
                    println("🔍 WithGalleryPermission requesting gallery access")
                    cameraManager.launch()
                },
                onGalleryClick = {
                    galleryManager.launch()
                },
                onDismiss = {
                    showPhotoDialog = false
                },
                onShowCameraPermissionDialog = {
                    showPhotoDialog = false
                    showCameraPermissionDialog = true
                },
                onShowGalleryPermissionDialog = {
                    showPhotoDialog = false
                    showGalleryPermissionDialog = true
                }
            )
        }
        
        // Camera permission dialog
        if (showCameraPermissionDialog) {
            PermissionRequiredDialog(
                permissionType = PermissionType.CAMERA,
                onOpenSettings = {
                    showCameraPermissionDialog = false
                },
                onDismiss = {
                    showCameraPermissionDialog = false
                }
            )
        }
        
        // Gallery permission dialog
        if (showGalleryPermissionDialog) {
            PermissionRequiredDialog(
                permissionType = PermissionType.GALLERY,
                onOpenSettings = {
                    showGalleryPermissionDialog = false
                },
                onDismiss = {
                    showGalleryPermissionDialog = false
                }
            )
        }
        
        // Affirmation snackbar
        if (state.showAffirmationSnackbar) {
            AffirmationSnackbar(
                affirmation = state.generatedAffirmationData,
                onShowAffirmation = {
                    viewModel.handleIntent(CaptureMomentIntent.ShowAffirmationDialog)
                },
                onDismiss = {
                    viewModel.handleIntent(CaptureMomentIntent.DismissAffirmationSnackbar)
                }
            )
        }
        
        // Affirmation error snackbar
        if (state.affirmationError != null) {
            AffirmationErrorSnackbar(
                error = state.affirmationError,
                onDismiss = {
                    viewModel.handleIntent(CaptureMomentIntent.DismissAffirmationSnackbar)
                }
            )
        }
    }
}

@Composable
fun PhotoPickerView(
    imageBitmap: ImageBitmap?,
    onPhotoClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Gray.copy(alpha = 0.3f))
            .clickable { onPhotoClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Selected Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Add Photo",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )
                Text(
                    text = "Tap to add photo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun PhotoSelectionDialog(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDismiss: () -> Unit,
    onShowCameraPermissionDialog: () -> Unit,
    onShowGalleryPermissionDialog: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Photo Source")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Choose how you want to add a photo:")
                
                // Camera button with permission handling
                WithCameraPermission(
                    content = {
                        Button(
                            onClick = {
                                println("🔍 WithGalleryPermission requesting gallery access")

                                // Log will be added in platform-specific implementation
                                onCameraClick()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Take Photo")
                        }
                    },
                    deniedContent = { requestPermission ->
                        Button(
                            onClick = {
                                onShowCameraPermissionDialog()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Grant Camera Permission")
                        }
                    }
                )
                
                // Gallery button with permission handling
                WithGalleryPermission(
                    content = {
                        Button(
                            onClick = {
                                // Log will be added in platform-specific implementation
                                onGalleryClick()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Choose from Gallery")
                        }
                    },
                    deniedContent = { requestPermission ->
                        Button(
                            onClick = {
                                onShowGalleryPermissionDialog()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Grant Gallery Permission")
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun Modifier.dashedBorder(width: Dp, radius: Dp, color: Color) = drawBehind {
    drawIntoCanvas {
        val paint = Paint().apply {
            strokeWidth = width.toPx()
            this.color = color
            style = PaintingStyle.Stroke
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        }
        it.drawRoundRect(
            width.toPx(),
            width.toPx(),
            size.width - width.toPx(),
            size.height - width.toPx(),
            radius.toPx(),
            radius.toPx(),
            paint
        )
    }
}
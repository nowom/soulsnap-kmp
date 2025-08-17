package pl.soulsnaps.features.capturemoment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pl.soulsnaps.components.PrimaryButton
import pl.soulsnaps.components.SecondaryButton
import pl.soulsnaps.designsystem.AppColorScheme
import pl.soulsnaps.designsystem.SoulSnapTypography
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.location.rememberLocationManager
import pl.soulsnaps.location.PhotoLocation
import pl.soulsnaps.location.PlaceInfo
import pl.soulsnaps.location.PlaceType
import pl.soulsnaps.permissions.WithCameraPermission
import pl.soulsnaps.permissions.WithGalleryPermission
import pl.soulsnaps.permissions.WithLocationPermission
import pl.soulsnaps.photo.rememberCameraManager
import pl.soulsnaps.photo.rememberGalleryManager
import pl.soulsnaps.photo.SharedImage
import kotlinx.datetime.Clock

@Composable
fun RealCaptureScreen(
    onNavigateToPhotoEnhancement: (String) -> Unit,
    onSaveMemory: (EnhancedMemoryData) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(CaptureStep.PHOTO) }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var selectedMood by remember { mutableStateOf<MoodType?>(null) }
    var memoryTitle by remember { mutableStateOf("") }
    var memoryDescription by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    
    // Real service instances
    val cameraManager = rememberCameraManager { sharedImage ->
        sharedImage?.let { image ->
            photoUri = "camera_photo_${Clock.System.now().toEpochMilliseconds()}"
            currentStep = CaptureStep.MOOD
        }
    }
    
    val galleryManager = rememberGalleryManager { sharedImage ->
        sharedImage?.let { image ->
            photoUri = "gallery_photo_${Clock.System.now().toEpochMilliseconds()}"
            currentStep = CaptureStep.MOOD
        }
    }
    
    val locationManager = rememberLocationManager()
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorScheme.surface)
    ) {
        // Simple header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColorScheme.onSurface
                )
            }
            
            Text(
                text = "Capture Memory",
                style = SoulSnapTypography.headlineSmall,
                color = AppColorScheme.onSurface
            )
            
            Text(
                text = "${currentStep.ordinal + 1}/4",
                style = SoulSnapTypography.bodyMedium,
                color = AppColorScheme.onSurfaceVariant
            )
        }
        
        // Content based on current step
        when (currentStep) {
            CaptureStep.PHOTO -> RealPhotoCaptureStep(
                photoUri = photoUri,
                cameraManager = cameraManager,
                galleryManager = galleryManager,
                onPhotoCaptured = { uri ->
                    photoUri = uri
                    currentStep = CaptureStep.MOOD
                },
                onPhotoFromGallery = { uri ->
                    photoUri = uri
                    currentStep = CaptureStep.MOOD
                }
            )
            
            CaptureStep.MOOD -> SimpleMoodSelectionStep(
                selectedMood = selectedMood,
                onMoodSelected = { mood ->
                    selectedMood = mood
                    currentStep = CaptureStep.DETAILS
                },
                onBack = { currentStep = CaptureStep.PHOTO }
            )
            
            CaptureStep.DETAILS -> RealMemoryDetailsStep(
                title = memoryTitle,
                onTitleChange = { memoryTitle = it },
                description = memoryDescription,
                onDescriptionChange = { memoryDescription = it },
                selectedLocation = selectedLocation,
                onLocationChange = { selectedLocation = it },
                isFavorite = isFavorite,
                onFavoriteChange = { isFavorite = it },
                locationManager = locationManager,
                onBack = { currentStep = CaptureStep.MOOD },
                onNext = { currentStep = CaptureStep.REVIEW }
            )
            
            CaptureStep.REVIEW -> SimpleMemoryReviewStep(
                photoUri = photoUri,
                mood = selectedMood,
                title = memoryTitle,
                description = memoryDescription,
                location = selectedLocation,
                isFavorite = isFavorite,
                onBack = { currentStep = CaptureStep.DETAILS },
                onSave = {
                    val memoryData = EnhancedMemoryData(
                        photoUri = photoUri,
                        mood = selectedMood,
                        title = memoryTitle,
                        description = memoryDescription,
                        location = selectedLocation,
                        isFavorite = isFavorite
                    )
                    onSaveMemory(memoryData)
                }
            )
        }
    }
}

@Composable
private fun RealPhotoCaptureStep(
    photoUri: String?,
    cameraManager: pl.soulsnaps.photo.CameraManager,
    galleryManager: pl.soulsnaps.photo.GalleryManager,
    onPhotoCaptured: (String) -> Unit,
    onPhotoFromGallery: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Capture Your Moment",
            style = SoulSnapTypography.headlineMedium,
            color = AppColorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Take a photo or choose from your gallery",
            style = SoulSnapTypography.bodyMedium,
            color = AppColorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Camera button with real permission handling
        WithCameraPermission(
            content = {
                PrimaryButton(
                    text = "Take Photo",
                    onClick = { 
                        cameraManager.launch()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            },
            deniedContent = {
                PrimaryButton(
                    text = "Grant Camera Permission",
                    onClick = { /* Permission request handled by wrapper */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gallery button with real permission handling
        WithGalleryPermission(
            content = {
                SecondaryButton(
                    text = "Choose from Gallery",
                    onClick = { 
                        galleryManager.launch()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            },
            deniedContent = {
                SecondaryButton(
                    text = "Grant Gallery Permission",
                    onClick = { /* Permission request handled by wrapper */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Photo preview if available
        if (photoUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = AppColorScheme.surfaceVariant,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = "Photo preview",
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center),
                    tint = AppColorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PrimaryButton(
                text = "Enhance Photo",
                onClick = { /* Navigate to photo enhancement */ },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RealMemoryDetailsStep(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    selectedLocation: String?,
    onLocationChange: (String?) -> Unit,
    isFavorite: Boolean,
    onFavoriteChange: (Boolean) -> Unit,
    locationManager: pl.soulsnaps.location.LocationManager,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentLocation by remember { mutableStateOf<String?>(null) }
    var isLocationLoading by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Memory Details",
            style = SoulSnapTypography.headlineMedium,
            color = AppColorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title input
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColorScheme.primary,
                unfocusedBorderColor = AppColorScheme.outline
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description input
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColorScheme.primary,
                unfocusedBorderColor = AppColorScheme.outline
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Location section with real location services
        WithLocationPermission(
            content = {
                Column {
                    Text(
                        text = "Location",
                        style = SoulSnapTypography.titleMedium,
                        color = AppColorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentLocation ?: "No location selected",
                            style = SoulSnapTypography.bodyMedium,
                            color = AppColorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (isLocationLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = AppColorScheme.primary
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        isLocationLoading = true
                                        val location = locationManager.getCurrentLocation()
                                        location?.let { loc ->
                                            val placeInfo = locationManager.getLocationByCoordinates(
                                                loc.latitude,
                                                loc.longitude
                                            )
                                            currentLocation = placeInfo?.name ?: "Current Location"
                                            onLocationChange(currentLocation)
                                        }
                                        isLocationLoading = false
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Get current location",
                                    tint = AppColorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            deniedContent = {
                Text(
                    text = "Location permission required to tag your memory",
                    style = SoulSnapTypography.bodySmall,
                    color = AppColorScheme.error
                )
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Favorite toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mark as favorite",
                style = SoulSnapTypography.bodyMedium,
                color = AppColorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            Switch(
                checked = isFavorite,
                onCheckedChange = onFavoriteChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppColorScheme.primary,
                    checkedTrackColor = AppColorScheme.primaryContainer
                )
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SecondaryButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f)
            )
            
            PrimaryButton(
                text = "Next",
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = title.isNotBlank()
            )
        }
    }
}

@Composable
private fun SimpleMoodSelectionStep(
    selectedMood: MoodType?,
    onMoodSelected: (MoodType) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "How are you feeling?",
            style = SoulSnapTypography.headlineMedium,
            color = AppColorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Simple mood selection
        MoodType.values().forEach { mood ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMood == mood,
                    onClick = { onMoodSelected(mood) }
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = mood.name.lowercase().capitalize(),
                    style = SoulSnapTypography.bodyLarge,
                    color = AppColorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SecondaryButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f)
            )
            
            PrimaryButton(
                text = "Next",
                onClick = { /* Will be handled by parent */ },
                modifier = Modifier.weight(1f),
                enabled = selectedMood != null
            )
        }
    }
}

@Composable
private fun SimpleMemoryReviewStep(
    photoUri: String?,
    mood: MoodType?,
    title: String,
    description: String,
    location: String?,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Review Your Memory",
            style = SoulSnapTypography.headlineMedium,
            color = AppColorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Simple review display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AppColorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = SoulSnapTypography.titleMedium,
                    color = AppColorScheme.onSurface
                )
                
                if (description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = SoulSnapTypography.bodyMedium,
                        color = AppColorScheme.onSurfaceVariant
                    )
                }
                
                if (mood != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mood: ${mood.name.lowercase().capitalize()}",
                        style = SoulSnapTypography.bodyMedium,
                        color = AppColorScheme.onSurfaceVariant
                    )
                }
                
                if (!location.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Location: $location",
                        style = SoulSnapTypography.bodyMedium,
                        color = AppColorScheme.onSurfaceVariant
                    )
                }
                
                if (isFavorite) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = AppColorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Marked as favorite",
                            style = SoulSnapTypography.bodyMedium,
                            color = AppColorScheme.primary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SecondaryButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f)
            )
            
            PrimaryButton(
                text = "Save Memory",
                onClick = onSave,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

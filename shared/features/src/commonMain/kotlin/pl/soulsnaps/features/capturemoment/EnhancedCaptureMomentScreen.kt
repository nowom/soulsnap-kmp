package pl.soulsnaps.features.capturemoment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import pl.soulsnaps.components.PrimaryButton
import pl.soulsnaps.components.SecondaryButton
import pl.soulsnaps.designsystem.AppColorScheme
import pl.soulsnaps.designsystem.SoulSnapTypography
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.permissions.WithCameraPermission
import pl.soulsnaps.permissions.WithGalleryPermission
import kotlinx.datetime.Clock

@Composable
fun EnhancedCaptureMomentScreen(
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
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorScheme.surface)
    ) {
        // Header with progress indicator
        CaptureHeader(
            currentStep = currentStep,
            onBack = onBack
        )
        
        // Content based on current step
        when (currentStep) {
            CaptureStep.PHOTO -> PhotoCaptureStep(
                photoUri = photoUri,
                onPhotoCaptured = { uri ->
                    photoUri = uri
                    currentStep = CaptureStep.MOOD
                },
                onPhotoFromGallery = { uri ->
                    photoUri = uri
                    currentStep = CaptureStep.MOOD
                }
            )
            
            CaptureStep.MOOD -> MoodSelectionStep(
                selectedMood = selectedMood,
                onMoodSelected = { mood ->
                    selectedMood = mood
                    currentStep = CaptureStep.DETAILS
                },
                onBack = { currentStep = CaptureStep.PHOTO }
            )
            
            CaptureStep.DETAILS -> MemoryDetailsStep(
                title = memoryTitle,
                onTitleChange = { memoryTitle = it },
                description = memoryDescription,
                onDescriptionChange = { memoryDescription = it },
                selectedLocation = selectedLocation,
                onLocationChange = { selectedLocation = it },
                isFavorite = isFavorite,
                onFavoriteChange = { isFavorite = it },
                onBack = { currentStep = CaptureStep.MOOD },
                onNext = { currentStep = CaptureStep.REVIEW }
            )
            
            CaptureStep.REVIEW -> MemoryReviewStep(
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
private fun CaptureHeader(
    currentStep: CaptureStep,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColorScheme.surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
            
            IconButton(onClick = { /* Settings */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = AppColorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CaptureStep.values().forEach { step ->
                val isCompleted = step.ordinal < currentStep.ordinal
                val isCurrent = step == currentStep
                
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> AppColorScheme.primary
                                isCurrent -> AppColorScheme.primaryContainer
                                else -> AppColorScheme.surfaceVariant
                            }
                        )
                        .border(
                            width = if (isCurrent) 2.dp else 0.dp,
                            color = AppColorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = AppColorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = step.ordinal.plus(1).toString(),
                            style = SoulSnapTypography.labelMedium,
                            color = if (isCurrent) AppColorScheme.primary else AppColorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (step != CaptureStep.values().last()) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(2.dp)
                            .background(
                                if (isCompleted) AppColorScheme.primary else AppColorScheme.surfaceVariant
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoCaptureStep(
    photoUri: String?,
    onPhotoCaptured: (String) -> Unit,
    onPhotoFromGallery: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
        
        // Camera button with permission handling
        WithCameraPermission(
            content = {
                PrimaryButton(
                    text = "Take Photo",
                    onClick = { onPhotoCaptured("camera_photo_${Clock.System.now().toEpochMilliseconds()}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            },
            deniedContent = {
                PrimaryButton(
                    text = "Grant Camera Permission",
                    onClick = { /* Request camera permission */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gallery button with permission handling
        WithGalleryPermission(
            content = {
                SecondaryButton(
                    text = "Choose from Gallery",
                    onClick = { onPhotoFromGallery("gallery_photo_${Clock.System.now().toEpochMilliseconds()}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            },
            deniedContent = {
                SecondaryButton(
                    text = "Grant Gallery Permission",
                    onClick = { /* Request gallery permission */ },
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColorScheme.surfaceVariant)
                    .border(
                        width = 2.dp,
                        color = AppColorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
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
private fun MoodSelectionStep(
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
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Select the mood that best describes this moment",
            style = SoulSnapTypography.bodyMedium,
            color = AppColorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Mood grid
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(MoodType.values()) { mood ->
                MoodOption(
                    mood = mood,
                    isSelected = selectedMood == mood,
                    onClick = { onMoodSelected(mood) }
                )
            }
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
                onClick = { selectedMood?.let { onMoodSelected(it) } },
                modifier = Modifier.weight(1f),
                enabled = selectedMood != null
            )
        }
    }
}

@Composable
private fun MoodOption(
    mood: MoodType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) AppColorScheme.primaryContainer
                else AppColorScheme.surfaceVariant
            )
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) AppColorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getMoodIcon(mood),
                contentDescription = mood.name,
                tint = if (isSelected) AppColorScheme.primary else AppColorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = mood.name.capitalize(),
                style = SoulSnapTypography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) AppColorScheme.primary else AppColorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MemoryDetailsStep(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    selectedLocation: String?,
    onLocationChange: (String) -> Unit,
    isFavorite: Boolean,
    onFavoriteChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
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
            label = { Text("Memory Title") },
            placeholder = { Text("Give your memory a name") },
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
            placeholder = { Text("What happened in this moment?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColorScheme.primary,
                unfocusedBorderColor = AppColorScheme.outline
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Location input
        OutlinedTextField(
            value = selectedLocation ?: "",
            onValueChange = onLocationChange,
            label = { Text("Location (Optional)") },
            placeholder = { Text("Where were you?") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location"
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColorScheme.primary,
                unfocusedBorderColor = AppColorScheme.outline
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Favorite toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) AppColorScheme.primary else AppColorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "Mark as favorite",
                style = SoulSnapTypography.bodyMedium,
                color = AppColorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
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
                text = "Review",
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = title.isNotBlank()
            )
        }
    }
}

@Composable
private fun MemoryReviewStep(
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
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Review Your Memory",
            style = SoulSnapTypography.headlineMedium,
            color = AppColorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Photo preview
        if (photoUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColorScheme.surfaceVariant)
                    .border(
                        width = 2.dp,
                        color = AppColorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
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
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Memory details
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
                    style = SoulSnapTypography.titleLarge,
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getMoodIcon(mood ?: MoodType.HAPPY),
                        contentDescription = "Mood",
                        tint = AppColorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = mood?.name?.capitalize() ?: "Unknown",
                        style = SoulSnapTypography.bodyMedium,
                        color = AppColorScheme.onSurface
                    )
                    
                    if (isFavorite) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = AppColorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                if (!location.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = AppColorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = location,
                            style = SoulSnapTypography.bodyMedium,
                            color = AppColorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action buttons
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

private fun getMoodIcon(mood: MoodType): ImageVector {
    return when (mood) {
        MoodType.HAPPY -> Icons.Default.SentimentSatisfied
        MoodType.SAD -> Icons.Default.SentimentDissatisfied
        MoodType.NEUTRAL -> Icons.Default.SentimentNeutral
        MoodType.EXCITED -> Icons.Default.SentimentVerySatisfied
        MoodType.RELAXED -> Icons.Default.SentimentSatisfied
    }
}

enum class CaptureStep {
    PHOTO, MOOD, DETAILS, REVIEW
}

data class EnhancedMemoryData(
    val photoUri: String?,
    val mood: MoodType?,
    val title: String,
    val description: String,
    val location: String?,
    val isFavorite: Boolean
)

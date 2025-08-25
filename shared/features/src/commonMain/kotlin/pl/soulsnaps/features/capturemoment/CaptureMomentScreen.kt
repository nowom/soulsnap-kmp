package pl.soulsnaps.features.capturemoment

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import pl.soulsnaps.components.FullScreenCircularProgress
import pl.soulsnaps.components.PrimaryButton
import pl.soulsnaps.components.showPlatformDatePicker
import pl.soulsnaps.components.AnimatedErrorMessage
import pl.soulsnaps.components.AnimatedSuccessMessage
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.navigation.LocalNavController
import pl.soulsnaps.photo.rememberCameraManager
import pl.soulsnaps.photo.rememberGalleryManager
import pl.soulsnaps.features.auth.ui.PaywallScreen
import pl.soulsnaps.features.analytics.CapacityAnalyticsScreen
import pl.soulsnaps.features.memoryhub.MemoryHubRoute


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemoryScreen(viewModel: CaptureMomentViewModel = koinViewModel()) {
    val moods = MoodType.entries.toTypedArray()
    var showPhotoDialog by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val navController = LocalNavController.current

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
        LaunchedEffect(memoryId) {
            delay(1500) // Show success message for 1.5 seconds
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

            Spacer(modifier = Modifier.height(8.dp))

            // Lokalizacja
            OutlinedTextField(
                value = state.location ?: "",
                onValueChange = { viewModel.handleIntent(CaptureMomentIntent.ChangeLocation(it)) },
                label = { Text("Location (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
//                    val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
//                    recordAudioLauncher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Record Audio")
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Data
            var showDatePicker by remember { mutableStateOf(false) }
            
            if (showDatePicker) {
                showPlatformDatePicker(
                    initialDateMillis = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                    onDateSelected = { timestamp ->
                        viewModel.handleIntent(CaptureMomentIntent.ChangeDate(timestamp))
                        showDatePicker = false
                    }
                )
            }
            
            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Select Date")
            }

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
                onClose = { viewModel.hideAnalytics() }
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
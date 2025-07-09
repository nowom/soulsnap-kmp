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
import org.koin.compose.viewmodel.koinViewModel
import pl.soulsnaps.components.FullScreenCircularProgress
import pl.soulsnaps.components.PrimaryButton
import pl.soulsnaps.components.showPlatformDatePicker
import pl.soulsnaps.domain.model.MoodType
import pl.soulsnaps.navigation.LocalNavController
import pl.soulsnaps.photo.rememberCameraManager
import pl.soulsnaps.photo.rememberGalleryManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemoryScreen(viewModel: CaptureMomentViewModel = koinViewModel()) {
    val moods = MoodType.entries.toTypedArray()
    var showPhotoDialog by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

//    val mediaRecorder = mediaRecorderremember { MediaRecorder() }
//    var audioFilePath by remember { mutableStateOf<String?>(null) }

//    val recordAudioLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult(),
//        onResult = { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val audioUri = result.data?.data
//                audioUri?.let {
//                    viewModel.handleIntent(CaptureMomentIntent.ChangeAudio(it))
//                }
//            }
//        }
//    )

    val cameraManager = rememberCameraManager {
        coroutineScope.launch {
            val bitmap = withContext(Dispatchers.Default) {
                it?.toImageBitmap()
            }
            imageBitmap = bitmap
        }
    }

    val galleryManager = rememberGalleryManager {
        coroutineScope.launch {
            val bitmap = withContext(Dispatchers.Default) {
                it?.toImageBitmap()
            }
            imageBitmap = bitmap
        }
    }
//    val pickImageLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent(),
//        onResult = { uri ->
//            if (uri != null) {
//                viewModel.handleIntent(CaptureMomentIntent.ChangePhoto(uri))
//            }
//            showPhotoDialog = false
//        })
//
//    val takePictureLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.TakePicturePreview(),
//        onResult = { bitmap ->
//            bitmap?.let {
//                val uri = saveImageToMediaStore(context, it)
//                viewModel.handleIntent(CaptureMomentIntent.ChangePhoto(uri))
//            }
//            showPhotoDialog = false
//        })

    if (state.isSaving) {
        FullScreenCircularProgress()
    }
    val navController = LocalNavController.current
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
            var showPicker by remember { mutableStateOf(false) }

            if (showPicker) {
                showPlatformDatePicker(null) { selectedMillis ->
                    println("Date selected: $selectedMillis")
                }
                showPicker = false
            }

            Button(onClick = { showPicker = true }) {
                Text("Pick Date")
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Mood selection
            Text(
                "Mood",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moods.forEach { mood ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (mood == state.selectedMood)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.tertiaryContainer
                            )
                            .clickable { viewModel.handleIntent(CaptureMomentIntent.ChangeMood(mood)) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            mood.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (mood == state.selectedMood)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Pokazanie wygenerowanej afirmacji (po zapisie)
            state.generatedAffirmation?.let { affirmation ->
                Text(
                    text = "Generated Affirmation:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = affirmation,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    if (showPhotoDialog) {
        PhotoPickerDialog(
            onDismiss = { showPhotoDialog = false },
            onTakePhoto = {
                cameraManager.launch()
                showPhotoDialog = false
            },
            onPickFromGallery = {
                galleryManager.launch()
                showPhotoDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPickerDialog(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, shape = Shapes().medium)
    ) {
        Column(
            Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Dodaj zdjęcie",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Wybierz sposób dodania zdjęcia",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onTakePhoto,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C7C59))
            ) {
                Text("Zrób zdjęcie", color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onPickFromGallery,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCBA4))
            ) {
                Text("Wybierz z galerii", color = Color.Black)
            }
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    }
}

@Composable
fun PhotoPickerView(
    imageBitmap: ImageBitmap?, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .dashedBorder(2.dp, 2.dp, Color.Black)
            .clickable { onClick() }
            .padding(4.dp), contentAlignment = Alignment.Center) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Selected Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .background(Color(0xFFFFD700), shape = CircleShape) // Złota ramka
                , contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Icon",
                    tint = Color.Black,
                    modifier = Modifier.size(12.dp) // Ikona 10x10 dp
                )
            }
        } else {

//            Icon(
//                painter = DsIcons.Add(),
//                contentDescription = "Camera Icon",
//                tint = Color.Gray,
//                modifier = Modifier.size(40.dp)
//            )
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
package pl.soulsnaps.features.memoryhub.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.soulsnaps.components.AnimatedErrorMessage
import pl.soulsnaps.components.AnimatedSuccessMessage
import pl.soulsnaps.components.FullScreenCircularProgress
import pl.soulsnaps.components.PrimaryButton
import pl.soulsnaps.domain.model.MoodType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMemoryScreen(
    state: EditMemoryState,
    onBack: () -> Unit,
    onIntent: (EditMemoryIntent) -> Unit
) {
    val moods = MoodType.entries.toTypedArray()

    if (state.isSaving) {
        FullScreenCircularProgress()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Edit Memory", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            PrimaryButton(
                text = "Save Changes", 
                modifier = Modifier.padding(16.dp),
                enabled = !state.isSaving
            ) {
                onIntent(EditMemoryIntent.SaveMemory)
            }
        }
    ) { paddingValues ->
        
        when {
            state.isLoading -> {
                FullScreenCircularProgress()
            }
            
            state.memory != null -> {
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

                    // Note: Photo editing is not implemented yet - showing current photo
                    state.memory!!.photoUri?.let { photoUri ->
                        Text(
                            text = "Photo: ${if (photoUri.length > 50) "${photoUri.take(50)}..." else photoUri}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Title
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = { onIntent(EditMemoryIntent.ChangeTitle(it)) },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.isTitleValid == false,
                        supportingText = {
                            if (state.isTitleValid == false) {
                                Text("Field required", color = MaterialTheme.colorScheme.error)
                            }
                        },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description
                    TextField(
                        value = state.description,
                        onValueChange = { onIntent(EditMemoryIntent.ChangeDescription(it)) },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Location (simple text field for now)
                    OutlinedTextField(
                        value = state.location ?: "",
                        onValueChange = { onIntent(EditMemoryIntent.ChangeLocation(it)) },
                        label = { Text("Location (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mood Selector (simple dropdown for now)
                    Text(
                        text = "Mood: ${state.selectedMood?.name ?: "Not selected"}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Simple mood buttons
                    Column {
                        moods.forEach { mood ->
                            TextButton(
                                onClick = { onIntent(EditMemoryIntent.ChangeMood(mood)) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = mood.name,
                                    color = if (state.selectedMood == mood) 
                                        MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    // Extra spacing at bottom for better scrolling
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            
            else -> {
                AnimatedErrorMessage(
                    message = state.errorMessage ?: "Failed to load memory",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

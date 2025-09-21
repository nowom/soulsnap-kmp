package pl.soulsnaps.features.location

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pl.soulsnaps.components.PrimaryButton
import pl.soulsnaps.components.SecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDialogWithAutocomplete(
    currentLocation: String?,
    onLocationSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    viewModel: LocationPickerViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    
    // Initialize ViewModel with current location
    LaunchedEffect(currentLocation) {
        viewModel.setInitialQuery(currentLocation)
    }
    
    // Auto-focus on the input when dialog opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = "Add Location", 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        // Save button in top bar
                        IconButton(
                            onClick = { 
                                val finalLocation = currentLocation
                                println("DEBUG: LocationPickerDialog - saving location: $finalLocation")
                                onLocationSelected(finalLocation)
                                onDismiss()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save location",
                                tint = MaterialTheme.colorScheme.primary
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
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    PrimaryButton(
                        text = "Save Location",
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.query.isNotBlank()
                    ) {
                        println("DEBUG: LocationPickerDialog - manual save: '${state.query}'")
                        onLocationSelected(state.query.takeIf { it.isNotBlank() })
                        onDismiss()
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Remove location button
                    if (currentLocation != null) {
                        SecondaryButton(
                            text = "Remove Location",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                println("DEBUG: LocationPickerDialog - removing location")
                                onLocationSelected(null)
                                onDismiss()
                            }
                        )
                    }
                }
            }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Location input
                    item {
                        OutlinedTextField(
                            value = state.query,
                            onValueChange = { viewModel.handleIntent(LocationPickerIntent.UpdateQuery(it)) },
                            label = { Text("Location") },
                            placeholder = { Text("Enter a location...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                when {
                                    state.isSearching -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                    state.query.isNotEmpty() -> {
                                        IconButton(onClick = { viewModel.handleIntent(LocationPickerIntent.ClearQuery) }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true
                        )
                    }
                    
                    // GPS Location Button
                    item {
                        CurrentLocationButton(
                            onClick = {
                                println("DEBUG: LocationPickerDialog - GPS location button clicked")
                                viewModel.handleIntent(LocationPickerIntent.UseCurrentLocation)
                            },
                            isLoading = state.isLoadingCurrentLocation
                        )
                    }
                    
                    // Error message
                    state.errorMessage?.let { error ->
                        item {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    
                    // Suggestions list
                    if (state.suggestions.isNotEmpty()) {
                        item {
                            Text(
                                text = "Suggestions",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(state.suggestions) { suggestion ->
                            LocationSuggestionItem(
                                suggestion = suggestion,
                                onClick = {
                                    println("DEBUG: LocationPickerDialog - suggestion selected: ${suggestion.name}")
                                    viewModel.handleIntent(LocationPickerIntent.SelectLocation(suggestion))
                                    onLocationSelected(suggestion.name)
                                    onDismiss()
                                }
                            )
                        }
                    } else if (state.query.length >= 2 && !state.isSearching) {
                        item {
                            Text(
                                text = "No suggestions found for '${state.query}'",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        // Helper text when not searching
                        item {
                            Column {
                                Text(
                                    text = "Start typing to see location suggestions...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Example suggestions
                                Text(
                                    text = "💡 Try typing:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "• Kra → Kraków\n• War → Warsaw\n• Gda → Gdańsk\n• Zak → Zakopane",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
        }
    }
}

@Composable
private fun LocationSuggestionItem(
    suggestion: LocationSuggestion,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location type icon
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = getLocationTypeColor(suggestion.type).copy(alpha = 0.1f),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = getLocationTypeIcon(suggestion.type),
                    contentDescription = null,
                    tint = getLocationTypeColor(suggestion.type),
                    modifier = Modifier.padding(6.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Location info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = suggestion.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = suggestion.fullAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun getLocationTypeIcon(type: LocationType) = when (type) {
    LocationType.PLACE -> Icons.Default.LocationOn
    LocationType.ADDRESS -> Icons.Default.LocationOn  
    LocationType.POI -> Icons.Default.Place
    LocationType.REGION -> Icons.Default.LocationOn
}

@Composable
private fun getLocationTypeColor(type: LocationType) = when (type) {
    LocationType.PLACE -> MaterialTheme.colorScheme.primary
    LocationType.ADDRESS -> Color(0xFF4CAF50)
    LocationType.POI -> Color(0xFFFF9800)
    LocationType.REGION -> Color(0xFF9C27B0)
}

@Composable
private fun CurrentLocationButton(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Current Location",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = if (isLoading) "Getting your location..." else "Use my current location",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

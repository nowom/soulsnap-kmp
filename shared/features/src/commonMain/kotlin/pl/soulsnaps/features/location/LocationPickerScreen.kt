package pl.soulsnaps.features.location

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.soulsnaps.components.PrimaryButton
import pl.soulsnaps.components.SecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    currentLocation: String?,
    onLocationSelected: (String?) -> Unit,
    onBack: () -> Unit
) {
    var locationText by remember { mutableStateOf(currentLocation ?: "") }
    val focusRequester = remember { FocusRequester() }
    
    // Auto-focus on the input when screen opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

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
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Save button
                    IconButton(
                        onClick = { 
                            onLocationSelected(locationText.takeIf { it.isNotBlank() })
                            onBack()
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    onLocationSelected(locationText.takeIf { it.isNotBlank() })
                    onBack()
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Clear location button
                if (currentLocation != null) {
                    SecondaryButton(
                        text = "Remove Location",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onLocationSelected(null)
                            onBack()
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Location input
            OutlinedTextField(
                value = locationText,
                onValueChange = { locationText = it },
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
                    if (locationText.isNotEmpty()) {
                        IconButton(onClick = { locationText = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Helper text
            Text(
                text = "Enter a place name, address, or landmark that describes where this memory was created.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Future: Here we could add:
            // - Recent locations
            // - Suggested locations
            // - Map integration
            Text(
                text = "💡 Coming soon: Recent locations and map integration",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

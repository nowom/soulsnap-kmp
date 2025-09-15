package pl.soulsnaps.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import pl.soulsnaps.location.PlaceInfo
import pl.soulsnaps.location.rememberLocationManager
import pl.soulsnaps.permissions.WithLocationPermission

@Composable
fun LocationPicker(
    selectedLocation: String?,
    onLocationSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Location",
    placeholder: String = "Search for a place...",
    enabled: Boolean = true
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<PlaceInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    val locationManager = rememberLocationManager()
    
    // Search for places when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty() && searchQuery.length > 2) {
            isLoading = true
            delay(300) // Debounce search
            try {
                suggestions = locationManager.searchPlaces(searchQuery)
                showSuggestions = true
            } catch (e: Exception) {
                suggestions = emptyList()
            }
            isLoading = false
        } else {
            suggestions = emptyList()
            showSuggestions = false
        }
    }
    
    Column(modifier = modifier) {
        // Location input field
        OutlinedTextField(
            value = searchQuery.ifEmpty { selectedLocation ?: "" },
            onValueChange = { 
                searchQuery = it
                if (it.isEmpty()) {
                    onLocationSelected("")
                }
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
        
        // Current location button
        if (searchQuery.isEmpty() && selectedLocation.isNullOrEmpty()) {
            WithLocationPermission(
                content = {
                    CurrentLocationButton(
                        onClick = {
                            // Get current location
                            // This would be implemented with actual location services
                            onLocationSelected("Current Location")
                        }
                    )
                },
                deniedContent = { requestPermission ->
                    CurrentLocationButton(
                        onClick = requestPermission,
                        text = "Grant Location Permission"
                    )
                }
            )
        }
        
        // Suggestions list
        if (showSuggestions && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(8.dp)
                ) {
                    items(suggestions) { place ->
                        LocationSuggestionItem(
                            place = place,
                            onClick = {
                                onLocationSelected(place.name)
                                searchQuery = place.name
                                showSuggestions = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentLocationButton(
    onClick: () -> Unit,
    text: String = "Use Current Location"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "Current Location",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LocationSuggestionItem(
    place: PlaceInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "Location",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (!place.address.isNullOrEmpty()) {
                    Text(
                        text = place.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (place.rating != null) {
                Text(
                    text = "★ ${place.rating}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

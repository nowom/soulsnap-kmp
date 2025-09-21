package pl.soulsnaps.features.memoryhub.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.features.R


@Composable
actual fun MapboxMapContainer(
    memories: List<Memory>,
    onMemoryClick: (Int) -> Unit
) {
    println("DEBUG: MapboxMapContainer.Android - showing ${memories.size} memories")
    
    // Check if Mapbox is available
    val mapViewState = rememberMapViewportState {
        setCameraOptions {
            zoom(3.5)
            center(Point.fromLngLat(19.0, 52.0)) // środek PL
            pitch(0.0)
            bearing(0.0)
        }
    }
    
    println("DEBUG: MapboxMapContainer.Android - MapViewState created successfully")
    
    MapboxMap(
        modifier = Modifier.fillMaxSize(),
        mapViewportState = mapViewState,
    ) {
        println("DEBUG: MapboxMapContainer.Android - MapboxMap composable entered")

        memories.forEach { memory ->
            val lat = memory.latitude ?: return@forEach
            val lon = memory.longitude ?: return@forEach
            
            println("DEBUG: MapboxMapContainer.Android - adding marker for memory: ${memory.title} at lat=$lat, lon=$lon")

            val marker = rememberIconImage(
                key = "markerResourceId",
                painter = painterResource(R.drawable.red_marker)
            )
            PointAnnotation(
                point = Point.fromLngLat(lon, lat),
            ) {
                iconImage = marker
                interactionsState.onClicked {
                    println("DEBUG: MapboxMapContainer.Android - marker clicked for memory: ${memory.id}")
                    onMemoryClick(memory.id)
                    true
                }
            }
        }
    }
    
    println("DEBUG: MapboxMapContainer.Android - MapboxMap setup completed")
}
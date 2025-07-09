package pl.soulsnaps.features.memoryhub.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
    val mapViewState = rememberMapViewportState {
        setCameraOptions {
            zoom(3.5)
            center(Point.fromLngLat(19.0, 52.0)) // środek PL
            pitch(0.0)
            bearing(0.0)
        }
    }
    MapboxMap(
        modifier = Modifier.fillMaxSize(),
        mapViewportState = mapViewState,
    ) {

        memories.forEach { memory ->
            val lat = memory.latitude ?: return@forEach
            val lon = memory.longitude ?: return@forEach

            val marker = rememberIconImage(
                key = "markerResourceId",
                painter = painterResource(R.drawable.red_marker)
            )
            PointAnnotation(
                point = Point.fromLngLat(lon, lat),
            ) {
                iconImage = marker
                interactionsState.onClicked {
                    onMemoryClick(memory.id)
                    true
                }
            }
        }
    }
}
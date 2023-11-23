package com.example.bike_navigation_kotlin.presentation.mapbox.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

@Composable
fun MapboxView(
    modifier: Modifier = Modifier,
    initialLat: Double = 55.861069,
    initialLng: Double = 9.872276,
    initialZoom: Double = 9.0
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
        }
    }
    val mapboxMap = mapView.getMapboxMap()

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = {
            // Here you can make further updates to the mapView if needed.
        }
    )

    DisposableEffect(Unit) {
        mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(initialLng, initialLat))
                .zoom(initialZoom)
                .build()
        )
        onDispose {
            mapView.onStop()
            mapView.onDestroy()
        }
    }
}
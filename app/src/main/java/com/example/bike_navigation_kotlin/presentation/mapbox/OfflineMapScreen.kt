package com.example.bike_navigation_kotlin.presentation.mapbox

import android.Manifest
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bike_navigation_kotlin.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.bindgen.Value
import com.mapbox.common.TileDataDomain
import com.mapbox.common.TileStore
import com.mapbox.common.TileStoreOptions
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearingSource
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.locationcomponent.location2

@OptIn(ExperimentalPermissionsApi::class)
fun launchPermissionRequest(locationPermissionState: PermissionState){
    locationPermissionState.launchPermissionRequest()
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OfflineMapScreen(regionId: String) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val mapboxMap = remember {mapView.getMapboxMap()}

    val bearingImage = AppCompatResources.getDrawable(context, R.drawable.mapbox_user_puck_icon)
    val shadowImage = AppCompatResources.getDrawable(context, R.drawable.mapbox_user_icon_shadow)

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    launchPermissionRequest(locationPermissionState)

    val tileStore = TileStore.create().also {
        // Set default access token for the created tile store instance
        it.setOption(
            TileStoreOptions.MAPBOX_ACCESS_TOKEN,
            TileDataDomain.MAPS,
            Value(context.getString(R.string.mapbox_access_token))
        )
    }

    val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }


    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            mapView
        },
        update = {view ->
            mapView.location2.puckBearingSource = PuckBearingSource.HEADING

            Log.d("REGION_ID", regionId)
            tileStore.getTileRegionGeometry(regionId) { geometry ->
                if (geometry.isValue) {
                    val cameraPosition = mapboxMap.cameraForGeometry(geometry.value!!)
                    mapboxMap.setCamera(cameraPosition)
                } else {
                    geometry.error?.let { error ->
                        Log.e("GEOGRAPHY_TAG", "Error fetching tile region geometry: $error")
                    }
                }
            }

            view.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {

                val lineString = LineString.fromLngLats(listOf(
                    Point.fromLngLat(-122.483696, 37.833818),
                    Point.fromLngLat(-122.483482, 37.833174),
                    Point.fromLngLat(-122.482773, 37.8327)
                ))

                val feature = Feature.fromGeometry(lineString)

                val sourceId = "polyline-source"

                /*style.addSource(geoJsonSource(sourceId) {
                    geometry(lineString)
                })

                style.addLayer(lineLayer("polyline-layer", sourceId) {
                    lineColor("blue")
                    lineWidth(5.0)
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                })

                val locationComponentPlugin = view.location
                locationComponentPlugin.updateSettings {
                    this.enabled = true
                    this.locationPuck = LocationPuck2D(
                        bearingImage = bearingImage,
                        shadowImage = shadowImage
                    )
                }
                 locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
                 locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)


            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            mapView.onStop()
            mapView.onDestroy()
        }
    }
}
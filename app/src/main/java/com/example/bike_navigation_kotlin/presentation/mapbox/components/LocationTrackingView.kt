import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bike_navigation_kotlin.R
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location

@Composable
fun LocationTrackingView(innerPadding: PaddingValues) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val bearingImage = AppCompatResources.getDrawable(context, R.drawable.mapbox_user_puck_icon)
    val shadowImage = AppCompatResources.getDrawable(context, R.drawable.mapbox_user_icon_shadow)

    val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { mapView }
    ) { view ->
        view.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            val locationComponentPlugin = view.location
            locationComponentPlugin.updateSettings {
                this.enabled = true
                this.locationPuck = LocationPuck2D(
                    bearingImage = bearingImage,
                    shadowImage = shadowImage
                )
            }
           // locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
           // locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)


        }
    }

    DisposableEffect(Unit) {
        // Anything you want to happen when the composable enters the composition
        onDispose {
            // Anything you want to happen when the composable leaves the composition
            // For instance, remove listeners from mapView
            mapView.onStop()
            mapView.onDestroy()
        }
    }
}
package com.example.bike_navigation_kotlin.presentation.mapbox

import android.content.Context
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bike_navigation_kotlin.R
import com.mapbox.bindgen.Value
import com.mapbox.common.NetworkRestriction
import com.mapbox.common.TileDataDomain
import com.mapbox.common.TileRegion
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.common.TileStoreOptions
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.GlyphsRasterizationMode
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.OfflineManager
import com.mapbox.maps.Style
import com.mapbox.maps.StylePackLoadOptions
import com.mapbox.maps.TilesetDescriptorOptions
import kotlin.math.max
import kotlin.math.min


fun getBufferedBoundingBox(points: List<Point>, buffer: Double): List<Point> {
    var minLat = Double.MAX_VALUE
    var minLng = Double.MAX_VALUE
    var maxLat = Double.MIN_VALUE
    var maxLng = Double.MIN_VALUE

    for (point in points) {
        minLat = min(minLat, point.latitude())
        minLng = min(minLng, point.longitude())
        maxLat = max(maxLat, point.latitude())
        maxLng = max(maxLng, point.longitude())
    }

    return listOf(
        Point.fromLngLat(minLng - buffer, minLat - buffer),  // bottom-left
        Point.fromLngLat(maxLng + buffer, minLat - buffer),  // bottom-right
        Point.fromLngLat(maxLng + buffer, maxLat + buffer),  // top-right
        Point.fromLngLat(minLng - buffer, maxLat + buffer),  // top-left
        Point.fromLngLat(minLng - buffer, minLat - buffer)   // close the polygon
    )
}


val routePoints = listOf(
    Point.fromLngLat(9.886936,55.867074),
   Point.fromLngLat(9.876902,55.866833),
    Point.fromLngLat(9.878794,55.870683),
Point.fromLngLat(9.888756,55.872749),
    Point.fromLngLat(9.886936,55.867074)
)

val route = LineString.fromLngLats(routePoints)
val bufferedBox = getBufferedBoundingBox(routePoints, 0.0)
val polygon = Polygon.fromLngLats(listOf(bufferedBox))
val STYLE_PACK_METADATA = mapOf(
    "description" to "My offline style pack for hiking routes",
    "version" to "1.0",
    "date_downloaded" to "2023-10-24"
)

val stylePackLoadOptions = StylePackLoadOptions.Builder()
    .glyphsRasterizationMode(GlyphsRasterizationMode.IDEOGRAPHS_RASTERIZED_LOCALLY)
   // .metadata(Value(STYLE_PACK_METADATA))

    .build()


class OfflineHandler(context: Context) {
    val offlineManager: OfflineManager =
        OfflineManager(MapInitOptions.getDefaultResourceOptions(context))

    // You need to keep a reference of the created tileStore and keep it during the download process.
// You are also responsible for initializing the TileStore properly, including setting the proper access token.
    val tileStore = TileStore.create().also {
        // Set default access token for the created tile store instance
        it.setOption(
            TileStoreOptions.MAPBOX_ACCESS_TOKEN,
            TileDataDomain.MAPS,
            Value(context.getString(R.string.mapbox_access_token))
        )
    }

    fun downloadRegion() {
        val stylePackCancelable = offlineManager.loadStylePack(
            Style.OUTDOORS,
            // Build Style pack load options
            stylePackLoadOptions,
            { progress ->
                // Handle the download progress
            },
            { expected ->
                if (expected.isValue) {
                    expected.value?.let { stylePack ->
                        // Style pack download finished successfully
                        Log.d("TAG", "Style pack: $stylePack")
                    }
                }
                expected.error?.let {
                    // Handle errors that occurred during the style pack download.
                    Log.d("TAG", "Load style pack error: $it")
                }
            }
        )

        val tilesetDescriptor = offlineManager.createTilesetDescriptor(
            TilesetDescriptorOptions.Builder()
                .styleURI(Style.OUTDOORS)
                .minZoom(0)
                .maxZoom(16)
                .build()
        )

        val tileRegionCancelable = tileStore.loadTileRegion(
            "proper_home",
            TileRegionLoadOptions.Builder()
              //  .geometry(Point.fromLngLat(55.867074, 9.886936))
                .geometry(polygon)
                .descriptors(listOf(tilesetDescriptor))
                //.metadata(Value(STYLE_PACK_METADATA))
                .acceptExpired(true)
                .networkRestriction(NetworkRestriction.NONE)
                .build(),
            { progress ->
                // Handle the download progress
            }
        ) { expected ->
            if (expected.isValue) {
                // Tile region download finishes successfully
                Log.d("TAG", "tile region download finished successfully")
            }
            expected.error?.let {
                Log.d("TAG", "Error downloading region: " + it.message)
                // Handle errors that occurred during the tile region download.
            }
        }
    }

    fun showDownloadedRegions(onRegionsFetched: (List<TileRegion>) -> Unit) {
        // Get a list of tile regions that are currently available.
        tileStore.getAllTileRegions { expected ->
            if (expected.isValue) {
                expected.value?.let { tileRegionList ->
                    onRegionsFetched(tileRegionList)
                    Log.d("TAG", "Existing tile regions: $tileRegionList")
                }
            }
            expected.error?.let { tileRegionError ->
                Log.d("TAG", "TileRegionError: $tileRegionError")
            }
        }
        // Get a list of style packs that are currently available.
        offlineManager.getAllStylePacks { expected ->
            if (expected.isValue) {
                expected.value?.let { stylePackList ->
                    Log.d("TAG", "Existing style packs: $stylePackList")
                }
            }
            expected.error?.let { stylePackError ->
                Log.d("TAG", "StylePackError: $stylePackError")
            }
        }

    }
}


@Composable
fun OfflineRegionsScreen(navController: NavController,) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    val isShowingRegions = remember { mutableStateOf(false) }
    val tileRegions = remember { mutableStateOf(emptyList<TileRegion>()) }
    val offlineHandler = OfflineHandler(context)


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Blue)
                .clickable {
                    if (isShowingRegions.value) {
                        offlineHandler.downloadRegion()
                        isShowingRegions.value = false
                    } else {
                        offlineHandler.showDownloadedRegions { regions ->
                            tileRegions.value =
                                regions // Assuming 'id' is a field in the TileRegion
                        }
                        isShowingRegions.value = true
                    }
                }
        ) {
            Text(
                text = if (isShowingRegions.value) "SHOW REGIONS" else "DOWNLOAD",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }

        // List the tile regions
        tileRegions.value.forEach { region ->
            Text(text = region.id, modifier = Modifier.padding(8.dp)
                .clickable{navController.navigate("offlineMapScreen/"+region.id)})
        }
    }
}
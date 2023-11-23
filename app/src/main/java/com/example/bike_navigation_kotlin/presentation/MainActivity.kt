package com.example.bike_navigation_kotlin.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bike_navigation_kotlin.presentation.mapbox.MapboxScreen
import com.example.bike_navigation_kotlin.presentation.mapbox.OfflineMapScreen
import com.example.bike_navigation_kotlin.presentation.mapbox.OfflineRegionsScreen
import com.example.bike_navigation_kotlin.presentation.util.Screen
import com.example.bike_navigation_kotlin.ui.theme.Bike_navigation_kotlinTheme
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PermissionsListener {

    private lateinit var permissionsManager: PermissionsManager

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Permission is already granted, so you can initialize your content
            initContent()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        // Here you can explain why the app needs the permissions.
        // Typically, you'd show a dialog or a snackbar.
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            initContent()
        } else {
            // Permission not granted. Show a message or handle appropriately.
        }
    }

    private fun initContent() {
        setContent {
            Bike_navigation_kotlinTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "offlineRegionsScreen"
                    ) {
                        composable(route=Screen.OfflineRegionsScreen.route) { OfflineRegionsScreen(
                            navController = navController) }

                        composable("offlineMapScreen/{regionId}") { backStackEntry ->
                            val regionId = backStackEntry.arguments?.getString("regionId")
                            if (regionId != null) {
                                OfflineMapScreen(regionId)
                            }
                        }
                        composable(route = Screen.MapboxScreen.route) {
                            MapboxScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
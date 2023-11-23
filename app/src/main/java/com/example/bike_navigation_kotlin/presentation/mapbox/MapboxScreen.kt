package com.example.bike_navigation_kotlin.presentation.mapbox


import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.bike_navigation_kotlin.presentation.mapbox.components.MapboxView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapboxScreen(
    navController: NavController,
    //viewModel: MapboxViewModel = hiltViewModel()
) {
   // val state = viewModel.state.value
    val scope = rememberCoroutineScope()

    Scaffold(){ innerPadding ->
        //LocationTrackingView(innerPadding)
        MapboxView(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        initialLat = 55.861069,
        initialLng = 9.872276,
        initialZoom = 9.0
    )
    }
}
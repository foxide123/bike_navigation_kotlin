package com.example.bike_navigation_kotlin.presentation.mapbox

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapboxViewModel @Inject constructor(
    //private val mapboxUseCases: MapboxUseCases
) : ViewModel(){
    private val _state = mutableStateOf(MapboxState())
    val state: State<MapboxState> = _state
}
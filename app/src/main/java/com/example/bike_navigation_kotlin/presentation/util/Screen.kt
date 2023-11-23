package com.example.bike_navigation_kotlin.presentation.util

sealed class Screen(val route:String){
    object MapboxScreen: Screen("mapbox")
    object OfflineRegionsScreen: Screen("offlineRegionsScreen")

}
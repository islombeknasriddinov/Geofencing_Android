package com.example.geofencing.util

import android.graphics.Color
import com.example.geofencing.model.Marker
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.MarkerOptions

fun buildCircle(marker: Marker): CircleOptions {
    val circleOptions = CircleOptions()
    circleOptions.center(marker.latLng)
    circleOptions.radius(marker.radius!!.toDouble())
    circleOptions.strokeColor(Color.argb(255, 255, 0, 0))
    circleOptions.fillColor(Color.argb(64, 255, 0, 0))
    circleOptions.strokeWidth(4F)
    return circleOptions
}

fun buildMarkerIcon(marker: Marker): MarkerOptions {
    val markerOptions = MarkerOptions()
    markerOptions.position(marker.latLng!!)
    markerOptions.title(marker.enter)
    return markerOptions
}
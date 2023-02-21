package com.example.geofencing.util

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import com.example.geofencing.model.Marker
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.MarkerOptions

fun buildCircle(marker: Marker): CircleOptions {
    val circleOptions = CircleOptions()
    marker.latLng?.let { circleOptions.center(it) }
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

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle.customGetParcelable(key: String): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= 33) {
        getParcelableArrayList(key, T::class.java) as ArrayList<T>
    } else {
        getParcelableArrayList(key)
    }
}
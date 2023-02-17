package com.example.geofencing.helper

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import com.example.geofencing.broadcast.GeofenceBroadcastReceiver
import com.example.geofencing.model.Marker
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng


class GeofenceHelper(base: Context?) : ContextWrapper(base) {
    private val TAG = GeofenceHelper::class.simpleName

    var pendingIntent: PendingIntent? = null
    private val geofenceList: ArrayList<Geofence> = ArrayList()
    fun getGeofencingRequest(
        markerList: ArrayList<Marker>, transitionTypes: Int
    ): GeofencingRequest {
        for (coordinate in markerList) {
            geofenceList.add(
                getGeofence(
                    coordinate.markerId,
                    coordinate.latLng!!,
                    coordinate.radius!!,
                    transitionTypes
                )
            )
        }
        val builder = GeofencingRequest.Builder()
        builder.addGeofences(geofenceList)

        return builder.build()
    }

    private fun getGeofence(
        ID: String?, latLng: LatLng, radius: Float, transitionTypes: Int
    ): Geofence {
        return Geofence.Builder().setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setRequestId(ID)
            .setTransitionTypes(transitionTypes).setLoiteringDelay(5000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE).build()
    }

    fun getIntentPending(markerList: ArrayList<Marker>): PendingIntent? {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        val bundle = Bundle()
        bundle.putParcelableArrayList("marker", markerList)
        intent.putExtra("data", bundle)
        pendingIntent =
            PendingIntent.getBroadcast(
                this, 2607, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        return pendingIntent
    }

    fun getErrorString(e: Exception): String? {
        if (e is ApiException) {
            when (e.statusCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> return "GEOFENCE_NOT_AVAILABLE"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> return "GEOFENCE_TOO_MANY_GEOFENCES"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> return "GEOFENCE_TOO_MANY_PENDING_INTENTS"
            }
        }
        return e.localizedMessage
    }

    fun clearGeofenceList() {
        geofenceList.clear()
    }
}
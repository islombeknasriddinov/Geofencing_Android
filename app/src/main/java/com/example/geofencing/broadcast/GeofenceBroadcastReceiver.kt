package com.example.geofencing.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.geofencing.activity.MapsActivity
import com.example.geofencing.helper.NotificationHelper
import com.example.geofencing.model.Marker
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val TAG = GeofenceBroadcastReceiver::class.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        val notificationHelper: NotificationHelper = NotificationHelper(context)
        val geofencingEvent: GeofencingEvent = GeofencingEvent.fromIntent(intent)
        val markerList = intent.extras?.getBundle("data")
            ?.getParcelableArrayList<Marker>("marker") as ArrayList<Marker>

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...")
            return
        }
        val geofenceList = geofencingEvent.triggeringGeofences
        Log.d(TAG, geofenceList.size.toString())

        val transitionType = geofencingEvent.geofenceTransition

        for (geofence in geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.requestId)
            val marker: Marker? = markerList.firstOrNull { it.markerId == geofence.requestId }
            if (marker != null) {
                showNotification(context, notificationHelper, marker, transitionType)
            }
        }
    }

    private fun showNotification(
        context: Context,
        notificationHelper: NotificationHelper,
        marker: Marker,
        transitionType: Int
    ) {
        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Toast.makeText(context, marker.enter, Toast.LENGTH_SHORT).show()
                notificationHelper.sendHighPriorityNotification(
                    marker.enter, "", MapsActivity::class.java
                )
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Toast.makeText(context, marker.dwell, Toast.LENGTH_SHORT).show()
                notificationHelper.sendHighPriorityNotification(
                    marker.dwell, "", MapsActivity::class.java
                )
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Toast.makeText(context, marker.exit, Toast.LENGTH_SHORT).show()
                notificationHelper.sendHighPriorityNotification(
                    marker.exit, "", MapsActivity::class.java
                )
            }
        }
    }
}
package com.example.geofencing.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.geofencing.activity.MapsActivity
import com.example.geofencing.helper.NotificationHelper
import com.example.geofencing.model.MyMarker
import com.example.geofencing.util.customGetParcelable
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val TAG = GeofenceBroadcastReceiver::class.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        val notificationHelper = NotificationHelper(context)
        val geofencingEvent: GeofencingEvent? = GeofencingEvent.fromIntent(intent)
        val myMarkerLists = intent.extras?.getBundle("my_usha_data")
            ?.customGetParcelable<MyMarker>("marker") as ArrayList<MyMarker>

        if (geofencingEvent?.hasError() == true) {
            Log.d(TAG, "onReceive: Error receiving geofence event...")
            return
        }
        val geofenceList = geofencingEvent?.triggeringGeofences ?: emptyList()

        val r = geofencingEvent?.triggeringLocation
        Log.d(TAG, "onReceive: $r")

        Log.d(TAG, geofenceList.size.toString())

        val transitionType = geofencingEvent?.geofenceTransition ?: -1


        for (geofence in geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.requestId)
            val myMarker: MyMarker? =
                myMarkerLists.firstOrNull { it.markerId == geofence.requestId }
            if (myMarker != null) {
                showNotification(context, notificationHelper, myMarker, transitionType)
            }
        }
    }

    private fun showNotification(
        context: Context,
        notificationHelper: NotificationHelper,
        myMarker: MyMarker,
        transitionType: Int
    ) {
        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Toast.makeText(context, myMarker.enter, Toast.LENGTH_SHORT).show()
                notificationHelper.sendHighPriorityNotification(
                    myMarker.enter, "", MapsActivity::class.java
                )
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Toast.makeText(context, myMarker.dwell, Toast.LENGTH_SHORT).show()
                notificationHelper.sendHighPriorityNotification(
                    myMarker.dwell, "", MapsActivity::class.java
                )
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Toast.makeText(context, myMarker.exit, Toast.LENGTH_SHORT).show()
                notificationHelper.sendHighPriorityNotification(
                    myMarker.exit, "", MapsActivity::class.java
                )
            }
        }
    }
}
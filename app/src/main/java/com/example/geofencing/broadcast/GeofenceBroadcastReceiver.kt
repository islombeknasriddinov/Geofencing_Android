package com.example.geofencing.broadcast

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.geofencing.activity.MapsActivity
import com.example.geofencing.helper.NotificationHelper
import com.example.geofencing.model.Message
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val TAG = GeofenceBroadcastReceiver::class.simpleName

    override fun onReceive(context: Context, intent: Intent) {

        val notificationHelper: NotificationHelper = NotificationHelper(context)
        val geofencingEvent: GeofencingEvent = GeofencingEvent.fromIntent(intent)
        val message = intent.extras?.getBundle("data")?.getSerializable("message") as Message

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...")
            return
        }
        val geofenceList = geofencingEvent.triggeringGeofences
        for (geofence in geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.requestId)
        }
        val transitionType = geofencingEvent.geofenceTransition

        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Toast.makeText(context, message.enter, Toast.LENGTH_SHORT).show()
                notificationHelper.sendHighPriorityNotification(
                    message.enter, "", MapsActivity::class.java
                )
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Toast.makeText(context, message.dwell, Toast.LENGTH_SHORT).show()
                notificationHelper.sendHighPriorityNotification(
                    message.dwell, "", MapsActivity::class.java
                )
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Toast.makeText(context, message.exit, Toast.LENGTH_SHORT).show()
                notificationHelper.sendHighPriorityNotification(
                    message.exit, "", MapsActivity::class.java
                )
            }
        }
    }
}
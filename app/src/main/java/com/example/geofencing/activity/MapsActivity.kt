package com.example.geofencing.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geofencing.R
import com.example.geofencing.adapter.MainAdapter
import com.example.geofencing.helper.GeofenceHelper
import com.example.geofencing.manager.PrefsManager
import com.example.geofencing.model.Message
import com.example.geofencing.util.DialogFragment
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class MapsActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private val TAG = MapsActivity::class.simpleName
    private lateinit var mMap: GoogleMap
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceHelper: GeofenceHelper
    private val dialogFragment = DialogFragment()
    private lateinit var prefsManager: PrefsManager
    private var recyclerView: RecyclerView? = null
    private lateinit var adapter: MainAdapter
    private val GEOFENCE_RADIUS = 200f
    private val GEOFENCE_ID = "SOME_GEOFENCE_ID"
    private val FINE_LOCATION_ACCESS_REQUEST_CODE = 10001
    private val BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        initView()
    }

    private fun initView() {
        prefsManager = PrefsManager.getInstance(this)!!
        val mapFragment: SupportMapFragment? =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        geofenceHelper = GeofenceHelper(this)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        refreshStoryAdapter(getAllItems())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        enableUserLocation()
        mMap.setOnMapLongClickListener(this)
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                //We need to show user a dialog for displaying why the permission is needed and then ask for the permission...
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_ACCESS_REQUEST_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_ACCESS_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(
                        this,
                        "Permission: ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION has not Granted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                mMap.isMyLocationEnabled = true
            } else {
                //We do not have the permission..
            }
        }
        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show()
            } else {
                //We do not have the permission..
                Toast.makeText(
                    this,
                    "Background location access is neccessary for geofences to trigger...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onMapLongClick(latLng: LatLng) {
        if (Build.VERSION.SDK_INT >= 29) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                handleMapLongClick(latLng)
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                ) {
                    //We show a dialog and ask for permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_ACCESS_REQUEST_CODE
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_ACCESS_REQUEST_CODE
                    )
                }
            }
        } else {
            handleMapLongClick(latLng)
        }
    }

    private fun handleMapLongClick(latLng: LatLng) {
        dialogFragment.show(supportFragmentManager, null)

        dialogFragment.saveClick = {
            mMap.clear()
            addMarker(latLng)
            addCircle(latLng, it.radius ?: GEOFENCE_RADIUS)
            addGeofence(latLng, it.radius ?: GEOFENCE_RADIUS, it)

            adapter.addLocation(latLng)
        }


    }

    @SuppressLint("VisibleForTests")
    private fun addGeofence(latLng: LatLng, newRadius: Float, message: Message) {
        val geofence: Geofence = geofenceHelper.getGeofence(
            GEOFENCE_ID,
            latLng,
            newRadius,
            Geofence.GEOFENCE_TRANSITION_ENTER or
                    Geofence.GEOFENCE_TRANSITION_DWELL or
                    Geofence.GEOFENCE_TRANSITION_EXIT
        )
        val geofencingRequest: GeofencingRequest = geofenceHelper.getGeofencingRequest(geofence)
        val pendingIntent: PendingIntent? = geofenceHelper.getIntentPending(message)
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                this,
                "Permission: ACCESS_FINE_LOCATION has not Granted",
                Toast.LENGTH_SHORT
            ).show()
        }

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                Log.d(
                    TAG, "onSuccess: Geofence Added..."
                )
            }.addOnFailureListener { e ->
                val errorMessage: String? = geofenceHelper.getErrorString(e)
                Log.d(TAG, "onFailure: $errorMessage")
            }

    }

    private fun addMarker(latLng: LatLng) {
        val markerOptions: MarkerOptions = MarkerOptions().position(latLng)
        mMap.addMarker(markerOptions)
    }

    private fun addCircle(latLng: LatLng, newRadius: Float) {
        val circleOptions = CircleOptions()
        circleOptions.center(latLng)
        circleOptions.radius(newRadius.toDouble())
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0))
        circleOptions.fillColor(Color.argb(64, 255, 0, 0))
        circleOptions.strokeWidth(4F)
        mMap.addCircle(circleOptions)
    }

    private fun getAllItems(): ArrayList<String> {
        val type: Type = object : TypeToken<ArrayList<String>>() {}.type
        return prefsManager.getArrayList<String>(PrefsManager.KEY_LIST, type)
    }


    private fun refreshStoryAdapter(items: ArrayList<String>) {
        adapter = MainAdapter(this, items)
        recyclerView?.adapter = adapter
    }
}
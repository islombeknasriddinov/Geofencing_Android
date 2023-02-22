package com.example.geofencing.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geofencing.R
import com.example.geofencing.adapter.MainAdapter
import com.example.geofencing.fragment.DialogFragment
import com.example.geofencing.helper.GeofenceHelper
import com.example.geofencing.manager.PrefsManager
import com.example.geofencing.model.MyMarker
import com.example.geofencing.util.buildCircle
import com.example.geofencing.util.buildMarkerIcon
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class MapsActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private val TAG = MapsActivity::class.simpleName

    private lateinit var mMap: GoogleMap
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceHelper: GeofenceHelper
    private lateinit var prefsManager: PrefsManager
    private var recyclerView: RecyclerView? = null
    private var adapter: MainAdapter? = null
    private val myMarkerList: ArrayList<MyMarker> = ArrayList()

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
        clearButton()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        refreshStoryAdapter(getAllItems())
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        enableUserLocation()
        mMap.setOnMapLongClickListener(this)

        loadDates()
    }

    private fun loadDates() {
        if (getAllItems().isNotEmpty()) {
            myMarkerList.addAll(getAllItems())
            addGeofence(myMarkerList)
            for (marker in myMarkerList) {
                addMarker(marker)
            }
        }
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
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
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        val dialogFragment = DialogFragment()

        dialogFragment.show(supportFragmentManager, null)
        dialogFragment.setCurrentLatLng(latLng)
        dialogFragment.saveClick = { marker ->
            myMarkerList.add(marker)
            addMarker(marker)
            addGeofence(myMarkerList)

            adapter?.addLocation(marker)
        }


    }

    @SuppressLint("VisibleForTests")
    private fun addGeofence(myMarkerList: ArrayList<MyMarker>) {
        val geofencingRequest: GeofencingRequest = geofenceHelper.getGeofencingRequest(
            myMarkerList,
            Geofence.GEOFENCE_TRANSITION_ENTER or
                    Geofence.GEOFENCE_TRANSITION_DWELL or
                    Geofence.GEOFENCE_TRANSITION_EXIT
        )

        val pendingIntent: PendingIntent = geofenceHelper.getIntentPending(myMarkerList)
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                this, "Permission: ACCESS_FINE_LOCATION has not Granted", Toast.LENGTH_SHORT
            ).show()
        }

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "onSuccess: Geofence Added...")

            }.addOnFailureListener { e ->
                val errorMessage: String? = geofenceHelper.getErrorString(e)
                Log.d(TAG, "onFailure: $errorMessage")
            }

    }

    private fun addMarker(myMarker: MyMarker) {
        mMap.addMarker(buildMarkerIcon(myMarker))
        mMap.addCircle(buildCircle(myMarker))
    }

    private fun getAllItems(): ArrayList<MyMarker> {
        val type: Type = object : TypeToken<ArrayList<MyMarker>>() {}.type
        return prefsManager.getArrayList(PrefsManager.KEY_LIST, type)
    }

    private fun refreshStoryAdapter(items: ArrayList<MyMarker>) {
        adapter = MainAdapter(this, items)
        recyclerView?.adapter = adapter
    }

    private fun clearButton() {
        val clearMap = findViewById<LinearLayout>(R.id.ll_clean)
        clearMap.setOnClickListener {
            mMap.clear()
            adapter?.clearHistory()
            myMarkerList.clear()
            geofenceHelper.clearGeofenceList()
            refreshStoryAdapter(getAllItems())
        }
    }
}
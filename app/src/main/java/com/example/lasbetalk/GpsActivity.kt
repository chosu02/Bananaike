package com.example.lasbetalk

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.OnSuccessListener

class GpsActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    lateinit var providerClient: FusedLocationProviderClient
    lateinit var apiClient: GoogleApiClient
    var googleMap: GoogleMap? = null
    var currentLocation: Location? = null
    var previousLocation: Location? = null
    lateinit var locationCallback: LocationCallback
    var polyline: Polyline? = null
    var currentMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it.all { permission -> permission.value == true }) {
                apiClient.connect()
            } else {
                Toast.makeText(this, "권한 거부..", Toast.LENGTH_SHORT).show()
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        providerClient = LocationServices.getFusedLocationProviderClient(this)
        apiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else {
            apiClient.connect()
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateLocation(location)
                }
            }
        }
    }

    private fun updateLocation(location: Location) {
        previousLocation = currentLocation
        currentLocation = location

        if (googleMap != null) {
            val latLng = LatLng(location.latitude, location.longitude)
            moveMap(latLng)

            previousLocation?.let {
                val previousLatLng = LatLng(it.latitude, it.longitude)
                if (polyline == null) {
                    polyline = googleMap?.addPolyline(
                        PolylineOptions().add(previousLatLng, latLng).color(android.graphics.Color.BLUE)
                    )
                } else {
                    val points = polyline?.points?.toMutableList() ?: mutableListOf()
                    points.add(latLng)
                    polyline?.points = points
                }
            }
        }
    }

    private fun moveMap(location: LatLng) {
        googleMap?.let { map ->
            val position: CameraPosition = CameraPosition.Builder()
                .target(location)
                .zoom(18f)
                .build()
            map.moveCamera(CameraUpdateFactory.newCameraPosition(position))

            if (currentMarker == null) {
                val markerOptions = MarkerOptions()
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                markerOptions.position(location)
                markerOptions.title("MyLocation")
                currentMarker = map.addMarker(markerOptions)
            } else {
                currentMarker?.position = location
            }
        } ?: run {
            Log.e("GpsActivity", "GoogleMap 객체가 초기화되지 않았습니다.")
        }
    }

    override fun onConnected(p0: Bundle?) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            providerClient.lastLocation.addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                location?.let {
                    currentLocation = it
                    val latLng = LatLng(it.latitude, it.longitude)
                    moveMap(latLng)
                }
            })

            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            providerClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        // 위치 제공자를 사용할 수 없을 때
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        // 사용할 수 있는 위치 제공자가 없을 때
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        currentLocation?.let {
            val latLng = LatLng(it.latitude, it.longitude)
            moveMap(latLng)
        }
    }
}


/*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class GpsActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    lateinit var providerClient: FusedLocationProviderClient
    lateinit var apiClient: GoogleApiClient
    var googleMap: GoogleMap? = null
    private lateinit var locationCallback: LocationCallback
    private val path = mutableListOf<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it.all { permission -> permission.value == true }) {
                apiClient.connect()
            } else {
                Toast.makeText(this, "권한 거부..", Toast.LENGTH_SHORT).show()
            }
        }

        (supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?)!!.getMapAsync(this)

        providerClient = LocationServices.getFusedLocationProviderClient(this)
        apiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            )
        } else {
            apiClient.connect()
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    path.add(latLng)
                    drawPolyline()
                }
            }
        }
    }

    private fun drawPolyline() {
        googleMap?.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(path)
                .color(ContextCompat.getColor(this, R.color.purple_700))
        )
    }

    private fun moveMap(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        val position: CameraPosition = CameraPosition.Builder()
            .target(latLng)
            .zoom(16f)
            .build()

        googleMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(position))

        val markerOptions = MarkerOptions()
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .position(latLng)
            .title("MyLocation")

        googleMap?.addMarker(markerOptions)
    }

    override fun onConnected(p0: Bundle?) {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            providerClient.lastLocation.addOnSuccessListener(this) { location ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    Log.d("noh", "$latitude, $longitude")
                    moveMap(latitude, longitude)
                }
            }

            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            providerClient.requestLocationUpdates(locationRequest, locationCallback, null)
            apiClient.disconnect()
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        // 위치 제공자를 사용할 수 없을 때
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        // 사용할 수 있는 위치 제공자가 없을 때
    }

    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0
    }
}
*/
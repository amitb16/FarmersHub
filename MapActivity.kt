package com.example.farmershub

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Set up the map fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Set Nashik as the center location
        val nashikLatLng = LatLng(20.0114, 73.7903)  // Nashik coordinates
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nashikLatLng, 12f)) // Zoom level 12
        googleMap.addMarker(MarkerOptions().position(nashikLatLng).title("Nashik"))

        // List of markets in Nashik with their LatLng
        val markets = listOf(
            Pair("Saraf Bazar", LatLng(20.0069, 73.7885)),
            Pair("Panchavati Market", LatLng(20.0124, 73.7938)),
            Pair("Ashok Stambh Market", LatLng(19.9982, 73.7764)),
            Pair("Canada Corner Market", LatLng(19.9877, 73.7769)),
            Pair("Main Road Market", LatLng(19.9980, 73.7869)),
            Pair("Deolali Camp Market", LatLng(19.9443, 73.8344)),
            Pair("Old Nashik Market", LatLng(20.0017, 73.7903))
        )

        // Add markers for each market in the list
        for (market in markets) {
            googleMap.addMarker(MarkerOptions()
                .position(market.second)
                .title(market.first))  // Add title (market name) to each marker
        }

        // Optionally enable location services if needed
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        } else {
            // Request permissions if they are not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1000
            )
        }
    }

    // Handle permission result (if necessary for location features)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1000) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, enable location
                googleMap.isMyLocationEnabled = true
            } else {
                // Permissions denied, show a message
                Toast.makeText(this, "Location permission required to display your location", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

package com.example.farmershub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Methods : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_methods)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up click listeners for each "Learn More" button
        setupButtonClickListeners()
    }

    private fun setupButtonClickListeners() {
        // Find all buttons by their IDs
        val organicFarmingButton = findViewById<Button>(R.id.btn_organic_farming)
        val hydroponicsButton = findViewById<Button>(R.id.btn_hydroponics)
        val dripIrrigationButton = findViewById<Button>(R.id.btn_drip_irrigation)
        val sprinklerButton = findViewById<Button>(R.id.btn_sprinkler)
        val floodIrrigationButton = findViewById<Button>(R.id.btn_flood_irrigation)

        // Set up click listeners to open YouTube videos
        organicFarmingButton.setOnClickListener {
            openYouTubeUrl("https://youtu.be/1nw3eX2MTsg?si=fSQ68NEDx_mXr-do") // Organic Farming Explained
        }

        hydroponicsButton.setOnClickListener {
            openYouTubeUrl("https://youtu.be/dZVyH_1oPN4?si=6vke44KeCADQ0ITz") // Introduction to Hydroponic Farming
        }

        dripIrrigationButton.setOnClickListener {
            openYouTubeUrl("https://youtu.be/eA96FaQ1diA?si=CIdWFNRhvOVmvgl3") // Drip Irrigation Explained
        }

        sprinklerButton.setOnClickListener {
            openYouTubeUrl("https://youtu.be/TUcTdkCwJxk?si=IO8qF525Hz-9YsI9") // Sprinkler Irrigation Systems
        }

        floodIrrigationButton.setOnClickListener {
            openYouTubeUrl("https://youtu.be/LgDS5l7funs?si=wF1_DGtSJmlPbqON") // Flood Irrigation Methods
        }
    }

    /**
     * Opens a YouTube video with the specified URL
     * @param url The complete YouTube URL
     */
    private fun openYouTubeUrl(url: String) {
        try {
            // Parse the URL
            val videoUri = Uri.parse(url)

            // First try to open in YouTube app
            val appIntent = Intent(Intent.ACTION_VIEW, videoUri)
            appIntent.setPackage("com.google.android.youtube")

            // If YouTube app isn't available, open in browser
            try {
                startActivity(appIntent)
            } catch (ex: Exception) {
                // YouTube app not found, use web browser
                val webIntent = Intent(Intent.ACTION_VIEW, videoUri)
                startActivity(webIntent)
            }
        } catch (e: Exception) {
            // Handle potential errors
            e.printStackTrace()
        }
    }
}
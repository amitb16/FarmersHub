package com.example.farmershub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)

        // Set up edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dashboard"

        // Set up bottom navigation
        setupBottomNavigation()

        // Set up quick links buttons
        setupQuickLinksButtons()
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home page
                    true
                }
                R.id.nav_profile -> {
                    // Navigate to profile page
                    val intent = Intent(this, ProfilePage::class.java)
                    startActivity(intent)
                    // Don't finish this activity
                    true
                }
                R.id.nav_weather -> {
                    // Navigate to weather page
                    val intent = Intent(this, About::class.java)
                    startActivity(intent)
                    // Don't finish this activity
                    true
                }
                else -> false
            }
        }

        // Set the home item as selected by default
        bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun setupQuickLinksButtons() {
        // Market Prices Button
        findViewById<MaterialButton>(R.id.btnMarketPrices).setOnClickListener {
            val intent = Intent(this, MarketPrices::class.java)
            startActivity(intent)
            // Don't finish this activity
        }

        // Schemes Button
        findViewById<MaterialButton>(R.id.btnSchemes).setOnClickListener {
            val intent = Intent(this, Schemes::class.java)
            startActivity(intent)
            // Don't finish this activity
        }

        // Methods Button
        findViewById<MaterialButton>(R.id.btnMethods).setOnClickListener {
            val intent = Intent(this, Methods::class.java)
            startActivity(intent)
            // Don't finish this activity
        }

        // Market Places Button
        findViewById<MaterialButton>(R.id.btnMarketPlaces).setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
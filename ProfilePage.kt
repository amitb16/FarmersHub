package com.example.farmershub

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfilePage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Activity result launcher for edit profile
    private val editProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Profile was updated, reload user data
            loadUserData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set up the toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Profile"

        // Set up bottom navigation
        setupBottomNavigation()

        // Load user data
        loadUserData()

        // Set up edit profile button - FIXED
        findViewById<MaterialButton>(R.id.btnEditProfile).setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)  // ← Fixed class name
            editProfileLauncher.launch(intent)  // ← Use launcher to handle result
        }

        // Set up logout button
        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomePage::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    // Already on profile page
                    true
                }
                R.id.nav_weather -> {
                    val intent = Intent(this, About::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        // Set the profile item as selected
        bottomNavigation.selectedItemId = R.id.nav_profile
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            // Get user data from Firestore
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Get user data
                        val username = document.getString("username") ?: "User"
                        val email = document.getString("email") ?: "No Email"
                        val phone = document.getString("phone") ?: "No Phone"
                        val createdAt = document.getLong("createdAt")

                        // Set the data to UI
                        findViewById<android.widget.TextView>(R.id.tvUserName).text = username
                        findViewById<android.widget.TextView>(R.id.tvUserEmail).text = email
                        findViewById<android.widget.TextView>(R.id.tvPhone).text = phone

                        // Format and set the join date
                        if (createdAt != null) {
                            val date = Date(createdAt)
                            val formatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                            val formattedDate = formatter.format(date)
                            findViewById<android.widget.TextView>(R.id.tvJoinDate).text = formattedDate
                        } else {
                            findViewById<android.widget.TextView>(R.id.tvJoinDate).text = "Unknown"
                        }
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // If not logged in, redirect to login
            Toast.makeText(this, "Please log in to view profile", Toast.LENGTH_SHORT).show()
            redirectToLogin()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
        auth.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        redirectToLogin()
    }

    private fun redirectToLogin() {
        // Assuming you have a LoginActivity
        val intent = Intent(this, LoginPage::class.java)
        // Clear the back stack so user can't go back after logout
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
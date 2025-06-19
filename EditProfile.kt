package com.example.farmershub

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfile : AppCompatActivity() {

    // Firebase instances
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // UI Components
    private lateinit var toolbar: Toolbar
    private lateinit var profileImage: ImageView
    private lateinit var tilName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPhone: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton

    // Data variables
    private var hasChanges = false
    private var originalData = ProfileData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initializeViews()
        setupToolbar()
        loadCurrentUserData()
        setupClickListeners()
        setupTextWatchers()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        profileImage = findViewById(R.id.profileImage)
        tilName = findViewById(R.id.tilName)
        tilEmail = findViewById(R.id.tilEmail)
        tilPhone = findViewById(R.id.tilPhone)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        }
        toolbar.setNavigationOnClickListener {
            if (hasChanges) {
                showUnsavedChangesDialog()
            } else {
                finish()
            }
        }
    }

    private fun loadCurrentUserData() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            // Get user data from Firestore
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Get user data
                        val username = document.getString("username") ?: ""
                        val email = document.getString("email") ?: ""
                        val phone = document.getString("phone") ?: ""

                        originalData = ProfileData(
                            name = username,
                            email = email,
                            phone = phone
                        )

                        // Populate the fields
                        etName.setText(originalData.name)
                        etEmail.setText(originalData.email)
                        etPhone.setText(originalData.phone)

                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // If not logged in, redirect to login
            Toast.makeText(this, "Please log in to edit profile", Toast.LENGTH_SHORT).show()
            redirectToLogin()
        }
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            if (validateInputs()) {
                saveProfile()
            }
        }

        btnCancel.setOnClickListener {
            if (hasChanges) {
                showUnsavedChangesDialog()
            } else {
                finish()
            }
        }
    }

    private fun setupTextWatchers() {
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                checkForChanges()
            }
        }

        etName.addTextChangedListener(textWatcher)
        etEmail.addTextChangedListener(textWatcher)
        etPhone.addTextChangedListener(textWatcher)
    }

    private fun checkForChanges() {
        val currentData = getCurrentFormData()
        hasChanges = currentData != originalData
    }

    private fun getCurrentFormData(): ProfileData {
        return ProfileData(
            name = etName.text.toString().trim(),
            email = etEmail.text.toString().trim(),
            phone = etPhone.text.toString().trim()
        )
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Clear previous errors
        tilName.error = null
        tilEmail.error = null
        tilPhone.error = null

        // Validate name
        if (etName.text.toString().trim().isEmpty()) {
            tilName.error = "Name is required"
            isValid = false
        }

        // Validate email
        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            tilEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Invalid email format"
            isValid = false
        }

        // Validate phone
        val phone = etPhone.text.toString().trim()
        if (phone.isEmpty()) {
            tilPhone.error = "Phone number is required"
            isValid = false
        } else if (phone.length < 10) {
            tilPhone.error = "Phone number must be at least 10 digits"
            isValid = false
        }

        return isValid
    }

    private fun saveProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading state
        btnSave.isEnabled = false
        btnSave.text = "Saving..."

        val userId = currentUser.uid
        val currentData = getCurrentFormData()

        // Create update map
        val updates = hashMapOf<String, Any>(
            "username" to currentData.name,
            "email" to currentData.email,
            "phone" to currentData.phone,
            "updatedAt" to System.currentTimeMillis()
        )

        // Update Firestore
        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                // Reset button state
                btnSave.isEnabled = true
                btnSave.text = "Save Changes"

                // Show success message
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()

                // Return result to calling activity
                val resultIntent = Intent().apply {
                    putExtra("profile_updated", true)
                    putExtra("name", currentData.name)
                    putExtra("email", currentData.email)
                    putExtra("phone", currentData.phone)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            .addOnFailureListener { e ->
                // Reset button state
                btnSave.isEnabled = true
                btnSave.text = "Save Changes"

                Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showUnsavedChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Unsaved Changes")
            .setMessage("You have unsaved changes. Are you sure you want to leave?")
            .setPositiveButton("Leave") { _, _ -> finish() }
            .setNegativeButton("Stay", null)
            .show()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginPage::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (hasChanges) {
            showUnsavedChangesDialog()
        } else {
            super.onBackPressed()
        }
    }

    // Data class to hold profile information
    data class ProfileData(
        val name: String = "",
        val email: String = "",
        val phone: String = ""
    )
}
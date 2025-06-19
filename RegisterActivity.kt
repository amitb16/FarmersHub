//package com.example.farmershub
//
//import android.os.Bundle
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//
//class RegisterActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_sigup)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }
//}
package com.example.farmershub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.farmershub.databinding.ActivitySigupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySigupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivitySigupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        db = Firebase.firestore

        // Set up register button click listener
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        // Set up login link click listener
        binding.tvLoginLink.setOnClickListener {
            // Navigate to Login Activity
            startActivity(Intent(this, LoginPage::class.java))
            finish()
        }
    }

    private fun registerUser() {
        // Get input values
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validate input fields
        if (validateInputs(username, email, phone, password)) {
            // Show progress bar
            binding.progressBar.visibility = View.VISIBLE

            // Create user with email and password
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign up success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser

                        // Store additional user information in Firestore
                        storeUserData(user?.uid, username, email, phone)
                    } else {
                        // If sign up fails, display a message to the user
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        binding.progressBar.visibility = View.GONE

                        // Handle specific exceptions with custom messages
                        when (task.exception) {
                            is FirebaseAuthUserCollisionException -> {
                                Toast.makeText(this, "Email already in use. Please try another email.",
                                    Toast.LENGTH_SHORT).show()
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                Toast.makeText(this, "Invalid email format. Please enter a valid email.",
                                    Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this, "Registration failed: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }
    }

    private fun storeUserData(userId: String?, username: String, email: String, phone: String) {
        if (userId == null) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create user data map
        val user = hashMapOf(
            "username" to username,
            "email" to email,
            "phone" to phone,
            "createdAt" to System.currentTimeMillis()
        )

        // Add user to Firestore
        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "User data stored successfully")
                binding.progressBar.visibility = View.GONE

                // Show success message
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                // Navigate to main activity or login
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error storing user data", e)
                binding.progressBar.visibility = View.GONE

                // Show error message
                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateInputs(username: String, email: String, phone: String, password: String): Boolean {
        if (username.isEmpty()) {
            binding.tilUsername.error = "Username cannot be empty"
            binding.etUsername.requestFocus()
            return false
        } else {
            binding.tilUsername.error = null
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email cannot be empty"
            binding.etEmail.requestFocus()
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email address"
            binding.etEmail.requestFocus()
            return false
        } else {
            binding.tilEmail.error = null
        }

        if (phone.isEmpty()) {
            binding.tilPhone.error = "Phone number cannot be empty"
            binding.etPhone.requestFocus()
            return false
        } else if (phone.length < 10) {
            binding.tilPhone.error = "Please enter a valid phone number"
            binding.etPhone.requestFocus()
            return false
        } else {
            binding.tilPhone.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password cannot be empty"
            binding.etPassword.requestFocus()
            return false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            binding.etPassword.requestFocus()
            return false
        } else {
            binding.tilPassword.error = null
        }

        return true
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, LoginPage::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}

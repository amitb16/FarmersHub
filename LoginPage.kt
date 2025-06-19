//package com.example.farmershub
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import android.widget.TextView
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//
//class LoginPage : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_login_page)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//        val getStartedButton = findViewById<TextView>(R.id.tvRegister)
//
//        getStartedButton.setOnClickListener {
//            val intent = Intent(this, RegisterActivity::class.java)
//            startActivity(intent)
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.farmershub.databinding.ActivityLoginPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginPage : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPageBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginpage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize view binding
        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Check if user is already signed in
        checkCurrentUser()

        // Set up login button click listener
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        // Set up register text click listener
        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Set up forgot password text click listener
        binding.tvForgotPassword.setOnClickListener {
            forgotPassword()
        }
    }

    private fun checkCurrentUser() {
        // Check if user is signed in (non-null)
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already signed in, redirect to MainActivity
            navigateToMain()
        }
    }

    private fun loginUser() {
        // Get email and password from input fields
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validate input fields
        if (validateInputs(email, password)) {
            // Show progress indicator (you might want to add a ProgressBar to your layout)
            showLoading(true)

            // Sign in with email and password
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    showLoading(false)

                    if (task.isSuccessful) {
                        // Sign in success
                        Log.d(TAG, "signInWithEmail:success")
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        navigateToMain()
                    } else {
                        // If sign in fails, display a message to the user
                        Log.w(TAG, "signInWithEmail:failure", task.exception)

                        // Handle specific exceptions
                        when (task.exception) {
                            is FirebaseAuthInvalidUserException -> {
                                // Email doesn't exist
                                Toast.makeText(this, "No account exists with this email",
                                    Toast.LENGTH_SHORT).show()
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                // Wrong password
                                Toast.makeText(this, "Invalid email or password",
                                    Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                // Generic error
                                Toast.makeText(this, "Authentication failed: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
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

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password cannot be empty"
            binding.etPassword.requestFocus()
            return false
        } else {
            binding.tilPassword.error = null
        }

        return true
    }

    private fun forgotPassword() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = "Please enter your email address"
            binding.etEmail.requestFocus()
            return
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email address"
            binding.etEmail.requestFocus()
            return
        }

        showLoading(true)

        // Send password reset email
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent to $email",
                        Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Failed to send reset email: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMain() {
        val intent = Intent(this, HomePage::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {

        // You might want to add a ProgressBar to your layout with id progressBar
        // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

        // Disable UI interaction during loading
        binding.btnLogin.isEnabled = !isLoading
        binding.tilEmail.isEnabled = !isLoading
        binding.tilPassword.isEnabled = !isLoading
        binding.tvForgotPassword.isEnabled = !isLoading
        binding.tvRegister.isEnabled = !isLoading
    }

    companion object {
        private const val TAG = "LoginPage"
    }
}

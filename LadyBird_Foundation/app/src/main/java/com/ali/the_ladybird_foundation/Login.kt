package com.ali.the_ladybird_foundation

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Logging in...")
            setCancelable(false)
        }

        val emailEditText: EditText = findViewById(R.id.login_email)
        val passwordEditText: EditText = findViewById(R.id.login_password)
        val loginButton: Button = findViewById(R.id.login_logBtn)
        val registerButton: Button = findViewById(R.id.login_regBtn)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showSnackbar("Please fill in all fields")
            } else {
                loginUser(email, password)
            }
        }

        registerButton.setOnClickListener {
            val intentRegister = Intent(this, Regsiter::class.java)
            startActivity(intentRegister)
        }
    }

    private fun loginUser(email: String, password: String) {
        progressDialog.show()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    // Login successful
                    showSnackbar("Login successful")
                    val intentDonation = Intent(this, Donate::class.java)
                    startActivity(intentDonation)
                    finish() // Optional: finish the login activity so the user can't go back to it
                } else {
                    // If login fails
                    showSnackbar("Login failed: ${task.exception?.message}")
                }
            }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }
}

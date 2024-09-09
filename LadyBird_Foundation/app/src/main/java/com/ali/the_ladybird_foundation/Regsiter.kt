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

class Regsiter : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var login: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regsiter)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Registering...")
            setCancelable(false)
        }

        val emailEditText: EditText = findViewById(R.id.register_email)
        val passwordEditText: EditText = findViewById(R.id.register_password)
        val confirmPasswordEditText: EditText = findViewById(R.id.register_confirmPassword)
        val registerButton: Button = findViewById(R.id.register_regBtn)
        login = findViewById(R.id.register_loginBtn)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showSnackbar("Please fill in all fields")
            } else if (password != confirmPassword) {
                showSnackbar("Passwords do not match")
            } else {
                registerUser(email, password)
            }
        }

        login.setOnClickListener {
            val intentLogin = Intent(this, Login::class.java)
            startActivity(intentLogin)
        }
    }

    private fun registerUser(email: String, password: String) {
        progressDialog.show()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    // Registration successful
                    showSnackbar("Registration successful")
                    showToast("Welcome! Registration successful.")
                    val intentLogin = Intent(this, Login::class.java)
                    startActivity(intentLogin)
                } else {
                    // If registration fails
                    showSnackbar("Registration failed: ${task.exception?.message}")
                }
            }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

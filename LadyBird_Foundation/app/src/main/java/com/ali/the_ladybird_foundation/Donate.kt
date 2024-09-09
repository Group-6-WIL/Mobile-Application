package com.ali.the_ladybird_foundation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Donate : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if the user is logged in
        if (auth.currentUser == null) {
            // User is not logged in, redirect to Login activity
            val intentLogin = Intent(this, Login::class.java)
            startActivity(intentLogin)
            finish() // Optionally finish the current activity to prevent back navigation
        }
    }
}

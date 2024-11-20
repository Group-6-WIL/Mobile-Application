package com.ali.the_ladybird_foundation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

class Home : AppCompatActivity() {

    private lateinit var homeBtn: ImageView
    private lateinit var aboutBtn: ImageView
    private lateinit var donateBtn: ImageView
    private lateinit var locationBtn : ImageView
    private lateinit var eventsBtn: ImageView
    private lateinit var contactBtn : ImageView
    private lateinit var ladybirdIcon: ImageView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        homeBtn = findViewById(R.id.home_homeImg)
        aboutBtn = findViewById(R.id.home_aboutUsImg)
        donateBtn = findViewById(R.id.home_donationImg)
        locationBtn = findViewById(R.id.home_locationImg)
        eventsBtn = findViewById(R.id.home_eventsImg)
        ladybirdIcon = findViewById(R.id.home_adminDashboard)
        contactBtn = findViewById(R.id.home_contactImg)

        aboutBtn.setOnClickListener {
            val intentAbout = Intent(this, AboutUs::class.java)
            startActivity(intentAbout)
        }

        donateBtn.setOnClickListener {
            val intentDonate = Intent(this, Donate::class.java)
            startActivity(intentDonate)
        }

        locationBtn.setOnClickListener {
            val intentLocation = Intent(this, Location::class.java)
            startActivity(intentLocation)
        }

        eventsBtn.setOnClickListener {
            val intentEvent = Intent(this, Events::class.java)
            startActivity(intentEvent)
        }

        ladybirdIcon.setOnClickListener {
            val intentAdmin = Intent(this, Admin_Dashboard::class.java)
            startActivity(intentAdmin)
        }

        contactBtn.setOnClickListener {
            val intentcONTACT = Intent(this, Contact_Us::class.java)
            startActivity(intentcONTACT)
        }


// Inside your onCreate method
        ladybirdIcon.setOnClickListener {
            showAdminPasswordDialog()
        }
    }
    // New method to show the password prompt dialog
    private fun showAdminPasswordDialog() {
        // Inflate the custom dialog layout
        val dialogView = layoutInflater.inflate(R.layout.admin_password, null)

        val passwordField = dialogView.findViewById<EditText>(R.id.adminPassword)
        val enterButton = dialogView.findViewById<Button>(R.id.admin_add_events_uploadIV)

        // Create an AlertDialog with the custom layout
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        enterButton.setOnClickListener {
            val enteredPassword = passwordField.text.toString()

            // Check if the entered password is correct
            if (enteredPassword == "TheLadyB1rdF0undation") {  // Replace with your desired password
                Toast.makeText(this, "Access Granted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()

                // Navigate to Admin Dashboard
                val intentAdmin = Intent(this, Admin_Dashboard::class.java)
                startActivity(intentAdmin)
            } else {
                Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
            }
        }

        // Show the dialog
        dialog.show()
    }

    fun openPopiaWebsite(view: android.view.View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://popia.co.za/"))
        startActivity(browserIntent)
    }
}

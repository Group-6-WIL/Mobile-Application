package com.ali.the_ladybird_foundation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Home : AppCompatActivity() {

    private lateinit var homeBtn: ImageView
    private lateinit var aboutBtn: ImageView
    private lateinit var donateBtn: ImageView
    private lateinit var locationBtn : ImageView
    private lateinit var eventsBtn: ImageView
    private lateinit var loginBtn: ImageView
    private lateinit var ladybirdIcon: ImageView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        homeBtn = findViewById(R.id.home_homeImg)
        aboutBtn = findViewById(R.id.home_aboutUsImg)
        donateBtn = findViewById(R.id.home_donationImg)
        locationBtn = findViewById(R.id.home_locationImg)
        eventsBtn = findViewById(R.id.home_eventsImg)
        loginBtn = findViewById(R.id.home_loginoutImg)
        ladybirdIcon = findViewById(R.id.home_adminDashboard)

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

        loginBtn.setOnClickListener {
            showLoginLogoutDialog()
        }

        ladybirdIcon.setOnClickListener {
            val intentAdmin = Intent(this, Admin_Dashboard::class.java)
            startActivity(intentAdmin)
        }
    }

    private fun showLoginLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_login_logout, null)
        val radioGroup: RadioGroup = dialogView.findViewById(R.id.radioGroup)
        val btnConfirm: Button = dialogView.findViewById(R.id.btnConfirm)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Login/Logout")
        builder.setView(dialogView)

        val alertDialog = builder.create()

        btnConfirm.setOnClickListener {
            val selectedRadioButtonId = radioGroup.checkedRadioButtonId
            when (selectedRadioButtonId) {
                R.id.radioLogin -> {
                    // Navigate to Login Activity
                    val intentLogin = Intent(this, Login::class.java)
                    startActivity(intentLogin)
                }
                R.id.radioLogout -> {
                    // Log out the user
                    FirebaseAuth.getInstance().signOut()
                    // Redirect to Home or Login activity
                    Toast.makeText(this, "You have been Succussfully logged out", Toast.LENGTH_SHORT).show()
                    val intentHome = Intent(this, Home::class.java)
                    startActivity(intentHome)
                }
            }
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}

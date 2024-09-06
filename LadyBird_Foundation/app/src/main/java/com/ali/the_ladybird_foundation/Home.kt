package com.ali.the_ladybird_foundation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class Home : AppCompatActivity() {

    private lateinit var homeBtn : ImageView
    private lateinit var aboutBtn : ImageView
    private lateinit var donateBtn : ImageView
    private lateinit var eventsBtn : ImageView
    private lateinit var loginBtn : ImageView
    private lateinit var ladybirdIcon : ImageView //admin login page

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        homeBtn = findViewById(R.id.home_homeImg)
        aboutBtn = findViewById(R.id.home_aboutUsImg)
        donateBtn = findViewById(R.id.home_donationImg)
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

        eventsBtn.setOnClickListener {
            val intentEvent = Intent(this, Events::class.java)
            startActivity(intentEvent)
        }

        loginBtn.setOnClickListener {
            val intentLogin = Intent(this, Login::class.java)
            startActivity(intentLogin)
        }

        ladybirdIcon.setOnClickListener {
            val intentAdmin = Intent(this, Admin_Dashboard::class.java)
            startActivity(intentAdmin)
        }
    }
}
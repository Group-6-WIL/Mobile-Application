package com.ali.the_ladybird_foundation

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {

    private lateinit var LBgif: ImageView
    private val delay: Long = 5000 // Delay time in milliseconds (3 seconds)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        // Reference to the GIF ImageView
        LBgif = findViewById(R.id.main_LBgif)

        // Animate the image using Glide to load the GIF
        Glide.with(this).load(R.drawable.lb1).into(LBgif)

        // Create a Timer object to handle the splash screen duration
        val runSplash = Timer()

        // Task to perform after the splash screen delay
        val showSplash = object : TimerTask() {
            override fun run() {
                // Move to the next screen (HomeActivity)
                val intentOne = Intent(this@MainActivity, Home::class.java)
                startActivity(intentOne)
                finish() // Close the MainActivity
            }
        }

        // Schedule the task to run after the specified delay
        runSplash.schedule(showSplash,delay)
    }
}

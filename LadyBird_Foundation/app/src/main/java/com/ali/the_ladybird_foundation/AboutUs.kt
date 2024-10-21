package com.ali.the_ladybird_foundation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AboutUs : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var currentPage = 0

    //nav bar buttons
    private lateinit var homeBtn : ImageView
    private lateinit var aboutUsBtn : ImageView
    private lateinit var donateBtn : ImageView
    private lateinit var locationBtn : ImageView
    private lateinit var eventsBtn : ImageView
    private lateinit var adminLogin : ImageView
    private lateinit var contactBtn : ImageView

    private lateinit var aboutUsTextView: TextView
    private lateinit var missionTextView: TextView
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about_us) // Corrected layout file

        val imageList = listOf(
            R.drawable.img1,
            R.drawable.img3,
            R.drawable.img4
        )

        viewPager = findViewById(R.id.imageCarousel)
        viewPager.adapter = ImageAdapter(imageList)
        homeBtn = findViewById(R.id.about_homeImg)
        aboutUsBtn = findViewById(R.id.about_aboutUsImg)
        donateBtn = findViewById(R.id.about_donationImg)
        locationBtn = findViewById(R.id.about_locationImg)
        eventsBtn = findViewById(R.id.about_eventsImg)
        adminLogin = findViewById(R.id.about_adminDashboard)
        contactBtn = findViewById(R.id.about_contactImg2)

        aboutUsTextView = findViewById(R.id.textView5) // Your About Us TextView
        missionTextView = findViewById(R.id.textView6)

        homeBtn.setOnClickListener {
            val intentHome = Intent(this, Home::class.java)
            startActivity(intentHome)
        }

        aboutUsBtn.setOnClickListener {
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
            val intentEvents = Intent(this, Events::class.java)
            startActivity(intentEvents)
        }

        adminLogin.setOnClickListener {
            showAdminPasswordDialog()
        }
        contactBtn.setOnClickListener {
            val intentcontact = Intent(this, Contact_Us::class.java)
            startActivity(intentcontact)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.about_root_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up the auto-slide feature
        setupAutoSlide(imageList.size)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Fetch data from Firebase
        fetchDataFromFirebase()

    }

    fun openPopiaWebsite(view: android.view.View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://popia.co.za/"))
        startActivity(browserIntent)
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

    private fun fetchDataFromFirebase() {
        database.child("aboutUs").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseSnapshot", snapshot.value.toString()) // Log the entire snapshot
                if (snapshot.exists()) {
                    val aboutUsContent = snapshot.child("Content").value?.toString() ?: "No content available"
                    val missionContent = snapshot.child("Mission").value?.toString() ?: "No mission statement available"

                    aboutUsTextView.text = aboutUsContent
                    missionTextView.text = missionContent
                } else {
                    Toast.makeText(this@AboutUs, "No data found", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AboutUs, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun setupAutoSlide(itemCount: Int) {
        runnable = Runnable {
            currentPage = (currentPage + 1) % itemCount
            viewPager.setCurrentItem(currentPage, true)
            handler.postDelayed(runnable, 3000) // Slide every 3 seconds
        }
        handler.postDelayed(runnable, 3000) // Initial delay
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) // Clean up the handler when the activity is destroyed
    }

}

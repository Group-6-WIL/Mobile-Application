package com.ali.the_ladybird_foundation

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth

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
    private lateinit var loginOutBtn : ImageView
    private lateinit var adminLogin : ImageView


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
        loginOutBtn = findViewById(R.id.about_loginoutImg)
        adminLogin = findViewById(R.id.about_adminDashboard)


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
            val intentALogin = Intent(this, Admin_Dashboard::class.java)
            startActivity(intentALogin)
        }

        loginOutBtn.setOnClickListener {
            showLoginLogoutDialog()
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.about_root_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up the auto-slide feature
        setupAutoSlide(imageList.size)
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

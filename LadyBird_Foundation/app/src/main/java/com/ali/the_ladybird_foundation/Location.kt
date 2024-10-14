package com.ali.the_ladybird_foundation

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

class Location : AppCompatActivity() {
    private var donationTable: TableLayout? = null
    private var databaseReference: DatabaseReference? = null

    // Navigation buttons
    private lateinit var homeBtn: ImageView
    private lateinit var aboutUsBtn: ImageView
    private lateinit var donateBtn: ImageView
    private lateinit var locationBtn: ImageView
    private lateinit var eventsBtn: ImageView
    private lateinit var loginOutBtn: ImageView
    private lateinit var adminLogin: ImageView

    // MapView for displaying pins
    private lateinit var mapView: MapView
    private val LOCATION_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        setContentView(R.layout.activity_location)

        donationTable = findViewById(R.id.donation_table)
        mapView = findViewById(R.id.mapView)

        setupMap()
        managePermissions()

        databaseReference = FirebaseDatabase.getInstance().getReference("locations")

        // Navigation button setup
        setupNavigationButtons()

        // Fetch data from Firebase and populate the table
        loadDonationSites()

        // Add markers to the map from Firebase locations
        loadMapMarkersByAddress() // New function for geocoding addresses
    }

    private fun setupMap() {
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        // Set initial map location
        val startPoint = GeoPoint(-29.8587, 31.0218) // Durban coordinates
        mapView.controller.setCenter(startPoint)
    }

    private fun managePermissions() {
        val requestPermissions = mutableListOf<String>()
        if (!isLocationPermissionGranted()) {
            requestPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (requestPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, requestPermissions.toTypedArray(), LOCATION_REQUEST_CODE)
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        val fineLocation = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fineLocation && coarseLocation
    }

    private fun loadDonationSites() {
        databaseReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val address = snapshot.child("address").getValue(String::class.java)

                    // Dynamically create a new row for each donation site
                    val row = TableRow(this@Location)
                    row.layoutParams = TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                    // Create TextViews for each column
                    val nameTextView = TextView(this@Location)
                    nameTextView.text = name
                    nameTextView.setPadding(16, 16, 16, 16)
                    nameTextView.gravity = Gravity.START

                    val addressTextView = TextView(this@Location)
                    addressTextView.text = address
                    addressTextView.setPadding(16, 16, 16, 16)
                    addressTextView.gravity = Gravity.START

                    row.addView(nameTextView)
                    row.addView(addressTextView)
                    donationTable!!.addView(row)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadMapMarkersByAddress() {
        val geocoder = Geocoder(this, Locale.getDefault())

        databaseReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val address = snapshot.child("address").getValue(String::class.java)

                    if (name != null && address != null) {
                        // Use Geocoder to convert the address to latitude and longitude
                        val addressList = geocoder.getFromLocationName(address, 1)
                        if (addressList != null && addressList.isNotEmpty()) {
                            val location = addressList[0]
                            val geoPoint = GeoPoint(location.latitude, location.longitude)
                            addMarkerToMap(name, geoPoint)
                        } else {
                            Toast.makeText(this@Location, "Unable to geocode address: $address", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@Location, "Failed to load locations", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addMarkerToMap(name: String, geoPoint: GeoPoint) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = name
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
    }

    private fun setupNavigationButtons() {
        homeBtn = findViewById(R.id.location_homeImg)
        aboutUsBtn = findViewById(R.id.location_aboutUsImg)
        donateBtn = findViewById(R.id.location_donationImg)
        locationBtn = findViewById(R.id.location_locationImg)
        eventsBtn = findViewById(R.id.location_eventsImg)
        loginOutBtn = findViewById(R.id.location_loginoutImg)
        adminLogin = findViewById(R.id.home_adminDashboard)

        homeBtn.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }
        aboutUsBtn.setOnClickListener {
            startActivity(Intent(this, AboutUs::class.java))
        }
        donateBtn.setOnClickListener {
            startActivity(Intent(this, Donate::class.java))
        }
        locationBtn.setOnClickListener {
            startActivity(Intent(this, Location::class.java))
        }
        eventsBtn.setOnClickListener {
            startActivity(Intent(this, Events::class.java))
        }
        adminLogin.setOnClickListener {
            startActivity(Intent(this, Admin_Dashboard::class.java))
        }
        loginOutBtn.setOnClickListener {
            showLoginLogoutDialog()
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
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioLogin -> startActivity(Intent(this, Login::class.java))
                R.id.radioLogout -> {
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(this, "You have logged out", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Login::class.java))
                }
            }
        }

        alertDialog.show()
    }
}
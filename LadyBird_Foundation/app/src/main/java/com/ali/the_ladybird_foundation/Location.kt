package com.ali.the_ladybird_foundation

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private lateinit var contactBtn : ImageView
    private lateinit var loginOutBtn: ImageView
    private lateinit var adminLogin: ImageView

    // MapView for displaying pins
    private lateinit var mapView: MapView
    private lateinit var geocoder: Geocoder
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
        geocoder = Geocoder(this, Locale.getDefault())

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
        val startPoint = GeoPoint(-29.7679675,30.7691305) // Main Office coordinates
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
                    val address = snapshot.child("address").getValue(String::class.java)
                    val name = snapshot.child("addressName").getValue(String::class.java)


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
        databaseReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@Location, "No locations found", Toast.LENGTH_SHORT).show()
                    return
                }

                // Debug: Log number of locations found
                println("Locations fetched: ${snapshot.childrenCount}")

                CoroutineScope(Dispatchers.IO).launch {
                    for (child in snapshot.children) {
                        val address = child.child("address").getValue(String::class.java)
                        val name = child.child("addressName").getValue(String::class.java)


                        if (name != null && address != null) {
                            println("Processing: $name, $address")
                            geocodeAddressAndAddMarker(name, address)
                        } else {
                            println("Invalid data: $name, $address")
                        }
                    }

                    withContext(Dispatchers.Main) {
                        mapView.invalidate()  // Refresh the map after adding markers
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Location, "Failed to load data", Toast.LENGTH_SHORT).show()
                println("Firebase error: ${error.message}")
            }
        })
    }

    private suspend fun geocodeAddressAndAddMarker(name: String, address: String) {
        try {
            val addressList = geocoder.getFromLocationName(address, 1)

            if (!addressList.isNullOrEmpty()) {
                val location = addressList[0]
                val geoPoint = GeoPoint(location.latitude, location.longitude)

                println("Geocoded: $name -> ${geoPoint.latitude}, ${geoPoint.longitude}")

                withContext(Dispatchers.Main) {
                    addMarkerToMap(name, geoPoint)
                }
            } else {
                println("Geocoding failed: $address")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Location, "Unable to geocode: $address", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            println("Error during geocoding: ${e.message}")
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Location, "Geocoding error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun addMarkerToMap(name: String, geoPoint: GeoPoint) {
        val marker = Marker(mapView).apply {
            position = geoPoint
            title = name
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        // Debug: Confirm marker addition
        println("Marker added: $name at ${geoPoint.latitude}, ${geoPoint.longitude}")

        mapView.overlays.add(marker)
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

    private fun setupNavigationButtons() {
        homeBtn = findViewById(R.id.location_homeImg)
        aboutUsBtn = findViewById(R.id.location_aboutUsImg)
        donateBtn = findViewById(R.id.location_donationImg)
        locationBtn = findViewById(R.id.location_locationImg)
        eventsBtn = findViewById(R.id.location_eventsImg)
        loginOutBtn = findViewById(R.id.location_loginoutImg)
        adminLogin = findViewById(R.id.home_adminDashboard)
        contactBtn = findViewById(R.id.location_contactImg5)

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
            showAdminPasswordDialog()
        }
        contactBtn.setOnClickListener {
            startActivity(Intent(this, Contact_Us::class.java))
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
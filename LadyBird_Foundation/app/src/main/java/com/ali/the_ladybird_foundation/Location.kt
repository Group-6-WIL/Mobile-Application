package com.ali.the_ladybird_foundation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.osmdroid.config.Configuration

class Location : AppCompatActivity() {

    private lateinit var suburbSpinner: Spinner
    private lateinit var locationsContainer: LinearLayout
    private lateinit var database: DatabaseReference

    // Navigation buttons
    private lateinit var homeBtn: ImageView
    private lateinit var aboutUsBtn: ImageView
    private lateinit var donateBtn: ImageView
    private lateinit var locationBtn: ImageView
    private lateinit var eventsBtn: ImageView
    private lateinit var contactBtn : ImageView
    private lateinit var adminLogin: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        setContentView(R.layout.activity_location)

        // Initialize Firebase Database Reference
        database = FirebaseDatabase.getInstance().reference

        suburbSpinner = findViewById(R.id.spinner)
        locationsContainer = findViewById(R.id.location_container) // Make sure this ID is correct

        setupNavigationButtons()

        // Set up Spinner listener
        suburbSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Check if the view is null before using it
                if (view != null) {
                    val selectedSuburb = parent.getItemAtPosition(position) as String
                    fetchLocations(selectedSuburb)
                } else {
                    // Handle the case where the view is null
                    Toast.makeText(this@Location, "Invalid selection", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle the case where nothing is selected if necessary
                Toast.makeText(this@Location, "No suburb selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

        private fun fetchLocations(suburb: String) {
        database.child("locations")
            .orderByChild("suburb")
            .equalTo(suburb)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Clear previous location entries
                    locationsContainer.removeAllViews()

                    if (snapshot.exists()) {
                        for (locationSnapshot in snapshot.children) {
                            val address = locationSnapshot.child("address").getValue(String::class.java) ?: ""
                            val addressName = locationSnapshot.child("addressName").getValue(String::class.java) ?: ""

                            // Create and add a new location entry
                            addLocationCard(addressName, address)
                        }
                    } else {
                        Toast.makeText(this@Location, "No locations found for selected suburb", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Location, "Error fetching locations: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun addLocationCard(addressName: String, address: String) {
        // Inflate the CardView layout
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.locationcar, locationsContainer, false) as CardView

        // Get references to the TextViews in the CardView
        val nameTextView: TextView = cardView.findViewById(R.id.location_name)
        val addressTextView: TextView = cardView.findViewById(R.id.location_address)

        // Set the data to the TextViews
        nameTextView.text = addressName
        addressTextView.text = address

        // Set a click listener for the card to display location details
        cardView.setOnClickListener {
            displayLocationDetails(addressName, address)
        }

        // Add the CardView to the container
        locationsContainer.addView(cardView)
    }

    private fun displayLocationDetails(addressName: String, address: String) {
        // Create a simple AlertDialog to display the details
        val builder = AlertDialog.Builder(this)
        builder.setTitle(addressName)
        builder.setMessage("Address: $address")

        // Add a positive button to dismiss the dialog
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

        // Show the AlertDialog
        builder.create().show()
    }

    fun openPopiaWebsite(view: View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://popia.co.za/"))
        startActivity(browserIntent)
    }

    private fun setupNavigationButtons() {
        homeBtn = findViewById(R.id.location_homeImg)
        aboutUsBtn = findViewById(R.id.location_aboutUsImg)
        donateBtn = findViewById(R.id.location_donationImg)
        locationBtn = findViewById(R.id.location_locationImg)
        eventsBtn = findViewById(R.id.location_eventsImg)
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
}

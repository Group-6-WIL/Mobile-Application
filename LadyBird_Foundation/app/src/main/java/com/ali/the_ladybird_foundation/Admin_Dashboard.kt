package com.ali.the_ladybird_foundation

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase

class Admin_Dashboard : AppCompatActivity() {

    // UI elements such as buttons and dialog box
    private lateinit var locationBtn: ImageView
    private lateinit var eventBtn: ImageView
    private lateinit var aboutUsBtn : ImageView
    private lateinit var contactBtn: ImageView
    private lateinit var donateBtn: ImageView
    private lateinit var dialog: Dialog

    private lateinit var homeBtn : ImageView
    private lateinit var aboutUs : ImageView
    private lateinit var donation : ImageView
    private lateinit var location : ImageView
    private lateinit var events : ImageView
    private lateinit var logout : ImageView

    // This method is called when the activity is first created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // Initializing UI components (buttons or icons for admin dashboard actions)
        locationBtn = findViewById(R.id.admin_location_IV)
        eventBtn = findViewById(R.id.admin_eventsIV)
        aboutUsBtn = findViewById(R.id.admin_aboutus_IV)
        contactBtn = findViewById(R.id.admin_dashboard_contactUs)
        donateBtn = findViewById(R.id.admin_dashbaord_donate)

        homeBtn = findViewById(R.id.home_homeImg)
        aboutUs = findViewById(R.id.home_aboutUsImg)
        donation = findViewById(R.id.home_donationImg)
        location = findViewById(R.id.home_locationImg)
        events = findViewById(R.id.home_eventsImg)
        logout = findViewById(R.id.home_loginoutImg)

        homeBtn.setOnClickListener {
            val intentHome = Intent(this, Home::class.java)
            startActivity(intentHome)
        }
       aboutUs.setOnClickListener {
            val intentAbout = Intent(this, AboutUs::class.java)
            startActivity(intentAbout)
        }
        donation.setOnClickListener {
            val intentDonate = Intent(this, Donate::class.java)
            startActivity(intentDonate)
        }
        location.setOnClickListener {
            val intentLocation = Intent(this, Location::class.java)
            startActivity(intentLocation)
        }
        events.setOnClickListener {
            val intentEvent = Intent(this, Events::class.java)
            startActivity(intentEvent)
        }

        logout.setOnClickListener {
            val intentLogout = Intent(this, Home::class.java)
            startActivity(intentLogout)
        }

        // Initializing a dialog for pop-up forms (used for adding location, contact info, etc.)
        dialog = Dialog(this)

        // Setting click listeners for the various buttons that perform different tasks.
        // When clicked, each button opens a corresponding form or activity.

        locationBtn.setOnClickListener { AddLocation() }  // Calls method to add location
        eventBtn.setOnClickListener {
            // Navigates to the events activity when eventBtn is clicked
            val intentEvent = Intent(this, admin_events::class.java)
            startActivity(intentEvent)
        }
        contactBtn.setOnClickListener { AddContact() }  // Calls method to add contact info
        donateBtn.setOnClickListener { AddDonationItem() }  // Calls method to add donation items
        aboutUsBtn.setOnClickListener { AboutUs() }  // Calls method to manage About Us section
    }

    // Method to add or edit contact details
    private fun AddContact() {
        // Setting the layout for the contact details dialog (popup window)
        dialog.setContentView(R.layout.admin_contact)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Finding the required UI elements within the dialog
        val name = dialog.findViewById<EditText>(R.id.editTextName)
        val number = dialog.findViewById<EditText>(R.id.editTextNumber)
        val email = dialog.findViewById<EditText>(R.id.editTextEmail)
        val save = dialog.findViewById<Button>(R.id.buttonSave)

        dialog.show() // Displaying the dialog
    }

    // Method to add or edit the "About Us" section
    private fun AboutUs() {
        // Setting the layout for the About Us section dialog
        dialog.setContentView(R.layout.activity_adminaboutus)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Finding UI elements within the dialog
        val aboutUs = dialog.findViewById<EditText>(R.id.admin_aboutused)  // About Us description
        val mission = dialog.findViewById<EditText>(R.id.admin_aboutus_mission)  // Mission statement
        val save = dialog.findViewById<Button>(R.id.admin_aboutus_updatebtn)  // Save button

        dialog.show() // Displaying the dialog
    }

    // Method to add a donation item (category and item name)
    private fun AddDonationItem() {
        // Setting the layout for the donation item dialog
        dialog.setContentView(R.layout.admi_donate)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Finding UI elements within the dialog
        val category = dialog.findViewById<EditText>(R.id.adminD_category)  // Donation category
        val item = dialog.findViewById<EditText>(R.id.adminD_item)  // Donation item name
        val save = dialog.findViewById<Button>(R.id.button4)  // Save button

        // Handling save button click: validate input, then save to Firebase database
        save.setOnClickListener {
            // Get user input from EditText fields
            val categoryText = category.text.toString().trim()
            val itemText = item.text.toString().trim()

            // Check if inputs are empty and show error message if necessary
            if (TextUtils.isEmpty(categoryText) || TextUtils.isEmpty(itemText)) {
                Toast.makeText(this, "Please enter both category and item", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Reference to the Firebase Realtime Database
            val database = FirebaseDatabase.getInstance().getReference("donationItems")
            // Generate a unique ID for the donation item
            val donationId = database.push().key

            // If a donation ID is successfully generated, save the donation item
            if (donationId != null) {
                val donationItem = DonationItem(categoryText, itemText)  // Create donation item object
                database.child(donationId).setValue(donationItem)  // Save to Firebase

                    // On successful save, show a success message, clear input fields, and dismiss the dialog
                    .addOnSuccessListener {
                        Toast.makeText(this, "Donation item saved successfully!", Toast.LENGTH_SHORT).show()
                        category.text.clear()
                        item.text.clear()
                        dialog.dismiss()  // Close the dialog
                    }
                    // On failure, show error message
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error saving donation item: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // If donation ID generation failed, show error message
                Toast.makeText(this, "Failed to generate donation ID", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()  // Displaying the dialog
    }

    // Method to add a new location to the Firebase database
    private fun AddLocation() {
        // Setting the layout for the location dialog
        dialog.setContentView(R.layout.activity_admin_location)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Finding UI elements within the dialog
        val addressEditText = dialog.findViewById<EditText>(R.id.aLocation_address)  // Location address
        val addressNameEditText = dialog.findViewById<EditText>(R.id.aLocation_addressName)  // Address name
        val saveButton = dialog.findViewById<Button>(R.id.alocation_saveBtn)  // Save button

        // Handling save button click: validate input, then save location to Firebase
        saveButton.setOnClickListener {
            val address = addressEditText.text.toString().trim()  // Get address input
            val addressName = addressNameEditText.text.toString().trim()  // Get address name input

            // Check if inputs are empty and show error message if necessary
            if (TextUtils.isEmpty(address) || TextUtils.isEmpty(addressName)) {
                Toast.makeText(this, "Please enter both address and address name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Generate Google Maps link from the address (formatted for Google Maps URL)
            val googleMapsLink = "https://www.google.com/maps/search/?api=1&query=" + address.replace(" ", "+")

            // Create location object with address, address name, and Google Maps link
            val locationData = LocationDC(address, addressName, googleMapsLink)

            // Generate a unique ID for the location
            val locationId = FirebaseDatabase.getInstance().getReference("locations").push().key
            if (locationId != null) {
                // Save the location data to Firebase
                FirebaseDatabase.getInstance().getReference("locations").child(locationId).setValue(locationData)
                    // On successful save, show success message, clear input fields, and dismiss dialog
                    .addOnSuccessListener {
                        Toast.makeText(this, "Location saved successfully!", Toast.LENGTH_SHORT).show()
                        addressEditText.text.clear()
                        addressNameEditText.text.clear()
                        dialog.dismiss()  // Close the dialog
                    }
                    // On failure, show error message
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error saving location: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // If location ID generation failed, show error message
                Toast.makeText(this, "Failed to generate location ID", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()  // Display the dialog
    }

    // Data class representing a donation item in Firebase
    data class DonationItem(
        val category: String? = "",  // Category of the donation item
        val item: String? = ""  // Name of the donation item
    )

    // Data class representing a location in Firebase
    data class LocationDC(
        val address: String? = "",  // Address of the location
        val addressName: String? = "",  // Name of the location
        val googleMapsLink: String? = ""  // Google Maps link to the location
    )
}

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
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase


class Admin_Dashboard : AppCompatActivity() {

    private lateinit var locationBtn: ImageView
    private lateinit var eventBtn: ImageView
    private lateinit var contactBtn: ImageView

    private lateinit var dialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        locationBtn = findViewById(R.id.admin_location_IV)
        eventBtn = findViewById(R.id.admin_eventsIV)
        contactBtn = findViewById(R.id.admin_dashboard_contactUs)


        dialog = Dialog(this)


        locationBtn.setOnClickListener { AddLocation() }

        eventBtn.setOnClickListener {
            val intentEvent = Intent(this, admin_events::class.java)
            startActivity(intentEvent)
        }

        contactBtn.setOnClickListener { AddContact() }


    }

    private fun AddContact() {
        dialog.setContentView(R.layout.admin_contact)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val imageView = findViewById<ImageView>(R.id.adminC_image)
        val uploadBtn = findViewById<Button>(R.id.button5)
        val name = findViewById<EditText>(R.id.editTextName)
        val number = findViewById<EditText>(R.id.editTextNumber)
        val email = findViewById<EditText>(R.id.editTextEmail)
        val save = findViewById<Button>(R.id.buttonSave)

        contactBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun AddLocation() {
        // Set up dialog with the custom layout
        dialog.setContentView(R.layout.activity_admin_location)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Initialize Firebase database reference
        val database = FirebaseDatabase.getInstance().getReference("locations")

        // Find views in the dialog
        val addressEditText = dialog.findViewById<EditText>(R.id.aLocation_address)
        val addressNameEditText = dialog.findViewById<EditText>(R.id.aLocation_addressName)
        val saveButton = dialog.findViewById<Button>(R.id.alocation_saveBtn)


        locationBtn.setOnClickListener {
            dialog.dismiss()
        }

        // Set listener for the Save button
        saveButton.setOnClickListener {
            // Get user input
            val address = addressEditText.text.toString().trim()
            val addressName = addressNameEditText.text.toString().trim()

            // Validate input
            if (TextUtils.isEmpty(address) || TextUtils.isEmpty(addressName)) {
                Toast.makeText(this, "Please enter both address and address name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Convert address to Google Maps link
            val googleMapsLink = "https://www.google.com/maps/search/?api=1&query=" + address.replace(" ", "+")

            // Create a location object to store in Firebase
            val locationData = Location(address, addressName, googleMapsLink)

            // Generate unique key for the location
            val locationId = database.push().key
            if (locationId != null) {
                // Save the location data to Firebase
                database.child(locationId).setValue(locationData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Location saved successfully!", Toast.LENGTH_SHORT).show()
                        // Clear input fields after saving
                        addressEditText.text.clear()
                        addressNameEditText.text.clear()
                        dialog.dismiss() // Optionally close the dialog after saving
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error saving location: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Failed to generate location ID", Toast.LENGTH_SHORT).show()
            }
        }

        // Show the dialog
        dialog.show()
    }



    // Define a data class to structure the location data
    data class Location(
        val address: String? = "",
        val addressName: String? = "",
        val googleMapsLink: String? = ""
    )
}
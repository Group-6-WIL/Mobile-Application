package com.ali.the_ladybird_foundation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class adminLocation : AppCompatActivity() {
    // Firebase Realtime Database instance
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_location)

        // Initialize Realtime Database
        database = FirebaseDatabase.getInstance().getReference("locations")

        val addressEditText = findViewById<EditText>(R.id.aLocation_address)
        val addressNameEditText = findViewById<EditText>(R.id.aLocation_addressName)
        val saveButton = findViewById<Button>(R.id.alocation_saveBtn)

        saveButton.setOnClickListener {
            val address = addressEditText.text.toString().trim()
            val addressName = addressNameEditText.text.toString().trim()

            if (TextUtils.isEmpty(address) || TextUtils.isEmpty(addressName)) {
                Toast.makeText(this, "Please enter both address and address name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Convert address to Google Maps link
            val googleMapsLink = "https://www.google.com/maps/search/?api=1&query=" + address.replace(" ", "+")

            // Create a location object to store in Realtime Database
            val locationData = Location(address, addressName, googleMapsLink)

            // Save the location data to Firebase Realtime Database
            val locationId = database.push().key // Generate unique key for the location
            if (locationId != null) {
                database.child(locationId).setValue(locationData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Location saved successfully!", Toast.LENGTH_SHORT).show()
                        // Clear the input fields after saving
                        addressEditText.text.clear()
                        addressNameEditText.text.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error saving location: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Failed to generate location ID", Toast.LENGTH_SHORT).show()
            }
        }
    }


// Define a data class to structure the location data
data class Location(
    val address: String? = "",
    val addressName: String? = "",
    val googleMapsLink: String? = ""
)
}
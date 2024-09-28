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

    private lateinit var locationBtn: ImageView
    private lateinit var eventBtn: ImageView
    private lateinit var aboutUsBtn : ImageView
    private lateinit var contactBtn: ImageView
    private lateinit var donateBtn: ImageView
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        locationBtn = findViewById(R.id.admin_location_IV)
        eventBtn = findViewById(R.id.admin_eventsIV)
        aboutUsBtn = findViewById(R.id.admin_aboutus_IV)
        contactBtn = findViewById(R.id.admin_dashboard_contactUs)
        donateBtn = findViewById(R.id.admin_dashbaord_donate)

        dialog = Dialog(this)

        locationBtn.setOnClickListener { AddLocation() }
        eventBtn.setOnClickListener {
            val intentEvent = Intent(this, admin_events::class.java)
            startActivity(intentEvent)
        }
        contactBtn.setOnClickListener { AddContact() }
        donateBtn.setOnClickListener { AddDonationItem() }
        aboutUsBtn.setOnClickListener { AboutUs() }
    }

    private fun AddContact() {
        dialog.setContentView(R.layout.admin_contact)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val name = dialog.findViewById<EditText>(R.id.editTextName)
        val number = dialog.findViewById<EditText>(R.id.editTextNumber)
        val email = dialog.findViewById<EditText>(R.id.editTextEmail)
        val save = dialog.findViewById<Button>(R.id.buttonSave)

        dialog.show()
    }

    private fun AboutUs() {
        dialog.setContentView(R.layout.activity_adminaboutus)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val aboutUs = dialog.findViewById<EditText>(R.id.admin_aboutused)
        val mission = dialog.findViewById<EditText>(R.id.admin_aboutus_mission)
        val save = dialog.findViewById<Button>(R.id.admin_aboutus_updatebtn)

        dialog.show()
    }


    private fun AddDonationItem() {
        dialog.setContentView(R.layout.admi_donate)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val category = dialog.findViewById<EditText>(R.id.adminD_category)
        val item = dialog.findViewById<EditText>(R.id.adminD_item)
        val save = dialog.findViewById<Button>(R.id.button4)

        save.setOnClickListener {
            // Get user input
            val categoryText = category.text.toString().trim()
            val itemText = item.text.toString().trim()

            // Validate input
            if (TextUtils.isEmpty(categoryText) || TextUtils.isEmpty(itemText)) {
                Toast.makeText(this, "Please enter both category and item", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save to Firebase
            val database = FirebaseDatabase.getInstance().getReference("donationItems")
            val donationId = database.push().key
            if (donationId != null) {
                val donationItem = DonationItem(categoryText, itemText)
                database.child(donationId).setValue(donationItem)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Donation item saved successfully!", Toast.LENGTH_SHORT).show()
                        category.text.clear()
                        item.text.clear()
                        dialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error saving donation item: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Failed to generate donation ID", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun AddLocation() {
        dialog.setContentView(R.layout.activity_admin_location)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val addressEditText = dialog.findViewById<EditText>(R.id.aLocation_address)
        val addressNameEditText = dialog.findViewById<EditText>(R.id.aLocation_addressName)
        val saveButton = dialog.findViewById<Button>(R.id.alocation_saveBtn)

        saveButton.setOnClickListener {
            val address = addressEditText.text.toString().trim()
            val addressName = addressNameEditText.text.toString().trim()

            if (TextUtils.isEmpty(address) || TextUtils.isEmpty(addressName)) {
                Toast.makeText(this, "Please enter both address and address name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val googleMapsLink = "https://www.google.com/maps/search/?api=1&query=" + address.replace(" ", "+")

            val locationData = Location(address, addressName, googleMapsLink)

            val locationId = FirebaseDatabase.getInstance().getReference("locations").push().key
            if (locationId != null) {
                FirebaseDatabase.getInstance().getReference("locations").child(locationId).setValue(locationData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Location saved successfully!", Toast.LENGTH_SHORT).show()
                        addressEditText.text.clear()
                        addressNameEditText.text.clear()
                        dialog.dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error saving location: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Failed to generate location ID", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    data class DonationItem(
        val category: String? = "",
        val item: String? = ""
    )

    data class Location(
        val address: String? = "",
        val addressName: String? = "",
        val googleMapsLink: String? = ""
    )
}

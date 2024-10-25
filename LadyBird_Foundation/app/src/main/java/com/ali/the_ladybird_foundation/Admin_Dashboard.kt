package com.ali.the_ladybird_foundation

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

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

    private var selectedImageUri: Uri? = null // Variable to hold selected image URI
    private val REQUEST_CODE_IMAGE_PICK = 100 // Any unique value for your request code


    // This method is called when the activity is first created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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

    private fun AddContact() {
        dialog.setContentView(R.layout.admin_contact)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val name = dialog.findViewById<EditText>(R.id.editTextName)
        val number = dialog.findViewById<EditText>(R.id.editTextNumber)
        val email = dialog.findViewById<EditText>(R.id.editTextEmail)
        val contactImageView = dialog.findViewById<ImageView>(R.id.adminC_image)
        val selectImage = dialog.findViewById<Button>(R.id.buttonUploadImage)
        val save = dialog.findViewById<Button>(R.id.buttonSave)

        dialog.show()

        selectImage.setOnClickListener {
            openImagePicker()
        }

        save.setOnClickListener {
            val contactName = name.text.toString()
            val contactNumber = number.text.toString()
            val contactEmail = email.text.toString()

            if (contactName.isNotEmpty() && contactNumber.isNotEmpty() && contactEmail.isNotEmpty()) {
                if (selectedImageUri != null) {
                    uploadImageAndSaveContact(contactName, contactNumber, contactEmail)
                } else {
                    saveContactToDatabase(contactName, contactNumber, contactEmail, null)
                }
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            dialog.findViewById<ImageView>(R.id.adminC_image).setImageURI(selectedImageUri)
        }
    }

    private fun uploadImageAndSaveContact(name: String, number: String, email: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("contact_images/${UUID.randomUUID()}.jpg")
        selectedImageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveContactToDatabase(name, number, email, uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveContactToDatabase(name: String, number: String, email: String, imageUrl: String?) {
        val contactId = FirebaseDatabase.getInstance().getReference("contacts").push().key
        val contact = Contact(contactId, name, number, email, imageUrl)

        if (contactId != null) {
            FirebaseDatabase.getInstance().getReference("contacts").child(contactId).setValue(contact)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Contact saved successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Failed to save contact: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // Data class for Contact with image URL
    data class Contact(
        val id: String?, // Contact ID (nullable)
        val name: String,
        val number: String,
        val email: String,
        val imageUrl: String? // Image URL can be nullable if no image is provided
    )


    // Method to add or edit the "About Us" section


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

            // Save the item under the respective category
            val categoryRef = database.child(categoryText)  // Use category as the key
            val itemId = categoryRef.push().key  // Generate a unique ID for the item

            // If an item ID is successfully generated, save the item
            if (itemId != null) {
                val donationItem = DonationItem(itemText)  // Create donation item object (consider modifying the constructor)
                categoryRef.child(itemId).setValue(donationItem)  // Save to Firebase under the category

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
                // If item ID generation failed, show error message
                Toast.makeText(this, "Failed to generate item ID", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()  // Displaying the dialog
    }

    private fun AboutUs() {
        // Setting the layout for the About Us section dialog
        dialog.setContentView(R.layout.activity_adminaboutus)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Finding UI elements within the dialog
        val aboutUsEditText = dialog.findViewById<EditText>(R.id.admin_aboutused)
        val missionEditText = dialog.findViewById<EditText>(R.id.admin_aboutus_mission)
        val saveButton = dialog.findViewById<Button>(R.id.admin_aboutus_updatebtn)

        dialog.show() // Displaying the dialog

        // Handle the save button click
        saveButton.setOnClickListener {
            val aboutUsText = aboutUsEditText.text.toString().trim()
            val missionText = missionEditText.text.toString().trim()

            if (aboutUsText.isNotEmpty() && missionText.isNotEmpty()) {
                val database = FirebaseDatabase.getInstance().getReference("aboutUs")
                val aboutUsData = mapOf(
                    "Content" to aboutUsText,
                    "Mission" to missionText
                )

                // Save data to Firebase
                database.setValue(aboutUsData)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "About Us updated successfully!", Toast.LENGTH_SHORT).show()
                            dialog.dismiss() // Close the dialog after saving
                        } else {
                            Toast.makeText(this, "Failed to update: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill out both fields", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Method to add a new location to the Firebase database
    private fun AddLocation() {
        // Set the layout for the location dialog
        dialog.setContentView(R.layout.activity_admin_location)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Find UI elements within the dialog
        val addressEditText = dialog.findViewById<EditText>(R.id.aLocation_address)  // Physical Address
        val addressNameEditText = dialog.findViewById<EditText>(R.id.aLocation_addressName)  // Address Name
        val areaSpinner = dialog.findViewById<Spinner>(R.id.area)  // Suburb Spinner
        val saveButton = dialog.findViewById<Button>(R.id.alocation_saveBtn)  // Save Button

        // Handling save button click: validate input, then save location to Firebase
        saveButton.setOnClickListener {
            val address = addressEditText.text.toString().trim()  // Get address input
            val addressName = addressNameEditText.text.toString().trim()  // Get address name input
            val selectedSuburb = areaSpinner.selectedItem.toString()  // Get selected suburb from Spinner

            // Check if inputs are empty and show error message if necessary
            if (address.isEmpty() || addressName.isEmpty() || selectedSuburb.isEmpty()) {
                Toast.makeText(this, "Please fill all fields and select a suburb", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create a location object with address, address name, and suburb
            val locationData = LocationDC(address, addressName, selectedSuburb)

            // Generate a unique ID for the location
            val locationId = FirebaseDatabase.getInstance().getReference("locations").push().key
            if (locationId != null) {
                // Save the location data to Firebase
                FirebaseDatabase.getInstance().getReference("locations").child(locationId).setValue(locationData)
                    .addOnSuccessListener {
                        // On success, show success message, clear input fields, and dismiss dialog
                        Toast.makeText(this, "Location saved successfully!", Toast.LENGTH_SHORT).show()
                        addressEditText.text.clear()
                        addressNameEditText.text.clear()
                        dialog.dismiss()  // Close the dialog
                    }
                    .addOnFailureListener { e ->
                        // On failure, show error message
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
        //   val category: String? = "",  // Category of the donation item
        val item: String? = ""  // Name of the donation item
    )

    // Data class representing a location in Firebase
    data class LocationDC(
        val address: String? = "",  // Address of the location
        val addressName: String? = "",  // Name of the location
        val suburb: String
    )
}
package com.ali.the_ladybird_foundation

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.getValue

class Donate : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var categorySpinner: Spinner
    private lateinit var itemSpinner: Spinner
    private lateinit var locationSpinner: Spinner
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var viewBankingButton: Button
    private lateinit var sendDonationButton: Button

    //nav buttons
    private lateinit var homeBtn : ImageView
    private lateinit var aboutUsBtn : ImageView
    private lateinit var donateBtn : ImageView
    private lateinit var locationBtn : ImageView
    private lateinit var eventsBtn : ImageView
    private lateinit var contactUs : ImageView
    private lateinit var adminLogin : ImageView

    private val categories = mutableListOf<String>()
    private val itemsMap = mutableMapOf<String, List<String>>() // Category to items mapping

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)


        homeBtn = findViewById(R.id.donate_homeImg)
        aboutUsBtn = findViewById(R.id.donate_aboutUsImg)
        donateBtn = findViewById(R.id.donate_donationImg)
        locationBtn = findViewById(R.id.donate_locationImg)
        eventsBtn = findViewById(R.id.donate_eventsImg)
        adminLogin = findViewById(R.id.donate_adminDashboard)
        contactUs = findViewById(R.id.donate_contactImg3)



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

        contactUs.setOnClickListener {
            val intentcontacts = Intent(this, Contact_Us::class.java)
            startActivity(intentcontacts)
        }

        adminLogin.setOnClickListener {
            showAdminPasswordDialog()
        }




        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("donationItems")

        // Initialize UI components
        categorySpinner = findViewById(R.id.donate_category_spinner)
        itemSpinner = findViewById(R.id.donate_item_spinner)
        locationSpinner = findViewById(R.id.donate_location_spinner)
        nameEditText = findViewById(R.id.editTextText4)
        emailEditText = findViewById(R.id.editTextTextEmailAddress)
        phoneEditText = findViewById(R.id.editTextPhone)
        viewBankingButton = findViewById(R.id.button)
        sendDonationButton = findViewById(R.id.button2)

        // Set up spinners
        setupCategorySpinner()
        setupLocationSpinner()

        // Set click listeners
        viewBankingButton.setOnClickListener { showBankingDetails() }
        sendDonationButton.setOnClickListener { sendDonationEmail() }
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
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
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

    private fun showBankingDetails() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Banking Details")
        builder.setMessage(
            """
            Account Name: Ladybird Foundation
            Account Number: 50007437061
            Bank:  Investec
            Branch Code: 580105
            Account Type: Trust Account
            """.trimIndent()
        )
        builder.setPositiveButton("OK", null)
        builder.show()
    }

    private fun setupCategorySpinner() {
        val database = FirebaseDatabase.getInstance().getReference("donationItems") // Reference to your database

        // Fetch categories from Firebase
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val categories = mutableListOf<String>()
                for (categorySnapshot in dataSnapshot.children) {
                    // Use the category key directly
                    categorySnapshot.key?.let { categories.add(it) }
                }

                // Populate the category spinner
                val categoryAdapter = ArrayAdapter(this@Donate, android.R.layout.simple_spinner_item, categories)
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = categoryAdapter

                // Set up category selection logic to load items in the second spinner
                categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedCategory = categories[position]
                        setupItemSpinner(selectedCategory) // Load items based on selected category
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Do nothing
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@Donate, "Error fetching categories: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupItemSpinner(category: String) {
        val database = FirebaseDatabase.getInstance().getReference("donationItems/$category") // Reference to the selected category

        // Fetch items based on the selected category from Firebase
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val items = mutableListOf<String>()
                for (itemSnapshot in dataSnapshot.children) {
                    // Here we expect each item to be a HashMap with a "name" key.
                    val itemMap = itemSnapshot.getValue<Map<String, Any>>() // Get the item as a Map
                    if (itemMap != null) {
                        val itemName = itemMap["item"] as? String // Adjust the key according to your data structure
                        if (!itemName.isNullOrEmpty()) {
                            items.add(itemName) // Add item name to the list
                        }
                    } else {
                        Log.d("ItemSpinner", "itemMap is null for snapshot: $itemSnapshot")
                    }
                }

                // Log the items to debug
                Log.d("ItemSpinner", "Items for category $category: $items")

                // Populate the item spinner
                val itemAdapter = ArrayAdapter(this@Donate, android.R.layout.simple_spinner_item, items)
                itemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                itemSpinner.adapter = itemAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@Donate, "Error fetching items: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                Log.e("ItemSpinner", "Database error: ${databaseError.message}")
            }
        })
    }


    private fun setupLocationSpinner() {
        // Reference to the Firebase Realtime Database
        val databaseReference = FirebaseDatabase.getInstance().getReference("locations")

        // Create a list to hold the locations
        val locations = mutableListOf<String>()

        // Add a ValueEventListener to retrieve data from Firebase
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the list to avoid duplicates
                locations.clear()

                // Loop through the children of the snapshot to get the location names
                for (locationSnapshot in snapshot.children) {
                    // Assume each location is stored as a Map with a "name" key
                    val locationMap = locationSnapshot.getValue<Map<String, String>>()
                    if (locationMap != null) {
                        val locationName = locationMap["address "] // Adjust based on your structure
                        if (locationName != null) {
                            locations.add(locationName)
                        }
                    }
                }

                // Create an ArrayAdapter for the spinner with the fetched locations
                val locationAdapter = ArrayAdapter(this@Donate, android.R.layout.simple_spinner_item, locations)
                locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                locationSpinner.adapter = locationAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
                Log.e("FirebaseError", "Failed to read locations: ${error.message}")
            }
        })
    }

    private fun sendDonationEmail() {
        // Collect input values
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val phone = phoneEditText.text.toString()
        val selectedCategory = categorySpinner.selectedItem.toString()
        val selectedItem = itemSpinner.selectedItem.toString()
        val dropLocation = locationSpinner.selectedItem.toString()

        // Validate inputs
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || selectedItem.isEmpty() || dropLocation.isEmpty()) {
            Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show()
        } else {
            // Call backend to process the donation
            sendEmailIntent(name, email, phone, selectedCategory, selectedItem, dropLocation)
        }
    }

    private fun sendEmailIntent(
        name: String,
        email: String,
        phone: String,
        category: String,
        item: String,
        location: String
    ) {
        val subject = "Donation Details"
        val body = """
        Donation Details:
        Name: $name
        Email: $email
        Phone: $phone
        Category: $category
        Item: $item
        Drop Location: $location
    """.trimIndent()

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("reesalison1@gmail.com")) // Recipient email - change to michelle when ready
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."))
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }

}

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
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Donate : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var categorySpinner: Spinner
    private lateinit var itemSpinner: Spinner
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var viewBankingButton: Button
    private lateinit var sendDonationButton: Button

    // Navigation buttons
    private lateinit var homeBtn: ImageView
    private lateinit var aboutUsBtn: ImageView
    private lateinit var donateBtn: ImageView
    private lateinit var locationBtn: ImageView
    private lateinit var eventsBtn: ImageView
    private lateinit var contactUs: ImageView
    private lateinit var adminLogin: ImageView

    private val categories = mutableListOf<String>()
    private val itemsMap = mutableMapOf<String, List<String>>() // Category to items mapping

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("donationItems")

        // Initialize UI components
        categorySpinner = findViewById(R.id.donate_category_spinner)
        itemSpinner = findViewById(R.id.donate_item_spinner)
        nameEditText = findViewById(R.id.editTextText4)
        emailEditText = findViewById(R.id.editTextTextEmailAddress)
        phoneEditText = findViewById(R.id.editTextPhone)
        viewBankingButton = findViewById(R.id.button)
        sendDonationButton = findViewById(R.id.button2)

        // Navigation buttons
        homeBtn = findViewById(R.id.donate_homeImg)
        aboutUsBtn = findViewById(R.id.donate_aboutUsImg)
        donateBtn = findViewById(R.id.donate_donationImg)
        locationBtn = findViewById(R.id.donate_locationImg)
        eventsBtn = findViewById(R.id.donate_eventsImg)
        adminLogin = findViewById(R.id.donate_adminDashboard)
        contactUs = findViewById(R.id.donate_contactImg3)

        // Set navigation button click listeners
        setupNavigationButtons()

        // Set up spinners
        setupCategorySpinner()

        // Set click listeners for buttons
        viewBankingButton.setOnClickListener { showBankingDetails() }
        sendDonationButton.setOnClickListener { sendDonationEmail() }
    }

    private fun setupNavigationButtons() {
        homeBtn.setOnClickListener { startActivity(Intent(this, Home::class.java)) }
        aboutUsBtn.setOnClickListener { startActivity(Intent(this, AboutUs::class.java)) }
        donateBtn.setOnClickListener { startActivity(Intent(this, Donate::class.java)) }
        locationBtn.setOnClickListener { startActivity(Intent(this, Location::class.java)) }
        eventsBtn.setOnClickListener { startActivity(Intent(this, Events::class.java)) }
        contactUs.setOnClickListener { startActivity(Intent(this, Contact_Us::class.java)) }
        adminLogin.setOnClickListener { showAdminPasswordDialog() }
    }

    private fun showAdminPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.admin_password, null)
        val passwordField = dialogView.findViewById<EditText>(R.id.adminPassword)
        val enterButton = dialogView.findViewById<Button>(R.id.admin_add_events_uploadIV)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        enterButton.setOnClickListener {
            val enteredPassword = passwordField.text.toString()
            if (enteredPassword == "TheLadyB1rdF0undation") { // Replace with your desired password
                Toast.makeText(this, "Access Granted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                startActivity(Intent(this, Admin_Dashboard::class.java))
            } else {
                Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showBankingDetails() {
        AlertDialog.Builder(this)
            .setTitle("Banking Details")
            .setMessage(
                """
                Account Name: Ladybird Foundation
                Account Number: 50007437061
                Bank: Investec
                Branch Code: 580105
                Account Type: Trust Account
                """.trimIndent()
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setupCategorySpinner() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                categories.clear() // Clear previous categories
                for (categorySnapshot in dataSnapshot.children) {
                    categorySnapshot.key?.let { categories.add(it) }
                }

                val categoryAdapter = ArrayAdapter(this@Donate, android.R.layout.simple_spinner_item, categories)
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = categoryAdapter

                categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedCategory = categories[position]
                        setupItemSpinner(selectedCategory)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@Donate, "Error fetching categories: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupItemSpinner(category: String) {
        val itemDatabase = FirebaseDatabase.getInstance().getReference("donationItems/$category")
        itemDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val items = mutableListOf<String>()
                for (itemSnapshot in dataSnapshot.children) {
                    val itemMap = itemSnapshot.getValue<Map<String, Any>>()
                    itemMap?.let {
                        val itemName = it["item"] as? String
                        itemName?.let { items.add(it) }
                    } ?: Log.d("ItemSpinner", "itemMap is null for snapshot: $itemSnapshot")
                }

                val itemAdapter = ArrayAdapter(this@Donate, android.R.layout.simple_spinner_item, items)
                itemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                itemSpinner.adapter = itemAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@Donate, "Error fetching items: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendDonationEmail() {
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val phone = phoneEditText.text.toString()
        val selectedCategory = categorySpinner.selectedItem?.toString() ?: ""
        val selectedItem = itemSpinner.selectedItem?.toString() ?: ""

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || selectedItem.isEmpty()) {
            Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show()
        } else {
            sendEmailIntent(name, email, phone, selectedCategory, selectedItem)
        }
    }

    private fun sendEmailIntent(name: String, email: String, phone: String, category: String, item: String) {
        val subject = "Donation Details"
        val body = """
        **Donation Details:**
        
        Name: $name
        Email: $email
        Phone: $phone
        Category: $category
        Item: $item
    """.trimIndent()

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("reesalison1@gmail.com")) // Update to the intended recipient
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

package com.ali.the_ladybird_foundation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Contact_Us : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var contactsContainer: LinearLayout

    //nav buttons
    private lateinit var homeBtn : ImageView
    private lateinit var aboutUsBtn : ImageView
    private lateinit var donateBtn : ImageView
    private lateinit var locationBtn : ImageView
    private lateinit var eventsBtn : ImageView
    private lateinit var contactUs : ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)

        // Initialize Firebase reference
        databaseRef = FirebaseDatabase.getInstance().getReference("contacts")

        // Reference to the container where the contacts will be displayed
        contactsContainer = findViewById(R.id.events_container)

        homeBtn = findViewById(R.id.contact_homeImg)
        aboutUsBtn = findViewById(R.id.contact_aboutUsImg)
        donateBtn = findViewById(R.id.contact_donationImg)
        locationBtn = findViewById(R.id.contact_locationImg)
        eventsBtn = findViewById(R.id.contact_eventsImg)


        contactUs = findViewById(R.id.contact_contactImg)

        // Load contacts from Firebase
        loadContacts()

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



    }
    fun openPopiaWebsite(view: android.view.View) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://popia.co.za/"))
        startActivity(browserIntent)
    }
    // Method to load contacts from Firebase
    private fun loadContacts() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contactsContainer.removeAllViews() // Clear previous views
                for (contactSnapshot in snapshot.children) {
                    val contact = contactSnapshot.getValue(Contact::class.java)
                    contact?.let {
                        addContactCard(it, contactsContainer) // Add a card for each contact
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@Contact_Us,
                    "Failed to load contacts: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // Method to dynamically add a contact card to the container
    private fun addContactCard(contact: Contact, container: LinearLayout) {
        // Inflate the CardView layout
        val cardView = layoutInflater.inflate(R.layout.contact_card, container, false)

        // Find the views in the inflated layout
        val contactImg = cardView.findViewById<ImageView>(R.id.contactImg)
        val contactName = cardView.findViewById<TextView>(R.id.event_name)
        val contactPhone = cardView.findViewById<TextView>(R.id.event_date)
        val contactEmail = cardView.findViewById<TextView>(R.id.viewDetails)

        // Set data to the views
        contactName.text = contact.name
        contactPhone.text = contact.number
        contactEmail.text = contact.email

        // Load image using Glide
        if (!contact.imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(contact.imageUrl)
                .into(contactImg)
        } else {
            contactImg.setImageResource(R.drawable.upload) // Placeholder image if no URL
        }

        // Add the CardView to the container
        container.addView(cardView)
    }

    data class Contact(
        val name: String = "",
        val number: String = "",
        val email: String = "",
        val imageUrl: String? = null // Optional image URL
    )
}



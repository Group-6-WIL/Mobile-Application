package com.ali.the_ladybird_foundation

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class Contact_Us : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var contactsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)

        // Initialize Firebase reference
        databaseRef = FirebaseDatabase.getInstance().getReference("contacts")

        // Reference to the container where the contacts will be displayed
        contactsContainer = findViewById(R.id.events_container)

        // Load contacts from Firebase
        loadContacts()
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
                Toast.makeText(this@Contact_Us, "Failed to load contacts: ${error.message}", Toast.LENGTH_SHORT).show()
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
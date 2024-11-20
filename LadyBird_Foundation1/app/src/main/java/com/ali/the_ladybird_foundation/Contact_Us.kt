package com.ali.the_ladybird_foundation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class Contact_Us : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var contactsContainer: LinearLayout

    //nav buttons
    private lateinit var homeBtn: ImageView
    private lateinit var aboutUsBtn: ImageView
    private lateinit var donateBtn: ImageView
    private lateinit var locationBtn: ImageView
    private lateinit var eventsBtn: ImageView
    private lateinit var contactUs: ImageView
    private lateinit var admindashboard: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_contact_us)

        // Initialize Firebase reference to "meet_the_member"
        databaseRef = FirebaseDatabase.getInstance().getReference("meet_the_member")

        // Reference to the container where the members will be displayed
        contactsContainer = findViewById(R.id.events_container)

        // Initialize nav buttons
        homeBtn = findViewById(R.id.contact_homeImg)
        aboutUsBtn = findViewById(R.id.contact_aboutUsImg)
        donateBtn = findViewById(R.id.contact_donationImg)
        locationBtn = findViewById(R.id.contact_locationImg)
        eventsBtn = findViewById(R.id.contact_eventsImg)
        admindashboard = findViewById(R.id.contact_adminDashboard2)
        contactUs = findViewById(R.id.contact_contactImg)

        // Load members from Firebase
        loadMembers()

        // Set up navigation
        homeBtn.setOnClickListener { startActivity(Intent(this, Home::class.java)) }
        aboutUsBtn.setOnClickListener { startActivity(Intent(this, AboutUs::class.java)) }
        donateBtn.setOnClickListener { startActivity(Intent(this, Donate::class.java)) }
        locationBtn.setOnClickListener { startActivity(Intent(this, Location::class.java)) }
        eventsBtn.setOnClickListener { startActivity(Intent(this, Events::class.java)) }
        contactUs.setOnClickListener { startActivity(Intent(this, Contact_Us::class.java)) }
        admindashboard.setOnClickListener { showAdminPasswordDialog() }
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

            if (enteredPassword == "TheLadyB1rdF0undation") {
                Toast.makeText(this, "Access Granted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                startActivity(Intent(this, Admin_Dashboard::class.java))
            } else {
                Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun loadMembers() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contactsContainer.removeAllViews() // Clear previous views
                for (memberSnapshot in snapshot.children) {
                    val member = memberSnapshot.getValue(Member::class.java)
                    member?.let {
                        addMemberCard(it, contactsContainer) // Add a card for each member
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@Contact_Us,
                    "Failed to load members: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun addMemberCard(member: Member, container: LinearLayout) {
        // Inflate the CardView layout
        val cardView = layoutInflater.inflate(R.layout.contact_card, container, false)

        val memberImg = cardView.findViewById<ImageView>(R.id.contactImg)
        val memberName = cardView.findViewById<TextView>(R.id.event_name)
        val viewDetails = cardView.findViewById<TextView>(R.id.viewDetails)

        // Set data to the views
        memberName.text = member.Name
        viewDetails.text = "Meet the Member"

        Glide.with(this)
            .load(member.ImageUrl)
            .placeholder(R.drawable.upload) // Placeholder image
            .into(memberImg)

        // Show the member detail dialog when "Meet the Member" is clicked
        viewDetails.setOnClickListener {
            showMemberDetailDialog(member)
        }

        container.addView(cardView)
    }

    private fun showMemberDetailDialog(member: Member) {
        // Inflate the detail dialog layout
        val dialogView = layoutInflater.inflate(R.layout.member_detail_card, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = dialogBuilder.create()

        // Set data in the dialog views
        val memberName = dialogView.findViewById<TextView>(R.id.detail_title)
        val memberDescription = dialogView.findViewById<TextView>(R.id.detail_description)

        memberName.text = member.Name
        memberDescription.text = member.Description
        Glide.with(this)
            .load(member.ImageUrl)
            .placeholder(R.drawable.upload) // Placeholder image

        dialog.show()
    }

    // Updated data class for "meet the member" info
    data class Member(
        val Name: String = "",
        val Description: String = "",
        val ImageUrl: String? = null
    )
}

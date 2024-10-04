package com.ali.the_ladybird_foundation

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class Events : AppCompatActivity() {

    private lateinit var eventsContainer: LinearLayout
    private var allEvents: MutableList<Event> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        eventsContainer = findViewById(R.id.events_container)

        fetchEvents()
    }

    private fun fetchEvents() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("events")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allEvents.clear() // Clear the list before adding new events

                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(Event::class.java)
                    event?.let {
                        allEvents.add(it)
                    }
                }

                // Debugging: Print all fetched events
                println("Fetched Events: $allEvents")

                // Sort events by date before displaying
                allEvents.sortBy { it.date }  // Ensure the date is in the format "DD-MM-YYYY"

                // Display all events initially
                displayEvents(allEvents)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Events, "Failed to load events: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayEvents(events: List<Event>) {
        eventsContainer.removeAllViews() // Clear existing views

        if (events.isEmpty()) {
            Toast.makeText(this, "No events found.", Toast.LENGTH_SHORT).show()
            return // Exit early if no events found
        }

        for (event in events) {
            addEventCard(event)
        }
    }

    private fun addEventCard(event: Event) {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.event_card, eventsContainer, false)

        val eventName: TextView = cardView.findViewById(R.id.event_name)
        val eventDate: TextView = cardView.findViewById(R.id.event_date)
    val image: ImageView = cardView.findViewById(R.id.ImageView11)
        val viewDetails: TextView = cardView.findViewById(R.id.viewDetails)

        eventName.text = event.eventName
        eventDate.text = event.date

        // Log the image URL
        Log.d("Events", "Image URL: ${event.imageUrl}") // Log the URL before loading it
        // Load the recipe image using Glide, but check if the Activity is still valid
        if (!isDestroyed && !isFinishing) { // Ensure the Activity is not destroyed
            Glide.with(this)
                .load(event.imageUrl)
                .placeholder(R.drawable.baseline_downloading_24) // Placeholder image while loading
                .into(image)
        } else {
            Log.e("Events", "Activity is destroyed or finishing, cannot load image")
        }

        // Set click listener for the card
        viewDetails.setOnClickListener {
            displayEventDetails(event)
        }

        eventsContainer.addView(cardView)
    }

    private fun displayEventDetails(event: Event) {
        eventsContainer.visibility = View.VISIBLE // Ensure the container is visible

        // Clear previous details
        eventsContainer.removeAllViews()

        // Create a new card view for the event details
        val detailCardView = LayoutInflater.from(this)
            .inflate(R.layout.event_detail_card, eventsContainer, false)

        // Set the event detail fields
        val titleTextView: TextView = detailCardView.findViewById(R.id.detail_title)
        val dateTextView: TextView = detailCardView.findViewById(R.id.detail_date)
        val descriptionTextView: TextView = detailCardView.findViewById(R.id.detail_description)

        // Set the values from the event object, including labels
        titleTextView.text = event.eventName
        dateTextView.text = "Date: ${event.date}" // Format with label
        descriptionTextView.text = "Description: ${event.description}" // Format with label

        // Add detail card view to the event details container
        eventsContainer.addView(detailCardView)
    }




    // Event data class for holding event information
data class Event(
    val eventId: String? = null,
    val eventName: String = "",
    val description: String = "",
    val date: String = "", // Ensure this matches the format used in showDatePicker()
    val imageUrl: String? = null // Include this if you plan to display images in the future
)}

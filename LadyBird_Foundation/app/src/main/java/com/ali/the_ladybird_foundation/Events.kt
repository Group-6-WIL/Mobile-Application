package com.ali.the_ladybird_foundation

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class Events : AppCompatActivity() {

    private lateinit var eventsContainer: LinearLayout
    private lateinit var datePickerEditText: EditText
    private var allEvents: MutableList<Event> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        eventsContainer = findViewById(R.id.events_container)
        datePickerEditText = findViewById(R.id.events_datepicker)
        val searchImageView: ImageView = findViewById(R.id.imageView9)

        // Set up date picker on the EditText field
        datePickerEditText.setOnClickListener {
            showDatePicker()
        }

        // Set up search action on the image view
        searchImageView.setOnClickListener {
            val selectedDate = datePickerEditText.text.toString()
            if (selectedDate.isNotEmpty()) {
                filterEvents(selectedDate)
            } else {
                Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show()
            }
        }

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

                // Display all events initially
                displayEvents(allEvents)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Events, "Failed to load events: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Format the selected date as "DD-MM-YYYY"
            val selectedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
            datePickerEditText.setText(selectedDate)
        }, year, month, day).show()
    }

    private fun filterEvents(selectedDate: String) {
        // Debugging: Log the selected date
        println("Selected Date for Filtering: $selectedDate")

        // Check what events exist and their dates
        allEvents.forEach { event ->
            println("Event Date: ${event.date}")
        }

        // Filter events based on the selected date
        val filteredEvents = allEvents.filter { event ->
            event.date == selectedDate
        }

        // Debugging: Log the filtered events
        println("Filtered Events: $filteredEvents")

        displayEvents(filteredEvents)
    }

    private fun displayEvents(events: List<Event>) {
        eventsContainer.removeAllViews() // Clear existing views

        if (events.isEmpty()) {
            Toast.makeText(this, "No events found for this date.", Toast.LENGTH_SHORT).show()
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
        val eventDescription: TextView = cardView.findViewById(R.id.event_description)

        eventName.text = event.eventName
        eventDate.text = event.date
        eventDescription.text = event.description

        eventsContainer.addView(cardView)
    }
}

// Event data class for holding event information
data class Event(
    val eventId: String? = null,
    val eventName: String = "",
    val description: String = "",
    val date: String = "", // Ensure this matches the format used in showDatePicker()
    val imageUrl: String? = null // Include this if you plan to display images in the future
)

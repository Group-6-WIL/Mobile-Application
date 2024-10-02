package com.ali.the_ladybird_foundation

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class admin_events : AppCompatActivity() {

    // UI elements
    private lateinit var addEventsBtn: Button
    private lateinit var editEventsBtn: Button
    private lateinit var deleteEventsBtn: Button
    private lateinit var back: Button
    lateinit var dialog: Dialog
    lateinit var eventImageView: ImageView
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    lateinit var dateEditText: EditText
    private var eventsList: List<Event> = emptyList() // Store events for later reference

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_events)

        // Initializing buttons
        addEventsBtn = findViewById(R.id.admin_events_addbtn)
        editEventsBtn = findViewById(R.id.admin_events_editbtn)
        deleteEventsBtn = findViewById(R.id.admin_events_deletebtn)
        back = findViewById(R.id.admin_events_admindashboard)
        dialog = Dialog(this)

        // Set listeners for add, edit, delete and back buttons
        addEventsBtn.setOnClickListener { AddEvent() }
        editEventsBtn.setOnClickListener { EditEvent() }
        deleteEventsBtn.setOnClickListener { DeleteEvent() }

        // Back button functionality: return to Admin Dashboard
        back.setOnClickListener {
            val intentback = Intent(this, Admin_Dashboard::class.java)
            startActivity(intentback)
        }
    }

    // Method to handle adding events
    private fun AddEvent() {
        dialog.setContentView(R.layout.admin_add_event)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        // Initialize UI elements within the dialog
        eventImageView = dialog.findViewById(R.id.admin_imageView)
        val eventName = dialog.findViewById<EditText>(R.id.admin_add_events_eventname)
        val description = dialog.findViewById<EditText>(R.id.admin_add_events_eventdescription)
        dateEditText = dialog.findViewById(R.id.admin_add_event_eventdate)
        val uploadImageBtn = dialog.findViewById<Button>(R.id.admin_add_events_uploadIV)
        val addBtn = dialog.findViewById<Button>(R.id.admin_add_events_save)

        // Set a DatePicker dialog when the date field is clicked
        dateEditText.isFocusable = false
        dateEditText.isClickable = true
        dateEditText.setOnClickListener { showDatePickerDialog() }

        // Upload image button functionality
        uploadImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Save event when the Add button is clicked
        addBtn.setOnClickListener {
            val eventNameStr = eventName.text.toString().trim()
            val descriptionStr = description.text.toString().trim()
            val dateStr = dateEditText.text.toString().trim()

            // Validate if all fields are filled
            if (eventNameStr.isEmpty() || descriptionStr.isEmpty() || dateStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If an image is selected, upload it to Firebase, else save event directly
            if (imageUri != null) {
                uploadImageToFirebase(eventNameStr, descriptionStr, dateStr)
            } else {
                saveEventToDatabase(eventNameStr, descriptionStr, dateStr, null)
            }
        }

        // Show the dialog to add an event
        dialog.show()
    }

    // Method to handle editing an event (similar to adding, but allows modification)
    private fun EditEvent() {
        dialog.setContentView(R.layout.admineditevent)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        eventImageView = dialog.findViewById(R.id.admin_imageViewE)
        val eventName = dialog.findViewById<EditText>(R.id.admin_edit_events_eventname)
        val description = dialog.findViewById<EditText>(R.id.admin_edit_events_eventdescription)
        dateEditText = dialog.findViewById(R.id.admin_edit_event_eventdate)
        val uploadImageBtn = dialog.findViewById<Button>(R.id.admin_edit_events_uploadIV)
        val addBtn = dialog.findViewById<Button>(R.id.admin_edit_events_save)

        // Set date picker
        dateEditText.isFocusable = false
        dateEditText.isClickable = true
        dateEditText.setOnClickListener { showDatePickerDialog() }

        // Allow image upload when clicked
        uploadImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Save event changes when Save button is clicked
        addBtn.setOnClickListener {
            val eventNameStr = eventName.text.toString().trim()
            val descriptionStr = description.text.toString().trim()
            val dateStr = dateEditText.text.toString().trim()

            if (eventNameStr.isEmpty() || descriptionStr.isEmpty() || dateStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageUri != null) {
                uploadImageToFirebase(eventNameStr, descriptionStr, dateStr)
            } else {
                saveEventToDatabase(eventNameStr, descriptionStr, dateStr, null)
            }
        }

        dialog.show()
    }

    // Method to delete an event
    private fun DeleteEvent() {
        dialog.setContentView(R.layout.admindeleteevents)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        val selectEventSpinner = dialog.findViewById<Spinner>(R.id.admin_delete_event_spinner)
        val deleteEventButton = dialog.findViewById<Button>(R.id.admin_delete_event_button)

        // Load events from Firebase and populate the Spinner
        loadEventsIntoSpinner(selectEventSpinner)

        // Delete event when the Delete button is clicked
        deleteEventButton.setOnClickListener {
            val selectedEventName = selectEventSpinner.selectedItem as String
            val selectedEvent = eventsList.find { it.eventName == selectedEventName }
            deleteEvent(selectedEvent?.eventId)
        }

        dialog.show()
    }

    // Load the events from Firebase and populate the Spinner for event deletion
    private fun loadEventsIntoSpinner(spinner: Spinner) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("events")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                eventsList = snapshot.children.mapNotNull { it.getValue(Event::class.java) }
                val adapter = ArrayAdapter(this@admin_events, android.R.layout.simple_spinner_item, eventsList.map { it.eventName })
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@admin_events, "Failed to load events: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Delete the selected event by its ID from Firebase
    private fun deleteEvent(eventId: String?) {
        if (eventId != null) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("events").child(eventId)
            databaseRef.removeValue().addOnSuccessListener {
                Toast.makeText(this@admin_events, "Event deleted successfully!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }.addOnFailureListener {
                Toast.makeText(this@admin_events, "Error deleting event: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this@admin_events, "No event selected!", Toast.LENGTH_SHORT).show()
        }
    }

    // Show date picker dialog when the date field is clicked
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            R.style.CustomDatePickerTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateString = dateFormat.format(selectedDate.time)
                dateEditText.setText(dateString)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    // Upload the image to Firebase storage and save event data to Firebase database
    private fun uploadImageToFirebase(eventName: String, description: String, date: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val fileName = formatter.format(Date())
        val imageRef = storageRef.child("event_images/$fileName")

        imageUri?.let {
            imageRef.putFile(it).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    saveEventToDatabase(eventName, description, date, imageUrl)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Save event information to Firebase database
    private fun saveEventToDatabase(eventName: String, description: String, date: String, imageUrl: String?) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("events")
        val eventId = databaseRef.push().key

        val event = Event(eventId, eventName, description, date, imageUrl)

        eventId?.let {
            databaseRef.child(it).setValue(event).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Failed to add event", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Handle result of image selection and display the selected image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            eventImageView.setImageURI(imageUri)
        }
    }

    // Data class to represent an Event model
    data class Event(
        val eventId: String? = null,
        val eventName: String = "",
        val description: String = "",
        val date: String = "",
        val imageUrl: String? = null
    )
}

package com.ali.the_ladybird_foundation

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class admin_events : AppCompatActivity() {

    // UI elements
    private lateinit var selectEventSpinner: Spinner // Add this line
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
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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

// Adjust the AddEvent and EditEvent methods to call the new saveImageUrlToDatabase correctly
        addBtn.setOnClickListener {
            val eventNameStr = eventName.text.toString().trim()
            val descriptionStr = description.text.toString().trim()
            val dateStr = dateEditText.text.toString().trim()

            if (eventNameStr.isEmpty() || descriptionStr.isEmpty() || dateStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedEvent = eventsList.find { it.eventName == eventNameStr }
            val eventId = selectedEvent?.eventId  // Use the existing event ID if editing

            if (imageUri != null) {
                uploadImageToFirebase(eventNameStr, descriptionStr, dateStr, eventId)
            } else {
                saveImageUrlToDatabase(null, eventNameStr, descriptionStr, dateStr, eventId)
            }
        }


        // Show the dialog to add an event
        dialog.show()
    }

    // Method to handle editing an event (similar to adding, but allows modification)
    private fun EditEvent() {
        dialog.setContentView(R.layout.admineditevent)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        val selectEventSpinner = dialog.findViewById<Spinner>(R.id.admin_edit_event_spinner)
        eventImageView = dialog.findViewById(R.id.admin_imageViewE)
        val eventName = dialog.findViewById<EditText>(R.id.admin_edit_events_eventname)
        val description = dialog.findViewById<EditText>(R.id.admin_edit_events_eventdescription)
        dateEditText = dialog.findViewById(R.id.admin_edit_event_eventdate)
        val uploadImageBtn = dialog.findViewById<Button>(R.id.admin_edit_events_uploadIV)
        val saveBtn = dialog.findViewById<Button>(R.id.admin_edit_events_save)

        // Load events into the spinner
        loadEventsIntoSpinner(selectEventSpinner)

        // When an event is selected, populate the form fields with its data
        selectEventSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedEvent = eventsList[position]
                eventName.setText(selectedEvent.eventName)
                description.setText(selectedEvent.description)
                dateEditText.setText(selectedEvent.date)

                // Optionally load the event's image if available
                selectedEvent.imageUrl?.let { imageUrl ->
                    Glide.with(this@admin_events).load(imageUrl).into(eventImageView)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }

        // Date picker setup
        dateEditText.isFocusable = false
        dateEditText.isClickable = true
        dateEditText.setOnClickListener { showDatePickerDialog() }

        // Image upload setup
        uploadImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Save button to update the event
        saveBtn.setOnClickListener {
            val selectedEvent = eventsList[selectEventSpinner.selectedItemPosition]
            val eventNameStr = eventName.text.toString().trim()
            val descriptionStr = description.text.toString().trim()
            val dateStr = dateEditText.text.toString().trim()

            if (eventNameStr.isEmpty() || descriptionStr.isEmpty() || dateStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update event with or without new image
            if (imageUri != null) {
                uploadImageToFirebase(eventNameStr, descriptionStr, dateStr, selectedEvent.eventId!!)
            } else {
                saveImageUrlToDatabase(null, eventNameStr, descriptionStr, dateStr, selectedEvent.eventId!!)
            }
        }

        dialog.show()
    }

    // Method to delete an event

    private fun DeleteEvent() {
        dialog.setContentView(R.layout.admindeleteevents)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        selectEventSpinner = dialog.findViewById(R.id.admin_delete_event_spinner) // Initialize here
        val deleteEventButton = dialog.findViewById<Button>(R.id.admin_delete_event_button)

        // Load events from Firebase and populate the Spinner
        loadEventsIntoSpinner(selectEventSpinner)

        // Delete event when the Delete button is clicked
        deleteEventButton.setOnClickListener {
            val selectedPosition = selectEventSpinner.selectedItemPosition

            if (eventsList.isNotEmpty() && selectedPosition >= 0) {
                val selectedEvent = eventsList[selectedPosition]
                Log.d("DeleteEvent", "Selected Event ID: ${selectedEvent.eventId}") // Log the selected event ID

                if (selectedEvent.eventId != null) {
                    deleteEvent(selectedEvent.eventId!!)  // Ensure we're passing the ID correctly
                }
            } else {
                Log.d("DeleteEvent", "No event selected or available!") // Log if no event is selected
                Toast.makeText(this, "No event selected or available!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    // Load events and populate spinner with valid data
    private fun loadEventsIntoSpinner(spinner: Spinner) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("events")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                eventsList = snapshot.children.mapNotNull { it.getValue(Event::class.java) }

                if (eventsList.isEmpty()) {
                    Toast.makeText(this@admin_events, "No events available for deletion.", Toast.LENGTH_SHORT).show()
                    spinner.adapter = null // Clear spinner if no events are available
                } else {
                    val eventNames = eventsList.map { it.eventName.takeIf { it.isNotBlank() } ?: "Unnamed Event" }
                    val adapter = ArrayAdapter(this@admin_events, android.R.layout.simple_spinner_item, eventNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@admin_events, "Failed to load events: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Delete the selected event by its ID from Firebase
    private fun deleteEvent(eventId: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("events").child(eventId)
        databaseRef.removeValue().addOnSuccessListener {
            Log.d("DeleteEvent", "Event deleted successfully!") // Log success
            Toast.makeText(this@admin_events, "Event deleted successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()  // Dismiss the dialog after successful deletion
            loadEventsIntoSpinner(selectEventSpinner)  // Reload spinner to reflect changes
        }.addOnFailureListener { exception ->
            Log.e("DeleteEvent", "Error deleting event: ${exception.message}") // Log error
            Toast.makeText(this@admin_events, "Error deleting event: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }


    // Show date picker dialog when the date field is clicked
// Show date picker dialog when the date field is clicked
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dateEditText.setText(dateFormat.format(selectedDate.time))
            },
            year, month, day
        )

        // Apply maroon color to the header text
        datePickerDialog.setOnShowListener {
            // Set the header text (date) to maroon
            val headerText = datePickerDialog.findViewById<TextView>(
                resources.getIdentifier("android:id/date_picker_header_date", null, null)
            )
            headerText?.setTextColor(Color.parseColor("#800000")) // Maroon color

            // Apply black text to the DatePicker dialog's day, month, and year pickers
            val dayPicker = datePickerDialog.findViewById<View>(
                resources.getIdentifier("android:id/day", null, null)
            )
            val monthPicker = datePickerDialog.findViewById<View>(
                resources.getIdentifier("android:id/month", null, null)
            )
            val yearPicker = datePickerDialog.findViewById<View>(
                resources.getIdentifier("android:id/year", null, null)
            )

            listOf(dayPicker, monthPicker, yearPicker).forEach { picker ->
                (picker as? View)?.let { applyBlackTextColor(it) }
            }
        }

        datePickerDialog.show()
    }

    // Helper function to apply black text color to all child TextViews within a View
    private fun applyBlackTextColor(view: View) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyBlackTextColor(view.getChildAt(i))
            }
        } else if (view is TextView) {
            view.setTextColor(resources.getColor(android.R.color.black, theme))
        }
    }



    // Upload the image to Firebase storage and save event data to Firebase database
    private fun uploadImageToFirebase(
        eventName: String,
        description: String,
        date: String,
        eventId: String? = null
    ) {
        val storageRef = FirebaseStorage.getInstance().reference
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val fileName = formatter.format(Date())
        val imageRef = storageRef.child("event_images/$fileName.jpg")

        imageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveImageUrlToDatabase(downloadUri.toString(), eventName, description, date, eventId)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
        }
    }


    // Updated method to save the event to the database
    private fun saveImageUrlToDatabase(
        imageUrl: String?,
        eventName: String,
        description: String,
        date: String,
        eventId: String? = null
    ) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("events")
        val newEventId = eventId ?: databaseRef.push().key  // Use the existing ID or generate a new one

        if (newEventId != null) {
            val event = Event(newEventId, eventName, description, date, imageUrl)
            databaseRef.child(newEventId).setValue(event)
                .addOnSuccessListener {
                    Toast.makeText(this, "Event saved successfully!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()  // Close dialog after saving
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save event. Please try again.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Failed to generate event ID.", Toast.LENGTH_SHORT).show()
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
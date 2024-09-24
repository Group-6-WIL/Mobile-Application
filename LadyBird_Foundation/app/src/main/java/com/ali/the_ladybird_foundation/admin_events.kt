package com.ali.the_ladybird_foundation

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class admin_events : AppCompatActivity() {

    private lateinit var addEventsBtn: Button
    lateinit var dialog: Dialog
    lateinit var eventImageView: ImageView
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    lateinit var dateEditText: EditText // Date EditText for the DatePickerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_events)

        addEventsBtn = findViewById(R.id.admin_events_addbtn)
        dialog = Dialog(this)

        addEventsBtn.setOnClickListener { AddEvent() }
    }

    private fun AddEvent() {
        dialog.setContentView(R.layout.admin_add_event)
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        eventImageView = dialog.findViewById(R.id.admin_imageView)
        val eventName = dialog.findViewById<EditText>(R.id.admin_add_events_eventname)
        val description = dialog.findViewById<EditText>(R.id.admin_add_events_eventdescription)
        dateEditText = dialog.findViewById(R.id.admin_add_event_eventdate) // Date EditText
        val uploadImageBtn = dialog.findViewById<Button>(R.id.admin_add_events_uploadIV)
        val addBtn = dialog.findViewById<Button>(R.id.admin_add_events_save)

        // Make the Date EditText non-editable and trigger DatePickerDialog on click
        dateEditText.isFocusable = false
        dateEditText.isClickable = true
        dateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        // Image upload functionality
        uploadImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Save event when Add button is clicked
        addBtn.setOnClickListener {
            val eventNameStr = eventName.text.toString().trim()
            val descriptionStr = description.text.toString().trim()
            val dateStr = dateEditText.text.toString().trim()

            if (eventNameStr.isEmpty() || descriptionStr.isEmpty() || dateStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageUri != null) {
                // If image is selected, upload it first and then save event
                uploadImageToFirebase(eventNameStr, descriptionStr, dateStr)
            } else {
                // Save event without image
                saveEventToDatabase(eventNameStr, descriptionStr, dateStr, null)
            }
        }

        dialog.show()
    }

    // Method to show DatePickerDialog
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Use the default DatePickerDialog constructor that includes the OK and Cancel buttons
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            eventImageView.setImageURI(imageUri)
        }
    }

    // Event model class
    data class Event(
        val eventId: String?,
        val eventName: String,
        val description: String,
        val date: String,
        val imageUrl: String?
    )
}

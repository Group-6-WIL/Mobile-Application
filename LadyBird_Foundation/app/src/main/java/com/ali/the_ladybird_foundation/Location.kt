package com.ali.the_ladybird_foundation

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Location : AppCompatActivity() {
    private var donationTable: TableLayout? = null
    private var databaseReference: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        donationTable = findViewById(R.id.donation_table)
        databaseReference = FirebaseDatabase.getInstance().getReference("locations")


        // Fetch data from Firebase and populate the table
        loadDonationSites()
    }

    private fun loadDonationSites() {
        databaseReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val name = snapshot.child("name").getValue(
                        String::class.java
                    )
                    val address = snapshot.child("address").getValue(
                        String::class.java
                    )
                    val addressLink = snapshot.child("addressLink").getValue(
                        String::class.java
                    )

                    // Dynamically create a new row for each donation site
                    val row = TableRow(this@Location)
                    row.layoutParams = TableRow.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    // Create TextViews for each column
                    val nameTextView = TextView(this@Location)
                    nameTextView.text = name
                    nameTextView.setPadding(16, 16, 16, 16)
                    nameTextView.gravity = Gravity.START
                    val addressTextView = TextView(this@Location)
                    addressTextView.text = address
                    addressTextView.setPadding(16, 16, 16, 16)
                    addressTextView.gravity = Gravity.START

                    // Add the TextViews to the row
                    row.addView(nameTextView)
                    row.addView(addressTextView)

                    // Add the row to the TableLayout
                    donationTable!!.addView(row)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors
            }
        })
    }
}

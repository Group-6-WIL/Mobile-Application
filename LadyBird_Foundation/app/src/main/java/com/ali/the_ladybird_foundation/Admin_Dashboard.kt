package com.ali.the_ladybird_foundation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class Admin_Dashboard : AppCompatActivity() {

    private lateinit var locationBtn : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        locationBtn = findViewById(R.id.admin_location_IV)


        locationBtn.setOnClickListener {
            val intentLocation = Intent(this, adminLocation::class.java)
            startActivity(intentLocation)
        }
    }
}
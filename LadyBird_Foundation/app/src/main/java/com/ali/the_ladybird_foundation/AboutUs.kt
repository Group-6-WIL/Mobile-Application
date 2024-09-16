package com.ali.the_ladybird_foundation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2

class AboutUs : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about_us)

        val imageList = listOf(
            R.drawable.img1,
            R.drawable.img4,
            R.drawable.img3
        )

        val viewPager: ViewPager2 = findViewById(R.id.imageCarousel)
        viewPager.adapter = ImageAdapter(imageList)


    }
}
package com.ali.the_ladybird_foundation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2

class AboutUs : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about_us) // Corrected layout file

        val imageList = listOf(
            R.drawable.img1,
            R.drawable.img3,
            R.drawable.img4
        )

        viewPager = findViewById(R.id.imageCarousel)
        viewPager.adapter = ImageAdapter(imageList)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.about_root_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up the auto-slide feature
        setupAutoSlide(imageList.size)
    }

    private fun setupAutoSlide(itemCount: Int) {
        runnable = Runnable {
            currentPage = (currentPage + 1) % itemCount
            viewPager.setCurrentItem(currentPage, true)
            handler.postDelayed(runnable, 3000) // Slide every 3 seconds
        }
        handler.postDelayed(runnable, 3000) // Initial delay
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) // Clean up the handler when the activity is destroyed
    }
}

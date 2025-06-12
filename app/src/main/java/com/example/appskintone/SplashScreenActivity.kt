package com.example.appskintone

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val secondActbutton = findViewById<Button>(R.id.button)
        secondActbutton.setOnClickListener {
            val intent = Intent(this, CardViewActivity::class.java)
            startActivity(intent)



        }
    }
}
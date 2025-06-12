package com.example.appskintone

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


import android.content.Intent
import android.widget.Toast

import androidx.cardview.widget.CardView

import com.example.appskintone.user.Dataset
import com.example.appskintone.user.Profile


class CardViewActivity : AppCompatActivity() {

    private lateinit var profile: CardView
    private lateinit var dataset: CardView
    private lateinit var simulasi: CardView
    private lateinit var logout: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_view)

        profile = findViewById(R.id.profileCard)
        dataset = findViewById(R.id.datasetCard)
        simulasi = findViewById(R.id.Simulasi)
        logout = findViewById(R.id.logoutCard)

        profile.setOnClickListener {
            showToast("Profil")
            val intent = Intent(this@CardViewActivity, Profile::class.java)
            startActivity(intent)
        }
        dataset.setOnClickListener {
            showToast("Dataset")
            val intent = Intent(this@CardViewActivity, Dataset::class.java)
            startActivity(intent)
        }

        simulasi.setOnClickListener {
            showToast("Simulasi")
            val intent = Intent(this@CardViewActivity, MainActivity::class.java)
            startActivity(intent)
        }
        logout.setOnClickListener {
            showToast("Berhasil Keluar")

            val intent = Intent(this@CardViewActivity, SplashScreenActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

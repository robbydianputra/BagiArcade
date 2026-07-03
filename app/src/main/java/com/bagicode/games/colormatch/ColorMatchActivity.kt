package com.bagicode.games.colormatch

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bagicode.games.R

class ColorMatchActivity : AppCompatActivity() {

    private lateinit var colorMatchView: ColorMatchView
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_color_match)

        colorMatchView = findViewById(R.id.colorMatchView)
        nextButton = findViewById(R.id.nextButton)
        val exitButton = findViewById<ImageButton>(R.id.exitButton)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        exitButton.setOnClickListener { confirmExit() }

        colorMatchView.onAllMatched = {
            nextButton.isEnabled = true
            showSuccessDialog()
        }

        nextButton.setOnClickListener {
            colorMatchView.nextLevel()
            nextButton.isEnabled = false
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hebat!")
            .setMessage("Semua warna cocok!")
            .setPositiveButton("Lanjut") { _, _ ->
                colorMatchView.nextLevel()
                nextButton.isEnabled = false
            }
            .setCancelable(false)
            .show()
    }

    private fun confirmExit() {
        AlertDialog.Builder(this)
            .setTitle("Keluar Game")
            .setMessage("Apakah Anda yakin ingin berhenti bermain?")
            .setPositiveButton("Ya") { _, _ -> finish() }
            .setNegativeButton("Tidak", null)
            .show()
    }
}

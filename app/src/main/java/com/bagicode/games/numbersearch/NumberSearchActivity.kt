package com.bagicode.games.numbersearch

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bagicode.games.R

class NumberSearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_number_search)

        val exitButton = findViewById<ImageButton>(R.id.exitButton)
        val numberSearchView = findViewById<NumberSearchView>(R.id.numberSearchView)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        exitButton.setOnClickListener { confirmExit() }
        
        numberSearchView.onWin = {
            showWinDialog()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                confirmExit()
            }
        })
    }

    private fun showWinDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hebat!")
            .setMessage("Kamu berhasil menemukan semua angka!")
            .setCancelable(false)
            .setPositiveButton("Main Lagi") { _, _ ->
                findViewById<NumberSearchView>(R.id.numberSearchView).nextLevel()
            }
            .setNegativeButton("Keluar") { _, _ ->
                finish()
            }
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

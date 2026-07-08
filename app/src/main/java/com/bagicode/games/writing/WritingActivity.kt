package com.bagicode.games.writing

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bagicode.games.R
import kotlin.random.Random

class WritingActivity : AppCompatActivity() {

    private lateinit var writingView: WritingView
    private var currentNum = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_writing)

        writingView = findViewById(R.id.writingView)
        val clearBtn = findViewById<ImageButton>(R.id.clearButton)
        val checkBtn = findViewById<ImageButton>(R.id.checkButton)
        val exitBtn = findViewById<ImageButton>(R.id.exitButton)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        exitBtn.setOnClickListener { confirmExit() }

        clearBtn.setOnClickListener { writingView.clear() }
        
        checkBtn.setOnClickListener {
            writingView.checkWriting()
        }

        writingView.onScoreResult = { isCorrect ->
            if (isCorrect) {
                showSuccessDialog()
            } else {
                Toast.makeText(this, "Coba lagi, tulisanmu belum pas!", Toast.LENGTH_SHORT).show()
            }
        }

        nextNumber()
    }

    private fun nextNumber() {
        currentNum = Random.nextInt(0, 9)
        writingView.setNumber(currentNum)
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Bagus Sekali!")
            .setMessage("Angka $currentNum kamu tulis dengan benar!")
            .setPositiveButton("Angka Lain") { _, _ ->
                nextNumber()
            }
            .setCancelable(false)
            .show()
    }

    private fun confirmExit() {
        AlertDialog.Builder(this)
            .setTitle("Keluar Game")
            .setMessage("Apakah Anda yakin ingin berhenti belajar menulis?")
            .setPositiveButton("Ya") { _, _ -> finish() }
            .setNegativeButton("Tidak", null)
            .show()
    }
}

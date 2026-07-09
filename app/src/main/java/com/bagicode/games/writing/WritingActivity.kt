package com.bagicode.games.writing

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bagicode.games.databinding.ActivityWritingBinding
import kotlin.random.Random

class WritingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWritingBinding
    private var currentNum = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWritingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.exitButton.setOnClickListener { confirmExit() }

        binding.clearButton.setOnClickListener { binding.writingView.clear() }
        
        binding.checkButton.setOnClickListener {
            binding.writingView.checkWriting()
        }

        binding.writingView.onScoreResult = { isCorrect ->
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
        binding.writingView.setNumber(currentNum)
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

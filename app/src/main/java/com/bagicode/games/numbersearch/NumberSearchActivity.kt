package com.bagicode.games.numbersearch

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bagicode.games.databinding.ActivityNumberSearchBinding

class NumberSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNumberSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNumberSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.exitButton.setOnClickListener { confirmExit() }
        
        binding.numberSearchView.onWin = {
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
                binding.numberSearchView.nextLevel()
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

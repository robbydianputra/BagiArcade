package com.bagicode.games.numbermatch

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bagicode.games.databinding.ActivityNumberMatchBinding

class NumberMatchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNumberMatchBinding
    private val handler = Handler(Looper.getMainLooper())

    private val regenerateRunnable = object : Runnable {
        override fun run() {
            // 🔥 RE-RANDOM POSISI ANGKA
            val randomDraft = randomDraftNumber()
            binding.gameNumberView.setTargetNumber(randomDraft)
            binding.tvTarget.text = "Ayo temukan Angka $randomDraft kecil pada $randomDraft besar"

            // 🔁 ulang 30 detik lagi
            handler.postDelayed(this, 60_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNumberMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.gameNumberView.setTargetNumber(1)
        binding.gameNumberView.setGameListener(
            object : NumberCanvasView.GameListener {
                override fun onGameComplete() {
                    stopTimer()
                    showNextDialog()
                }
            }
        )

        binding.exitButton.setOnClickListener { confirmExit() }

        startTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(regenerateRunnable)
    }

    private fun showNextDialog(){
        val randomDraft = randomDraftNumber()
        AlertDialog.Builder(this)
            .setTitle("Hebat! 🎉")
            .setMessage("Semua angka sudah ditemukan")
            .setCancelable(false)
            .setPositiveButton("Next"){ dialog, _ ->
                dialog.dismiss()
                binding.tvTarget.text = "Ayo temukan Angka $randomDraft kecil pada $randomDraft besar"
                binding.gameNumberView.setTargetNumber(randomDraft)
                startTimer()
            }
            .show()

    }

    private fun randomDraftNumber() : Int {
        return kotlin.random.Random.nextInt(1, 9)
    }

    private fun startTimer() {
        handler.removeCallbacks(regenerateRunnable)
        handler.postDelayed(regenerateRunnable, 60_000)
    }

    private fun stopTimer() {
        handler.removeCallbacks(regenerateRunnable)
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

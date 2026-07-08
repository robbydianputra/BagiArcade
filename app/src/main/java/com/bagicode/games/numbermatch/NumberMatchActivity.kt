package com.bagicode.games.numbermatch

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bagicode.games.R

class NumberMatchActivity : AppCompatActivity() {

    private lateinit var gameNumberView: NumberCanvasView
    private lateinit var tvTarget: TextView

    private val handler = Handler(Looper.getMainLooper())

    private val regenerateRunnable = object : Runnable {
        override fun run() {
            // 🔥 RE-RANDOM POSISI ANGKA
            val randomDraft = randomDraftNumber()
            gameNumberView.setTargetNumber(randomDraft)
            tvTarget.setText("Ayo temukan Angka $randomDraft kecil pada $randomDraft besar")

            // 🔁 ulang 30 detik lagi
            handler.postDelayed(this, 60_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_number_match)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvTarget = findViewById<TextView>(R.id.tvTarget)
        gameNumberView = findViewById<NumberCanvasView>(R.id.gameNumberView)
        val exitButton = findViewById<ImageButton>(R.id.exitButton)

        gameNumberView.setTargetNumber(1)
        gameNumberView.setGameListener(
            object : NumberCanvasView.GameListener {
                override fun onGameComplete() {
                    stopTimer()
                    showNextDialog()
                }
            }
        )

        exitButton.setOnClickListener { confirmExit() }

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
                tvTarget.setText("Ayo temukan Angka $randomDraft kecil pada $randomDraft besar")
                gameNumberView.setTargetNumber(randomDraft)
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
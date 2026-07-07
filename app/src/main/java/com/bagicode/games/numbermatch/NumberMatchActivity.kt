package com.bagicode.games.numbermatch

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
            tvTarget.setText("Cari semua angka $randomDraft")

            // 🔁 ulang 30 detik lagi
            handler.postDelayed(this, 30_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_number_match)

        tvTarget = findViewById<TextView>(R.id.tvTarget)
        gameNumberView = findViewById<NumberCanvasView>(R.id.gameNumberView)

        gameNumberView.setTargetNumber(1)
        gameNumberView.setGameListener(
            object : NumberCanvasView.GameListener {
                override fun onGameComplete() {
                    stopTimer()
                    showNextDialog()
                }
            }
        )

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
                tvTarget.setText("Cari semua angka $randomDraft")
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
        handler.postDelayed(regenerateRunnable, 30_000)
    }

    private fun stopTimer() {
        handler.removeCallbacks(regenerateRunnable)
    }
}
package com.bagicode.games.numbermatch

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bagicode.games.R
import java.util.Random

class NumberMatchActivity : AppCompatActivity() {

    private lateinit var gameNumberView: NumberCanvasView

    private val handler = Handler(Looper.getMainLooper())

    private val regenerateRunnable = object : Runnable {
        override fun run() {

            // 🔥 RE-RANDOM POSISI ANGKA
            val randomDraft = kotlin.random.Random.nextInt(1, 9)
            gameNumberView.setTargetNumber(randomDraft)

            // 🔁 ulang 30 detik lagi
            handler.postDelayed(this, 10_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_number_match)

        gameNumberView = findViewById<NumberCanvasView>(R.id.gameNumberView)
        gameNumberView.setTargetNumber(1)

        handler.postDelayed(regenerateRunnable, 30_000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(regenerateRunnable)
    }

}
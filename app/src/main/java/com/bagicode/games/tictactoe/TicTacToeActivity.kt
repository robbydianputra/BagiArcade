package com.bagicode.games.tictactoe

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bagicode.games.R

class TicTacToeActivity : AppCompatActivity() {

    private lateinit var ticTacToeView: TicTacToeView
    private lateinit var statusTextTop: TextView
    private lateinit var statusTextBottom: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tictactoe)

        ticTacToeView = findViewById(R.id.ticTacToeView)
        statusTextTop = findViewById(R.id.statusTextTop)
        statusTextBottom = findViewById(R.id.statusTextBottom)
        val exitTop = findViewById<ImageButton>(R.id.exitButtonTop)
        val exitBottom = findViewById<ImageButton>(R.id.exitButtonBottom)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val confirmExit = {
            AlertDialog.Builder(this)
                .setTitle("Keluar Game")
                .setMessage("Apakah Anda yakin ingin berhenti bermain?")
                .setPositiveButton("Ya") { _, _ -> finish() }
                .setNegativeButton("Tidak", null)
                .show()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                confirmExit()
            }
        })

        exitTop.setOnClickListener { confirmExit() }
        exitBottom.setOnClickListener { confirmExit() }

        ticTacToeView.onMoveListener = {
            updateUI()
            if (ticTacToeView.game.isGameOver) {
                showWinnerDialog()
            }
        }
    }

    private fun updateUI() {
        val status = "Turn: ${ticTacToeView.game.currentTurn}"
        statusTextTop.text = status
        statusTextBottom.text = status
    }

    private fun showWinnerDialog() {
        val game = ticTacToeView.game
        val message = when {
            game.isDraw -> "It's a Draw!"
            else -> "Winner: ${game.winner}"
        }

        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Play Again") { _, _ ->
                game.resetGame()
                ticTacToeView.invalidate()
                updateUI()
            }
            .setNegativeButton("Close") { _, _ ->
                finish()
            }
            .show()
    }
}

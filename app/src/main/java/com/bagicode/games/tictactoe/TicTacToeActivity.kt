package com.bagicode.games.tictactoe

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bagicode.games.databinding.ActivityTictactoeBinding

class TicTacToeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTictactoeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTictactoeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
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

        binding.exitButtonTop.setOnClickListener { confirmExit() }
        binding.exitButtonBottom.setOnClickListener { confirmExit() }

        binding.ticTacToeView.onMoveListener = {
            updateUI()
            if (binding.ticTacToeView.game.isGameOver) {
                showWinnerDialog()
            }
        }
    }

    private fun updateUI() {
        val status = "Turn: ${binding.ticTacToeView.game.currentTurn}"
        binding.statusTextTop.text = status
        binding.statusTextBottom.text = status
    }

    private fun showWinnerDialog() {
        val game = binding.ticTacToeView.game
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
                binding.ticTacToeView.invalidate()
                updateUI()
            }
            .setNegativeButton("Close") { _, _ ->
                finish()
            }
            .show()
    }
}

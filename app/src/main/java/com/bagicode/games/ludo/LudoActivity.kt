package com.bagicode.games.ludo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.animation.RotateAnimation
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bagicode.games.R
import com.bagicode.games.databinding.ActivityLudoBinding
import kotlin.random.Random

class LudoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLudoBinding
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLudoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.exitButtonTop.setOnClickListener { confirmExit() }
        binding.exitButtonBottom.setOnClickListener { confirmExit() }

        binding.diceImageView.setOnClickListener {
            if (binding.ludoView.game.canRollDice) {
                showDiceRollDialog()
            }
        }

        binding.ludoView.onPieceSelected = { pieceId ->
            if (binding.ludoView.game.movePiece(pieceId)) {
                updateUI()
                binding.ludoView.invalidate()
                if (binding.ludoView.game.isGameOver) {
                    showWinnerDialog()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                confirmExit()
            }
        })

        showPlayerCountDialog()
    }
    
    private fun updateDiceIcon(roll: Int) {
        // Implementation remains same or could use binding if needed
    }

    private fun showDiceRollDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_dice, null)
        val diceView = dialogView.findViewById<DiceView>(R.id.dialogDiceView)
        val rollingText = dialogView.findViewById<TextView>(R.id.rollingText)
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        var count = 0
        val runnable = object : Runnable {
            override fun run() {
                if (count < 12) {
                    val tempRoll = Random.nextInt(1, 7)
                    diceView.roll = tempRoll
                    
                    val rotate = RotateAnimation(0f, 90f, diceView.width / 2f, diceView.height / 2f)
                    rotate.duration = 100
                    diceView.startAnimation(rotate)
                    
                    count++
                    handler.postDelayed(this, 100)
                } else {
                    val finalRoll = binding.ludoView.game.rollDice()
                    diceView.roll = finalRoll
                    rollingText.text = "You rolled a $finalRoll!"
                    
                    handler.postDelayed({
                        dialog.dismiss()
                        updateUI()
                        binding.ludoView.invalidate()
                        
                        val currentPlayer = binding.ludoView.game.players[binding.ludoView.game.currentTurnIndex]
                        if (binding.ludoView.game.getMovablePieces(currentPlayer, finalRoll).isEmpty() && finalRoll != 6) {
                            handler.postDelayed({
                                binding.ludoView.game.nextTurn()
                                updateUI()
                                binding.ludoView.invalidate()
                            }, 1000)
                        }
                    }, 1000)
                }
            }
        }
        handler.post(runnable)
    }

    private fun showPlayerCountDialog() {
        val options = arrayOf("2 Players", "3 Players", "4 Players")
        AlertDialog.Builder(this)
            .setTitle("Select Players")
            .setItems(options) { _, which ->
                binding.ludoView.game.setPlayerCount(which + 2)
                updateUI()
                binding.ludoView.invalidate()
            }
            .setCancelable(false)
            .show()
    }

    private fun updateUI() {
        val currentPlayer = binding.ludoView.game.players[binding.ludoView.game.currentTurnIndex]
        val status = "Turn: $currentPlayer"
        binding.statusTextTop.text = status
        binding.statusTextBottom.text = status
        
        binding.diceImageView.alpha = if (binding.ludoView.game.canRollDice) 1.0f else 0.5f
    }

    private fun confirmExit() {
        AlertDialog.Builder(this)
            .setTitle("Keluar Game")
            .setMessage("Apakah Anda yakin ingin berhenti bermain?")
            .setPositiveButton("Ya") { _, _ -> finish() }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun showWinnerDialog() {
        val winner = binding.ludoView.game.winner
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("Winner: $winner")
            .setCancelable(false)
            .setPositiveButton("Play Again") { _, _ ->
                binding.ludoView.game.resetGame()
                binding.ludoView.invalidate()
                updateUI()
            }
            .setNegativeButton("Close") { _, _ ->
                finish()
            }
            .show()
    }
}

package com.bagicode.games.ludo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
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

        val diceClickListener = View.OnClickListener {
            if (binding.ludoView.game.canRollDice) {
                showDiceRollDialog()
            }
        }
        
        binding.diceImageViewTop.setOnClickListener(diceClickListener)
        binding.diceImageViewBottom.setOnClickListener(diceClickListener)

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
        val game = binding.ludoView.game
        val currentPlayer = game.players[game.currentTurnIndex]
        val status = "Turn: $currentPlayer"
        binding.statusTextTop.text = status
        binding.statusTextBottom.text = status
        
        // Logic to move dice:
        // RED and GREEN are Top players.
        // YELLOW and BLUE are Bottom players.
        if (currentPlayer == LudoPlayer.RED || currentPlayer == LudoPlayer.GREEN) {
            binding.diceImageViewTop.visibility = View.VISIBLE
            binding.diceImageViewBottom.visibility = View.INVISIBLE
            binding.diceImageViewTop.alpha = if (game.canRollDice) 1.0f else 0.5f

            val params = binding.diceImageViewTop.layoutParams as LinearLayout.LayoutParams
            params.gravity = if(currentPlayer == LudoPlayer.RED) Gravity.END else Gravity.START
            binding.diceImageViewTop.layoutParams = params
        } else {
            binding.diceImageViewTop.visibility = View.INVISIBLE
            binding.diceImageViewBottom.visibility = View.VISIBLE
            binding.diceImageViewBottom.alpha = if (game.canRollDice) 1.0f else 0.5f

            val params = binding.diceImageViewTop.layoutParams as LinearLayout.LayoutParams
            params.gravity = if(currentPlayer == LudoPlayer.BLUE) Gravity.START else Gravity.END
            binding.diceImageViewBottom.layoutParams = params
        }
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

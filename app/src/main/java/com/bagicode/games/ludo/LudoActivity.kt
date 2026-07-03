package com.bagicode.games.ludo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.animation.RotateAnimation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bagicode.games.R
import kotlin.random.Random

class LudoActivity : AppCompatActivity() {

    private lateinit var ludoView: LudoView
    private lateinit var statusTextTop: TextView
    private lateinit var statusTextBottom: TextView
    private lateinit var diceImageView: ImageView
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ludo)

        ludoView = findViewById(R.id.ludoView)
        statusTextTop = findViewById(R.id.statusTextTop)
        statusTextBottom = findViewById(R.id.statusTextBottom)
        diceImageView = findViewById(R.id.diceImageView)
        val exitTop = findViewById<ImageButton>(R.id.exitButtonTop)
        val exitBottom = findViewById<ImageButton>(R.id.exitButtonBottom)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        exitTop.setOnClickListener { confirmExit() }
        exitBottom.setOnClickListener { confirmExit() }

        diceImageView.setOnClickListener {
            if (ludoView.game.canRollDice) {
                showDiceRollDialog()
            }
        }

        ludoView.onPieceSelected = { pieceId ->
            if (ludoView.game.movePiece(pieceId)) {
                updateUI()
                ludoView.invalidate()
                if (ludoView.game.isGameOver) {
                    showWinnerDialog()
                }
            }
        }

        showPlayerCountDialog()
    }
    
    private fun updateDiceIcon(roll: Int) {
        // This is called during rolling animation and for the final result
        // The DiceView inside the dialog handles the drawing
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
                    val finalRoll = ludoView.game.rollDice()
                    diceView.roll = finalRoll
                    rollingText.text = "You rolled a $finalRoll!"
                    
                    handler.postDelayed({
                        dialog.dismiss()
                        updateUI()
                        ludoView.invalidate()
                        
                        // If no movable pieces, auto skip
                        val currentPlayer = ludoView.game.players[ludoView.game.currentTurnIndex]
                        if (ludoView.game.getMovablePieces(currentPlayer, finalRoll).isEmpty() && finalRoll != 6) {
                            handler.postDelayed({
                                ludoView.game.nextTurn()
                                updateUI()
                                ludoView.invalidate()
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
                ludoView.game.setPlayerCount(which + 2)
                updateUI()
                ludoView.invalidate()
            }
            .setCancelable(false)
            .show()
    }

    private fun updateUI() {
        val currentPlayer = ludoView.game.players[ludoView.game.currentTurnIndex]
        val status = "Turn: $currentPlayer"
        statusTextTop.text = status
        statusTextBottom.text = status
        
        diceImageView.alpha = if (ludoView.game.canRollDice) 1.0f else 0.5f
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
        val winner = ludoView.game.winner
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("Winner: $winner")
            .setCancelable(false)
            .setPositiveButton("Play Again") { _, _ ->
                ludoView.game.resetGame()
                ludoView.invalidate()
                updateUI()
            }
            .setNegativeButton("Close") { _, _ ->
                finish()
            }
            .show()
    }
}

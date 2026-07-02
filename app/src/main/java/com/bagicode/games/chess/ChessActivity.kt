package com.bagicode.games.chess

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bagicode.games.R
import com.bagicode.games.chess.model.PieceType
import com.bagicode.games.chess.model.Player
import com.bagicode.games.chess.view.ChessView

class ChessActivity : AppCompatActivity() {
    
    private lateinit var chessView: ChessView
    private lateinit var statusTextTop: TextView
    private lateinit var statusTextBottom: TextView
    private lateinit var timerText1: TextView
    private lateinit var timerText2: TextView
    private lateinit var player2Layout: LinearLayout
    
    private var timer1: CountDownTimer? = null
    private var timer2: CountDownTimer? = null
    
    private var timerDurationMinutes = 5
    private var timeLeft1 = 300000L
    private var timeLeft2 = 300000L
    private var timerEnabled = true

    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            applySettings()
            resetGame()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chess)
        
        chessView = findViewById(R.id.chessView)
        statusTextTop = findViewById(R.id.statusTextTop)
        statusTextBottom = findViewById(R.id.statusTextBottom)
        timerText1 = findViewById(R.id.player1Timer)
        timerText2 = findViewById(R.id.player2Timer)
        player2Layout = findViewById(R.id.player2Layout)
        
        val resign1 = findViewById<Button>(R.id.resignButton1)
        val resign2 = findViewById<Button>(R.id.resignButton2)
        val settingsButtonTop = findViewById<ImageButton>(R.id.settingsButtonTop)
        val settingsButtonBottom = findViewById<ImageButton>(R.id.settingsButtonBottom)
        val exitButtonTop = findViewById<ImageButton>(R.id.exitButtonTop)
        val exitButtonBottom = findViewById<ImageButton>(R.id.exitButtonBottom)

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

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                confirmExit()
            }
        })

        exitButtonTop.setOnClickListener { confirmExit() }
        exitButtonBottom.setOnClickListener { confirmExit() }

        chessView.onMoveListener = {
            updateUI()
            if (timerEnabled && !chessView.game.isPromotionPending()) {
                switchTimer()
            }
            if (chessView.game.isGameOver) {
                stopTimers()
                showWinnerDialog()
            }
        }

        chessView.game.onPromotionRequired = { _, _ ->
            showPromotionDialog()
        }

        resign1.setOnClickListener {
            chessView.game.resign(Player.WHITE)
            updateStatusAndStop()
            showWinnerDialog()
        }

        resign2.setOnClickListener {
            chessView.game.resign(Player.BLACK)
            updateStatusAndStop()
            showWinnerDialog()
        }
        
        val openSettings = {
            val intent = Intent(this, SettingsActivity::class.java)
            settingsLauncher.launch(intent)
        }
        
        settingsButtonTop.setOnClickListener { openSettings() }
        settingsButtonBottom.setOnClickListener { openSettings() }

        applySettings()
        resetGame()
    }

    private fun showWinnerDialog() {
        val winner = chessView.game.winner
        val isCheck = chessView.game.isInCheck(chessView.game.board, chessView.game.currentTurn)
        
        val title = if (winner != null) {
            if (isCheck) "Checkmate!" else "Resigned"
        } else {
            "Stalemate"
        }
        
        val message = if (winner != null) "Winner: $winner" else "It's a Draw!"
        
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Play Again") { _, _ ->
                resetGame()
            }
            .setNegativeButton("Close") { _, _ ->
                finish()
            }
            .show()
    }

    private fun showPromotionDialog() {
        val player = chessView.game.currentTurn
        
        val options = arrayOf("Queen", "Rook", "Bishop", "Knight")
        val types = arrayOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)
        
        val unicodes = arrayOf("\u265B", "\u265C", "\u265D", "\u265E")

        val adapter = object : ArrayAdapter<String>(this, R.layout.item_promotion, R.id.pieceName, options) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val iconText = view.findViewById<TextView>(R.id.pieceIconText)
                val text = view.findViewById<TextView>(R.id.pieceName)
                
                iconText.text = unicodes[position]
                if (player == Player.WHITE) {
                    iconText.setTextColor(Color.WHITE)
                    iconText.setShadowLayer(2f, 0f, 0f, Color.BLACK)
                } else {
                    iconText.setTextColor(Color.BLACK)
                    iconText.setShadowLayer(0f, 0f, 0f, 0)
                }

                text.text = options[position]
                return view
            }
        }
        
        AlertDialog.Builder(this)
            .setTitle("Pawn Promotion")
            .setAdapter(adapter) { _, which ->
                chessView.game.promotePawn(types[which])
                chessView.invalidate()
                updateUI()
                if (chessView.game.isGameOver) {
                    stopTimers()
                    showWinnerDialog()
                } else if (timerEnabled) {
                    switchTimer()
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun applySettings() {
        val prefs = getSharedPreferences("chess_prefs", MODE_PRIVATE)
        val mirrorMode = prefs.getBoolean("mirror_mode", true)
        val predictionEnabled = prefs.getBoolean("prediction_enabled", true)
        timerEnabled = prefs.getBoolean("timer_enabled", true)
        timerDurationMinutes = prefs.getInt("timer_duration", 5)
        
        chessView.isMirrorMode = mirrorMode
        chessView.isPredictionEnabled = predictionEnabled
        player2Layout.rotation = if (mirrorMode) 180f else 0f
        
        timerText1.visibility = if (timerEnabled) View.VISIBLE else View.GONE
        timerText2.visibility = if (timerEnabled) View.VISIBLE else View.GONE
        
        updateUI()
        chessView.invalidate()
    }

    private fun updateUI() {
        val status = chessView.getStatus()
        statusTextTop.text = status
        statusTextBottom.text = status
        
        val prefs = getSharedPreferences("chess_prefs", MODE_PRIVATE)
        val mirrorMode = prefs.getBoolean("mirror_mode", true)
        
        statusTextTop.rotation = if (mirrorMode) 180f else 0f
    }

    private fun updateStatusAndStop() {
        updateUI()
        stopTimers()
        chessView.invalidate()
    }

    private fun resetGame() {
        chessView.reset()
        updateUI()
        
        timeLeft1 = timerDurationMinutes * 60 * 1000L
        timeLeft2 = timerDurationMinutes * 60 * 1000L
        
        updateTimerTexts()
        stopTimers()
        
        if (timerEnabled) {
            startTimer1()
        }
    }

    private fun switchTimer() {
        stopTimers()
        if (chessView.game.currentTurn == Player.WHITE) {
            startTimer1()
        } else {
            startTimer2()
        }
    }

    private fun startTimer1() {
        timer1 = object : CountDownTimer(timeLeft1, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft1 = millisUntilFinished
                updateTimerTexts()
            }
            override fun onFinish() {
                chessView.game.resign(Player.WHITE)
                updateStatusAndStop()
                showWinnerDialog()
            }
        }.start()
    }

    private fun startTimer2() {
        timer2 = object : CountDownTimer(timeLeft2, 1000) {
            override fun onTick(timeLeft: Long) {
                timeLeft2 = timeLeft
                updateTimerTexts()
            }
            override fun onFinish() {
                chessView.game.resign(Player.BLACK)
                updateStatusAndStop()
                showWinnerDialog()
            }
        }.start()
    }

    private fun stopTimers() {
        timer1?.cancel()
        timer2?.cancel()
    }

    private fun updateTimerTexts() {
        timerText1.text = "WHITE: ${formatTime(timeLeft1)}"
        timerText2.text = "BLACK: ${formatTime(timeLeft2)}"
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimers()
    }
}

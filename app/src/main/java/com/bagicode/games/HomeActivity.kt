package com.bagicode.games

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bagicode.games.chess.ChessActivity
import com.bagicode.games.chess.adapter.GameAdapter
import com.bagicode.games.chess.model.GameMenu
import com.bagicode.games.tictactoe.TicTacToeActivity
import com.bagicode.games.ludo.LudoActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val recyclerView = findViewById<RecyclerView>(R.id.menuRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val games = listOf(
            GameMenu(
                title = "Chess",
                description = "2 Player Local PvP with standard rules",
                iconRes = R.drawable.ic_chess,
                activityClass = ChessActivity::class.java
            ),
            GameMenu(
                title = "Tic-Tac-Toe",
                description = "Classic 3x3 grid game",
                iconRes = R.drawable.ic_tictactoe,
                activityClass = TicTacToeActivity::class.java
            ),
            GameMenu(
                title = "Ludo",
                description = "Classic race game for 2 players",
                iconRes = R.drawable.ic_ludo,
                activityClass = LudoActivity::class.java
            )
        )

        recyclerView.adapter = GameAdapter(games) { game ->
            val intent = Intent(this, game.activityClass)
            startActivity(intent)
        }
    }
}

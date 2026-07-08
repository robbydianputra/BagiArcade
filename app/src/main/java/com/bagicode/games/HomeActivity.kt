package com.bagicode.games

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bagicode.games.chess.ChessActivity
import com.bagicode.games.chess.adapter.GameAdapter
import com.bagicode.games.chess.model.GameMenu
import com.bagicode.games.tictactoe.TicTacToeActivity
import com.bagicode.games.ludo.LudoActivity
import com.bagicode.games.colormatch.ColorMatchActivity
import com.bagicode.games.dino.DinoActivity
import com.bagicode.games.numbermatch.NumberMatchActivity
import com.bagicode.games.numbersearch.NumberSearchActivity
import com.bagicode.games.writing.WritingActivity
import com.bagicode.games.shapematch.ShapeMatchActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var adapter: GameAdapter
    private lateinit var allGames: List<GameMenu>
    private var isGridView = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val recyclerView = findViewById<RecyclerView>(R.id.menuRecyclerView)
        val searchView = findViewById<SearchView>(R.id.searchView)
        val layoutButton = findViewById<ImageButton>(R.id.layoutToggleButton)

        allGames = listOf(
            GameMenu(
                title = "Color Match",
                description = "Match fruits with crayon colors",
                iconRes = R.drawable.ic_match_color,
                activityClass = ColorMatchActivity::class.java
            ),
            GameMenu(
                title = "Shape Match",
                description = "Match objects with their shadows",
                iconRes = R.drawable.ic_match_shape,
                activityClass = ShapeMatchActivity::class.java
            ),
            GameMenu(
                title = "Number Match",
                description = "Find numbers hidden inside big shapes",
                iconRes = R.drawable.ic_number_match,
                activityClass = NumberMatchActivity::class.java
            ),
            GameMenu(
                title = "Number Search",
                description = "Find and mark numbers with colored discs",
                iconRes = R.drawable.ic_number_search,
                activityClass = NumberSearchActivity::class.java
            ),
            GameMenu(
                title = "Number Writing",
                description = "Learn to write numbers correctly",
                iconRes = R.drawable.ic_number_writing,
                activityClass = WritingActivity::class.java
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
            ),
            GameMenu(
                title = "Chess",
                description = "2 Player Local PvP with standard rules",
                iconRes = R.drawable.ic_chess,
                activityClass = ChessActivity::class.java
            ),
            GameMenu(
                title = "Dino",
                description = "dino rules",
                iconRes = R.drawable.ic_chess,
                activityClass = DinoActivity::class.java
            ),
        )

        adapter = GameAdapter(allGames) { game ->
            val intent = Intent(this, game.activityClass)
            startActivity(intent)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterGames(newText)
                return true
            }
        })

        layoutButton.setOnClickListener {
            isGridView = !isGridView
            if (isGridView) {
                recyclerView.layoutManager = GridLayoutManager(this, 2)
                layoutButton.setImageResource(R.drawable.ic_view_list)
            } else {
                recyclerView.layoutManager = LinearLayoutManager(this)
                layoutButton.setImageResource(R.drawable.ic_view_grid)
            }
            adapter.isGridView = isGridView
        }
    }

    private fun filterGames(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            allGames
        } else {
            allGames.filter { it.title.contains(query, ignoreCase = true) }
        }
        adapter.updateList(filteredList)
    }
}

package com.bagicode.games

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bagicode.games.BuildConfig
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
import com.bagicode.games.carrace.CarRaceActivity
import com.bagicode.games.databinding.ActivityHomeBinding
import com.bagicode.games.maze.MazeActivity
import com.bagicode.games.repository.`interface`.ApiService
import com.bagicode.games.update.model.UpdateData
import com.bagicode.games.update.ui.UpdateDialog
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: GameAdapter
    private lateinit var allGames: List<GameMenu>
    private var isGridView = false
    val currentVersionCode = BuildConfig.VERSION_CODE
    private val apiService by lazy { ApiService.create() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val composeView = ComposeView(this)
        binding.root.addView(composeView)
        getFlagVersion(composeView)

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
                iconRes = R.drawable.ic_dino_run,
                activityClass = DinoActivity::class.java
            ),
            GameMenu(
                title = "Car Race",
                description = "Avoid obstacles and race to the finish",
                iconRes = R.drawable.ic_car_race,
                activityClass = CarRaceActivity::class.java
            ),
            GameMenu(
                title = "Maze",
                description = "lets go home",
                iconRes = R.mipmap.ic_launcher_round,
                activityClass = MazeActivity::class.java
            ),
        )

        adapter = GameAdapter(allGames) { game ->
            val intent = Intent(this, game.activityClass)
            startActivity(intent)
        }
        
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.menuRecyclerView.adapter = adapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterGames(newText)
                return true
            }
        })

        binding.layoutToggleButton.setOnClickListener {
            isGridView = !isGridView
            if (isGridView) {
                binding.menuRecyclerView.layoutManager = GridLayoutManager(this, 2)
                binding.layoutToggleButton.setImageResource(R.drawable.ic_view_list)
            } else {
                binding.menuRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.layoutToggleButton.setImageResource(R.drawable.ic_view_grid)
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

    private fun getFlagVersion(composeView : ComposeView) {
        lifecycleScope.launch {
            try {
                val response = apiService.getAppVersion()
                val updateData = response.data?.mappingToUi() ?: UpdateData()

                // Cek apakah build number dari server lebih tinggi dari aplikasi saat ini
                if (updateData.buildNumber > currentVersionCode) {
                    // Set isi ComposeView hanya jika kondisi update terpenuhi
                    composeView.setContent {
                        var showDialog by remember { mutableStateOf(true) }

                        if (showDialog) {
                            UpdateDialog(
                                updateData = updateData,
                                onDismiss = { showDialog = false }
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Log jika terjadi gangguan koneksi atau kegagalan parsing API
                Log.e("HomeActivity", "Gagal mengambil data versi: ${e.message}")
            }
        }
    }
}

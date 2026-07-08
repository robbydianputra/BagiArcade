package com.bagicode.games.chess.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bagicode.games.R
import com.bagicode.games.chess.model.GameMenu

class GameAdapter(
    private var games: List<GameMenu>,
    private val onItemClick: (GameMenu) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    var isGridView: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.gameIcon)
        val title: TextView = view.findViewById(R.id.gameTitle)
        val description: TextView? = view.findViewById(R.id.gameDescription) // might be null in grid
    }

    override fun getItemViewType(position: Int): Int {
        return if (isGridView) VIEW_TYPE_GRID else VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val layout = if (viewType == VIEW_TYPE_GRID) R.layout.item_game_grid else R.layout.item_game_menu
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        holder.title.text = game.title
        holder.description?.text = game.description
        holder.icon.setImageResource(game.iconRes)
        holder.itemView.setOnClickListener { onItemClick(game) }
    }

    override fun getItemCount() = games.size

    fun updateList(newList: List<GameMenu>) {
        games = newList
        notifyDataSetChanged()
    }

    companion object {
        private const val VIEW_TYPE_LIST = 1
        private const val VIEW_TYPE_GRID = 2
    }
}

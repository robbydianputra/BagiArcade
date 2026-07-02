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
    private val games: List<GameMenu>,
    private val onItemClick: (GameMenu) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.gameIcon)
        val title: TextView = view.findViewById(R.id.gameTitle)
        val description: TextView = view.findViewById(R.id.gameDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game_menu, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        holder.title.text = game.title
        holder.description.text = game.description
        holder.icon.setImageResource(game.iconRes)
        holder.itemView.setOnClickListener { onItemClick(game) }
    }

    override fun getItemCount() = games.size
}
package com.bagicode.games.chess.model

data class GameMenu(
    val title: String,
    val description: String,
    val iconRes: Int,
    val activityClass: Class<*>
)
package com.bagicode.games.numbermatch

data class SmallNumber(
    val value: Int,
    val x: Float,
    val y: Float,
    var found: Boolean = false,
    var wrong: Boolean = false
)
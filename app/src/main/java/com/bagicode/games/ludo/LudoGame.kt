package com.bagicode.games.ludo

import kotlin.random.Random

enum class LudoPlayer { RED, GREEN, YELLOW, BLUE }

data class LudoPiece(
    val player: LudoPlayer,
    val id: Int,
    var position: Int = -1, // -1 base, 0-51 path, 52-57 home
    var isFinished: Boolean = false
)

class LudoGame {
    var players = mutableListOf(LudoPlayer.RED, LudoPlayer.GREEN)
    var pieces = mutableListOf<LudoPiece>()
    var currentTurnIndex = 0
    var lastDiceRoll = 0
    var canRollDice = true
    var isGameOver = false
    var winner: LudoPlayer? = null

    // Standard Ludo Safe Squares (Global Indices)
    private val safeSquares = listOf(0, 8, 13, 21, 26, 34, 39, 47)

    init {
        resetGame()
    }

    fun setPlayerCount(count: Int) {
        players.clear()
        val allPlayers = LudoPlayer.entries
        for (i in 0 until count.coerceAtMost(4)) {
            players.add(allPlayers[i])
        }
        resetGame()
    }

    fun resetGame() {
        pieces.clear()
        for (player in LudoPlayer.entries) {
            for (i in 0..3) {
                pieces.add(LudoPiece(player, i))
            }
        }
        currentTurnIndex = 0
        lastDiceRoll = 0
        canRollDice = true
        isGameOver = false
        winner = null
    }

    fun rollDice(): Int {
        if (!canRollDice) return lastDiceRoll
        lastDiceRoll = Random.nextInt(1, 7)
        canRollDice = false 
        return lastDiceRoll
    }

    fun movePiece(pieceId: Int): Boolean {
        val currentPlayer = players[currentTurnIndex]
        val piece = pieces.find { it.player == currentPlayer && it.id == pieceId } ?: return false
        
        if (canRollDice) return false
        if (!canMovePiece(piece, lastDiceRoll)) return false

        var bonusTurn = false

        if (piece.position == -1) {
            if (lastDiceRoll == 6) {
                piece.position = 0 
                bonusTurn = true // Roll again after getting out
            } else return false
        } else {
            piece.position += lastDiceRoll
            if (piece.position >= 57) {
                piece.position = 57
                piece.isFinished = true
                bonusTurn = true // Bonus for finishing
            }
        }

        // Capture logic
        if (piece.position in 0..51 && !piece.isFinished) {
            val globalPos = getGlobalPosition(piece)
            if (globalPos !in safeSquares) {
                pieces.forEach { other ->
                    if (other.player != piece.player && other.position != -1 && !other.isFinished) {
                        if (getGlobalPosition(other) == globalPos) {
                            other.position = -1 
                            bonusTurn = true // Bonus for capture
                        }
                    }
                }
            }
        }

        checkWinner()
        
        if (!isGameOver) {
            if (lastDiceRoll != 6 && !bonusTurn) {
                nextTurn()
            } else {
                canRollDice = true
            }
        }
        return true
    }

    fun nextTurn() {
        currentTurnIndex = (currentTurnIndex + 1) % players.size
        canRollDice = true
    }

    fun getMovablePieces(player: LudoPlayer, roll: Int): List<LudoPiece> {
        return pieces.filter { it.player == player && canMovePiece(it, roll) }
    }

    private fun canMovePiece(piece: LudoPiece, roll: Int): Boolean {
        if (piece.isFinished) return false
        if (piece.position == -1) return roll == 6
        if (piece.position + roll > 57) return false
        return true
    }

    fun getStartingPosition(player: LudoPlayer): Int {
        return when (player) {
            LudoPlayer.RED -> 0
            LudoPlayer.GREEN -> 13
            LudoPlayer.YELLOW -> 26
            LudoPlayer.BLUE -> 39
        }
    }

    fun getGlobalPosition(piece: LudoPiece): Int {
        if (piece.position == -1 || piece.position >= 52) return -1
        val start = getStartingPosition(piece.player)
        return (start + piece.position) % 52
    }

    private fun checkWinner() {
        val currentPlayer = players[currentTurnIndex]
        if (pieces.count { it.player == currentPlayer && it.isFinished } == 4) {
            isGameOver = true
            winner = currentPlayer
        }
    }
}

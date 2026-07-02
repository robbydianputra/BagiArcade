package com.bagicode.games.tictactoe

enum class TicTacToePiece { X, O }

class TicTacToeGame {
    var board = Array(3) { arrayOfNulls<TicTacToePiece>(3) }
    var currentTurn = TicTacToePiece.X
    var isGameOver = false
    var winner: TicTacToePiece? = null
        private set
    var isDraw = false
        private set

    fun resetGame() {
        board = Array(3) { arrayOfNulls<TicTacToePiece>(3) }
        currentTurn = TicTacToePiece.X
        isGameOver = false
        winner = null
        isDraw = false
    }

    fun handleTouch(row: Int, col: Int): Boolean {
        if (isGameOver || row !in 0..2 || col !in 0..2) return false
        if (board[row][col] != null) return false

        board[row][col] = currentTurn
        checkGameOver()
        if (!isGameOver) {
            currentTurn = if (currentTurn == TicTacToePiece.X) TicTacToePiece.O else TicTacToePiece.X
        }
        return true
    }

    private fun checkGameOver() {
        // Check rows
        for (i in 0..2) {
            if (board[i][0] != null && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                setWinner(board[i][0])
                return
            }
        }

        // Check columns
        for (i in 0..2) {
            if (board[0][i] != null && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                setWinner(board[0][i])
                return
            }
        }

        // Check diagonals
        if (board[0][0] != null && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            setWinner(board[0][0])
            return
        }
        if (board[0][2] != null && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            setWinner(board[0][2])
            return
        }

        // Check draw
        if (board.all { row -> row.all { it != null } }) {
            isGameOver = true
            isDraw = true
        }
    }

    private fun setWinner(piece: TicTacToePiece?) {
        isGameOver = true
        winner = piece
    }
}

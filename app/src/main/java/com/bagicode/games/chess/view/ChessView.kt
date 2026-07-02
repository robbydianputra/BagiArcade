package com.bagicode.games.chess.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.bagicode.games.chess.model.ChessGame
import com.bagicode.games.chess.model.ChessPiece
import com.bagicode.games.chess.model.PieceType
import com.bagicode.games.chess.model.Player

class ChessView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint()
    val game = ChessGame()
    private var cellSize = 0f
    var onMoveListener: (() -> Unit)? = null
    var isMirrorMode = true
    var isPredictionEnabled = true

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        cellSize = width.coerceAtMost(height) / 8f

        drawBoard(canvas)
        drawCheckHighlight(canvas)
        drawSelection(canvas)
        drawPredictions(canvas)
        drawPieces(canvas)
    }

    private fun drawBoard(canvas: Canvas) {
        for (row in 0..7) {
            for (col in 0..7) {
                paint.color = if ((row + col) % 2 == 0) Color.parseColor("#EEEED2") else Color.parseColor("#769656")
                canvas.drawRect(col * cellSize, row * cellSize, (col + 1) * cellSize, (row + 1) * cellSize, paint)
            }
        }
    }

    private fun drawCheckHighlight(canvas: Canvas) {
        // Highlight king in red if in check
        val players = listOf(Player.WHITE, Player.BLACK)
        for (player in players) {
            if (game.isInCheck(game.board, player)) {
                findKing(player)?.let { (r, c) ->
                    paint.color = Color.argb(150, 255, 0, 0)
                    canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, paint)
                }
            }
        }
    }

    private fun findKing(player: Player): Pair<Int, Int>? {
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = game.board[r][c]
                if (piece?.type == PieceType.KING && piece.player == player) return r to c
            }
        }
        return null
    }

    private fun drawPieces(canvas: Canvas) {
        paint.textSize = cellSize * 0.8f
        paint.textAlign = Paint.Align.CENTER
        val fontMetrics = paint.fontMetrics
        val yOffset = (fontMetrics.ascent + fontMetrics.descent) / 2

        for (row in 0..7) {
            for (col in 0..7) {
                val piece = game.board[row][col]
                if (piece != null) {
                    val centerX = col * cellSize + cellSize / 2
                    val centerY = row * cellSize + cellSize / 2

                    canvas.save()
                    // Rotate Black pieces 180 degrees if mirror mode is on
                    if (isMirrorMode && piece.player == Player.BLACK) {
                        canvas.rotate(180f, centerX, centerY)
                    }

                    if (piece.player == Player.WHITE) {
                        paint.style = Paint.Style.FILL
                        paint.color = Color.WHITE
                        val text = getPieceUnicode(piece)
                        canvas.drawText(text, centerX, centerY - yOffset, paint)

                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 2f
                        paint.color = Color.BLACK
                        canvas.drawText(text, centerX, centerY - yOffset, paint)
                        paint.style = Paint.Style.FILL
                    } else {
                        paint.color = Color.BLACK
                        val text = getPieceUnicode(piece)
                        canvas.drawText(text, centerX, centerY - yOffset, paint)
                    }
                    canvas.restore()
                }
            }
        }
    }

    private fun drawSelection(canvas: Canvas) {
        game.selectedSquare?.let { (row, col) ->
            paint.color = Color.argb(100, 255, 255, 0)
            canvas.drawRect(col * cellSize, row * cellSize, (col + 1) * cellSize, (row + 1) * cellSize, paint)
        }
    }

    private fun drawPredictions(canvas: Canvas) {
        if (!isPredictionEnabled) return

        game.selectedSquare?.let { (fromRow, fromCol) ->
            for (toRow in 0..7) {
                for (toCol in 0..7) {
                    if (game.isValidMoveAndSafeForPrediction(fromRow, fromCol, toRow, toCol)) {
                        paint.color = Color.argb(100, 0, 255, 0)
                        canvas.drawCircle(
                            toCol * cellSize + cellSize / 2,
                            toRow * cellSize + cellSize / 2,
                            cellSize / 6,
                            paint
                        )
                    }
                }
            }
        }
    }

    private fun getPieceUnicode(piece: ChessPiece): String {
        return when (piece.type) {
//            PieceType.KING -> if (piece.player == Player.WHITE) "\u2654" else "\u265A"
//            PieceType.QUEEN -> if (piece.player == Player.WHITE) "\u2655" else "\u265B"
//            PieceType.ROOK -> if (piece.player == Player.WHITE) "\u2656" else "\u265C"
//            PieceType.BISHOP -> if (piece.player == Player.WHITE) "\u2657" else "\u265D"
//            PieceType.KNIGHT -> if (piece.player == Player.WHITE) "\u2658" else "\u265E"
//            PieceType.PAWN -> if (piece.player == Player.WHITE) "\u2659" else "\u265F"

            // coba ganti warna dah
            PieceType.KING -> "\u265A"
            PieceType.QUEEN -> "\u265B"
            PieceType.ROOK -> "\u265C"
            PieceType.BISHOP -> "\u265D"
            PieceType.KNIGHT -> "\u265E"
            PieceType.PAWN -> "\u265F"
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val col = (event.x / cellSize).toInt()
            val row = (event.y / cellSize).toInt()

            if (row in 0..7 && col in 0..7) {
                if (game.handleTouch(row, col)) {
                    performClick()
                    invalidate()
                    onMoveListener?.invoke()
                }
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun getStatus(): String {
        if (game.isGameOver) return "Winner: ${game.winner}"
        val checkStatus = if (game.isInCheck(game.board, game.currentTurn)) " (CHECK!)" else ""
        return "Turn: ${game.currentTurn}$checkStatus"
    }

    fun reset() {
        game.resetGame()
        invalidate()
    }
}
package com.bagicode.games.tictactoe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class TicTacToeView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }
    val game = TicTacToeGame()
    private var cellSize = 0f
    private var boardSize = 0f
    private var offsetX = 0f
    private var offsetY = 0f

    var onMoveListener: (() -> Unit)? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        boardSize = width.coerceAtMost(height) * 0.8f
        cellSize = boardSize / 3f
        offsetX = (width - boardSize) / 2f
        offsetY = (height - boardSize) / 2f

        canvas.save()
        canvas.translate(offsetX, offsetY)
        drawGrid(canvas)
        drawPieces(canvas)
        canvas.restore()
    }

    private fun drawGrid(canvas: Canvas) {
        paint.color = Color.BLACK
        paint.strokeWidth = 10f
        // Vertical lines
        canvas.drawLine(cellSize, 0f, cellSize, boardSize, paint)
        canvas.drawLine(cellSize * 2, 0f, cellSize * 2, boardSize, paint)
        // Horizontal lines
        canvas.drawLine(0f, cellSize, boardSize, cellSize, paint)
        canvas.drawLine(0f, cellSize * 2, boardSize, cellSize * 2, paint)
    }

    private fun drawPieces(canvas: Canvas) {
        val padding = cellSize * 0.2f
        for (r in 0..2) {
            for (c in 0..2) {
                val piece = game.board[r][c] ?: continue
                val left = c * cellSize + padding
                val top = r * cellSize + padding
                val right = (c + 1) * cellSize - padding
                val bottom = (r + 1) * cellSize - padding

                if (piece == TicTacToePiece.X) {
                    paint.color = Color.RED
                    canvas.drawLine(left, top, right, bottom, paint)
                    canvas.drawLine(right, top, left, bottom, paint)
                } else {
                    paint.color = Color.BLUE
                    canvas.drawOval(left, top, right, bottom, paint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val touchX = event.x - offsetX
            val touchY = event.y - offsetY
            
            if (touchX in 0f..boardSize && touchY in 0f..boardSize) {
                val col = (touchX / cellSize).toInt()
                val row = (touchY / cellSize).toInt()
                
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
}

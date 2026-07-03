package com.bagicode.games.ludo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class LudoView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint().apply { isAntiAlias = true }
    val game = LudoGame()
    private var cellSize = 0f
    private var offsetX = 0f
    private var offsetY = 0f
    
    var onPieceSelected: ((Int) -> Unit)? = null

    private val pathPoints = listOf(
        PointF(1f, 6f), PointF(2f, 6f), PointF(3f, 6f), PointF(4f, 6f), PointF(5f, 6f),
        PointF(6f, 5f), PointF(6f, 4f), PointF(6f, 3f), PointF(6f, 2f), PointF(6f, 1f), PointF(6f, 0f),
        PointF(7f, 0f), PointF(8f, 0f),
        PointF(8f, 1f), PointF(8f, 2f), PointF(8f, 3f), PointF(8f, 4f), PointF(8f, 5f),
        PointF(9f, 6f), PointF(10f, 6f), PointF(11f, 6f), PointF(12f, 6f), PointF(13f, 6f), PointF(14f, 6f),
        PointF(14f, 7f), PointF(14f, 8f),
        PointF(13f, 8f), PointF(12f, 8f), PointF(11f, 8f), PointF(10f, 8f), PointF(9f, 8f),
        PointF(8f, 9f), PointF(8f, 10f), PointF(8f, 11f), PointF(8f, 12f), PointF(8f, 13f), PointF(8f, 14f),
        PointF(7f, 14f), PointF(6f, 14f),
        PointF(6f, 13f), PointF(6f, 12f), PointF(6f, 11f), PointF(6f, 10f), PointF(6f, 9f),
        PointF(5f, 8f), PointF(4f, 8f), PointF(3f, 8f), PointF(2f, 8f), PointF(1f, 8f), PointF(0f, 8f),
        PointF(0f, 7f), PointF(0f, 6f)
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val boardSize = width.coerceAtMost(height).toFloat()
        cellSize = boardSize / 15f
        offsetX = (width - boardSize) / 2f
        offsetY = (height - boardSize) / 2f
        
        canvas.save()
        canvas.translate(offsetX, offsetY)
        drawBoard(canvas)
        drawHighlights(canvas)
        drawPieces(canvas)
        canvas.restore()
    }

    private fun drawHighlights(canvas: Canvas) {
        if (game.canRollDice) return
        val currentPlayer = game.players.getOrNull(game.currentTurnIndex) ?: return
        val movablePieces = game.getMovablePieces(currentPlayer, game.lastDiceRoll)
        val highlightColor = Color.parseColor("#80FFA000")
        
        movablePieces.forEach { piece ->
            val coords = getBoardPieceCoordinates(piece, false)
            paint.style = Paint.Style.FILL
            paint.color = highlightColor
            canvas.drawCircle(coords.x, coords.y, cellSize * 0.7f, paint)
        }
    }

    private fun drawBoard(canvas: Canvas) {
        val boardRed = Color.parseColor("#E53935")
        val boardGreen = Color.parseColor("#43A047")
        val boardYellow = Color.parseColor("#FDD835")
        val boardBlue = Color.parseColor("#1E88E5")

        paint.style = Paint.Style.STROKE
        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        for (i in 0..14) {
            for (j in 0..14) {
                canvas.drawRect(i * cellSize, j * cellSize, (i + 1) * cellSize, (j + 1) * cellSize, paint)
            }
        }

        // Safe Squares (stars)
//        val safeIndices = listOf(0, 8, 13, 21, 26, 34, 39, 47)
        val safeIndices = listOf(0, 13, 26, 39)
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#E0E0E0")
        safeIndices.forEach { idx ->
            val p = pathPoints[idx]
            canvas.drawRect(p.x * cellSize, p.y * cellSize, (p.x + 1) * cellSize, (p.y + 1) * cellSize, paint)
        }

        drawBase(canvas, 0, 0, boardRed)
        drawBase(canvas, 9, 0, boardGreen)
        drawBase(canvas, 9, 9, boardYellow)
        drawBase(canvas, 0, 9, boardBlue)

        drawHomeStretch(canvas, 1, 7, 5, 1, boardRed) 
        drawHomeStretch(canvas, 7, 1, 1, 5, boardGreen)
        drawHomeStretch(canvas, 9, 7, 5, 1, boardYellow)
        drawHomeStretch(canvas, 7, 9, 1, 5, boardBlue)
        
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawRect(6 * cellSize, 6 * cellSize, 9 * cellSize, 9 * cellSize, paint)
        val path = Path()
        paint.color = boardRed
        path.reset(); path.moveTo(6*cellSize, 6*cellSize); path.lineTo(7.5f*cellSize, 7.5f*cellSize); path.lineTo(6*cellSize, 9*cellSize); path.close()
        canvas.drawPath(path, paint)
        paint.color = boardGreen
        path.reset(); path.moveTo(6*cellSize, 6*cellSize); path.lineTo(7.5f*cellSize, 7.5f*cellSize); path.lineTo(9*cellSize, 6*cellSize); path.close()
        canvas.drawPath(path, paint)
        paint.color = boardYellow
        path.reset(); path.moveTo(9*cellSize, 6*cellSize); path.lineTo(7.5f*cellSize, 7.5f*cellSize); path.lineTo(9*cellSize, 9*cellSize); path.close()
        canvas.drawPath(path, paint)
        paint.color = boardBlue
        path.reset(); path.moveTo(6*cellSize, 9*cellSize); path.lineTo(7.5f*cellSize, 7.5f*cellSize); path.lineTo(9*cellSize, 9*cellSize); path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawBase(canvas: Canvas, x: Int, y: Int, color: Int) {
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawRect(x * cellSize, y * cellSize, (x + 6) * cellSize, (y + 6) * cellSize, paint)
        paint.color = Color.WHITE
        canvas.drawRect((x + 1) * cellSize, (y + 1) * cellSize, (x + 5) * cellSize, (y + 5) * cellSize, paint)
        paint.color = color
        val basePos = listOf(PointF(x+2f, y+2f), PointF(x+4f, y+2f), PointF(x+2f, y+4f), PointF(x+4f, y+4f))
        basePos.forEach { canvas.drawCircle(it.x * cellSize, it.y * cellSize, cellSize * 0.6f, paint) }
    }

    private fun drawHomeStretch(canvas: Canvas, x: Int, y: Int, w: Int, h: Int, color: Int) {
        paint.style = Paint.Style.FILL
        paint.color = color
        for (i in 0 until w) {
            for (j in 0 until h) {
                canvas.drawRect((x + i) * cellSize, (y + j) * cellSize, (x + i + 1) * cellSize, (y + j + 1) * cellSize, paint)
            }
        }
    }

    private fun drawPieces(canvas: Canvas) {
        val currentPlayer = game.players.getOrNull(game.currentTurnIndex)
        val boardRed = Color.parseColor("#E53935")
        val boardGreen = Color.parseColor("#43A047")
        val boardYellow = Color.parseColor("#FDD835")
        val boardBlue = Color.parseColor("#1E88E5")

        game.pieces.forEach { piece ->
            if (piece.isFinished || piece.player !in game.players) return@forEach
            val coords = getBoardPieceCoordinates(piece, true)
            
            if (piece.player == currentPlayer && !game.canRollDice) {
                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#E65100")
                paint.strokeWidth = 10f
                canvas.drawCircle(coords.x, coords.y, cellSize * 0.58f, paint)
            }

            paint.style = Paint.Style.FILL
            paint.color = when(piece.player) {
                LudoPlayer.RED -> boardRed
                LudoPlayer.GREEN -> boardGreen
                LudoPlayer.YELLOW -> boardYellow
                LudoPlayer.BLUE -> boardBlue
            }
            canvas.drawCircle(coords.x, coords.y, cellSize * 0.45f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.WHITE
            paint.strokeWidth = 4f
            canvas.drawCircle(coords.x, coords.y, cellSize * 0.45f, paint)
        }
    }

    private fun getBoardPieceCoordinates(piece: LudoPiece, applyStacking: Boolean): PointF {
        val rawPos: PointF
        if (piece.position == -1) {
            val basePos = when(piece.player) {
                LudoPlayer.RED -> PointF(0f, 0f)
                LudoPlayer.GREEN -> PointF(9f, 0f)
                LudoPlayer.YELLOW -> PointF(9f, 9f)
                LudoPlayer.BLUE -> PointF(0f, 9f)
            }
            val offset = listOf(PointF(2f, 2f), PointF(4f, 2f), PointF(2f, 4f), PointF(4f, 4f))[piece.id]
            rawPos = PointF((basePos.x + offset.x) * cellSize, (basePos.y + offset.y) * cellSize)
        } else if (piece.position >= 52) {
            val step = piece.position - 52
            val gridPos = when(piece.player) {
                LudoPlayer.RED -> PointF(step + 1f, 7f)
                LudoPlayer.GREEN -> PointF(7f, step + 1f)
                LudoPlayer.YELLOW -> PointF(13f - step, 7f)
                LudoPlayer.BLUE -> PointF(7f, 13f - step)
            }
            rawPos = PointF((gridPos.x + 0.5f) * cellSize, (gridPos.y + 0.5f) * cellSize)
        } else {
            val startIdx = when(piece.player) {
                LudoPlayer.RED -> 0
                LudoPlayer.GREEN -> 13
                LudoPlayer.YELLOW -> 26
                LudoPlayer.BLUE -> 39
            }
            val gridPos = pathPoints[(startIdx + piece.position) % 52]
            rawPos = PointF((gridPos.x + 0.5f) * cellSize, (gridPos.y + 0.5f) * cellSize)
        }

        if (applyStacking && piece.position != -1 && piece.position < 52) {
            val globalPos = game.getGlobalPosition(piece)
            val piecesOnSameSquare = game.pieces.filter { 
                it.player in game.players && it.position != -1 && it.position < 52 && game.getGlobalPosition(it) == globalPos 
            }
            if (piecesOnSameSquare.size > 1) {
                val index = piecesOnSameSquare.indexOf(piece)
                val offset = (index - (piecesOnSameSquare.size - 1) / 2f) * (cellSize * 0.2f)
                return PointF(rawPos.x + offset, rawPos.y + offset)
            }
        }
        return rawPos
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val boardX = event.x - offsetX
            val boardY = event.y - offsetY
            val currentPlayer = game.players.getOrNull(game.currentTurnIndex)
            
            val clickedPiece = game.pieces.find { piece ->
                if (piece.player != currentPlayer || piece.isFinished) return@find false
                val coords = getBoardPieceCoordinates(piece, true)
                val dist = Math.hypot((boardX - coords.x).toDouble(), (boardY - coords.y).toDouble())
                dist < cellSize * 1.0f
            }
            
            clickedPiece?.let {
                performClick()
                onPieceSelected?.invoke(it.id)
            }
        }
        return true
    }
    
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}

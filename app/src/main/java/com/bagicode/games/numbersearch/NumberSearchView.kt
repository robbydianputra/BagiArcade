package com.bagicode.games.numbersearch

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

data class Marker(
    val value: Int,
    val color: Int,
    var x: Float,
    var y: Float,
    var isDragging: Boolean = false,
    var placedCell: Pair<Int, Int>? = null,
    var isTrayMarker: Boolean = false
)

class NumberSearchView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    
    private val columns = 5
    private val rows = 6 // 1 header + 5 grid rows
    private var cellSize = 0f
    
    private val gridNumbers = Array(rows) { IntArray(columns) }
    private val colorPool = listOf(
        Color.parseColor("#3949AB"), // Blue
        Color.parseColor("#F06292"), // Pink
        Color.parseColor("#9C27B0"), // Purple
        Color.parseColor("#424242"), // Grey
        Color.parseColor("#FB8C00"), // Orange
        Color.parseColor("#E53935"), // Red
        Color.parseColor("#00ACC1"), // Teal
        Color.parseColor("#43A047"), // Green
        Color.parseColor("#880E4F"), // Maroon
        Color.parseColor("#FDD835")  // Yellow
    )
    
    private var currentLevelTargets = mutableListOf<Int>()
    private var currentLevelColors = mutableListOf<Int>()
    
    private val markers = mutableListOf<Marker>()
    private var activeMarker: Marker? = null

    var onWin: (() -> Unit)? = null

    init {
        nextLevel()
    }

    fun nextLevel() {
        val allNumbers = listOf(10, 20, 30, 40, 50, 60, 70, 80, 90, 100)
        val selected = allNumbers.shuffled().take(columns)
        currentLevelTargets.clear()
        currentLevelTargets.addAll(selected)
        
        currentLevelColors.clear()
        currentLevelColors.addAll(colorPool.shuffled().take(columns))
        
        // Generate grid
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                if (r == 0) {
                    gridNumbers[r][c] = currentLevelTargets[c]
                } else {
                    // Randomly pick from current targets to ensure they appear
                    gridNumbers[r][c] = currentLevelTargets[Random.nextInt(columns)]
                }
            }
        }
        
        markers.clear()
        if (width > 0) {
            setupTray()
        }
        invalidate()
    }

    private fun setupTray() {
        val trayY = cellSize * (rows + 1)
        for (i in 0 until columns) {
            markers.add(
                Marker(
                    value = currentLevelTargets[i],
                    color = currentLevelColors[i],
                    x = (i + 0.5f) * cellSize,
                    y = trayY,
                    isTrayMarker = true
                )
            )
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellSize = w.toFloat() / columns
        if (markers.isEmpty()) {
            setupTray()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawGrid(canvas)
        drawMarkers(canvas)
    }

    private fun drawGrid(canvas: Canvas) {
        paint.style = Paint.Style.STROKE
        paint.color = Color.LTGRAY
        paint.strokeWidth = 2f
        textPaint.textSize = cellSize * 0.4f
        
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                val left = c * cellSize
                val top = r * cellSize
                val right = left + cellSize
                val bottom = top + cellSize
                
                canvas.drawRect(left, top, right, bottom, paint)
                
                if (r == 0) {
                    textPaint.color = currentLevelColors[c]
                } else {
                    textPaint.color = Color.DKGRAY
                }
                
                canvas.drawText(
                    gridNumbers[r][c].toString(),
                    left + cellSize / 2,
                    top + cellSize / 2 + textPaint.textSize / 3,
                    textPaint
                )
            }
        }
    }

    private fun drawMarkers(canvas: Canvas) {
        markers.forEach { marker ->
            paint.style = Paint.Style.FILL
            paint.color = marker.color
            paint.alpha = 180 
            canvas.drawCircle(marker.x, marker.y, cellSize * 0.4f, paint)
            
            paint.style = Paint.Style.STROKE
            paint.color = Color.WHITE
            paint.strokeWidth = 4f
            paint.alpha = 255
            canvas.drawCircle(marker.x, marker.y, cellSize * 0.4f, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Find a tray marker to drag
                activeMarker = markers.findLast { it.isTrayMarker && Math.hypot((event.x - it.x).toDouble(), (event.y - it.y).toDouble()) < cellSize * 0.5 }
                
                activeMarker?.let {
                    // Create a copy to drag so tray marker stays (or we decide based on logic)
                    val newMarker = it.copy(isTrayMarker = false, isDragging = true)
                    markers.add(newMarker)
                    activeMarker = newMarker
                }
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                activeMarker?.let {
                    it.x = event.x
                    it.y = event.y
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                activeMarker?.let { marker ->
                    val col = (marker.x / cellSize).toInt()
                    val row = (marker.y / cellSize).toInt()
                    
                    // Check if dropped on correct grid cell
                    val isCorrect = row in 1 until rows && col in 0 until columns && 
                                    gridNumbers[row][col] == marker.value &&
                                    markers.none { it.placedCell == (row to col) }

                    if (isCorrect) {
                        marker.x = (col + 0.5f) * cellSize
                        marker.y = (row + 0.5f) * cellSize
                        marker.placedCell = row to col
                        marker.isDragging = false
                        
                        checkTrayVisibility(marker.value)
                    } else {
                        markers.remove(marker)
                    }
                    
                    activeMarker = null
                    invalidate()
                    checkWin()
                    performClick()
                }
            }
        }
        return true
    }

    private fun checkTrayVisibility(value: Int) {
        // Count how many times this value appears in the grid (rows 1 to 5)
        var totalInGrid = 0
        for (r in 1 until rows) {
            for (c in 0 until columns) {
                if (gridNumbers[r][c] == value) totalInGrid++
            }
        }
        
        // Count how many are covered
        val covered = markers.count { it.value == value && it.placedCell != null }
        
        if (covered >= totalInGrid) {
            // Remove from tray
            markers.removeAll { it.isTrayMarker && it.value == value }
        }
    }

    private fun checkWin() {
        if (markers.none { it.isTrayMarker }) {
            onWin?.invoke()
        }
    }
    
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}

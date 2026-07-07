package com.bagicode.games.numbersearch

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

data class Marker(
    val value: Int,
    val color: Int,
    var x: Float,
    var y: Float,
    var isDragging: Boolean = false,
    var placedCell: Pair<Int, Int>? = null
)

class NumberSearchView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    
    private val columns = 5
    private val rows = 5
    private var cellSize = 0f
    
    private val gridNumbers = Array(rows) { IntArray(columns) }
    private val targetColors = listOf(
        Color.parseColor("#3949AB"), // 20 - Blue
        Color.parseColor("#F06292"), // 70 - Pink
        Color.parseColor("#9C27B0"), // 50 - Purple
        Color.parseColor("#424242"), // 80 - Grey
        Color.parseColor("#FB8C00"), // 10 - Orange
        Color.parseColor("#E53935"), // 40 - Red
        Color.parseColor("#00ACC1"), // 100 - Teal
        Color.parseColor("#43A047"), // 30 - Green
        Color.parseColor("#880E4F"), // 60 - Maroon
        Color.parseColor("#FDD835")  // 90 - Yellow
    )
    private val targetValues = listOf(20, 70, 50, 80, 10, 40, 100, 30, 60, 90)
    
    private val markers = mutableListOf<Marker>()
    private var activeMarker: Marker? = null

    init {
        generateGrid()
    }

    private fun generateGrid() {
        val baseValues = listOf(10, 20, 30, 40, 50, 60, 70, 80, 90, 100)
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                if (r == 0) {
                    gridNumbers[r][c] = targetValues[c]
                } else {
                    gridNumbers[r][c] = baseValues[(c + r) % 10]
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellSize = w.toFloat() / columns
        
        if (markers.isEmpty()) {
            val trayY = cellSize * (rows + 1)
            for (i in targetValues.indices) {
                markers.add(
                    Marker(
                        value = targetValues[i],
                        color = targetColors[i],
                        x = (i + 0.5f) * cellSize,
                        y = trayY
                    )
                )
            }
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
                    textPaint.color = targetColors[c]
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
                activeMarker = markers.findLast { marker ->
                    marker.placedCell == null && Math.hypot((event.x - marker.x).toDouble(), (event.y - marker.y).toDouble()) < cellSize * 0.5
                }
                activeMarker?.isDragging = true
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
                    
                    if (row in 1 until rows && col in 0 until columns && gridNumbers[row][col] == marker.value) {
                        marker.x = (col + 0.5f) * cellSize
                        marker.y = (row + 0.5f) * cellSize
                        marker.placedCell = row to col
                        
                        val trayY = cellSize * (rows + 1)
                        val originalIdx = targetValues.indexOf(marker.value)
                        markers.add(0, Marker(
                            value = marker.value,
                            color = marker.color,
                            x = (originalIdx + 0.5f) * cellSize,
                            y = trayY
                        ))
                    } else {
                        val originalIdx = targetValues.indexOf(marker.value)
                        marker.x = (originalIdx + 0.5f) * cellSize
                        marker.y = cellSize * (rows + 1)
                    }
                    marker.isDragging = false
                    activeMarker = null
                    invalidate()
                    performClick()
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

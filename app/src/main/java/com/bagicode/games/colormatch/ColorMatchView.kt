package com.bagicode.games.colormatch

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

data class MatchItem(val emoji: String, val color: Int, val colorName: String)

class ColorMatchView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 10f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    
    private val itemPool = listOf(
        MatchItem("🍓", Color.parseColor("#E53935"), "Red"),
        MatchItem("🍋", Color.parseColor("#FDD835"), "Yellow"),
        MatchItem("🍇", Color.parseColor("#7B1FA2"), "Purple"),
        MatchItem("🍏", Color.parseColor("#43A047"), "Green"),
        MatchItem("🫐", Color.parseColor("#1E88E5"), "Blue"),
        MatchItem("🍊", Color.parseColor("#FB8C00"), "Orange"),
        MatchItem("🍉", Color.parseColor("#D81B60"), "Pink"),
        MatchItem("🍒", Color.parseColor("#B71C1C"), "Dark Red"),
        MatchItem("🍆", Color.parseColor("#4A148C"), "Dark Purple"),
        MatchItem("🍈", Color.parseColor("#81C784"), "Light Green"),
        MatchItem("🧄", Color.parseColor("#CFD8DC"), "White")
    )

    private var leftItems = mutableListOf<MatchItem>()
    private var rightItems = mutableListOf<MatchItem>()
    private val connections = mutableMapOf<Int, Int>() 
    private var activeStartIdx: Int? = null
    private var currentX = 0f
    private var currentY = 0f

    var onAllMatched: (() -> Unit)? = null

    init {
        nextLevel()
    }

    fun nextLevel() {
        val shuffled = itemPool.shuffled().take(3)
        leftItems = shuffled.toMutableList()
        rightItems = shuffled.shuffled().toMutableList()
        connections.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val h = height.toFloat()
        val w = width.toFloat()
        val itemH = h / 4f

        // Draw Left Items (Fruits)
        paint.textSize = itemH * 0.5f
        paint.textAlign = Paint.Align.CENTER
        for (i in 0 until 3) {
            val centerY = (i + 1) * itemH
            paint.color = Color.BLACK
            canvas.drawText(leftItems[i].emoji, w * 0.15f, centerY + paint.textSize / 3, paint)
            
            // Left Dot
            paint.color = Color.parseColor("#37474F")
            canvas.drawCircle(w * 0.35f, centerY, 20f, paint)
        }

        // Draw Right Items (Crayons)
        for (i in 0 until 3) {
            val centerY = (i + 1) * itemH
            drawCrayon(canvas, w * 0.85f, centerY, itemH * 0.6f, rightItems[i].color)
            
            // Right Dot
            paint.color = Color.parseColor("#37474F")
            canvas.drawCircle(w * 0.65f, centerY, 20f, paint)
        }

        // Draw established connections
        for ((l, r) in connections) {
            linePaint.color = leftItems[l].color
            canvas.drawLine(w * 0.35f, (l + 1) * itemH, w * 0.65f, (r + 1) * itemH, linePaint)
        }

        // Draw active line
        activeStartIdx?.let { l ->
            linePaint.color = leftItems[l].color
            canvas.drawLine(w * 0.35f, (l + 1) * itemH, currentX, currentY, linePaint)
        }
    }

    private fun drawCrayon(canvas: Canvas, x: Float, y: Float, size: Float, color: Int) {
        val cw = size * 0.4f
        val ch = size * 0.8f
        
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(45f) 
        
        val rect = RectF(-cw/2, -ch/2, cw/2, ch/2)
        paint.color = color
        paint.style = Paint.Style.FILL
        canvas.drawRect(rect, paint)
        
        // Tip
        val path = Path()
        path.moveTo(-cw/2, -ch/2)
        path.lineTo(0f, -ch/2 - cw/2)
        path.lineTo(cw/2, -ch/2)
        path.close()
        canvas.drawPath(path, paint)
        
        // Details
        paint.color = Color.BLACK
        paint.alpha = 40
        canvas.drawRect(-cw/2, -ch/4, cw/2, ch/4, paint)
        paint.alpha = 255
        
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val w = width.toFloat()
        val h = height.toFloat()
        val itemH = h / 4f

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                for (i in 0 until 3) {
                    val dotY = (i + 1) * itemH
                    if (Math.abs(event.x - w * 0.35f) < 80f && Math.abs(event.y - dotY) < 80f) {
                        activeStartIdx = i
                        currentX = event.x
                        currentY = event.y
                        invalidate()
                        return true
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (activeStartIdx != null) {
                    currentX = event.x
                    currentY = event.y
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                activeStartIdx?.let { l ->
                    for (i in 0 until 3) {
                        val dotY = (i + 1) * itemH
                        if (Math.abs(event.x - w * 0.65f) < 80f && Math.abs(event.y - dotY) < 80f) {
                            if (leftItems[l].color == rightItems[i].color) {
                                connections[l] = i
                                checkWin()
                            }
                        }
                    }
                }
                activeStartIdx = null
                invalidate()
                performClick()
            }
        }
        return true
    }

    private fun checkWin() {
        if (connections.size == 3) {
            onAllMatched?.invoke()
        }
    }
    
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}

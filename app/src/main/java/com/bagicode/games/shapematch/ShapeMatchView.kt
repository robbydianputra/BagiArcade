package com.bagicode.games.shapematch

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

data class ShapeItem(val id: Int, val emoji: String)

class ShapeMatchView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) }) // Grayscale
    }
    private val blackShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // Create a black silhouette of the emoji
        colorFilter = PorterDuffColorFilter(Color.parseColor("#424242"), PorterDuff.Mode.SRC_IN)
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 30f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#43A047") // Green lines for connection
    }
    
    private val itemPool = listOf(
        ShapeItem(1, "🍒"),
        ShapeItem(2, "🍏"),
        ShapeItem(3, "🍐"),
        ShapeItem(4, "🍇"),
        ShapeItem(5, "🍑"),
        ShapeItem(6, "🍊"),
        ShapeItem(7, "🍓"),
        ShapeItem(8, "🍍"),
        ShapeItem(9, "🍌"),
        ShapeItem(10, "🍉")
    )

    private val countItem = 3
    private var leftItems = mutableListOf<ShapeItem>()
    private var rightItems = mutableListOf<ShapeItem>()
    private val connections = mutableMapOf<Int, Int>() 
    private var activeStartIdx: Int? = null
    private var currentX = 0f
    private var currentY = 0f

    var onAllMatched: (() -> Unit)? = null

    init {
        nextLevel()
    }

    fun nextLevel() {
        val shuffled = itemPool.shuffled().take(countItem)
        leftItems = shuffled.toMutableList()
        rightItems = shuffled.shuffled().toMutableList()
        connections.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val h = height.toFloat()
        val w = width.toFloat()
        val itemH = h / (countItem + 1).toFloat()

        // Draw Left Items (Colored)
        paint.textSize = itemH * 0.5f
        paint.textAlign = Paint.Align.CENTER
        for (i in 0 until countItem) {
            val centerY = (i + 1) * itemH
            canvas.drawText(leftItems[i].emoji, w * 0.15f, centerY + paint.textSize / 3, paint)
            
            // Left Dot
            paint.color = Color.parseColor("#37474F")
            canvas.drawCircle(w * 0.35f, centerY, 40f, paint)
        }

        // Draw Right Items (Shadows)
        blackShadowPaint.textSize = paint.textSize
        blackShadowPaint.textAlign = Paint.Align.CENTER
        for (i in 0 until countItem) {
            val centerY = (i + 1) * itemH
            canvas.drawText(rightItems[i].emoji, w * 0.85f, centerY + paint.textSize / 3, blackShadowPaint)
            
            // Right Dot
            paint.color = Color.parseColor("#37474F")
            canvas.drawCircle(w * 0.65f, centerY, 40f, paint)
        }

        // Draw established connections
        for ((l, r) in connections) {
            canvas.drawLine(w * 0.35f, (l + 1) * itemH, w * 0.65f, (r + 1) * itemH, linePaint)
        }

        // Draw active line
        activeStartIdx?.let { l ->
            canvas.drawLine(w * 0.35f, (l + 1) * itemH, currentX, currentY, linePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val w = width.toFloat()
        val h = height.toFloat()
        val itemH = h / (countItem + 1).toFloat()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                for (i in 0 until countItem) {
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
                    for (i in 0 until countItem) {
                        val dotY = (i + 1) * itemH
                        if (Math.abs(event.x - w * 0.65f) < 80f && Math.abs(event.y - dotY) < 80f) {
                            if (leftItems[l].id == rightItems[i].id) {
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
        if (connections.size == countItem) {
            onAllMatched?.invoke()
        }
    }
    
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}

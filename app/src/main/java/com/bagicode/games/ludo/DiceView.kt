package com.bagicode.games.ludo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class DiceView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    var roll: Int = 1
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = width.coerceAtMost(height).toFloat()
        if (size <= 0) return
        
        val padding = size * 0.1f
        val diceSize = size - 2 * padding
        
        rectF.set(padding, padding, size - padding, size - padding)
        
        // Draw dice body
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rectF, size * 0.2f, size * 0.2f, paint)
        
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size * 0.05f
        canvas.drawRoundRect(rectF, size * 0.2f, size * 0.2f, paint)
        
        // Draw dots
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        val dotRadius = size * 0.08f
        val mid = size / 2
        val start = padding + diceSize * 0.25f
        val end = padding + diceSize * 0.75f
        
        when (roll) {
            1 -> drawDots(canvas, listOf(PointF(mid, mid)), dotRadius)
            2 -> drawDots(canvas, listOf(PointF(start, start), PointF(end, end)), dotRadius)
            3 -> drawDots(canvas, listOf(PointF(start, start), PointF(mid, mid), PointF(end, end)), dotRadius)
            4 -> drawDots(canvas, listOf(PointF(start, start), PointF(end, start), PointF(start, end), PointF(end, end)), dotRadius)
            5 -> drawDots(canvas, listOf(PointF(start, start), PointF(end, start), PointF(mid, mid), PointF(start, end), PointF(end, end)), dotRadius)
            6 -> drawDots(canvas, listOf(PointF(start, start), PointF(end, start), PointF(start, mid), PointF(end, mid), PointF(start, end), PointF(end, end)), dotRadius)
        }
    }
    
    private fun drawDots(canvas: Canvas, points: List<PointF>, radius: Float) {
        points.forEach { canvas.drawCircle(it.x, it.y, radius, paint) }
    }
    
    private data class PointF(val x: Float, val y: Float)
}

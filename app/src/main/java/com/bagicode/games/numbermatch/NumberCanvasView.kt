package com.bagicode.games.numbermatch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class NumberCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val numbers = mutableListOf<SmallNumber>()
    private var targetNumber = 1

    private val bigTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 1800f
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 48f
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    private val numberPath = Path()
    private val numberRegion = Region()

    fun setTargetNumber(number: Int) {
        targetNumber = number
        generateLevel()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        generateLevel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // draw BIG NUMBER
        canvas.drawPath(numberPath, Paint().apply {
            color = Color.parseColor("#FFE082")
            style = Paint.Style.FILL
        })

        canvas.drawPath(numberPath, Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 10f
        })

        numbers.forEach {

            val paint = when {
                it.found -> smallTextPaint.apply { color = Color.GREEN }
                it.wrong -> smallTextPaint.apply { color = Color.RED }
                else -> smallTextPaint.apply { color = Color.BLACK }
            }

            canvas.drawText(
                it.value.toString(),
                it.x,
                it.y,
                paint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true

        numbers.forEach { item ->
            val dx = event.x - item.x
            val dy = event.y - item.y

            if (dx * dx + dy * dy < 60f * 60f) {
                if (item.value == targetNumber) {
                    item.found = true
                } else {
                    item.wrong = true
                    postDelayed({
                        item.wrong = false
                        invalidate()
                    }, 300)
                }

                invalidate()
                return true
            }
        }

        return true
    }

    private fun buildNumberPath() {

        numberPath.reset()

        bigTextPaint.getTextPath(
            targetNumber.toString(),
            0,
            1,
            width * 0.05f,
            height * 0.75f,
            numberPath
        )

        val r = RectF()
        numberPath.computeBounds(r, true)

        val region = Region()
        region.setPath(
            numberPath,
            Region(
                0,
                0,
                width,
                height
            )
        )

        numberRegion.set(region)
    }

    private fun generateLevel() {

        if (width == 0 || height == 0) return

        buildNumberPath()

        numbers.clear()

        val random = Random(System.currentTimeMillis())

        var tries = 0
        var placed = 0

        while (tries < 5000 && placed < 120) {

            tries++

            val x = random.nextInt(width)
            val y = random.nextInt(height)

            // ❌ HARUS di dalam shape
            if (!numberRegion.contains(x, y)) continue

            // ❌ anti tabrakan
            if (isTooClose(x.toFloat(), y.toFloat())) continue

            numbers.add(
                SmallNumber(
                    value = random.nextInt(1, 10),
                    x = x.toFloat(),
                    y = y.toFloat()
                )
            )

            placed++
        }
    }

    private fun isInsideShape(x: Int, y: Int): Boolean {
        return numberRegion.contains(x, y)
    }

    private fun isTooClose(x: Float, y: Float): Boolean {
        numbers.forEach {
            val dx = x - it.x
            val dy = y - it.y
            val dist = dx * dx + dy * dy
            if (dist < 100f * 100f) return true
        }
        return false
    }
}
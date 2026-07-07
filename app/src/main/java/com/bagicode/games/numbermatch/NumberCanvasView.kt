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

    private var gameListener: GameListener? = null

    fun setGameListener(listener: GameListener){
        this.gameListener = listener
    }

    private val themes = listOf(
        // Montessori
        NumberTheme(
            backgroundColor = Color.parseColor("#FAF8F3"),
            shapeColor = Color.parseColor("#F4D35E"),
            outlineColor = Color.parseColor("#555555"),
            numberColor = Color.parseColor("#2E2E2E"),
            successColor = Color.parseColor("#43A047"),
            errorColor = Color.parseColor("#E53935")
        ),

        // Ocean
        NumberTheme(
            backgroundColor = Color.parseColor("#F3FAFF"),
            shapeColor = Color.parseColor("#7FB3D5"),
            outlineColor = Color.parseColor("#2C3E50"),
            numberColor = Color.parseColor("#1B2631"),
            successColor = Color.parseColor("#2ECC71"),
            errorColor = Color.parseColor("#E74C3C")
        ),

        // Forest
        NumberTheme(
            backgroundColor = Color.parseColor("#F6FBF4"),
            shapeColor = Color.parseColor("#81C784"),
            outlineColor = Color.parseColor("#33691E"),
            numberColor = Color.parseColor("#263238"),
            successColor = Color.parseColor("#2E7D32"),
            errorColor = Color.parseColor("#C62828")
        ),

        // Candy
        NumberTheme(
            backgroundColor = Color.parseColor("#FFF5FB"),
            shapeColor = Color.parseColor("#F8BBD0"),
            outlineColor = Color.parseColor("#AD1457"),
            numberColor = Color.parseColor("#4A148C"),
            successColor = Color.parseColor("#66BB6A"),
            errorColor = Color.parseColor("#EF5350")
        ),

        // Sunset
        NumberTheme(
            backgroundColor = Color.parseColor("#FFF8F0"),
            shapeColor = Color.parseColor("#FFB74D"),
            outlineColor = Color.parseColor("#6D4C41"),
            numberColor = Color.parseColor("#3E2723"),
            successColor = Color.parseColor("#4CAF50"),
            errorColor = Color.parseColor("#E53935")
        )
    )
    private var currentTheme = themes.random()

    private val bigTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 1800f
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 80f
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    private val numberPath = Path()
    private val numberRegion = Region()

    fun setTargetNumber(number: Int) {
        targetNumber = number
        currentTheme = themes.random()
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
        val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = currentTheme.shapeColor
            style = Paint.Style.FILL
        }
//        shapePaint.alpha = 190
        canvas.drawPath(numberPath, shapePaint)

        canvas.drawPath(numberPath, Paint().apply {
            color = currentTheme.shapeColor
            style = Paint.Style.STROKE
            strokeWidth = 40f
        })

        // draw SMALL Number
        numbers.forEach {
            val paint = when {
                it.found -> smallTextPaint.apply {
                    color = currentTheme.successColor
                }

                it.wrong -> smallTextPaint.apply {
                    color = currentTheme.errorColor
                }

                else -> smallTextPaint.apply {
                    color = currentTheme.numberColor
                }

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
                    checkGameComplete()
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
        numbers[1] = numbers[1].copy(
            value = targetNumber
        )
    }

    private fun isInsideShape(x: Int, y: Int): Boolean {
        return numberRegion.contains(x, y)
    }

    private fun isTooClose(x: Float, y: Float): Boolean {
        numbers.forEach {
            val dx = x - it.x
            val dy = y - it.y
            val dist = dx * dx + dy * dy
            if (dist < 200f * 200f) return true
        }
        return false
    }

    private fun checkGameComplete(){
        val remaining = numbers.any {
            it.value == targetNumber && !it.found
        }

        if(!remaining){
            gameListener?.onGameComplete()
        }
    }

    interface GameListener {
        fun onGameComplete()
    }
}
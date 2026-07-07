package com.bagicode.games.writing

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class WritingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val drawPaint = Paint().apply {
        color = Color.BLUE
        isAntiAlias = true
        strokeWidth = 80f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    
    private val referencePaint = Paint().apply {
        color = Color.parseColor("#EEEEEE")
        isAntiAlias = true
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 1200f
        typeface = Typeface.DEFAULT_BOLD
    }

    private var drawPath = Path()
    private var currentNumber = 1
    
    private lateinit var refBitmap: Bitmap
    private lateinit var userBitmap: Bitmap
    private lateinit var refCanvas: Canvas
    private lateinit var userCanvas: Canvas
    
    var onScoreResult: ((Boolean) -> Unit)? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setNumber(num: Int) {
        currentNumber = num
        drawPath.reset()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            refBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            userBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            refCanvas = Canvas(refBitmap)
            userCanvas = Canvas(userBitmap)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val cx = width / 2f
        val cy = height / 2f + (referencePaint.textSize / 3)
        canvas.drawText(currentNumber.toString(), cx, cy, referencePaint)
        
        canvas.drawPath(drawPath, drawPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath.moveTo(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                drawPath.lineTo(x, y)
            }
            MotionEvent.ACTION_UP -> {
                performClick()
            }
        }
        invalidate()
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun checkWriting() {
        if (width <= 0 || height <= 0 || drawPath.isEmpty) {
            onScoreResult?.invoke(false)
            return
        }

        refBitmap.eraseColor(Color.TRANSPARENT)
        val cx = width / 2f
        val cy = height / 2f + (referencePaint.textSize / 3)
        refCanvas.drawText(currentNumber.toString(), cx, cy, referencePaint)

        userBitmap.eraseColor(Color.TRANSPARENT)
        val userDrawPaint = Paint(drawPaint).apply { color = Color.BLACK }
        userCanvas.drawPath(drawPath, userDrawPaint)

        var totalRefPixels = 0
        var matchingPixels = 0
        var wrongPixels = 0

        for (i in 0 until width step 8) {
            for (j in 0 until height step 8) {
                val refPixel = refBitmap.getPixel(i, j)
                val userPixel = userBitmap.getPixel(i, j)

                val isRefOccupied = refPixel != Color.TRANSPARENT
                val isUserOccupied = userPixel != Color.TRANSPARENT

                if (isRefOccupied) {
                    totalRefPixels++
                    if (isUserOccupied) matchingPixels++
                } else if (isUserOccupied) {
                    wrongPixels++
                }
            }
        }

        if (totalRefPixels == 0) {
            onScoreResult?.invoke(false)
            return
        }

        val coverage = (matchingPixels.toFloat() / totalRefPixels) * 100
        val accuracy = if (matchingPixels + wrongPixels > 0) {
            (matchingPixels.toFloat() / (matchingPixels + wrongPixels)) * 100
        } else 0f

        val isCorrect = coverage > 25 && accuracy > 20
        onScoreResult?.invoke(isCorrect)
    }
    
    fun clear() {
        drawPath.reset()
        invalidate()
    }
}

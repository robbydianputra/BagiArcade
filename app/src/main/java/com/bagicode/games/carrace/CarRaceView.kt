package com.bagicode.games.carrace

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class CarRaceView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var isPlaying = false
    private var score = 0
    private var highScore = 0

    // Car properties
    private var carX = 0f
    private var carY = 0f
    private var carWidth = 0f
    private var carHeight = 0f
    
    // Road properties
    private var roadWidth = 0f
    private var laneCount = 3
    private var laneWidth = 0f
    private var roadOffset = 0f
    
    // Enemy cars
    private val enemies = mutableListOf<RectF>()
    private val enemyColors = listOf(Color.RED, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN)
    private val enemyColorMap = mutableMapOf<RectF, Int>()
    private var gameSpeed = 15f
    private var enemySpawnTimer = 0
    
    private var lastOccupiedLanes = mutableSetOf<Int>()

    var onGameOver: ((Int) -> Unit)? = null
    var onScoreUpdate: ((Int) -> Unit)? = null

    init {
        paint.typeface = Typeface.DEFAULT_BOLD
    }

    fun startGame() {
        isPlaying = true
        score = 0
        gameSpeed = 15f
        enemies.clear()
        enemyColorMap.clear()
        lastOccupiedLanes.clear()
        carX = width / 2f - carWidth / 2f
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        roadWidth = w.toFloat()
        laneWidth = roadWidth / laneCount
        carWidth = laneWidth * 0.6f
        carHeight = carWidth * 1.8f
        carX = w / 2f - carWidth / 2f
        carY = h * 0.8f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        drawRoad(canvas)
        if (isPlaying) {
            updateGame()
            drawEnemies(canvas)
            drawPlayerCar(canvas)
            invalidate()
        } else if (score == 0) {
            drawStartScreen(canvas)
        } else {
            drawEnemies(canvas)
            drawPlayerCar(canvas)
            drawGameOverScreen(canvas)
        }
    }

    private fun updateGame() {
        roadOffset = (roadOffset + gameSpeed) % (height / 4f)

        enemySpawnTimer--
        if (enemySpawnTimer <= 0) {
            // Guarantee at least one free lane
            // We'll spawn 1 or 2 cars, but never 3 in the same 'wave'
            val carsToSpawn = if (Random.nextBoolean()) 1 else 2
            val availableLanes = (0 until laneCount).toMutableList()
            
            for (i in 0 until carsToSpawn) {
                if (availableLanes.isEmpty()) break
                val laneIdx = Random.nextInt(availableLanes.size)
                val lane = availableLanes.removeAt(laneIdx)
                
                val left = lane * laneWidth + (laneWidth - carWidth) / 2f
                val enemy = RectF(left, -carHeight, left + carWidth, 0f)
                enemies.add(enemy)
                enemyColorMap[enemy] = enemyColors[Random.nextInt(enemyColors.size)]
            }
            
            enemySpawnTimer = (35 + Random.nextInt(25)).coerceAtMost((1800 / gameSpeed).toInt())
        }

        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            enemy.offset(0f, gameSpeed)

            val playerRect = RectF(carX, carY, carX + carWidth, carY + carHeight)
            if (RectF.intersects(playerRect, enemy)) {
                gameOver()
                return
            }

            if (enemy.top > height) {
                iterator.remove()
                enemyColorMap.remove(enemy)
                score++
                onScoreUpdate?.invoke(score)
                if (score % 5 == 0) gameSpeed += 0.5f
            }
        }
    }

    private fun drawRoad(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#8BC34A"))
        paint.color = Color.parseColor("#424242")
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        paint.color = Color.WHITE
        paint.strokeWidth = 10f
        for (i in 1 until laneCount) {
            val x = i * laneWidth
            var currentY = -height / 4f + roadOffset
            while (currentY < height) {
                canvas.drawLine(x, currentY, x, currentY + 50f, paint)
                currentY += 100f
            }
        }
        
        paint.color = Color.YELLOW
        paint.strokeWidth = 15f
        canvas.drawLine(10f, 0f, 10f, height.toFloat(), paint)
        canvas.drawLine(width - 10f, 0f, width - 10f, height.toFloat(), paint)
    }

    private fun drawPlayerCar(canvas: Canvas) {
        drawCar(canvas, carX, carY, Color.BLUE)
    }

    private fun drawEnemies(canvas: Canvas) {
        for (enemy in enemies) {
            drawCar(canvas, enemy.left, enemy.top, enemyColorMap[enemy] ?: Color.RED)
        }
    }

    private fun drawCar(canvas: Canvas, x: Float, y: Float, color: Int) {
        paint.color = color
        paint.style = Paint.Style.FILL
        val bodyRect = RectF(x, y, x + carWidth, y + carHeight)
        canvas.drawRoundRect(bodyRect, 15f, 15f, paint)

        paint.color = Color.parseColor("#B3E5FC")
        val windowMargin = carWidth * 0.15f
        canvas.drawRect(x + windowMargin, y + carHeight * 0.2f, x + carWidth - windowMargin, y + carHeight * 0.4f, paint)
        canvas.drawRect(x + windowMargin, y + carHeight * 0.6f, x + carWidth - windowMargin, y + carHeight * 0.75f, paint)

        paint.color = Color.BLACK
        val wheelW = carWidth * 0.15f
        val wheelH = carHeight * 0.2f
        canvas.drawRect(x - wheelW/2, y + 20f, x + wheelW/2, y + 20f + wheelH, paint)
        canvas.drawRect(x + carWidth - wheelW/2, y + 20f, x + carWidth + wheelW/2, y + 20f + wheelH, paint)
        canvas.drawRect(x - wheelW/2, y + carHeight - 20f - wheelH, x + wheelW/2, y + carHeight - 20f, paint)
        canvas.drawRect(x + carWidth - wheelW/2, y + carHeight - 20f - wheelH, x + carWidth + wheelW/2, y + carHeight - 20f, paint)
    }

    private fun drawStartScreen(canvas: Canvas) {
        paint.color = Color.WHITE
        paint.textSize = width * 0.08f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("TAP TO RACE!", width / 2f, height / 2f, paint)
    }

    private fun drawGameOverScreen(canvas: Canvas) {
        paint.color = Color.argb(150, 0, 0, 0)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        paint.color = Color.WHITE
        paint.textSize = width * 0.12f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("CRASHED!", width / 2f, height * 0.4f, paint)
        
        paint.textSize = width * 0.06f
        canvas.drawText("Final Score: $score", width / 2f, height * 0.5f, paint)
        canvas.drawText("Tap to Try Again", width / 2f, height * 0.6f, paint)
    }

    private fun gameOver() {
        isPlaying = false
        if (score > highScore) highScore = score
        onGameOver?.invoke(score)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!isPlaying) {
                    startGame()
                } else {
                    // Check if player clicked a lane to jump there
                    val clickedLane = (event.x / laneWidth).toInt()
                    if (clickedLane in 0 until laneCount) {
                        carX = clickedLane * laneWidth + (laneWidth - carWidth) / 2f
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isPlaying) {
                    // Manual drag listener
                    carX = event.x - carWidth / 2f
                    if (carX < 0) carX = 0f
                    if (carX > width - carWidth) carX = width - carWidth
                }
            }
        }
        performClick()
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}

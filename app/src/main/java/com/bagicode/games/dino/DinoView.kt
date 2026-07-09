package com.bagicode.games.dino

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.bagicode.games.R
import kotlin.random.Random

class DinoView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var isPlaying = false
    private var score = 0
    private var highScore = 0
    
    // Dino properties
    private var dinoY = 0f
    private var dinoVelocity = 0f
    private val gravity = 2.5f
    private val jumpStrength = -45f
    private var isJumping = false
    private var groundY = 0f
    private var dinoSize = 0f
    
    // Obstacle properties
    private val obstacles = mutableListOf<RectF>()
    private var gameSpeed = 15f
    private var nextObstacleTimer = 0
    
    var onGameOver: ((Int) -> Unit)? = null

    private val dinoRun1 = BitmapFactory.decodeResource(resources, R.drawable.dino_run1)
    private val dinoRun2 = BitmapFactory.decodeResource(resources, R.drawable.dino_run2)
    private val dinoJump = BitmapFactory.decodeResource(resources, R.drawable.dino_jump)
    private val dinoDead = BitmapFactory.decodeResource(resources, R.drawable.dino_dead)
//    private val cactusBitmap =
//        BitmapFactory.decodeResource(resources, R.drawable.cactus)
    private var frame = 0
    private var lastTime = System.currentTimeMillis()

    init {
        paint.typeface = Typeface.DEFAULT_BOLD
    }

    fun startGame() {
        isPlaying = true
        score = 0
        gameSpeed = 15f
        obstacles.clear()
        dinoY = groundY
        dinoVelocity = 0f
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        groundY = h * 0.7f
        dinoY = groundY
        dinoSize = w * 0.1f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isPlaying && score == 0) {
            drawStartScreen(canvas)
            return
        }

        // updateGame()
        updateRefreshRate()
        drawGround(canvas)
        drawDino(canvas)
        drawObstacles(canvas)
        drawScore(canvas)

        if (isPlaying) {
            invalidate()
        }
    }

    private fun updateRefreshRate() {
        val currentTime = System.currentTimeMillis()

        val deltaTime =
            (currentTime - lastTime) / 1000f

        lastTime = currentTime


        updateGame(deltaTime)
    }

    private fun updateGame(deltaTime: Float) {
        if (!isPlaying) return

        // Update Dino tergantung FPS device
//        dinoVelocity += gravity
//        dinoY += dinoVelocity
        // Update Dino
        dinoVelocity += gravity * deltaTime * 60
        dinoY += dinoVelocity * deltaTime * 60

        if (dinoY >= groundY) {
            dinoY = groundY
            dinoVelocity = 0f
            isJumping = false
        }

        // Update Obstacles
        nextObstacleTimer--
        if (nextObstacleTimer <= 0) {
            val obstacleWidth = dinoSize * 0.6f
//            val obstacleHeight = dinoSize * (0.5f + Random.nextFloat())
//            obstacles.add(RectF(width.toFloat(), groundY - obstacleHeight, width.toFloat() + obstacleWidth, groundY))

            val obstacleHeight = dinoSize
            obstacles.add(
                RectF(
                    width.toFloat(),
                    groundY - obstacleHeight,
                    width.toFloat() + obstacleWidth,
                    groundY
                )
            )
            nextObstacleTimer = (40 + Random.nextInt(40)).coerceAtLeast((2000 / gameSpeed).toInt())
        }

        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val obs = iterator.next()
//            obs.offset(-gameSpeed, 0f)
            obs.offset(
                -gameSpeed * deltaTime * 60,
                0f
            )
            
            // Collision Detection
            val dinoRect = RectF(width * 0.2f, dinoY - dinoSize, width * 0.2f + dinoSize, dinoY)
            if (RectF.intersects(dinoRect, obs)) {
                gameOver()
            }

            if (obs.right < 0) {
                iterator.remove()
                score++
                if (score % 10 == 0) gameSpeed += 1f
            }
        }
    }

    private fun drawStartScreen(canvas: Canvas) {
        paint.color = Color.DKGRAY
        paint.textSize = width * 0.08f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("TAP TO START DINO RUN", width / 2f, height / 2f, paint)
    }

    private fun drawGround(canvas: Canvas) {
        paint.color = Color.BLACK
        paint.strokeWidth = 5f
        canvas.drawLine(0f, groundY, width.toFloat(), groundY, paint)
    }

//    private fun drawDino(canvas: Canvas) {
//        paint.color = Color.parseColor("#535353")
//        val x = width * 0.2f
//        // Simplified Dino Shape (Square-ish)
//        canvas.drawRect(x, dinoY - dinoSize, x + dinoSize, dinoY, paint)
//
//        // Add an eye
//        paint.color = Color.WHITE
//        canvas.drawCircle(x + dinoSize * 0.7f, dinoY - dinoSize * 0.8f, dinoSize * 0.1f, paint)
//    }

    private fun drawDino(canvas: Canvas) {


        val x = width * 0.2f


        val bitmap = when {

            !isPlaying -> {
                dinoDead
            }

            isJumping -> {
                dinoJump
            }

            else -> {

                frame++

                if(frame % 20 < 10)
                    dinoRun1
                else
                    dinoRun2
            }
        }


        val scaled =
            scaleBitmap(
                bitmap,
                dinoSize
            )


        canvas.drawBitmap(
            scaled,
            x,
            dinoY - dinoSize,
            paint
        )
    }

    private fun drawObstacles(canvas: Canvas) {
        paint.color = Color.parseColor("#535353")
        for (obs in obstacles) {
            canvas.drawRect(obs, paint)
        }
    }

//    private fun drawObstacles(canvas: Canvas) {
//
//
//        for(obs in obstacles){
//
//
//            val cactus =
//                Bitmap.createScaledBitmap(
//                    cactusBitmap,
//                    (obs.width()).toInt(),
//                    (obs.height()).toInt(),
//                    true
//                )
//
//
//            canvas.drawBitmap(
//                cactus,
//                obs.left,
//                obs.top,
//                paint
//            )
//        }
//    }

    private fun drawScore(canvas: Canvas) {
        paint.color = Color.BLACK
        paint.textSize = width * 0.05f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("HI: $highScore  SCORE: $score", width - 50f, height * 0.1f, paint)
    }

    private fun gameOver() {
        isPlaying = false
        if (score > highScore) highScore = score
        onGameOver?.invoke(score)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (!isPlaying) {
                startGame()
            } else if (!isJumping) {
                dinoVelocity = jumpStrength
                isJumping = true
            }
            performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun scaleBitmap(
        bitmap: Bitmap,
        size: Float
    ): Bitmap {
        return Bitmap.createScaledBitmap(
            bitmap,
            size.toInt(),
            size.toInt(),
            true
        )
    }
}

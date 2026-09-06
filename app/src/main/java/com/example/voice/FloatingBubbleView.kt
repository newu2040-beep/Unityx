package com.example.voice

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

/**
 * High-polish floating circle assistant view designed to overlay smoothly
 * across the Android home screen and any application.
 */
class FloatingBubbleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val density = context.resources.displayMetrics.density

    // Dimensions
    val bubbleDiameter = (58 * density).toInt()
    private val radius get() = bubbleDiameter / 2f

    // States
    var isListening = false
        set(value) {
            field = value
            if (value) startPulseAnimation() else stopPulseAnimation()
            invalidate()
        }

    var isPaused = false
        set(value) {
            field = value
            invalidate()
        }

    // Callbacks
    var onBubbleClickListener: (() -> Unit)? = null
    var onBubbleLongClickListener: (() -> Unit)? = null
    var onLayoutPositionChanged: ((x: Int, y: Int) -> Unit)? = null

    // WindowManager interaction
    var windowParams: WindowManager.LayoutParams? = null
    var windowManager: WindowManager? = null

    // Pulse animation for active voice listening
    private var pulseRadiusRatio = 0f
    private var pulseAlpha = 0
    private var pulseAnimator: ValueAnimator? = null

    // Touch gesture tracking
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var touchDownTime = 0L

    // Paints
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33000000")
        style = Paint.Style.FILL
    }

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.2f * density
        color = Color.parseColor("#4361EE")
    }

    private val pulsePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f * density
        color = Color.parseColor("#4CC9F0")
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val iconStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
        strokeCap = Paint.Cap.ROUND
        color = Color.WHITE
    }

    private val statusDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val statusDotBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
        color = Color.WHITE
    }

    init {
        isClickable = true
        isFocusable = false
    }

    private fun startPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { anim ->
                val fraction = anim.animatedValue as Float
                pulseRadiusRatio = 0.85f + fraction * 0.45f
                pulseAlpha = ((1f - fraction) * 200).toInt()
                invalidate()
            }
            start()
        }
    }

    private fun stopPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        pulseAlpha = 0
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = (bubbleDiameter * 1.5f).toInt()
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val r = radius

        // 1. Listening Pulse Aura
        if (isListening && pulseAlpha > 0) {
            pulsePaint.alpha = pulseAlpha
            canvas.drawCircle(cx, cy, r * pulseRadiusRatio, pulsePaint)
        }

        // 2. Drop shadow
        canvas.drawCircle(cx, cy + (3 * density), r, shadowPaint)

        // 3. Main Circular Body Gradient
        val bodyColors = when {
            isPaused -> intArrayOf(Color.parseColor("#374151"), Color.parseColor("#1F2937"))
            isListening -> intArrayOf(Color.parseColor("#0284C7"), Color.parseColor("#0F172A"))
            else -> intArrayOf(Color.parseColor("#2563EB"), Color.parseColor("#0F172A"))
        }

        basePaint.shader = RadialGradient(
            cx, cy - (r * 0.3f), r * 1.2f,
            bodyColors, floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, r, basePaint)

        // 4. Outer Border Accent
        borderPaint.color = when {
            isPaused -> Color.parseColor("#6B7280")
            isListening -> Color.parseColor("#38BDF8")
            else -> Color.parseColor("#60A5FA")
        }
        canvas.drawCircle(cx, cy, r - borderPaint.strokeWidth / 2f, borderPaint)

        // 5. Draw Clean Native Microphone Icon
        drawMicrophoneIcon(canvas, cx, cy)

        // 6. Draw Status Indicator Dot (Top-Right of circle)
        val dotCx = cx + r * 0.62f
        val dotCy = cy - r * 0.62f
        val dotR = 4.5f * density

        statusDotPaint.color = when {
            isPaused -> Color.parseColor("#F59E0B") // Amber
            isListening -> Color.parseColor("#38BDF8") // Cyan glow
            else -> Color.parseColor("#10B981") // Emerald Green
        }
        canvas.drawCircle(dotCx, dotCy, dotR, statusDotPaint)
        canvas.drawCircle(dotCx, dotCy, dotR, statusDotBorder)
    }

    private fun drawMicrophoneIcon(canvas: Canvas, cx: Float, cy: Float) {
        val s = density
        // Capsule
        val capsuleLeft = cx - 4.5f * s
        val capsuleRight = cx + 4.5f * s
        val capsuleTop = cy - 9f * s
        val capsuleBottom = cy + 2f * s
        val capsuleRadius = 4.5f * s

        canvas.drawRoundRect(
            capsuleLeft, capsuleTop, capsuleRight, capsuleBottom,
            capsuleRadius, capsuleRadius, iconPaint
        )

        // Arc around capsule
        val arcOvalLeft = cx - 8f * s
        val arcOvalRight = cx + 8f * s
        val arcOvalTop = cy - 4f * s
        val arcOvalBottom = cy + 7f * s

        canvas.drawArc(
            arcOvalLeft, arcOvalTop, arcOvalRight, arcOvalBottom,
            0f, 180f, false, iconStrokePaint
        )

        // Stem & Base
        canvas.drawLine(cx, cy + 7f * s, cx, cy + 11f * s, iconStrokePaint)
        canvas.drawLine(cx - 5f * s, cy + 11f * s, cx + 5f * s, cy + 11f * s, iconStrokePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val params = windowParams ?: return super.onTouchEvent(event)
        val wm = windowManager ?: return super.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                touchDownTime = System.currentTimeMillis()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - initialTouchX
                val dy = event.rawY - initialTouchY
                val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()

                if (dist > touchSlop || isDragging) {
                    isDragging = true
                    params.x = (initialX + dx).toInt()
                    params.y = (initialY + dy).toInt()

                    try {
                        wm.updateViewLayout(this, params)
                        onLayoutPositionChanged?.invoke(params.x, params.y)
                    } catch (_: Exception) {}
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                val duration = System.currentTimeMillis() - touchDownTime
                val dx = abs(event.rawX - initialTouchX)
                val dy = abs(event.rawY - initialTouchY)

                if (!isDragging && dx < touchSlop && dy < touchSlop && duration < 350) {
                    // Tap recognized!
                    triggerHaptic()
                    performClick()
                    onBubbleClickListener?.invoke()
                } else if (isDragging) {
                    // Snap to closest edge with smooth decelerating animation
                    snapToNearestEdge(wm, params)
                }
                isDragging = false
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun snapToNearestEdge(wm: WindowManager, params: WindowManager.LayoutParams) {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val currentX = params.x
        val margin = (12 * density).toInt()

        val targetX = if (currentX + (width / 2) < screenWidth / 2) {
            margin
        } else {
            screenWidth - width - margin
        }

        val anim = ValueAnimator.ofInt(currentX, targetX).apply {
            duration = 240
            interpolator = DecelerateInterpolator(1.6f)
            addUpdateListener { valueAnim ->
                params.x = valueAnim.animatedValue as Int
                try {
                    wm.updateViewLayout(this@FloatingBubbleView, params)
                    onLayoutPositionChanged?.invoke(params.x, params.y)
                } catch (_: Exception) {}
            }
        }
        anim.start()
    }

    private fun triggerHaptic() {
        try {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        } catch (_: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                v?.vibrate(30)
            }
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopPulseAnimation()
    }
}

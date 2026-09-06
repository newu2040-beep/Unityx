package com.example.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.UnityXDatabase
import com.example.data.repository.UnityXRepository
import com.example.domain.intelligence.ActionExecutor
import com.example.domain.intelligence.NluEngine
import com.example.domain.intelligence.TaskPlanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Foreground Service hosting the smooth floating circle assistant outside the app.
 * Provides interactive notification controls to Hide/Show, Pause/Resume, and Stop.
 */
class FloatingBubbleService : Service() {

    private var windowManager: WindowManager? = null
    private var bubbleView: FloatingBubbleView? = null
    private var windowParams: WindowManager.LayoutParams? = null

    private lateinit var speechManager: SpeechManager
    private lateinit var ttsManager: TtsManager
    private lateinit var nluEngine: NluEngine
    private lateinit var taskPlanner: TaskPlanner
    private lateinit var actionExecutor: ActionExecutor
    private lateinit var repository: UnityXRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        const val CHANNEL_ID = "unityx_floating_assistant_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START = "com.example.unityx.action.START_FLOATING_BUBBLE"
        const val ACTION_STOP = "com.example.unityx.action.STOP_FLOATING_BUBBLE"
        const val ACTION_TOGGLE_VISIBILITY = "com.example.unityx.action.TOGGLE_VISIBILITY"
        const val ACTION_TOGGLE_PAUSE = "com.example.unityx.action.TOGGLE_PAUSE"
        const val ACTION_HIDE = "com.example.unityx.action.HIDE_BUBBLE"
        const val ACTION_SHOW = "com.example.unityx.action.SHOW_BUBBLE"
        const val ACTION_PAUSE = "com.example.unityx.action.PAUSE_BUBBLE"
        const val ACTION_RESUME = "com.example.unityx.action.RESUME_BUBBLE"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _isHidden = MutableStateFlow(false)
        val isHidden: StateFlow<Boolean> = _isHidden.asStateFlow()

        private val _isPaused = MutableStateFlow(false)
        val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

        private val _isListening = MutableStateFlow(false)
        val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, FloatingBubbleService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingBubbleService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun toggleVisibility(context: Context) {
            val intent = Intent(context, FloatingBubbleService::class.java).apply {
                action = ACTION_TOGGLE_VISIBILITY
            }
            context.startService(intent)
        }

        fun togglePause(context: Context) {
            val intent = Intent(context, FloatingBubbleService::class.java).apply {
                action = ACTION_TOGGLE_PAUSE
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        _isRunning.value = true

        val db = UnityXDatabase.getInstance(applicationContext)
        repository = UnityXRepository(db)
        nluEngine = NluEngine()
        taskPlanner = TaskPlanner(nluEngine)
        actionExecutor = ActionExecutor(applicationContext, repository)
        speechManager = SpeechManager(applicationContext)
        ttsManager = TtsManager(applicationContext)

        createNotificationChannel()
        setupSpeechListener()
        initializeOverlayView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_STOP -> {
                stopServiceCompletely()
                return START_NOT_STICKY
            }

            ACTION_TOGGLE_VISIBILITY -> {
                setHidden(!_isHidden.value)
            }

            ACTION_HIDE -> {
                setHidden(true)
            }

            ACTION_SHOW -> {
                setHidden(false)
            }

            ACTION_TOGGLE_PAUSE -> {
                setPaused(!_isPaused.value)
            }

            ACTION_PAUSE -> {
                setPaused(true)
            }

            ACTION_RESUME -> {
                setPaused(false)
            }

            ACTION_START -> {
                // Ensure foreground service notification is active
                startForegroundWithNotification()
            }
        }

        startForegroundWithNotification()
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            startForeground(NOTIFICATION_ID, notification, fgsType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun setHidden(hidden: Boolean) {
        _isHidden.value = hidden
        bubbleView?.visibility = if (hidden) View.GONE else View.VISIBLE
        if (hidden && _isListening.value) {
            speechManager.stopListening()
            _isListening.value = false
            bubbleView?.isListening = false
        }
        updateNotification()
    }

    private fun setPaused(paused: Boolean) {
        _isPaused.value = paused
        bubbleView?.isPaused = paused
        if (paused && _isListening.value) {
            speechManager.stopListening()
            _isListening.value = false
            bubbleView?.isListening = false
        }
        updateNotification()
    }

    private fun setupSpeechListener() {
        speechManager.setOnResultListener { transcript ->
            _isListening.value = false
            bubbleView?.isListening = false

            if (transcript.isNotBlank()) {
                mainHandler.post {
                    Toast.makeText(applicationContext, "UNITYX: \"$transcript\"", Toast.LENGTH_SHORT).show()
                }
                processSpokenQuery(transcript)
            }
        }
    }

    private fun processSpokenQuery(query: String) {
        if (_isPaused.value) return

        serviceScope.launch {
            val plan = taskPlanner.planTask(query)
            var allSuccess = true
            var lastErrorMessage: String? = null
            var lastSuccessMessage: String? = null

            for (step in plan.steps) {
                val stepResult = actionExecutor.executeStep(step)
                if (!stepResult.isSuccess) {
                    allSuccess = false
                    lastErrorMessage = stepResult.error ?: stepResult.message
                    break
                } else {
                    lastSuccessMessage = stepResult.message
                }
            }

            // Speak natural response back to user
            val responseText = if (allSuccess) {
                if (plan.naturalResponse.isNotBlank()) plan.naturalResponse else (lastSuccessMessage ?: "Task completed.")
            } else {
                lastErrorMessage ?: "Task could not be completed."
            }

            ttsManager.speak(responseText)

            mainHandler.post {
                Toast.makeText(applicationContext, responseText, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initializeOverlayView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Cannot show overlay view without overlay permission; notification will still allow user to manage
            return
        }

        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            val density = resources.displayMetrics.density
            val bubbleSize = (58 * density).toInt()

            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            windowParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = (resources.displayMetrics.widthPixels - (bubbleSize * 1.5f).toInt() - (12 * density).toInt())
                y = (resources.displayMetrics.heightPixels * 0.35f).toInt()
            }

            bubbleView = FloatingBubbleView(this).apply {
                this.windowParams = this@FloatingBubbleService.windowParams
                this.windowManager = this@FloatingBubbleService.windowManager
                this.isPaused = _isPaused.value
                this.visibility = if (_isHidden.value) View.GONE else View.VISIBLE

                onBubbleClickListener = {
                    handleBubbleTap()
                }

                onBubbleLongClickListener = {
                    openApp()
                }
            }

            windowManager?.addView(bubbleView, windowParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleBubbleTap() {
        if (_isPaused.value) {
            Toast.makeText(applicationContext, "UNITYX is paused. Resume from notification shade.", Toast.LENGTH_SHORT).show()
            return
        }

        if (_isListening.value) {
            speechManager.stopListening()
            _isListening.value = false
            bubbleView?.isListening = false
        } else {
            _isListening.value = true
            bubbleView?.isListening = true
            speechManager.startListening()
        }
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "UNITYX Floating Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background floating circle assistant controls (Hide, Pause, Stop)"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val isHidden = _isHidden.value
        val isPaused = _isPaused.value

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 100, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: Hide / Show
        val toggleVisibilityIntent = Intent(this, FloatingBubbleService::class.java).apply {
            action = ACTION_TOGGLE_VISIBILITY
        }
        val toggleVisibilityPendingIntent = PendingIntent.getService(
            this, 101, toggleVisibilityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val visibilityActionTitle = if (isHidden) "Show" else "Hide"

        // Action 2: Pause / Resume
        val togglePauseIntent = Intent(this, FloatingBubbleService::class.java).apply {
            action = ACTION_TOGGLE_PAUSE
        }
        val togglePausePendingIntent = PendingIntent.getService(
            this, 102, togglePauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val pauseActionTitle = if (isPaused) "Resume" else "Pause"

        // Action 3: Stop
        val stopIntent = Intent(this, FloatingBubbleService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 103, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when {
            isPaused -> "UNITYX Assistant (Paused)"
            isHidden -> "UNITYX Assistant (Hidden)"
            else -> "UNITYX Assistant Active"
        }

        val text = when {
            isPaused -> "Background triggers paused. Tap Resume to activate."
            isHidden -> "Circle hidden from screen. Tap Show to reveal."
            else -> "Floating circle active. Tap to speak or open app."
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_view, visibilityActionTitle, toggleVisibilityPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, pauseActionTitle, togglePausePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun stopServiceCompletely() {
        _isRunning.value = false
        _isListening.value = false
        speechManager.stopListening()
        ttsManager.stop()

        removeOverlayView()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun removeOverlayView() {
        try {
            if (bubbleView != null && windowManager != null) {
                windowManager?.removeView(bubbleView)
                bubbleView = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _isRunning.value = false
        _isListening.value = false
        removeOverlayView()
        serviceScope.cancel()
        speechManager.stopListening()
        ttsManager.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

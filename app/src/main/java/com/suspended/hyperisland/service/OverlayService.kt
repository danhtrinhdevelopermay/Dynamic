package com.suspended.hyperisland.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.suspended.hyperisland.HyperIslandApp
import com.suspended.hyperisland.MainActivity
import com.suspended.hyperisland.R
import com.suspended.hyperisland.manager.*
import com.suspended.hyperisland.model.IslandEvent
import com.suspended.hyperisland.ui.components.DynamicIsland
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class OverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {
    
    companion object {
        private const val TAG = "OverlayService"
        const val ACTION_START_TIMER = "com.suspended.hyperisland.START_TIMER"
        const val ACTION_STOP_TIMER = "com.suspended.hyperisland.STOP_TIMER"
        const val ACTION_TOGGLE_TIMER = "com.suspended.hyperisland.TOGGLE_TIMER"
        const val EXTRA_TIMER_DURATION = "timer_duration"
        private const val BRING_TO_FRONT_INTERVAL = 3000L
        
        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            context.stopService(intent)
        }
        
        fun startTimer(context: Context, durationMs: Long) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_START_TIMER
                putExtra(EXTRA_TIMER_DURATION, durationMs)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
    
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    
    private lateinit var mediaManager: MediaManager
    private lateinit var timerManager: TimerManager
    private lateinit var flashlightManager: FlashlightManager
    private lateinit var chargingManager: ChargingManager
    private lateinit var settingsManager: SettingsManager
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val handler = Handler(Looper.getMainLooper())
    
    private var alwaysOnTop = true
    private var bringToFrontRunnable: Runnable? = null
    private var focusChangeListener: ViewTreeObserver.OnWindowFocusChangeListener? = null
    
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
    
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
    
    private val eventListener: (IslandEvent) -> Unit = { event ->
        handleEvent(event)
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "OverlayService onCreate")
        
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        initializeManagers()
        
        IslandStateManager.addEventListener(eventListener)
        
        startForeground(HyperIslandApp.NOTIFICATION_ID, createNotification())
        
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        
        createOverlayView()
        
        NotificationListenerService.setConnectionCallback {
            handler.post {
                Log.d(TAG, "NotificationListener connected, reinitializing MediaManager")
                mediaManager.reinitialize()
            }
        }
    }
    
    private fun initializeManagers() {
        mediaManager = MediaManager(this)
        timerManager = TimerManager()
        flashlightManager = FlashlightManager(this)
        chargingManager = ChargingManager(this)
        settingsManager = SettingsManager(this)
        
        mediaManager.initialize()
        flashlightManager.initialize()
        chargingManager.initialize()
        
        observeSettingsChanges()
    }
    
    private fun observeSettingsChanges() {
        serviceScope.launch {
            combine(
                settingsManager.positionX,
                settingsManager.positionY,
                settingsManager.sizeScale
            ) { x, y, scale ->
                Triple(x, y, scale)
            }.collect { (x, y, _) ->
                updateOverlayPosition(x, y)
            }
        }
        
        serviceScope.launch {
            settingsManager.alwaysOnTop.collect { enabled ->
                alwaysOnTop = enabled
                Log.d(TAG, "Always on top setting changed: $enabled")
                if (enabled) {
                    bringOverlayToFront()
                }
            }
        }
    }
    
    private fun updateOverlayPosition(x: Int, y: Int) {
        overlayParams?.let { params ->
            params.x = x
            params.y = y
            overlayView?.let { view ->
                try {
                    windowManager.updateViewLayout(view, params)
                    Log.d(TAG, "Overlay position updated: x=$x, y=$y")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update overlay position", e)
                }
            }
        }
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, HyperIslandApp.CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_island)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
    
    private fun createOverlayView() {
        overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 0
        }
        
        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            
            setContent {
                DynamicIslandOverlay(
                    mediaManager = mediaManager,
                    timerManager = timerManager,
                    flashlightManager = flashlightManager,
                    settingsManager = settingsManager
                )
            }
        }
        
        try {
            windowManager.addView(overlayView, overlayParams)
            Log.d(TAG, "Overlay view added successfully")
            
            startBringToFrontCheck()
            setupFocusChangeListener()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay view", e)
        }
    }
    
    private fun bringOverlayToFront() {
        if (!alwaysOnTop) return
        
        overlayView?.let { view ->
            overlayParams?.let { params ->
                try {
                    if (view.isAttachedToWindow) {
                        windowManager.removeViewImmediate(view)
                    }
                    windowManager.addView(view, params)
                    Log.d(TAG, "Overlay brought to front")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to bring overlay to front", e)
                }
            }
        }
    }
    
    private fun startBringToFrontCheck() {
        bringToFrontRunnable = object : Runnable {
            override fun run() {
                if (alwaysOnTop) {
                    bringOverlayToFront()
                }
                handler.postDelayed(this, BRING_TO_FRONT_INTERVAL)
            }
        }
        handler.postDelayed(bringToFrontRunnable!!, BRING_TO_FRONT_INTERVAL)
    }
    
    private fun stopBringToFrontCheck() {
        bringToFrontRunnable?.let { handler.removeCallbacks(it) }
        bringToFrontRunnable = null
    }
    
    private fun setupFocusChangeListener() {
        overlayView?.let { view ->
            focusChangeListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
                if (!hasFocus && alwaysOnTop) {
                    handler.postDelayed({
                        bringOverlayToFront()
                    }, 100)
                }
            }
            view.viewTreeObserver.addOnWindowFocusChangeListener(focusChangeListener)
        }
    }
    
    private fun removeFocusChangeListener() {
        overlayView?.let { view ->
            focusChangeListener?.let { listener ->
                view.viewTreeObserver.removeOnWindowFocusChangeListener(listener)
            }
        }
        focusChangeListener = null
    }
    
    private fun handleEvent(event: IslandEvent) {
        when (event) {
            is IslandEvent.MediaPlay -> mediaManager.play()
            is IslandEvent.MediaPause -> mediaManager.pause()
            is IslandEvent.MediaNext -> mediaManager.next()
            is IslandEvent.MediaPrevious -> mediaManager.previous()
            is IslandEvent.MediaSeek -> mediaManager.seekTo(event.position)
            
            is IslandEvent.TimerStart -> {}
            is IslandEvent.TimerPause -> timerManager.pauseTimer()
            is IslandEvent.TimerResume -> timerManager.resumeTimer()
            is IslandEvent.TimerStop -> timerManager.stopTimer()
            
            is IslandEvent.FlashlightToggle -> flashlightManager.toggle()
            
            else -> {}
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_TIMER -> {
                    val duration = it.getLongExtra(EXTRA_TIMER_DURATION, 60000)
                    timerManager.startTimer(duration)
                }
                ACTION_STOP_TIMER -> timerManager.stopTimer()
                ACTION_TOGGLE_TIMER -> timerManager.togglePause()
            }
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        Log.d(TAG, "OverlayService onDestroy")
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        
        serviceScope.cancel()
        stopBringToFrontCheck()
        removeFocusChangeListener()
        
        NotificationListenerService.setConnectionCallback(null)
        IslandStateManager.removeEventListener(eventListener)
        
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove overlay view", e)
            }
        }
        
        mediaManager.release()
        timerManager.release()
        flashlightManager.release()
        chargingManager.release()
        
        super.onDestroy()
    }
}

@Composable
fun DynamicIslandOverlay(
    mediaManager: MediaManager,
    timerManager: TimerManager,
    flashlightManager: FlashlightManager,
    settingsManager: SettingsManager
) {
    val state by IslandStateManager.state.collectAsState()
    val sizeScale by settingsManager.sizeScale.collectAsState(initial = SettingsManager.DEFAULT_SIZE_SCALE)
    
    DynamicIsland(
        state = state,
        sizeScale = sizeScale,
        onExpand = { type ->
            IslandStateManager.processEvent(IslandEvent.ExpandIsland(type))
        },
        onCollapse = {
            IslandStateManager.processEvent(IslandEvent.CollapseIsland)
        },
        onMediaPlayPause = {
            if (state.mediaState?.isPlaying == true) {
                IslandStateManager.processEvent(IslandEvent.MediaPause)
            } else {
                IslandStateManager.processEvent(IslandEvent.MediaPlay)
            }
        },
        onMediaNext = {
            IslandStateManager.processEvent(IslandEvent.MediaNext)
        },
        onMediaPrevious = {
            IslandStateManager.processEvent(IslandEvent.MediaPrevious)
        },
        onMediaSeek = { position ->
            IslandStateManager.processEvent(IslandEvent.MediaSeek(position))
        },
        onTimerToggle = {
            if (state.timerState?.isPaused == true) {
                IslandStateManager.processEvent(IslandEvent.TimerResume)
            } else {
                IslandStateManager.processEvent(IslandEvent.TimerPause)
            }
        },
        onTimerStop = {
            IslandStateManager.processEvent(IslandEvent.TimerStop)
        },
        onFlashlightToggle = {
            IslandStateManager.processEvent(IslandEvent.FlashlightToggle)
        },
        onNotificationDismiss = {
            IslandStateManager.processEvent(IslandEvent.NotificationDismiss)
        },
        onFileTransferAccept = {
            IslandStateManager.processEvent(IslandEvent.FileTransferAccept)
        },
        onFileTransferDecline = {
            IslandStateManager.processEvent(IslandEvent.FileTransferDecline)
        }
    )
}

package com.suspended.hyperisland.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
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

class OverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {
    
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    
    private lateinit var mediaManager: MediaManager
    private lateinit var timerManager: TimerManager
    private lateinit var flashlightManager: FlashlightManager
    private lateinit var chargingManager: ChargingManager
    
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
        
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        initializeManagers()
        
        IslandStateManager.addEventListener(eventListener)
        
        startForeground(HyperIslandApp.NOTIFICATION_ID, createNotification())
        
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        
        createOverlayView()
    }
    
    private fun initializeManagers() {
        mediaManager = MediaManager(this)
        timerManager = TimerManager()
        flashlightManager = FlashlightManager(this)
        chargingManager = ChargingManager(this)
        
        try {
            mediaManager.initialize()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        try {
            flashlightManager.initialize()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        try {
            chargingManager.initialize()
        } catch (e: Exception) {
            e.printStackTrace()
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
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 0
        }
        
        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            
            setContent {
                DynamicIslandOverlay(
                    mediaManager = mediaManager,
                    timerManager = timerManager,
                    flashlightManager = flashlightManager
                )
            }
        }
        
        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        
        IslandStateManager.removeEventListener(eventListener)
        
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        mediaManager.release()
        timerManager.release()
        flashlightManager.release()
        chargingManager.release()
        
        super.onDestroy()
    }
    
    companion object {
        const val ACTION_START_TIMER = "com.suspended.hyperisland.START_TIMER"
        const val ACTION_STOP_TIMER = "com.suspended.hyperisland.STOP_TIMER"
        const val ACTION_TOGGLE_TIMER = "com.suspended.hyperisland.TOGGLE_TIMER"
        const val EXTRA_TIMER_DURATION = "timer_duration"
        
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
}

@Composable
fun DynamicIslandOverlay(
    mediaManager: MediaManager,
    timerManager: TimerManager,
    flashlightManager: FlashlightManager
) {
    val state by IslandStateManager.state.collectAsState()
    
    DynamicIsland(
        state = state,
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

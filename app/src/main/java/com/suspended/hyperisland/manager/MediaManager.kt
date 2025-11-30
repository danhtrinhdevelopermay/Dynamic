package com.suspended.hyperisland.manager

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.suspended.hyperisland.model.IslandEvent
import com.suspended.hyperisland.model.MediaState
import com.suspended.hyperisland.service.NotificationListenerService

class MediaManager(private val context: Context) {
    
    companion object {
        private const val TAG = "MediaManager"
    }
    
    private var mediaSessionManager: MediaSessionManager? = null
    private var activeController: MediaController? = null
    private var mediaCallback: MediaController.Callback? = null
    private val handler = Handler(Looper.getMainLooper())
    private var positionUpdateRunnable: Runnable? = null
    private var isInitialized = false
    private var isListenerRegistered = false
    
    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        handler.post {
            updateActiveController(controllers)
        }
    }
    
    fun initialize() {
        if (isInitialized) {
            Log.d(TAG, "Already initialized")
            return
        }
        
        if (!NotificationListenerService.isConnected) {
            Log.d(TAG, "NotificationListener not connected yet, deferring initialization")
            return
        }
        
        try {
            mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            
            if (mediaSessionManager == null) {
                Log.w(TAG, "MediaSessionManager not available")
                return
            }
            
            val componentName = ComponentName(context, NotificationListenerService::class.java)
            
            val controllers = mediaSessionManager?.getActiveSessions(componentName)
            if (controllers != null) {
                updateActiveController(controllers)
                
                if (!isListenerRegistered) {
                    mediaSessionManager?.addOnActiveSessionsChangedListener(sessionListener, componentName)
                    isListenerRegistered = true
                }
                
                isInitialized = true
                Log.d(TAG, "MediaManager initialized successfully with ${controllers.size} sessions")
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException during initialization - notification access not granted")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MediaManager", e)
        }
    }
    
    fun reinitialize() {
        Log.d(TAG, "Reinitializing MediaManager")
        
        stopPositionUpdates()
        
        activeController?.let { controller ->
            mediaCallback?.let { callback ->
                try {
                    controller.unregisterCallback(callback)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to unregister callback during reinitialize", e)
                }
            }
        }
        activeController = null
        mediaCallback = null
        
        isInitialized = false
        
        initialize()
    }
    
    private fun updateActiveController(controllers: List<MediaController>?) {
        activeController?.let { controller ->
            mediaCallback?.let { callback ->
                try {
                    controller.unregisterCallback(callback)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to unregister callback", e)
                }
            }
        }
        
        activeController = controllers?.firstOrNull { controller ->
            controller.playbackState?.state == PlaybackState.STATE_PLAYING ||
            controller.playbackState?.state == PlaybackState.STATE_PAUSED
        } ?: controllers?.firstOrNull()
        
        activeController?.let { controller ->
            Log.d(TAG, "Active controller: ${controller.packageName}")
            
            mediaCallback = object : MediaController.Callback() {
                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    handler.post { updateMediaState() }
                }
                
                override fun onMetadataChanged(metadata: MediaMetadata?) {
                    handler.post { updateMediaState() }
                }
                
                override fun onSessionDestroyed() {
                    handler.post {
                        IslandStateManager.processEvent(IslandEvent.MediaStop)
                    }
                }
            }
            
            try {
                controller.registerCallback(mediaCallback!!, handler)
                updateMediaState()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register callback", e)
            }
        }
    }
    
    private fun updateMediaState() {
        val controller = activeController ?: return
        val metadata = controller.metadata
        val playbackState = controller.playbackState
        
        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
        
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
        
        if (title.isBlank() && artist.isBlank() && !isPlaying) {
            return
        }
        
        val albumArt = try {
            metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) 
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        } catch (e: Exception) {
            null
        }
        
        val mediaState = MediaState(
            isPlaying = isPlaying,
            title = title,
            artist = artist,
            albumArt = albumArt,
            duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0,
            position = playbackState?.position ?: 0,
            appPackage = controller.packageName
        )
        
        IslandStateManager.processEvent(IslandEvent.MediaUpdate(mediaState))
        
        if (isPlaying) {
            startPositionUpdates()
        } else {
            stopPositionUpdates()
        }
    }
    
    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateRunnable = object : Runnable {
            override fun run() {
                updateMediaState()
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(positionUpdateRunnable!!, 1000)
    }
    
    private fun stopPositionUpdates() {
        positionUpdateRunnable?.let { handler.removeCallbacks(it) }
        positionUpdateRunnable = null
    }
    
    fun play() {
        try {
            activeController?.transportControls?.play()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play", e)
        }
    }
    
    fun pause() {
        try {
            activeController?.transportControls?.pause()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause", e)
        }
    }
    
    fun playPause() {
        val state = activeController?.playbackState?.state
        if (state == PlaybackState.STATE_PLAYING) {
            pause()
        } else {
            play()
        }
    }
    
    fun next() {
        try {
            activeController?.transportControls?.skipToNext()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to skip next", e)
        }
    }
    
    fun previous() {
        try {
            activeController?.transportControls?.skipToPrevious()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to skip previous", e)
        }
    }
    
    fun seekTo(position: Long) {
        try {
            activeController?.transportControls?.seekTo(position)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to seek", e)
        }
    }
    
    fun stop() {
        try {
            activeController?.transportControls?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop", e)
        }
    }
    
    fun release() {
        stopPositionUpdates()
        activeController?.let { controller ->
            mediaCallback?.let { callback ->
                try {
                    controller.unregisterCallback(callback)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to unregister callback during release", e)
                }
            }
        }
        if (isListenerRegistered) {
            try {
                mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionListener)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to remove session listener", e)
            }
            isListenerRegistered = false
        }
        isInitialized = false
    }
}

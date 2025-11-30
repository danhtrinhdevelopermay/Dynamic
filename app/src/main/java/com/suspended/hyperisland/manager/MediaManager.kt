package com.suspended.hyperisland.manager

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import com.suspended.hyperisland.model.IslandEvent
import com.suspended.hyperisland.model.MediaState
import com.suspended.hyperisland.service.NotificationListenerService

class MediaManager(private val context: Context) {
    
    private var mediaSessionManager: MediaSessionManager? = null
    private var activeController: MediaController? = null
    private var mediaCallback: MediaController.Callback? = null
    private val handler = Handler(Looper.getMainLooper())
    private var positionUpdateRunnable: Runnable? = null
    
    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        updateActiveController(controllers)
    }
    
    fun initialize() {
        try {
            mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val componentName = ComponentName(context, NotificationListenerService::class.java)
            
            val controllers = mediaSessionManager?.getActiveSessions(componentName)
            updateActiveController(controllers)
            
            mediaSessionManager?.addOnActiveSessionsChangedListener(sessionListener, componentName)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    private fun updateActiveController(controllers: List<MediaController>?) {
        activeController?.let { controller ->
            mediaCallback?.let { callback ->
                controller.unregisterCallback(callback)
            }
        }
        
        activeController = controllers?.firstOrNull { controller ->
            controller.playbackState?.state == PlaybackState.STATE_PLAYING ||
            controller.playbackState?.state == PlaybackState.STATE_PAUSED
        } ?: controllers?.firstOrNull()
        
        activeController?.let { controller ->
            mediaCallback = object : MediaController.Callback() {
                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    updateMediaState()
                }
                
                override fun onMetadataChanged(metadata: MediaMetadata?) {
                    updateMediaState()
                }
            }
            
            controller.registerCallback(mediaCallback!!, handler)
            updateMediaState()
        }
    }
    
    private fun updateMediaState() {
        val controller = activeController ?: return
        val metadata = controller.metadata
        val playbackState = controller.playbackState
        
        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
        
        val mediaState = MediaState(
            isPlaying = isPlaying,
            title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "",
            artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "",
            albumArt = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) 
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART),
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
        activeController?.transportControls?.play()
    }
    
    fun pause() {
        activeController?.transportControls?.pause()
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
        activeController?.transportControls?.skipToNext()
    }
    
    fun previous() {
        activeController?.transportControls?.skipToPrevious()
    }
    
    fun seekTo(position: Long) {
        activeController?.transportControls?.seekTo(position)
    }
    
    fun stop() {
        activeController?.transportControls?.stop()
    }
    
    fun release() {
        stopPositionUpdates()
        activeController?.let { controller ->
            mediaCallback?.let { callback ->
                controller.unregisterCallback(callback)
            }
        }
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

package com.suspended.hyperisland.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.suspended.hyperisland.manager.MediaManager

class MediaControllerService : Service() {
    
    private lateinit var mediaManager: MediaManager
    
    override fun onCreate() {
        super.onCreate()
        mediaManager = MediaManager(this)
        mediaManager.initialize()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        mediaManager.release()
    }
}

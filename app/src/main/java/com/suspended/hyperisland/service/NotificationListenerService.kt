package com.suspended.hyperisland.service

import android.app.Notification
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.suspended.hyperisland.manager.IslandStateManager
import com.suspended.hyperisland.model.IslandEvent
import com.suspended.hyperisland.model.NotificationState

class NotificationListenerService : NotificationListenerService() {
    
    companion object {
        private const val TAG = "NotificationListener"
        
        private val IGNORED_PACKAGES = setOf(
            "com.suspended.hyperisland",
            "com.android.systemui",
            "android"
        )
        
        private val MEDIA_PACKAGES = setOf(
            "com.spotify.music",
            "com.google.android.apps.youtube.music",
            "com.apple.android.music",
            "com.amazon.mp3",
            "com.soundcloud.android",
            "com.pandora.android",
            "com.google.android.music"
        )
        
        var isConnected = false
            private set
        
        private var connectionCallback: (() -> Unit)? = null
        
        fun setConnectionCallback(callback: (() -> Unit)?) {
            connectionCallback = callback
            if (isConnected) {
                callback?.invoke()
            }
        }
    }
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        isConnected = true
        Log.d(TAG, "NotificationListener connected")
        connectionCallback?.invoke()
    }
    
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isConnected = false
        Log.d(TAG, "NotificationListener disconnected")
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        try {
            if (sbn.packageName in IGNORED_PACKAGES) return
            
            if (sbn.packageName in MEDIA_PACKAGES) return
            
            val notification = sbn.notification
            
            if (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) return
            
            val extras = notification.extras
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            
            if (title.isBlank() && text.isBlank()) return
            
            val appIcon = getAppIcon(sbn.packageName)
            val appName = getAppName(sbn.packageName)
            
            val notificationState = NotificationState(
                packageName = sbn.packageName,
                appName = appName,
                appIcon = appIcon,
                title = title,
                text = text,
                timestamp = sbn.postTime,
                key = sbn.key
            )
            
            IslandStateManager.processEvent(IslandEvent.NotificationReceived(notificationState))
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
    }
    
    private fun getAppIcon(packageName: String): Bitmap? {
        return try {
            val drawable = packageManager.getApplicationIcon(packageName)
            if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth.coerceAtLeast(1),
                    drawable.intrinsicHeight.coerceAtLeast(1),
                    Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        } catch (e: PackageManager.NameNotFoundException) {
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app icon", e)
            null
        }
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}

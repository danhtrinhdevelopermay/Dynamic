package com.suspended.hyperisland.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.suspended.hyperisland.manager.PermissionManager
import com.suspended.hyperisland.service.OverlayService

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (PermissionManager.hasAllRequiredPermissions(context)) {
                OverlayService.start(context)
            }
        }
    }
}

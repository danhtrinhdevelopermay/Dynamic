package com.suspended.hyperisland

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.suspended.hyperisland.manager.PermissionManager
import com.suspended.hyperisland.service.OverlayService
import com.suspended.hyperisland.ui.screens.MainScreen
import com.suspended.hyperisland.ui.screens.PermissionScreen
import com.suspended.hyperisland.ui.theme.HyperIslandTheme

class MainActivity : ComponentActivity() {
    
    private var isServiceRunning by mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            HyperIslandTheme {
                var hasAllPermissions by remember { 
                    mutableStateOf(PermissionManager.hasAllRequiredPermissions(this)) 
                }
                
                LaunchedEffect(Unit) {
                    while (true) {
                        hasAllPermissions = PermissionManager.hasAllRequiredPermissions(this@MainActivity)
                        kotlinx.coroutines.delay(500)
                    }
                }
                
                if (hasAllPermissions) {
                    MainScreen(
                        isServiceRunning = isServiceRunning,
                        onToggleService = { enabled ->
                            if (enabled) {
                                startOverlayService()
                            } else {
                                stopOverlayService()
                            }
                        }
                    )
                } else {
                    PermissionScreen(
                        onAllPermissionsGranted = {
                            hasAllPermissions = true
                            startOverlayService()
                        }
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (PermissionManager.hasAllRequiredPermissions(this)) {
            if (!isServiceRunning) {
                startOverlayService()
            }
        }
    }
    
    private fun startOverlayService() {
        if (PermissionManager.hasOverlayPermission(this)) {
            OverlayService.start(this)
            isServiceRunning = true
        }
    }
    
    private fun stopOverlayService() {
        OverlayService.stop(this)
        isServiceRunning = false
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            PermissionManager.REQUEST_OVERLAY_PERMISSION -> {
            }
            PermissionManager.REQUEST_NOTIFICATION_PERMISSION -> {
            }
        }
    }
}

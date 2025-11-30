package com.suspended.hyperisland.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suspended.hyperisland.manager.PermissionManager
import com.suspended.hyperisland.ui.theme.IslandColors

@Composable
fun PermissionScreen(
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    var hasOverlayPermission by remember { mutableStateOf(PermissionManager.hasOverlayPermission(context)) }
    var hasNotificationListenerPermission by remember { mutableStateOf(PermissionManager.hasNotificationListenerPermission(context)) }
    var hasNotificationPermission by remember { mutableStateOf(PermissionManager.hasNotificationPermission(context)) }
    
    LaunchedEffect(Unit) {
        while (true) {
            hasOverlayPermission = PermissionManager.hasOverlayPermission(context)
            hasNotificationListenerPermission = PermissionManager.hasNotificationListenerPermission(context)
            hasNotificationPermission = PermissionManager.hasNotificationPermission(context)
            
            if (hasOverlayPermission && hasNotificationListenerPermission && hasNotificationPermission) {
                onAllPermissionsGranted()
                break
            }
            
            kotlinx.coroutines.delay(500)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IslandColors.BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(IslandColors.Primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = IslandColors.Primary,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Permissions Required",
            color = IslandColors.TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "HyperIsland needs some permissions to work properly",
            color = IslandColors.TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        PermissionItem(
            icon = Icons.Default.Layers,
            title = "Display Over Other Apps",
            description = "Required to show Dynamic Island on top of other apps",
            isGranted = hasOverlayPermission,
            onRequest = {
                activity?.let { PermissionManager.requestOverlayPermission(it) }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PermissionItem(
            icon = Icons.Default.Notifications,
            title = "Notification Access",
            description = "Required to display notifications in Dynamic Island",
            isGranted = hasNotificationListenerPermission,
            onRequest = {
                activity?.let { PermissionManager.requestNotificationListenerPermission(it) }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PermissionItem(
            icon = Icons.Default.NotificationsActive,
            title = "Post Notifications",
            description = "Required to keep the service running",
            isGranted = hasNotificationPermission,
            onRequest = {
                activity?.let { PermissionManager.requestNotificationPermission(it) }
            }
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (hasOverlayPermission && hasNotificationListenerPermission && hasNotificationPermission) {
            Button(
                onClick = onAllPermissionsGranted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IslandColors.Primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = IslandColors.Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isGranted) IslandColors.AccentGreen.copy(alpha = 0.2f)
                        else IslandColors.Primary.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else icon,
                    contentDescription = null,
                    tint = if (isGranted) IslandColors.AccentGreen else IslandColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = IslandColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = description,
                    color = IslandColors.TextSecondary,
                    fontSize = 13.sp
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            if (!isGranted) {
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IslandColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Grant",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

package com.suspended.hyperisland.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.suspended.hyperisland.manager.SettingsManager
import com.suspended.hyperisland.service.OverlayService
import com.suspended.hyperisland.ui.theme.IslandColors
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    isServiceRunning: Boolean,
    onToggleService: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    val settingsManager = remember { SettingsManager(context) }
    val positionX by settingsManager.positionX.collectAsState(initial = SettingsManager.DEFAULT_POSITION_X)
    val positionY by settingsManager.positionY.collectAsState(initial = SettingsManager.DEFAULT_POSITION_Y)
    val sizeScale by settingsManager.sizeScale.collectAsState(initial = SettingsManager.DEFAULT_SIZE_SCALE)
    val alwaysOnTop by settingsManager.alwaysOnTop.collectAsState(initial = SettingsManager.DEFAULT_ALWAYS_ON_TOP)
    
    var showTimerDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IslandColors.BackgroundDark)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    if (isServiceRunning) IslandColors.AccentGreen.copy(alpha = 0.2f)
                    else IslandColors.Surface
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Dashboard,
                contentDescription = null,
                tint = if (isServiceRunning) IslandColors.AccentGreen else IslandColors.TextSecondary,
                modifier = Modifier.size(50.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "HyperIsland",
            color = IslandColors.TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = if (isServiceRunning) "Service is running" else "Service is stopped",
            color = if (isServiceRunning) IslandColors.AccentGreen else IslandColors.TextSecondary,
            fontSize = 15.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = IslandColors.Surface
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dynamic Island",
                        color = IslandColors.TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Show Dynamic Island overlay",
                        color = IslandColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }
                
                Switch(
                    checked = isServiceRunning,
                    onCheckedChange = onToggleService,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = IslandColors.AccentGreen,
                        uncheckedThumbColor = IslandColors.TextSecondary,
                        uncheckedTrackColor = IslandColors.SurfaceVariant
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showSettingsDialog = true },
            colors = CardDefaults.cardColors(
                containerColor = IslandColors.Surface
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = IslandColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "Position & Size",
                            color = IslandColors.TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Adjust Dynamic Island position and size",
                            color = IslandColors.TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = IslandColors.TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "QUICK ACTIONS",
            color = IslandColors.TextTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Timer,
                title = "Timer",
                color = IslandColors.AccentOrange,
                onClick = { showTimerDialog = true }
            )
            
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.FlashlightOn,
                title = "Test Flash",
                color = IslandColors.ChargingYellow,
                onClick = {
                }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.MusicNote,
                title = "Test Media",
                color = IslandColors.AccentPurple,
                onClick = {
                }
            )
            
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Notifications,
                title = "Test Notif",
                color = IslandColors.AccentBlue,
                onClick = {
                }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "FEATURES",
            color = IslandColors.TextTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        FeatureItem(
            icon = Icons.Default.MusicNote,
            title = "Media Controls",
            description = "Control music playback from Dynamic Island"
        )
        
        FeatureItem(
            icon = Icons.Default.Timer,
            title = "Timer",
            description = "View and control timers"
        )
        
        FeatureItem(
            icon = Icons.Default.Mic,
            title = "Recording",
            description = "See when apps are recording"
        )
        
        FeatureItem(
            icon = Icons.Default.BatteryChargingFull,
            title = "Charging",
            description = "View charging status and wattage"
        )
        
        FeatureItem(
            icon = Icons.Default.FlashlightOn,
            title = "Flashlight",
            description = "Quick flashlight control"
        )
        
        FeatureItem(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            description = "View notifications in Dynamic Island"
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Version 1.0.0",
            color = IslandColors.TextTertiary,
            fontSize = 12.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
    
    if (showTimerDialog) {
        TimerDialog(
            onDismiss = { showTimerDialog = false },
            onStartTimer = { duration ->
                OverlayService.startTimer(context, duration)
                showTimerDialog = false
            }
        )
    }
    
    if (showSettingsDialog) {
        SettingsDialog(
            positionX = positionX,
            positionY = positionY,
            sizeScale = sizeScale,
            alwaysOnTop = alwaysOnTop,
            onPositionXChange = { x ->
                scope.launch { settingsManager.setPositionX(x) }
            },
            onPositionYChange = { y ->
                scope.launch { settingsManager.setPositionY(y) }
            },
            onSizeScaleChange = { scale ->
                scope.launch { settingsManager.setSizeScale(scale) }
            },
            onAlwaysOnTopChange = { enabled ->
                scope.launch { settingsManager.setAlwaysOnTop(enabled) }
            },
            onReset = {
                scope.launch { settingsManager.resetToDefaults() }
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = IslandColors.Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                color = IslandColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = IslandColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IslandColors.Primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = IslandColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = description,
                    color = IslandColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
            
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = IslandColors.AccentGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TimerDialog(
    onDismiss: () -> Unit,
    onStartTimer: (Long) -> Unit
) {
    var minutes by remember { mutableStateOf("5") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IslandColors.Surface,
        title = {
            Text(
                text = "Start Timer",
                color = IslandColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter duration in minutes",
                    color = IslandColors.TextSecondary,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() } && it.length <= 3) {
                            minutes = it
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IslandColors.Primary,
                        unfocusedBorderColor = IslandColors.SurfaceVariant,
                        focusedTextColor = IslandColors.TextPrimary,
                        unfocusedTextColor = IslandColors.TextPrimary
                    ),
                    suffix = {
                        Text(
                            text = "min",
                            color = IslandColors.TextSecondary
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mins = minutes.toLongOrNull() ?: 5
                    onStartTimer(mins * 60 * 1000)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = IslandColors.Primary
                )
            ) {
                Text("Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = IslandColors.TextSecondary
                )
            }
        }
    )
}

@Composable
private fun SettingsDialog(
    positionX: Int,
    positionY: Int,
    sizeScale: Float,
    alwaysOnTop: Boolean,
    onPositionXChange: (Int) -> Unit,
    onPositionYChange: (Int) -> Unit,
    onSizeScaleChange: (Float) -> Unit,
    onAlwaysOnTopChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IslandColors.Surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    color = IslandColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                
                IconButton(
                    onClick = onReset,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = IslandColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Always on Top",
                            color = IslandColors.TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Keep Dynamic Island above other overlays",
                            color = IslandColors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    
                    Switch(
                        checked = alwaysOnTop,
                        onCheckedChange = onAlwaysOnTopChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = IslandColors.Primary,
                            checkedTrackColor = IslandColors.Primary.copy(alpha = 0.5f),
                            uncheckedThumbColor = IslandColors.TextSecondary,
                            uncheckedTrackColor = IslandColors.SurfaceVariant
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "Horizontal Position (X)",
                    color = IslandColors.TextSecondary,
                    fontSize = 13.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${positionX}px",
                        color = IslandColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(60.dp)
                    )
                    
                    Slider(
                        value = positionX.toFloat(),
                        onValueChange = { onPositionXChange(it.toInt()) },
                        valueRange = SettingsManager.MIN_POSITION_X.toFloat()..SettingsManager.MAX_POSITION_X.toFloat(),
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = IslandColors.Primary,
                            activeTrackColor = IslandColors.Primary,
                            inactiveTrackColor = IslandColors.SurfaceVariant
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Vertical Position (Y)",
                    color = IslandColors.TextSecondary,
                    fontSize = 13.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${positionY}px",
                        color = IslandColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(60.dp)
                    )
                    
                    Slider(
                        value = positionY.toFloat(),
                        onValueChange = { onPositionYChange(it.toInt()) },
                        valueRange = SettingsManager.MIN_POSITION_Y.toFloat()..SettingsManager.MAX_POSITION_Y.toFloat(),
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = IslandColors.Primary,
                            activeTrackColor = IslandColors.Primary,
                            inactiveTrackColor = IslandColors.SurfaceVariant
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Size Scale",
                    color = IslandColors.TextSecondary,
                    fontSize = 13.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(sizeScale * 100).toInt()}%",
                        color = IslandColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(60.dp)
                    )
                    
                    Slider(
                        value = sizeScale,
                        onValueChange = { onSizeScaleChange(it) },
                        valueRange = SettingsManager.MIN_SIZE_SCALE..SettingsManager.MAX_SIZE_SCALE,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = IslandColors.Primary,
                            activeTrackColor = IslandColors.Primary,
                            inactiveTrackColor = IslandColors.SurfaceVariant
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Changes are applied instantly.",
                    color = IslandColors.TextTertiary,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IslandColors.Primary
                )
            ) {
                Text("Done")
            }
        }
    )
}

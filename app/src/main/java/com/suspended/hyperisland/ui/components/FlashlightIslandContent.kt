package com.suspended.hyperisland.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suspended.hyperisland.model.FlashlightState
import com.suspended.hyperisland.model.IslandMode
import com.suspended.hyperisland.ui.theme.IslandColors

@Composable
fun FlashlightIslandContent(
    mode: IslandMode,
    flashlightState: FlashlightState,
    onToggle: () -> Unit
) {
    when (mode) {
        IslandMode.COMPACT -> FlashlightCompact(flashlightState)
        IslandMode.MEDIUM -> FlashlightMedium(flashlightState, onToggle)
        IslandMode.EXPANDED -> FlashlightExpanded(flashlightState, onToggle)
    }
}

@Composable
private fun FlashlightCompact(flashlightState: FlashlightState) {
    val infiniteTransition = rememberInfiniteTransition(label = "flashlight")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.FlashlightOn,
            contentDescription = null,
            tint = if (flashlightState.isOn) 
                IslandColors.ChargingYellow.copy(alpha = glowAlpha) 
            else IslandColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun FlashlightMedium(
    flashlightState: FlashlightState,
    onToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flashlight")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FlashlightOn,
                contentDescription = null,
                tint = if (flashlightState.isOn) 
                    IslandColors.ChargingYellow.copy(alpha = glowAlpha) 
                else IslandColors.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Column {
                Text(
                    text = "Flashlight",
                    color = IslandColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = if (flashlightState.isOn) "On" else "Off",
                    color = IslandColors.TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun FlashlightExpanded(
    flashlightState: FlashlightState,
    onToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flashlight")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (flashlightState.isOn) 
                                IslandColors.ChargingYellow.copy(alpha = 0.2f)
                            else IslandColors.SurfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashlightOn,
                        contentDescription = null,
                        tint = if (flashlightState.isOn) 
                            IslandColors.ChargingYellow.copy(alpha = glowAlpha) 
                        else IslandColors.TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Flashlight",
                        color = IslandColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Text(
                        text = "Flashlight is ${if (flashlightState.isOn) "on" else "off"}",
                        color = IslandColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = onToggle,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (flashlightState.isOn) 
                    IslandColors.AccentRed 
                else IslandColors.AccentBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (flashlightState.isOn) "Turn off" else "Turn on",
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

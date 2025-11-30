package com.suspended.hyperisland.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suspended.hyperisland.model.ChargingState
import com.suspended.hyperisland.model.IslandMode
import com.suspended.hyperisland.ui.theme.IslandColors

@Composable
fun ChargingIslandContent(
    mode: IslandMode,
    chargingState: ChargingState
) {
    when (mode) {
        IslandMode.COMPACT -> ChargingCompact(chargingState)
        IslandMode.MEDIUM -> ChargingMedium(chargingState)
        IslandMode.EXPANDED -> ChargingExpanded(chargingState)
    }
}

@Composable
private fun ChargingCompact(chargingState: ChargingState) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val batteryColor = when {
            chargingState.batteryLevel < 20 -> IslandColors.AccentRed
            chargingState.batteryLevel < 50 -> IslandColors.ChargingYellow
            else -> IslandColors.ChargingGreen
        }
        
        Icon(
            imageVector = Icons.Default.BatteryChargingFull,
            contentDescription = null,
            tint = batteryColor,
            modifier = Modifier.size(18.dp)
        )
        
        Spacer(modifier = Modifier.width(6.dp))
        
        Text(
            text = "${chargingState.batteryLevel}%",
            color = IslandColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ChargingMedium(chargingState: ChargingState) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val batteryColor = when {
            chargingState.batteryLevel < 20 -> IslandColors.AccentRed
            chargingState.batteryLevel < 50 -> IslandColors.ChargingYellow
            else -> IslandColors.ChargingGreen
        }
        
        Icon(
            imageVector = Icons.Default.BatteryChargingFull,
            contentDescription = null,
            tint = batteryColor,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        if (chargingState.chargingWattage > 0) {
            Text(
                text = "${chargingState.chargingWattage.toInt()}W",
                color = IslandColors.ChargingGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = "${String.format("%.2f", chargingState.batteryLevel.toFloat())}%",
            color = IslandColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = IslandColors.ChargingYellow,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ChargingExpanded(chargingState: ChargingState) {
    val infiniteTransition = rememberInfiniteTransition(label = "charging")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val batteryColor = when {
        chargingState.batteryLevel < 20 -> IslandColors.AccentRed
        chargingState.batteryLevel < 50 -> IslandColors.ChargingYellow
        else -> IslandColors.ChargingGreen
    }
    
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
                if (chargingState.chargingWattage > 0) {
                    Text(
                        text = "${chargingState.chargingWattage.toInt()}W",
                        color = IslandColors.ChargingGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                Text(
                    text = "${String.format("%.2f", chargingState.batteryLevel.toFloat())}%",
                    color = IslandColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = IslandColors.ChargingYellow.copy(alpha = pulseAlpha),
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(IslandColors.SurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(chargingState.batteryLevel / 100f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(batteryColor)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Charging",
                color = IslandColors.TextSecondary,
                fontSize = 12.sp
            )
            
            if (chargingState.estimatedTimeToFull > 0) {
                val minutes = chargingState.estimatedTimeToFull / 60000
                Text(
                    text = "${minutes}min until full",
                    color = IslandColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

package com.suspended.hyperisland.ui.components

import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suspended.hyperisland.model.IslandMode
import com.suspended.hyperisland.model.RecordingState
import com.suspended.hyperisland.model.RecordingType
import com.suspended.hyperisland.ui.theme.IslandColors

@Composable
fun RecordingIslandContent(
    mode: IslandMode,
    recordingState: RecordingState
) {
    when (mode) {
        IslandMode.COMPACT -> RecordingCompact(recordingState)
        IslandMode.MEDIUM -> RecordingMedium(recordingState)
        IslandMode.EXPANDED -> RecordingExpanded(recordingState)
    }
}

@Composable
private fun RecordingCompact(recordingState: RecordingState) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(IslandColors.RecordingRed.copy(alpha = pulseAlpha))
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = formatRecordingTime(recordingState.durationMs),
            color = IslandColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RecordingMedium(recordingState: RecordingState) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
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
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(IslandColors.RecordingRed.copy(alpha = pulseAlpha))
            )
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Text(
                text = formatRecordingTime(recordingState.durationMs),
                color = IslandColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Text(
            text = getRecordingTypeLabel(recordingState.type),
            color = IslandColors.TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun RecordingExpanded(recordingState: RecordingState) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val waveformHeights = remember { List(12) { (4..16).random().toFloat() } }
    
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
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(IslandColors.RecordingRed.copy(alpha = pulseAlpha))
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = formatRecordingTime(recordingState.durationMs),
                    color = IslandColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Icon(
                imageVector = when (recordingState.type) {
                    RecordingType.AUDIO -> Icons.Default.Mic
                    RecordingType.VIDEO -> Icons.Default.Videocam
                    RecordingType.SCREEN -> Icons.Default.ScreenShare
                },
                contentDescription = null,
                tint = IslandColors.RecordingRed,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            waveformHeights.forEachIndexed { index, baseHeight ->
                val animatedHeight by infiniteTransition.animateFloat(
                    initialValue = baseHeight * 0.5f,
                    targetValue = baseHeight,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 300 + index * 50,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "waveform$index"
                )
                
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(animatedHeight.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(IslandColors.RecordingRed.copy(alpha = 0.7f))
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = getRecordingTypeLabel(recordingState.type),
            color = IslandColors.TextSecondary,
            fontSize = 13.sp
        )
    }
}

private fun formatRecordingTime(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun getRecordingTypeLabel(type: RecordingType): String {
    return when (type) {
        RecordingType.AUDIO -> "Audio Recording"
        RecordingType.VIDEO -> "Video Recording"
        RecordingType.SCREEN -> "Screen Recording"
    }
}

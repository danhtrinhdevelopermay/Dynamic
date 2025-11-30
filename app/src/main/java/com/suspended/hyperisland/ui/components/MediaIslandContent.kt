package com.suspended.hyperisland.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suspended.hyperisland.model.IslandMode
import com.suspended.hyperisland.model.MediaState
import com.suspended.hyperisland.ui.theme.IslandColors

@Composable
fun MediaIslandContent(
    mode: IslandMode,
    mediaState: MediaState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit
) {
    when (mode) {
        IslandMode.COMPACT -> MediaCompact(mediaState, onPlayPause)
        IslandMode.MEDIUM -> MediaMedium(mediaState, onPlayPause)
        IslandMode.EXPANDED -> MediaExpanded(mediaState, onPlayPause, onNext, onPrevious, onSeek)
    }
}

@Composable
private fun MediaCompact(
    mediaState: MediaState,
    onPlayPause: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (mediaState.albumArt != null) {
            Image(
                bitmap = mediaState.albumArt.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(IslandColors.AccentPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        if (mediaState.isPlaying) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val animatedHeight by infiniteTransition.animateFloat(
                        initialValue = 4f,
                        targetValue = 16f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 400 + index * 100,
                                easing = FastOutSlowInEasing
                            ),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "bar$index"
                    )
                    
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(animatedHeight.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(IslandColors.WaveformActive)
                    )
                }
            }
        } else {
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = null,
                tint = IslandColors.TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun MediaMedium(
    mediaState: MediaState,
    onPlayPause: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (mediaState.albumArt != null) {
            Image(
                bitmap = mediaState.albumArt.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(IslandColors.AccentPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        if (mediaState.isPlaying) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    val animatedHeight by infiniteTransition.animateFloat(
                        initialValue = 4f,
                        targetValue = 18f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 350 + index * 80,
                                easing = FastOutSlowInEasing
                            ),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "bar$index"
                    )
                    
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(animatedHeight.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(IslandColors.WaveformActive)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = formatDuration(mediaState.position),
            color = IslandColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MediaExpanded(
    mediaState: MediaState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            if (mediaState.albumArt != null) {
                Image(
                    bitmap = mediaState.albumArt.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(IslandColors.AccentPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mediaState.title.ifEmpty { "Unknown Title" },
                    color = IslandColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = mediaState.artist.ifEmpty { "Unknown Artist" },
                    color = IslandColors.TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Icon(
                imageVector = Icons.Default.Cast,
                contentDescription = "Cast",
                tint = IslandColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = IslandColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = IslandColors.TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(IslandColors.TextPrimary.copy(alpha = 0.1f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onPlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (mediaState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (mediaState.isPlaying) "Pause" else "Play",
                    tint = IslandColors.TextPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            IconButton(
                onClick = onNext,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = IslandColors.TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            IconButton(
                onClick = {},
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = IslandColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(mediaState.position),
                color = IslandColors.TextSecondary,
                fontSize = 11.sp
            )
            
            val progress = if (mediaState.duration > 0) {
                mediaState.position.toFloat() / mediaState.duration.toFloat()
            } else 0f
            
            Slider(
                value = progress,
                onValueChange = { newProgress ->
                    onSeek((newProgress * mediaState.duration).toLong())
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .height(4.dp),
                colors = SliderDefaults.colors(
                    thumbColor = IslandColors.TextPrimary,
                    activeTrackColor = IslandColors.TextPrimary,
                    inactiveTrackColor = IslandColors.MediaProgressBg
                )
            )
            
            Text(
                text = formatDuration(mediaState.duration),
                color = IslandColors.TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

package com.suspended.hyperisland.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.suspended.hyperisland.model.*
import com.suspended.hyperisland.ui.theme.IslandColors

@Composable
fun DynamicIsland(
    state: IslandState,
    onExpand: (IslandType) -> Unit,
    onCollapse: () -> Unit,
    onMediaPlayPause: () -> Unit,
    onMediaNext: () -> Unit,
    onMediaPrevious: () -> Unit,
    onMediaSeek: (Long) -> Unit,
    onTimerToggle: () -> Unit,
    onTimerStop: () -> Unit,
    onFlashlightToggle: () -> Unit,
    onNotificationDismiss: () -> Unit,
    onFileTransferAccept: () -> Unit,
    onFileTransferDecline: () -> Unit
) {
    val density = LocalDensity.current
    
    val islandWidth by animateDpAsState(
        targetValue = when (state.mode) {
            IslandMode.COMPACT -> 120.dp
            IslandMode.MEDIUM -> when (state.type) {
                IslandType.MEDIA -> 200.dp
                IslandType.TIMER -> 160.dp
                IslandType.CHARGING -> 140.dp
                IslandType.NOTIFICATION -> 280.dp
                IslandType.FILE_TRANSFER -> 280.dp
                else -> 160.dp
            }
            IslandMode.EXPANDED -> 340.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "islandWidth"
    )
    
    val islandHeight by animateDpAsState(
        targetValue = when (state.mode) {
            IslandMode.COMPACT -> 36.dp
            IslandMode.MEDIUM -> when (state.type) {
                IslandType.NOTIFICATION, IslandType.FILE_TRANSFER -> 72.dp
                else -> 36.dp
            }
            IslandMode.EXPANDED -> when (state.type) {
                IslandType.MEDIA -> 180.dp
                IslandType.TIMER -> 80.dp
                IslandType.NOTIFICATION -> 100.dp
                IslandType.FILE_TRANSFER -> 110.dp
                IslandType.FLASHLIGHT -> 80.dp
                IslandType.CHARGING -> 80.dp
                else -> 120.dp
            }
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "islandHeight"
    )
    
    val cornerRadius by animateDpAsState(
        targetValue = when (state.mode) {
            IslandMode.COMPACT -> 18.dp
            IslandMode.MEDIUM -> 24.dp
            IslandMode.EXPANDED -> 32.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cornerRadius"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = state.isVisible || state.type != IslandType.IDLE,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            Box(
                modifier = Modifier
                    .width(islandWidth)
                    .height(islandHeight)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(IslandColors.Background)
                    .pointerInput(state.mode) {
                        detectTapGestures(
                            onTap = {
                                if (state.mode == IslandMode.EXPANDED) {
                                    onCollapse()
                                } else {
                                    onExpand(state.type)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = state.mode to state.type,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(200)) togetherWith
                                fadeOut(animationSpec = tween(200))
                    },
                    label = "islandContent"
                ) { (mode, type) ->
                    when {
                        type == IslandType.MEDIA && state.mediaState != null -> {
                            MediaIslandContent(
                                mode = mode,
                                mediaState = state.mediaState!!,
                                onPlayPause = onMediaPlayPause,
                                onNext = onMediaNext,
                                onPrevious = onMediaPrevious,
                                onSeek = onMediaSeek
                            )
                        }
                        type == IslandType.TIMER && state.timerState != null -> {
                            TimerIslandContent(
                                mode = mode,
                                timerState = state.timerState!!,
                                onToggle = onTimerToggle,
                                onStop = onTimerStop
                            )
                        }
                        type == IslandType.CHARGING && state.chargingState != null -> {
                            ChargingIslandContent(
                                mode = mode,
                                chargingState = state.chargingState!!
                            )
                        }
                        type == IslandType.FLASHLIGHT && state.flashlightState != null -> {
                            FlashlightIslandContent(
                                mode = mode,
                                flashlightState = state.flashlightState!!,
                                onToggle = onFlashlightToggle
                            )
                        }
                        type == IslandType.NOTIFICATION && state.notificationState != null -> {
                            NotificationIslandContent(
                                mode = mode,
                                notificationState = state.notificationState!!,
                                onDismiss = onNotificationDismiss
                            )
                        }
                        type == IslandType.FILE_TRANSFER && state.fileTransferState != null -> {
                            FileTransferIslandContent(
                                mode = mode,
                                fileTransferState = state.fileTransferState!!,
                                onAccept = onFileTransferAccept,
                                onDecline = onFileTransferDecline
                            )
                        }
                        type == IslandType.RECORDING && state.recordingState != null -> {
                            RecordingIslandContent(
                                mode = mode,
                                recordingState = state.recordingState!!
                            )
                        }
                        else -> {
                            IdleIslandContent()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IdleIslandContent() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(IslandColors.TextSecondary.copy(alpha = 0.3f))
        )
    }
}

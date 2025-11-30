package com.suspended.hyperisland.manager

import com.suspended.hyperisland.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object IslandStateManager {
    
    private val _state = MutableStateFlow(IslandState())
    val state: StateFlow<IslandState> = _state.asStateFlow()
    
    private val eventListeners = mutableListOf<(IslandEvent) -> Unit>()
    
    fun processEvent(event: IslandEvent) {
        _state.update { currentState ->
            when (event) {
                is IslandEvent.ExpandIsland -> currentState.copy(
                    mode = IslandMode.EXPANDED,
                    type = event.type,
                    isVisible = true
                )
                is IslandEvent.CollapseIsland -> currentState.copy(
                    mode = IslandMode.COMPACT
                )
                is IslandEvent.HideIsland -> currentState.copy(
                    isVisible = false
                )
                is IslandEvent.ShowIsland -> currentState.copy(
                    isVisible = true
                )
                
                // Media events
                is IslandEvent.MediaUpdate -> currentState.copy(
                    mediaState = event.state,
                    type = if (event.state.isPlaying) IslandType.MEDIA else currentState.type,
                    isVisible = if (event.state.isPlaying) true else currentState.isVisible,
                    mode = if (event.state.isPlaying && currentState.mode == IslandMode.COMPACT) 
                        IslandMode.MEDIUM else currentState.mode
                )
                is IslandEvent.MediaStop -> currentState.copy(
                    mediaState = null,
                    type = if (currentState.type == IslandType.MEDIA) IslandType.IDLE else currentState.type
                )
                is IslandEvent.MediaPlay,
                is IslandEvent.MediaPause,
                is IslandEvent.MediaNext,
                is IslandEvent.MediaPrevious,
                is IslandEvent.MediaSeek -> {
                    notifyEventListeners(event)
                    currentState
                }
                
                // Timer events
                is IslandEvent.TimerUpdate -> currentState.copy(
                    timerState = event.state,
                    type = if (event.state.isRunning) IslandType.TIMER else currentState.type,
                    isVisible = if (event.state.isRunning) true else currentState.isVisible,
                    mode = if (event.state.isRunning && currentState.mode == IslandMode.COMPACT)
                        IslandMode.MEDIUM else currentState.mode
                )
                is IslandEvent.TimerStop -> currentState.copy(
                    timerState = null,
                    type = if (currentState.type == IslandType.TIMER) IslandType.IDLE else currentState.type
                )
                is IslandEvent.TimerStart,
                is IslandEvent.TimerPause,
                is IslandEvent.TimerResume -> {
                    notifyEventListeners(event)
                    currentState
                }
                
                // Recording events
                is IslandEvent.RecordingUpdate -> currentState.copy(
                    recordingState = event.state,
                    type = if (event.state.isRecording) IslandType.RECORDING else currentState.type,
                    isVisible = if (event.state.isRecording) true else currentState.isVisible,
                    mode = if (event.state.isRecording && currentState.mode == IslandMode.COMPACT)
                        IslandMode.MEDIUM else currentState.mode
                )
                is IslandEvent.RecordingStop -> {
                    notifyEventListeners(event)
                    currentState.copy(
                        recordingState = null,
                        type = if (currentState.type == IslandType.RECORDING) IslandType.IDLE else currentState.type
                    )
                }
                
                // Charging events
                is IslandEvent.ChargingUpdate -> currentState.copy(
                    chargingState = event.state,
                    type = if (event.state.isCharging) IslandType.CHARGING else 
                        if (currentState.type == IslandType.CHARGING) IslandType.IDLE else currentState.type,
                    isVisible = if (event.state.isCharging) true else currentState.isVisible,
                    mode = if (event.state.isCharging && currentState.mode == IslandMode.COMPACT)
                        IslandMode.MEDIUM else currentState.mode
                )
                
                // Flashlight events
                is IslandEvent.FlashlightUpdate -> currentState.copy(
                    flashlightState = event.state,
                    type = if (event.state.isOn) IslandType.FLASHLIGHT else 
                        if (currentState.type == IslandType.FLASHLIGHT) IslandType.IDLE else currentState.type,
                    isVisible = if (event.state.isOn) true else currentState.isVisible,
                    mode = if (event.state.isOn && currentState.mode == IslandMode.COMPACT)
                        IslandMode.MEDIUM else currentState.mode
                )
                is IslandEvent.FlashlightToggle -> {
                    notifyEventListeners(event)
                    currentState
                }
                
                // Notification events
                is IslandEvent.NotificationReceived -> currentState.copy(
                    notificationState = event.state,
                    type = IslandType.NOTIFICATION,
                    isVisible = true,
                    mode = IslandMode.MEDIUM
                )
                is IslandEvent.NotificationDismiss -> currentState.copy(
                    notificationState = null,
                    type = if (currentState.type == IslandType.NOTIFICATION) IslandType.IDLE else currentState.type,
                    mode = IslandMode.COMPACT
                )
                
                // File transfer events
                is IslandEvent.FileTransferUpdate -> currentState.copy(
                    fileTransferState = event.state,
                    type = IslandType.FILE_TRANSFER,
                    isVisible = true,
                    mode = IslandMode.MEDIUM
                )
                is IslandEvent.FileTransferAccept,
                is IslandEvent.FileTransferDecline -> {
                    notifyEventListeners(event)
                    currentState.copy(
                        fileTransferState = null,
                        type = if (currentState.type == IslandType.FILE_TRANSFER) IslandType.IDLE else currentState.type,
                        mode = IslandMode.COMPACT
                    )
                }
                
                // Meeting events
                is IslandEvent.MeetingUpdate -> currentState.copy(
                    meetingState = event.state,
                    type = IslandType.MEETING,
                    isVisible = true,
                    mode = IslandMode.MEDIUM
                )
                is IslandEvent.MeetingRemindLater -> {
                    notifyEventListeners(event)
                    currentState.copy(
                        meetingState = null,
                        type = if (currentState.type == IslandType.MEETING) IslandType.IDLE else currentState.type,
                        mode = IslandMode.COMPACT
                    )
                }
                
                // Call events
                is IslandEvent.CallUpdate -> currentState.copy(
                    callState = event.state,
                    type = IslandType.CALL,
                    isVisible = true,
                    mode = IslandMode.MEDIUM
                )
                is IslandEvent.CallAnswer,
                is IslandEvent.CallDecline -> {
                    notifyEventListeners(event)
                    currentState.copy(
                        callState = null,
                        type = if (currentState.type == IslandType.CALL) IslandType.IDLE else currentState.type,
                        mode = IslandMode.COMPACT
                    )
                }
                
                // Navigation events
                is IslandEvent.NavigationUpdate -> currentState.copy(
                    navigationState = event.state,
                    type = IslandType.NAVIGATION,
                    isVisible = true,
                    mode = IslandMode.MEDIUM
                )
            }
        }
    }
    
    fun addEventListener(listener: (IslandEvent) -> Unit) {
        eventListeners.add(listener)
    }
    
    fun removeEventListener(listener: (IslandEvent) -> Unit) {
        eventListeners.remove(listener)
    }
    
    private fun notifyEventListeners(event: IslandEvent) {
        eventListeners.forEach { it(event) }
    }
    
    fun reset() {
        _state.value = IslandState()
    }
}

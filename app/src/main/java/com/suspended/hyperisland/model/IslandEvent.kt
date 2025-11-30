package com.suspended.hyperisland.model

sealed class IslandEvent {
    // Mode changes
    data class ExpandIsland(val type: IslandType) : IslandEvent()
    object CollapseIsland : IslandEvent()
    object HideIsland : IslandEvent()
    object ShowIsland : IslandEvent()
    
    // Media events
    data class MediaUpdate(val state: MediaState) : IslandEvent()
    object MediaPlay : IslandEvent()
    object MediaPause : IslandEvent()
    object MediaNext : IslandEvent()
    object MediaPrevious : IslandEvent()
    data class MediaSeek(val position: Long) : IslandEvent()
    object MediaStop : IslandEvent()
    
    // Timer events
    data class TimerUpdate(val state: TimerState) : IslandEvent()
    object TimerStart : IslandEvent()
    object TimerPause : IslandEvent()
    object TimerResume : IslandEvent()
    object TimerStop : IslandEvent()
    
    // Recording events
    data class RecordingUpdate(val state: RecordingState) : IslandEvent()
    object RecordingStop : IslandEvent()
    
    // Charging events
    data class ChargingUpdate(val state: ChargingState) : IslandEvent()
    
    // Flashlight events
    data class FlashlightUpdate(val state: FlashlightState) : IslandEvent()
    object FlashlightToggle : IslandEvent()
    
    // Notification events
    data class NotificationReceived(val state: NotificationState) : IslandEvent()
    object NotificationDismiss : IslandEvent()
    
    // File transfer events
    data class FileTransferUpdate(val state: FileTransferState) : IslandEvent()
    object FileTransferAccept : IslandEvent()
    object FileTransferDecline : IslandEvent()
    
    // Meeting events
    data class MeetingUpdate(val state: MeetingState) : IslandEvent()
    object MeetingRemindLater : IslandEvent()
    
    // Call events
    data class CallUpdate(val state: CallState) : IslandEvent()
    object CallAnswer : IslandEvent()
    object CallDecline : IslandEvent()
    
    // Navigation events
    data class NavigationUpdate(val state: NavigationState) : IslandEvent()
}

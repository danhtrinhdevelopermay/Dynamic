package com.suspended.hyperisland.model

import android.graphics.Bitmap

enum class IslandMode {
    COMPACT,    // Small pill with just icons
    MEDIUM,     // Medium size with icon and text
    EXPANDED    // Full expanded with controls
}

enum class IslandType {
    IDLE,
    NOTIFICATION,
    MEDIA,
    TIMER,
    RECORDING,
    CHARGING,
    FLASHLIGHT,
    FILE_TRANSFER,
    MEETING,
    CALL,
    NAVIGATION
}

data class IslandState(
    val mode: IslandMode = IslandMode.COMPACT,
    val type: IslandType = IslandType.IDLE,
    val isVisible: Boolean = false,
    val mediaState: MediaState? = null,
    val timerState: TimerState? = null,
    val recordingState: RecordingState? = null,
    val chargingState: ChargingState? = null,
    val flashlightState: FlashlightState? = null,
    val notificationState: NotificationState? = null,
    val fileTransferState: FileTransferState? = null,
    val meetingState: MeetingState? = null,
    val callState: CallState? = null,
    val navigationState: NavigationState? = null
)

data class MediaState(
    val isPlaying: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val albumArt: Bitmap? = null,
    val duration: Long = 0,
    val position: Long = 0,
    val appPackage: String = "",
    val appIcon: Bitmap? = null
)

data class TimerState(
    val remainingTimeMs: Long = 0,
    val totalTimeMs: Long = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false
)

data class RecordingState(
    val isRecording: Boolean = false,
    val durationMs: Long = 0,
    val type: RecordingType = RecordingType.AUDIO
)

enum class RecordingType {
    AUDIO,
    VIDEO,
    SCREEN
}

data class ChargingState(
    val isCharging: Boolean = false,
    val batteryLevel: Int = 0,
    val chargingWattage: Float = 0f,
    val estimatedTimeToFull: Long = 0
)

data class FlashlightState(
    val isOn: Boolean = false
)

data class NotificationState(
    val packageName: String = "",
    val appName: String = "",
    val appIcon: Bitmap? = null,
    val title: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    val key: String = ""
)

data class FileTransferState(
    val fileName: String = "",
    val senderName: String = "",
    val senderDevice: String = "",
    val fileSize: Long = 0,
    val progress: Float = 0f,
    val isIncoming: Boolean = true
)

data class MeetingState(
    val title: String = "",
    val location: String = "",
    val startTime: Long = 0,
    val endTime: Long = 0,
    val attendees: List<String> = emptyList()
)

data class CallState(
    val callerName: String = "",
    val callerNumber: String = "",
    val callerPhoto: Bitmap? = null,
    val isIncoming: Boolean = true,
    val duration: Long = 0
)

data class NavigationState(
    val destination: String = "",
    val distance: String = "",
    val eta: String = "",
    val instruction: String = "",
    val appPackage: String = ""
)

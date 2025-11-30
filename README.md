# HyperIsland

A Dynamic Island-style notification overlay for Android, inspired by Xiaomi HyperOS 3.

## Features

- **Media Controls**: View and control music playback with album art, song info, and progress bar
- **Timer**: Display active timers with pause/stop controls
- **Recording Indicator**: Shows when apps are recording audio/video/screen
- **Charging Status**: View charging wattage and battery percentage
- **Flashlight**: Quick toggle for flashlight
- **Notifications**: Preview notifications in Dynamic Island style
- **File Transfer**: Accept/decline incoming file transfers

## Screenshots

The Dynamic Island appears at the top of your screen and can display various types of information:

- **Compact Mode**: Small pill showing just icons
- **Medium Mode**: Expanded pill with icon and text
- **Expanded Mode**: Full card with controls

## Installation

### From Releases

1. Go to the [Releases](../../releases) page
2. Download the latest APK
3. Install the APK on your Android device
4. Grant the required permissions:
   - **Display over other apps**: Allows the Dynamic Island to appear on top
   - **Notification access**: Allows reading notifications
   - **Post notifications**: Allows the service to run in background

### Build from Source

#### Using GitHub Actions

1. Fork this repository
2. Go to Actions tab
3. Run the "Build APK" workflow
4. Download the APK from artifacts

#### Local Build

Requirements:
- JDK 17
- Android SDK with Build Tools 34

```bash
# Clone the repository
git clone https://github.com/yourusername/hyperisland.git
cd hyperisland

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

The APK will be in `app/build/outputs/apk/`

## Permissions

The app requires the following permissions:

| Permission | Purpose |
|------------|---------|
| `SYSTEM_ALERT_WINDOW` | Display overlay on top of other apps |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Read notifications |
| `POST_NOTIFICATIONS` | Keep service running |
| `CAMERA` | Flashlight control |
| `FOREGROUND_SERVICE` | Background operation |

## Architecture

```
com.suspended.hyperisland/
├── model/           # Data classes for island states
├── manager/         # State management and feature managers
├── service/         # Android services (Overlay, Notification)
├── receiver/        # Broadcast receivers
└── ui/
    ├── theme/       # Colors and theming
    ├── components/  # Dynamic Island UI components
    └── screens/     # Main app screens
```

## Requirements

- Android 8.0 (API 26) or higher
- Permissions must be granted for full functionality

## License

MIT License

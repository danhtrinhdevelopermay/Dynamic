# HyperIsland - Android Dynamic Island

## Overview
This is an Android Kotlin application that replicates the Dynamic Island feature from Xiaomi's HyperOS 3. The app displays a floating overlay on top of other apps that shows:
- Media playback controls
- Timer/countdown
- Recording indicator
- Charging status
- Flashlight status
- Notifications
- File transfer prompts

## Project Structure
```
/
├── app/
│   ├── src/main/
│   │   ├── java/com/suspended/hyperisland/
│   │   │   ├── manager/          # State managers (Media, Timer, etc.)
│   │   │   ├── model/            # Data classes (IslandState, IslandEvent)
│   │   │   ├── receiver/         # Broadcast receivers
│   │   │   ├── service/          # Android services (Overlay, Notification)
│   │   │   └── ui/
│   │   │       ├── components/   # Dynamic Island UI components
│   │   │       ├── screens/      # App screens
│   │   │       └── theme/        # Colors and theming
│   │   ├── res/                  # Android resources
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── .github/workflows/build.yml   # GitHub Actions for APK build
├── gradle/                       # Gradle wrapper
└── build.gradle.kts              # Root build config
```

## Tech Stack
- Kotlin
- Jetpack Compose for UI
- Android Services for background operation
- MediaSessionManager for media controls
- NotificationListenerService for notifications

## Building
### GitHub Actions
Push to main/master branch triggers automatic APK build. Download from Releases.

### Local Build
```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

## Required Permissions
- SYSTEM_ALERT_WINDOW - Display over other apps
- BIND_NOTIFICATION_LISTENER_SERVICE - Read notifications
- POST_NOTIFICATIONS - Keep service running
- CAMERA - Flashlight control
- FOREGROUND_SERVICE - Background operation

## Recent Changes
- **2025-11-30**: Added "Always on Top" feature for Dynamic Island
  - Overlay automatically stays above other app overlays
  - Uses periodic bring-to-front mechanism (every 3 seconds)
  - Focus change listener for instant recovery when covered
  - Toggle switch in Settings to enable/disable this feature
  - Default: enabled (always on top)
- **2025-11-30**: Added position and size customization for Dynamic Island
  - Users can now adjust horizontal position (X: -200px to +200px)
  - Users can now adjust vertical position (Y: -50px to +200px)
  - Users can now scale the size (50% to 150%)
  - Settings are saved using DataStore and persist across app restarts
  - Changes apply in real-time while adjusting sliders
  - Added reset button to restore default settings
  - Fixed memory leak: coroutine scope properly cancelled in OverlayService.onDestroy()
- **2025-11-30**: Fixed corrupted Gradle wrapper files that were causing GitHub Actions build failures
  - **Issue 1**: `gradlew` script had corrupted sed command causing `./gradlew: 208: s~.*~\&'~; : not found`
  - **Issue 2**: `gradle-wrapper.jar` was corrupted causing `no main manifest attribute` error
  - **Solution**: Regenerated both files using official Gradle 8.2 distribution
  - **Verification**: gradle-wrapper.jar SHA256: `a8451eeda314d0568b5340498b36edf147a8f0d692c5ff58082d477abe9146e4` (matches GitHub Actions validation)
- Initial project setup with full HyperOS 3-style Dynamic Island
- Implemented all core features: media, timer, charging, flashlight, notifications
- Added GitHub Actions for automated APK builds

## Build Notes
- This project requires Android SDK to build, which is not available in Replit
- Push changes to GitHub to trigger automated APK builds via GitHub Actions
- Download APK artifacts from the GitHub Actions workflow run

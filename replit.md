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
- **2025-11-30**: Fixed corrupted `gradlew` script that was causing GitHub Actions build failures
  - Error was: `./gradlew: 208: s~.*~\&'~; : not found` and `Could not find or load main class "-Xmx64m"`
  - Solution: Replaced with latest official Gradle wrapper script from gradle/gradle repository
- Initial project setup with full HyperOS 3-style Dynamic Island
- Implemented all core features: media, timer, charging, flashlight, notifications
- Added GitHub Actions for automated APK builds

## Build Notes
- This project requires Android SDK to build, which is not available in Replit
- Push changes to GitHub to trigger automated APK builds via GitHub Actions
- Download APK artifacts from the GitHub Actions workflow run

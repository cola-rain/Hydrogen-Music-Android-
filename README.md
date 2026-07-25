# Hydrogen Music re_alpha

Hydrogen Music re_alpha is a first-pass Android port scaffold for the Hydrogen desktop player.  
This repository is intentionally conservative: it focuses on **landscape tablet use**, **local audio playback**, and **touch-friendly UI** first.

## What this version is for

- Landscape / tablet-first interface
- Local music folder picking and scanning (including sidecar .lrc lyric files)
- Local audio playback with Media3 / ExoPlayer
- Basic playback controls: play, pause, next, previous, seek, volume
- Touch-friendly list cards and larger tap targets
- Online features kept as switches/placeholders for later wiring:
  - login
  - sync
  - NetEase API bridge
  - online search
- Dolby Atmos / HiFi backend is deferred for now

## Build requirements

For local development:

- Android Studio (a recent stable version)
- The Android SDK matching `compileSdk 34`
- JDK 21 is used by the GitHub Actions workflow
- If you build locally, Android Studio's embedded JBR/JDK is usually the safest choice

For cloud builds:

- GitHub Actions
- The provided workflow builds a debug APK and uploads it as an artifact

## Build with Android Studio

1. Open the repository root in Android Studio.
2. Wait for Gradle sync to finish.
3. Let Android Studio download the missing SDK / library dependencies.
4. Click **Run** or use **Build > Build APK**.

## Build with GitHub Actions

1. Push the project to GitHub.
2. Open the repository's **Actions** tab.
3. Run the workflow named **Build Android APK**.
4. Download the uploaded debug APK artifact after the job finishes.

## Current feature status

### Working now
- Select a local folder with the system folder picker
- Scan supported local audio files
- Load sidecar `.lrc` lyric files with matching filenames
- Show songs in a touch-friendly list
- Open the original-style player screen from the home page
- Play / pause / previous / next
- Seek and volume control
- Theme mode switch
- Settings for local-first playback and scan behavior

### Reserved for later
- Real NetEase login bridge
- Real NetEase API calls
- Sync of library / user state
- MPV HiFi backend
- Dolby / Atmos special handling
- Desktop-only features from the original app

## File layout notes

The code currently keeps the original Java package namespace for stability during the early porting stage. The repository name / root folder and app label are intentionally more generic for GitHub publishing.

## Common problems

### Gradle sync is slow the first time
This is normal. Gradle, Android Gradle Plugin, Compose, and Media3 dependencies must be downloaded once.

### Build fails with a Java version mismatch
Use Java 21 in GitHub Actions. For local development, use the Android Studio embedded JDK if possible.

### Android SDK errors
If Gradle says a platform or build-tools package is missing, install the requested SDK version in Android Studio's SDK Manager.

### Local music does not appear
The app only scans folders that you explicitly pick and grant access to.

### Some audio formats do not play
Supported playback still depends on the device and Android's built-in decoders. Highly specialized formats may need later native handling.

### Online buttons do nothing
That is expected in this stage. They are present as switches and placeholders so the UI and settings layout are ready for later integration.

## Notes for contributors

Please keep the first goal simple: get the app building, opening, and playing local audio reliably before adding heavy online or native playback features.

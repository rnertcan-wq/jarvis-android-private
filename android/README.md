# JARVIS AI 2.0 development base

This is the buildable Android project recovered from the available JARVIS APK source archive.
It is a clean development baseline, not a claim that all planned 2.0 features are complete.

## Build
Requirements: JDK 17, Android SDK Platform 35, Build Tools 35.0.0, Gradle 8.11.1.

```bash
gradle --no-daemon clean testDebugUnitTest assembleDebug
```

## Configuration
- EVREN and Gemini credentials are configured locally in app settings; never commit API keys.
- The test application ID remains `local.mertcan.jarvis.next` so it can be installed beside an existing JARVIS build.
- Camera, microphone, contacts, calling and location require Android runtime permissions.
- Google Maps turn-by-turn navigation is delegated to the installed Google Maps app. The dashboard map tiles are not a Google Maps SDK embed.
- Driver-monitoring and road-warning features must be verified on the target phone before being treated as safety features.

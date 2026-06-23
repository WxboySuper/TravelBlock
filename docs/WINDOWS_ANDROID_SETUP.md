# Windows + Android Setup Guide for TravelBlock

## Goal

Get to the point where you can open the native Android project, run it on an emulator or physical Android phone, and let agents handle focused implementation phases.

## What You Need

- Windows machine
- Android Studio
- Android phone or Android emulator
- Git
- Java/JDK handled by Android Studio unless project needs otherwise

## Recommended Starting Path

1. Create a new folder:

```text
TravelBlockAndroid
```

2. Copy this docs pack into it.

3. Open Android Studio.

4. Create a new project:

```text
New Project -> Empty Activity
Language: Kotlin
Use Jetpack Compose: yes
Minimum SDK: API 26 or similar
Package name: com.travelblock.app
```

5. Let Gradle sync.

6. Press Run with an emulator or physical device.

## Physical Android Device Setup

On your Android phone:

1. Enable Developer Options.
2. Enable USB Debugging.
3. Plug phone into PC.
4. Accept the USB debugging prompt.
5. In Android Studio, select your phone as the run target.
6. Press Run.

## Emulator Setup

In Android Studio:

1. Open Device Manager.
2. Create a virtual device.
3. Choose a Pixel-like device.
4. Download a recent Android system image.
5. Start the emulator.
6. Press Run.

## First Build Expectations

The first Gradle sync/build may take a while. That is normal. Let the machine chew through it.

The first success target is simple:

- Empty app opens.
- You see a screen.
- No crash.

Do not try to build the full app before this works.

## Agent Setup Strategy

Once the empty app runs:

1. Commit the clean empty project.
2. Give an agent Phase 1 from `AGENT_PROMPTS.md`.
3. Let it create the shell.
4. Build and run.
5. Commit if it works.

## Useful Gradle Commands

From the project root:

```bash
./gradlew assembleDebug
./gradlew test
```

On Windows PowerShell, this may be:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
```

## If Android Studio Is Confusing

Only learn the minimum at first:

- Project panel shows files.
- Run button builds and launches app.
- Logcat shows errors.
- Gradle sync downloads dependencies.
- Device Manager creates emulators.

Everything else can wait.

## Common Agent Rule

If an agent changes Gradle files and the project stops syncing, stop feature work and repair Gradle first.

No feature matters until the app builds.

# v0.7.0-alpha Implementation Plan

**Goal**: Making it feel like a flight

**Status**: Not Started

**Timeline**: February 16-28 (12 days, school pace with async agent support)

---

## Overview

v0.7.0 adds immersive audio cues to enhance the flight experience. Users will hear a seatbelt chime at takeoff, engine/cabin ambience during flight, periodic announcements, and landing sounds upon arrival. Audio is configurable and respects device mute/volume settings.

This version is split into **3 focused PRs** — simpler than previous versions, allowing faster completion.

---

## PR Breakdown

### PR #1: Audio Service & Sound Loading 🔊

**Branch**: `feature/audio-service`

**Size**: Small-Medium (~150-200 lines)

**Priority**: High (Foundation)

**Dependencies**: None

**Description**: Create an audio service that loads, caches, and plays sounds, with proper audio session configuration and permissions.

#### Tasks (PR #1)

- [ ] Install `expo-av` for audio playback
- [ ] Create `services/audioService.ts`:
  - `loadSound(name: string): Promise<Audio.Sound>` - Load and cache sound
  - `playSound(name: string, options?: PlayOptions): Promise<void>` - Play single sound
  - `loopSound(name: string): void` - Play looping sound (cabin ambience)
  - `stopSound(name: string): void` - Stop specific sound
  - `stopAllSounds(): void` - Stop all playback
  - `setVolume(volume: 0-1): void` - Set master volume
  - `muteSounds(muted: boolean): void` - Mute all sounds
- [ ] Add audio assets:
  - Seatbelt chime (short, distinct sound, ~1-2 seconds)
  - Landing gear sound (distinctive, ~2-3 seconds)
  - Engine rumble (looping ambient, high-quality)
  - Cabin noise (low-level ambient, looping)
  - Periodic announcements (or atmospheric sounds as placeholder)
- [ ] Implement audio session configuration:
  - Set up audio category (playback, ambient, etc.)
  - Handle mute switch (respect device settings)
  - Handle volume buttons
  - Configure ducking (reduce volume if other audio starts)
- [ ] Add error handling:
  - Gracefully handle missing sound files
  - Handle audio unavailability
  - Log errors without crashing
- [ ] Create TypeScript types in `types/audio.ts`:
  - `SoundName` enum
  - `PlayOptions` interface
- [ ] Add basic unit tests (loading, error scenarios)

#### Acceptance Criteria (PR #1)

- Audio loads reliably without lag
- Sounds play at appropriate volumes
- Device mute is respected
- No audio playback issues on device
- Memory is freed after sounds finish
- Error handling is robust
- Ready for integration (PR #2)

#### Example Prompt for PR #1 (Async Agent Optional)

```text
Create audio service for flight immersion:
1. Install expo-av for audio playback
2. Implement audioService with load, play, loop, stop functions
3. Add sound caching for performance
4. Configure audio session (handle mute, volume, ducking)
5. Add 5 audio assets: seatbelt chime, landing gear, engine, cabin, announcement
6. Implement master volume control
7. Add error handling and graceful fallbacks
8. Write unit tests for loading and playback

Ensure sounds play smoothly without lag and respect device settings.
```

---

### PR #2: Flight Event Sound Triggers 🎵

**Branch**: `feature/flight-audio-events`

**Size**: Medium (~150-200 lines)

**Priority**: High (Core integration)

**Dependencies**: PR #1, v0.5.0 flight state

**Description**: Integrate audio service with flight events: play seatbelt chime on takeoff, cabin ambience during flight, landing sounds on arrival.

#### Tasks (PR #2)

- [ ] Create `services/flightAudioService.ts`:
  - `onFlightStart(flight: FlightState): void` - Play seatbelt chime, start engine
  - `onFlightCruising(flight: FlightState): void` - Start cabin ambience
  - `onFlightArrival(): void` - Play landing gear, stop ambience
  - `onFlightDivert(): void` - Optional divert sound
  - `triggerPeriodicAnnouncement(): void` - Optional random announcement (every 20-30 min)
- [ ] Implement flight phase audio:
  - Climbing phase: Engine ramping, increasing cabin noise
  - Cruising phase: Steady engine, cabin ambience
  - Descending phase: Cabin pressure relief sounds, descent chime
- [ ] Integrate with v0.5.0 flight events:
  - Subscribe to flightTimerService events
  - Play appropriate sounds on tick/phase-change/arrival/divert
  - Clean up event listeners on unmount
- [ ] Add audio timing:
  - Seatbelt chime on start (immediate)
  - Cabin ambience starts after 2-3 seconds (settling in)
  - Landing gear on arrival (3-5 seconds before full stop)
  - Periodic announcement every 20-30 minutes of flight
- [ ] Implement sound mixing:
  - Seatbelt chime plays over cabin ambience
  - Landing sounds fade cabin ambience
  - No overlapping sounds (one at a time generally)
- [ ] Add TypeScript types:
  - `FlightPhase` event interface
  - `AudioEventTiming` interface

#### Acceptance Criteria (PR #2)

- Seatbelt chime plays correctly on flight start
- Cabin ambience loops continuously during cruise
- Landing sounds play appropriately on arrival
- No overlapping or jarring sound transitions
- Sounds respect phase changes (climbing → cruising → descending)
- All sounds are non-blocking to app performance
- Works correctly on device

#### Example Prompt for PR #2

```text
Integrate audio with flight events:
1. Create flightAudioService that hooks into flight lifecycle
2. Play seatbelt chime on flight start (immediate)
3. Start cabin ambience after 2-3 seconds (settling)
4. Play phase-specific sounds (climbing, cruising, descending)
5. Trigger landing sounds on arrival (with timing for effect)
6. Implement periodic announcements every 20-30 min (optional)
7. Handle flight divert with optional sound
8. Ensure smooth sound transitions without overlapping

Integrate with v0.5.0 flightTimerService events.
```

---

### PR #3: Audio Settings & User Configuration ⚙️

**Branch**: `feature/audio-settings`

**Size**: Small (~100-150 lines)

**Priority**: Medium

**Dependencies**: PR #1, PR #2

**Description**: Add settings screen for audio customization: enable/disable, volume control, sound selection.

#### Tasks (PR #3)

- [ ] Create `screens/AudioSettingsScreen.tsx`:
  - Toggle to enable/disable all audio
  - Master volume slider
  - Individual sound toggles (seatbelt, landing, cabin, announcement)
  - Preview buttons for each sound
  - Settings saved to AsyncStorage (using v0.3.0 storage service)
- [ ] Create `hooks/useAudioSettings.ts`:
  - Hook to access and update audio settings
  - Provides current settings and update functions
  - Persists to storage
- [ ] Integrate with app settings:
  - Add audio settings to main settings screen (if exists)
  - Or create dedicated audio settings route
- [ ] Implement preview functionality:
  - "Test Seatbelt Chime" button
  - "Test Landing Sound" button
  - "Test Cabin Ambience" (short loop)
  - Helpful for users to customize before flights
- [ ] Add context/store updates:
  - Update flightAudioService with user preferences
  - Respect disabled sounds during flights
  - Override with user volume settings
- [ ] Add TypeScript types in `types/audio.ts`:
  - `AudioSettings` interface
  - `AudioPreferences` enum

#### Acceptance Criteria (PR #3)

- Audio settings screen is clean and easy to use
- Volume slider works smoothly
- Enable/disable toggles work correctly
- Preview buttons play sounds without issues
- Settings persist across app restarts
- Disabled sounds don't play during flights
- Dark/light mode both look good

#### Example Prompt for PR #3 (Async Agent)

```text
Create audio settings screen:
1. Build AudioSettingsScreen with toggles and volume slider
2. Implement individual sound enable/disable options
3. Add preview buttons for each sound (test before flight)
4. Create useAudioSettings hook for persistent settings
5. Integrate with v0.3.0 AsyncStorage for persistence
6. Update flightAudioService to respect user preferences
7. Add to main app settings navigation

Ensure smooth slider interactions and proper theme support.
```

---

## Testing Strategy

### Unit Tests

- Sound loading and caching
- Event timing calculations

### Integration Tests

- Full flight with audio (start → cruise → arrival)
- Audio settings persistence
- Sound preview functionality

### Manual Testing Checklist

- [ ] Seatbelt chime plays on flight start
- [ ] Cabin ambience loops correctly during flight
- [ ] Landing sounds play on arrival
- [ ] Audio respects device mute switch
- [ ] Volume slider controls audio level
- [ ] Disabled sounds don't play during flights
- [ ] Settings persist across app restart
- [ ] Preview sounds work from settings screen
- [ ] No audio lag or stuttering
- [ ] Audio stops correctly on flight divert
- [ ] Periodic announcement plays at appropriate intervals
- [ ] Light/dark mode settings UI looks good

---

## Timeline Estimate

- **PR #1** (Audio service): 1 day
- **PR #2** (Flight events): 1-1.5 days
- **PR #3** (Settings): 0.5-1 day

**Total**: ~2.5-3.5 days (much faster than previous versions!)

**Recommended Approach**:

- Start PR #1 on Feb 16 (immediately after v0.6.0)
- Queue PR #2 for async agent by Feb 17
- Work on PR #3 while agent implements flight audio integration
- Final integration by Feb 25, buffer days for testing

---

## Audio Asset Sourcing

For audio files, you can:

- Use royalty-free sounds from freesound.org, zapsplat.com
- Create simple chimes/beeps with online generators
- Use minimal sound effects for lightweight APK
- Consider licensing music for ambience or announcements

Keep file sizes small to avoid increasing APK size.

---

## Notes

- Audio is optional but adds significantly to user experience
- Keep ambient sounds relatively quiet (not intrusive)
- Periodic announcements should be atmospheric (optional to disable)
- Consider battery impact of audio playback (minimal if using short clips)
- Audio can be toggled off by users who prefer silent focus

---

## Success Criteria for v0.7.0-alpha

✅ Seatbelt chime plays reliably on takeoff

✅ Cabin ambience enhances immersion during flight

✅ Landing sounds signal arrival correctly

✅ Audio respects device mute and volume settings

✅ Users can customize audio preferences

✅ Audio adds to experience without being intrusive

✅ Ready to integrate with v0.8.0 (post-flight flows)

---

## Integration Points

- ⬅️ **v0.5.0**: Consumes flight events (start, phases, arrival, divert)
- 📍 **v0.7.0**: Audio immersion layer
- ➡️ **v0.8.0**: Adds post-flight flows (landing can trigger landing screen)

---

### Last Updated

January 4, 2026

### Author

Development Team

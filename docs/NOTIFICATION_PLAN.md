# TravelBlock Notification Plan

## Current foundation (v0.13)

- `NotificationHelper` posts a local "Flight landed" notification when Cockpit detects arrival while the app is running.
- Android 13+ `POST_NOTIFICATIONS` permission is declared in the manifest and requested on first launch from `MainActivity`.
- Tapping the notification opens `MainActivity` (future: deep link to Arrival or active Cockpit).
- No background worker or exact alarm is scheduled yet.

## Why notifications matter

Users may background TravelBlock during a focus flight while using another app for homework. When the simulated flight lands, they need a reliable alert even if TravelBlock is not in the foreground.

## Target architecture (next phase)

1. **Persist active flight** with `startedAt`, `plannedArrivalAt`, route metadata, and notification scheduling flag.
2. **Schedule at `plannedArrivalAt`** using one of:
   - `WorkManager` `OneTimeWorkRequest` with `setInitialDelay` (preferred for maintainability), or
   - `AlarmManager.setExactAndAllowWhileIdle` for tighter timing (requires careful battery/permission policy).
3. **Worker / receiver** loads active flight from Room/DataStore, verifies still active, posts notification via `NotificationHelper`.
4. **Notification content**: `Flight TB123 has landed at KTUL` with tap action to Arrival screen.
5. **Cancel** scheduled work when user diverts early or completes while foregrounded (avoid duplicate alerts).

## TODO hooks in code

- `CockpitViewModel.saveResult` — cancel pending landing work when flight completes in foreground.
- Active flight start (`TravelBlockApp.onStartFocusFlight`) — schedule landing notification work.
- Process-death restore — reschedule from persisted `plannedArrivalAt` when active flight is restored.

## Dependencies (when implemented)

- `androidx.work:work-runtime-ktx` (minimal WorkManager only).
- Optional: `androidx.core:core-ktx` notification APIs (already used).

## Testing

- Unit: mock `NotificationManagerCompat` boundary or test helper guard when permission denied.
- Manual: start a short debug flight, background app, wait for landing, confirm notification on Android 13+ with permission granted.
- Manual: deny permission — app must not crash; notification is skipped silently.

## Out of scope for this pass

- Push notifications / FCM
- Rich notification actions (pause is intentionally not offered)
- Per-flight custom sounds until audio layer exists

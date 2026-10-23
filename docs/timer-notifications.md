# Timer notifications (Pomodoro and Routine runner)

Both timers show an ongoing notification from a foreground service, so a countdown survives the app being
backgrounded. The services own no timer state: `PomodoroTimerEngine` and the routine run engine are Koin
singletons that keep ticking; the services only reflect their state.

## How the notification is built

- **Chronometer:** while running, the notification uses `setUsesChronometer(true)` +
  `setChronometerCountDown(true)` with `setWhen(endTime)`, so the system draws the countdown itself.
  While paused it is replaced by a static time label.
- **Re-post only on state change:** the service compares a `*PostedSnapshot` of the last post with the current
  one (`shouldRepost`). Per-second engine ticks do not re-post; mode, pause, step, total or an end-time drift
  of more than 2 s do.
- **API 36+ path:** the notification requests promotion (`setRequestPromotedOngoing`), adds
  `ShortCriticalText` when no chronometer is shown, and a `ProgressStyle` (Pomodoro: elapsed/total seconds;
  routine: completed steps / step count).
- **Fallback:** `Notifier.canPostPromotedNotifications()` is checked on API 36+. Below API 36, or if the user
  disabled promoted notifications, the planner sets `promoted = false` and the plain chronometer notification
  is posted silently, with no progress style.

## Foreground services

`PomodoroTimerService` and `RoutineRunService` are declared in `AndroidManifest.xml` with
`foregroundServiceType="specialUse"`. Starting foreground can be refused (Android 12+
`ForegroundServiceStartNotAllowedException`); the services log it and stop themselves.

## Actions

- Pomodoro: Pause/Resume and Stop.
- Routine runner: Pause/Resume (timed steps only), Skip step and Stop; tapping the notification opens the run
  screen.

## Where the code lives

- `app/.../pomodoro/`: `PomodoroTimerService`, `PomodoroNotificationPlan` (pure planner, snapshot, repost
  decision, time format), `PomodoroNotificationFactory` (applies a plan to a `NotificationCompat.Builder`).
- `app/.../routine/`: `RoutineRunService`, `RoutineRunNotificationPlan`, `RoutineRunNotificationFactory`.
- Channels: `Notifier.CHANNEL_FOCUS_TIMER`, `Notifier.CHANNEL_ROUTINE_RUN` in
  `app/.../core/utils/Notifier.kt`.
- Tests: `app/src/test/.../pomodoro` and `.../routine` cover the planners (pure) and the factories (mocked
  builder).

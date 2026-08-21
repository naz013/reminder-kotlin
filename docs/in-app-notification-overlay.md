# In-App Notification Overlay Banner

Feasibility analysis and implementation plan for an in-app banner, shown below the top app bar,
that mirrors a reminder/birthday system notification the moment it fires — so a user already inside
the app can acknowledge/snooze/call without pulling down the notification shade. Implemented as
described below; see `InAppAlertBus`/`InAppAlertViewModel`/`InAppAlertBanner` for the current code.

Gated by a single preference, `Prefs.isInAppAlertBannerEnabled` (default on), surfaced as one toggle
on the Reminders settings screen ("In-app notification banner") even though it governs both reminder
and birthday banners — both `ReminderActionProcessor.process()` and `BirthdayActionProcessor.process()`
check it alongside the existing foreground check before calling `InAppAlertBus.show(...)`. There is no
separate toggle on Birthday settings; this is the one control for the whole feature.

## Feasibility verdict

**Feasible, moderate effort, no new Gradle module needed.** The system-notification pipeline already
resolves everything a banner needs (title, text, icon, actions, quiet-hours suppression) in one place
(`ReminderActionProcessor`/`BirthdayActionProcessor` in the `app` module), and the whole app renders
inside a single Activity (`BottomNavActivity`) with one shared `NavDisplay`, so one overlay wired at
that level covers every screen without per-screen duplication. The two real costs are (1) there is no
app-wide event bus or foreground-tracking today — both need to be built from scratch — and (2) there
is no single shared `Scaffold`/`TopAppBar`, so "below the app bar" has to be approximated with a fixed
offset rather than measured precisely per screen. Neither is an architectural blocker.

## Goal

When a reminder or birthday alarm fires **and the app process is already in the foreground** (some
Activity of this app resumed), show a Material 3 banner docked just under the top app bar, in addition
to (not instead of) the system notification:

- Same content as the system notification: icon, title, text.
- Same actions: "OK" (reminders + birthdays), "Snooze" (reminders without places), "Call"/"SMS"
  (birthdays with a phone number).
- Tapping an action performs the exact same effect as tapping it in the notification shade,
  including cancelling the already-posted system notification (no orphaned notification left behind).
- Respects quiet hours / Do Not Disturb exactly like the system notification — for free, if wired at
  the right point in the pipeline (see below).
- If the app is backgrounded or the process is dead when the alarm fires, behavior is unchanged from
  today: system notification only, no banner (there is nothing to render a banner into).

## Current state (from codebase research)

- **Emission path** (all in the `app` module, package `com.elementary.tasks`):
  `AlarmReceiver` (`core/services/alarm/AlarmReceiver.kt`) →
  `ReminderActionProcessor.process(id)` / `BirthdayActionProcessor` (`core/services/action/{reminder,birthday}/...ActionProcessor.kt`)
  → quiet-hours check via `DoNotDisturbManager.applyDoNotDisturb(priority)` → on pass,
  `ReminderHandlerFactory`/`BirthdayHandlerFactory` build a handler that extends the shared
  `NotificationAlertActionHandler` base (`core/services/action/NotificationAlertActionHandler.kt`),
  which builds the `NotificationCompat` and calls `Notifier.notify()` (`core/utils/Notifier.kt`,
  implementing `NotificationApi` from `data:notification-api`).
- **Quiet hours already gate notification construction**, not just presentation: if
  `DoNotDisturbManager.applyDoNotDisturb(...)` returns true, `ReminderActionProcessor` never calls
  the handler at all — it reschedules or drops the alarm. Anything hooked in *after* that check
  (i.e., alongside/after `handler.handle(reminder)`) inherits quiet-hours suppression automatically.
  Anything hooked in earlier (e.g. at `AlarmReceiver`) would need to duplicate the DND check itself.
- **Actions and their handlers**: "OK" → `ReminderActionReceiver`/`BirthdayActionReceiver`
  (`core/services/{ReminderActionReceiver,BirthdayActionReceiver}.kt`) → `ReminderActionProcessor.complete(id)` /
  `BirthdayActionProcessor.cancel(id)`, wrapped by `CancelNotificationDecorator` which also cancels the
  posted notification (and its wear companion). "Snooze" → `ReminderActionProcessor.snooze(id)`.
  Birthday "Call"/"SMS" dial/compose directly. These are plain suspend-function calls under the
  receivers — nothing here requires going through a `BroadcastReceiver`/`PendingIntent` round trip if
  called in-process, which a banner action handler can do directly.
- **Process model**: no manifest component declares `android:process`, so everything runs in the
  app's single default process — but `AlarmReceiver` can cold-start that process with no Activity
  alive (Android delivers the broadcast, may kill the process again right after). There is currently
  **no foreground/resumed-state tracking**: `ActivityObserver` (`app/.../ActivityObserver.kt`) exists
  but today only switches theming context on `onActivityCreated`, it doesn't expose "is any Activity
  resumed." This must be added — it's the gate that decides banner-vs-notification-only.
- **App shell**: single launcher Activity `BottomNavActivity` (`app/.../navigation/BottomNavActivity.kt`,
  `launchMode="singleInstance"`) hosts one Compose tree with one Navigation 3 `NavDisplay`
  (`app/.../navigation/nav3/AppNavGraph.kt`) — every feature screen (Home, Notes, Reminders,
  Birthdays, Settings, …) is an entry in that same backstack. **There is no single shared
  `Scaffold`/`TopAppBar`** — each screen builds its own `Scaffold { TopAppBar { ... } }` internally, so
  app-bar height isn't perfectly uniform across screens. Two extra Activities,
  `ReminderActionActivity` and `BirthdayActionActivity` (the notification's tap targets), live outside
  this graph entirely and would not be covered by an overlay placed in `BottomNavActivity`.
- **No existing app-wide event bus.** The closest precedents are `SingleLiveEvent`
  (`core/feature-common`, per-ViewModel consume-once events — not cross-Activity) and
  `NavigatorImpl`/`NavigationObservable` (`app/.../navigation/`, a Koin-singleton pub/sub used to
  route external navigation requests into `BottomNavActivity`, but single-subscriber and typed only
  for navigation `Destination`s). A new mechanism is needed; `NavigatorImpl`'s
  Koin-singleton-plus-`Application`-lifecycle-subscription shape is a reasonable template to copy.

## Proposed architecture

### 1. New alert bus (Koin singleton, `app` module)

```kotlin
// app/.../core/services/action/inapp/InAppAlertBus.kt
internal interface InAppAlertBus {
    val alerts: SharedFlow<InAppAlert>
    suspend fun emit(alert: InAppAlert)
}

internal class InAppAlertBusImpl : InAppAlertBus {
    private val _alerts = MutableSharedFlow<InAppAlert>(extraBufferCapacity = 4)
    override val alerts = _alerts.asSharedFlow()
    override suspend fun emit(alert: InAppAlert) { _alerts.emit(alert) }
}
```

`InAppAlert` is a small data class carrying exactly what the banner needs to render + act:
id, domain (`Reminder`/`Birthday`), title, text, icon res (via `DrawableCatalog`, per the icon
convention in `CLAUDE.md`), and the set of applicable actions (`Ok`, `Snooze`, `Call`, `Sms`) — a
direct mirror of what `NotificationAlertActionHandler` already computes, not a new derivation.

### 2. Emission point

Inject `InAppAlertBus` into `ReminderActionProcessor`/`BirthdayActionProcessor` and emit right after
`handler.handle(reminder)` succeeds inside the existing "DND passed" branch — same place, same
guard, so quiet hours are inherited for free. Additionally gate on the new foreground check (§3): no
point building/emitting an `InAppAlert` object if nothing can render it (also avoids the bus buffering
stale alerts for a later foreground transition, which would show a banner for an alarm that's minutes
old).

### 3. Foreground gating

Extend `ActivityObserver` (or add a sibling `ForegroundStateTracker`, Koin singleton) to track
resumed state via `onActivityResumed`/`onActivityPaused` across all Activities, exposing
`val isForeground: StateFlow<Boolean>`. `ReminderActionProcessor`/`BirthdayActionProcessor` check this
before building an `InAppAlert`. This is the one genuinely new piece of infra beyond the bus itself —
today nothing in the codebase answers "is the app foregrounded."

### 4. Consumption + placement

In `BottomNavActivity`'s Compose tree, wrap `NavDisplay` in a `Box`, collect
`InAppAlertBus.alerts` (via a small `InAppAlertViewModel` — per `CLAUDE.md`, no logic in the
Activity/Composable itself) into a queue (`List<InAppAlert>`, FIFO, show the head), and render a new
`ui-common` composable (`InAppAlertBanner`, alongside the existing `Selectable`/`SelectionTopBar`
shared-component family described in `docs/multiselect.md`) anchored to the top of the `Box`.

Positioning "below the app bar": since there's no shared `Scaffold` to measure, use a fixed top
offset — `WindowInsets.statusBars` padding plus the standard M3 `TopAppBar` height. Check
`ui-common`'s M3 token set (see `docs/m3-expressive-adoption.md`) for an existing height constant
before hardcoding one. This is an approximation, not pixel-perfect alignment with every screen's
actual app bar (some screens may have a taller/shorter or absent app bar) — acceptable for a first
version; call out as a known limitation rather than re-architecting every screen's `Scaffold` to
report its height (that's a much larger, separately-scoped change).

The two out-of-graph Activities (`ReminderActionActivity`, `BirthdayActionActivity`) are reached only
by explicitly tapping the system notification, so they're out of scope — no banner needed there.

### 5. Action wiring

Banner actions call the same suspend functions the `BroadcastReceiver`s already call
(`ReminderActionProcessor.complete/snooze`, `BirthdayActionProcessor.cancel`, plus the birthday
call/SMS intents), from `InAppAlertViewModel`, injected the same way `ReminderActionReceiver` gets
them today. Because `complete`/`cancel` are wrapped by the existing `CancelNotificationDecorator`,
tapping "OK" in the banner also cancels the already-posted system notification — no special-casing
needed, no orphaned notification.

Tapping the banner body itself (not an action button) is an open design choice: mirror the system
notification's tap target (launch `ReminderActionActivity`/`BirthdayActionActivity`) for consistency,
or — since the app is already open — navigate within the existing `NavDisplay` to the reminder/birthday
preview screen via `NavigatorImpl` for a smoother in-app feel. Recommend the latter; flag for
product sign-off since it's a UX behavior difference from the notification shade.

### 6. Queueing — latest-wins, not a FIFO queue

Multiple alarms can fire close together (e.g. several birthdays due the same day, processed in one
loop by `BirthdayActionProcessor.process()`). As implemented, there is no queue at all:
`InAppAlertBus` wraps a single `MutableStateFlow<InAppAlert?>`, and every `show(alert)` call simply
overwrites the current value. A `StateFlow` only ever holds its latest value, so this gives "only the
most recent alert is ever shown, never stacked" for free — no list, no FIFO, no "+N more" affordance.
An older, still-unacknowledged banner is silently replaced when a newer one arrives; its underlying
system notification is untouched (only the in-app banner is swapped). `InAppAlertBus.clear(alertId)`
is guarded by id so that an in-flight dismiss/action on an alert that has since been superseded
doesn't blank out the newer banner that replaced it. See
`app/.../core/services/action/inapp/InAppAlertBus.kt`.

## File-by-file touch list

| Area | File(s) | Change |
|---|---|---|
| `app` | `core/services/action/inapp/InAppAlertBus.kt` (new) | Koin-singleton `SharedFlow` bus + `InAppAlert` model |
| `app` | `core/services/action/inapp/ForegroundStateTracker.kt` (new, or extend `ActivityObserver.kt`) | Track resumed-Activity state app-wide |
| `app` | `core/services/action/reminder/ReminderActionProcessor.kt` | Emit `InAppAlert` after successful `handler.handle(...)`, gated on foreground |
| `app` | `core/services/action/birthday/BirthdayActionProcessor.kt` | Same, birthday path |
| `app` | `navigation/InAppAlertViewModel.kt` (new) | Collects bus, owns queue, exposes actions calling into the processors |
| `app` | `navigation/BottomNavActivity.kt` | Wrap `NavDisplay` in a `Box`, host the banner composable + collect view model state |
| `ui:ui-common` | `compose/foundation/InAppAlertBanner.kt` (new) | Stateless banner composable, `AppIcons`-driven icon, action buttons |
| `app` | Koin module (wherever `ReminderActionProcessor` etc. are bound) | Register `InAppAlertBus`, `ForegroundStateTracker`/tracker, `InAppAlertViewModel` |
| `.maestro/notifications/` | new flow(s) | E2E: reminder fires while app foregrounded → banner appears with correct actions; app backgrounded → banner does not appear |

No changes needed in `core:domain`, `data:*`, `logic:*`, or `feature:*` — this is entirely an `app` +
`ui-common` feature, since all the data the banner needs is already resolved by the existing
notification-handler pipeline.

## Risks / open questions

- **Banner offset accuracy** — fixed top offset won't be pixel-perfect on every screen given no
  shared `Scaffold`. Acceptable for v1; worth a quick visual pass across the main screens (Home,
  Reminders, Birthdays, Settings) to confirm no jarring overlap.
- **Foreground tracking correctness** — `onActivityResumed`/`onActivityPaused` counting needs care
  around configuration changes (rotation) and multi-window/split-screen so it doesn't flicker
  foreground state during a same-Activity recreate. Should reuse whatever pattern, if any, similar
  Android apps in this codebase's history have used for "is app foregrounded" (none found — this is
  genuinely new).
- **Banner tap-target UX** (open in-graph preview vs. launch the existing popup Activity) needs a
  product decision (§5).
- **Accessibility/timing** — decide whether the banner auto-dismisses after a timeout or persists
  until acted on. Recommend persisting (matches notification-shade behavior, avoids missed reminders)
  but flag for product input.
- **Testing** — unit tests for `InAppAlertViewModel` queueing/action-dispatch (JUnit4 + MockK, per
  `CLAUDE.md` conventions) are straightforward. The foreground-gating and cross-Activity visual
  behavior are better covered by a Maestro flow than a unit test, given the existing
  `.maestro/notifications/*.yaml` suite already exercises the quiet-hours/notification-shade paths
  this feature sits next to.

## Effort estimate

No new module, no changes below the `app`/`ui-common` layer. The two genuinely new pieces of
infrastructure (event bus, foreground tracking) are each small and self-contained. Rough sizing for
one engineer already familiar with this codebase: **3–5 days** including the banner composable, both
processor hookups, queueing, action wiring, unit tests, and one or two new Maestro flows — assuming
the open UX questions above (tap target, offset tolerance, auto-dismiss) are settled before or during
implementation rather than blocking it.

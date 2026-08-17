# End-to-End (E2E) UI Testing

This document proposes how to add real device/emulator UI testing to this repo — Compose UI
tests for fast, in-process screen/flow verification, and [Maestro](https://maestro.dev) for
true black-box user journeys (including notification-tray checks) — wired into GitHub Actions
across multiple Android versions.

**Status: none of this exists yet.** There is no `androidTest` source set anywhere in the repo
today (verified via `find . -type d -name androidTest`), and CI
(`.github/workflows/build_and_test.yml`) only runs `test`/`testProDebugUnitTest` on plain
`ubuntu-latest` — no emulator. Everything below is new infrastructure to add, not a fix to
something broken. Dependency versions referenced already exist in
`gradle/libs.versions.toml` (`androidx-test-core/runner/rules`, `compose-ui-test-junit4`) except
where called out as new.

See also: [architecture.md](architecture.md) (module map), [multiselect.md](multiselect.md)
(bulk-selection pattern under test in §F), and
[reminderv2-notification-hierarchy.md](reminderv2-notification-hierarchy.md) (the
Settings→Group→Reminder notification override system exercised in §C — **read the caveat in
§4 of this doc before writing notification-delivery assertions**, the hierarchy isn't fully
wired to actual notification firing yet).

---

## 1. Strategy: two complementary layers

| | **Compose UI tests** | **Maestro** |
|---|---|---|
| What it drives | Composables in-process, via `ComposeTestRule` / `ActivityScenario` | The installed, compiled APK — indistinguishable from a real user |
| Speed | Fast (seconds per test, no full app restart) | Slower (real app install + launch per flow) |
| Good for | Screen state/validation logic, builder constraint rules, ViewModel wiring, fast regression on every PR | Cross-Activity journeys, system permission dialogs, the notification shade, true multi-API-level smoke coverage |
| Weak at | Anything crossing outside the app process (system notification tray, permission dialogs render differently per OEM) | Fine-grained assertions on internal state; slower feedback loop |
| Where it lives | `androidTest` source sets (module-local for single-screen tests, `app` for cross-screen flows) | `.maestro/*.yaml` at repo root, run against an installed APK |

Use Compose UI tests as the default for anything that doesn't need to leave the app process —
they're what should run on **every PR**. Reserve Maestro for journeys that must interact with
Android system UI (notification shade, permission prompts) or where you want a true black-box
smoke test across several API levels without touching Gradle test wiring per module. Both run in
the same GitHub Actions emulator job (§3) so there's only one place to enable KVM/hardware
acceleration.

### 1a. Compose UI tests — two tiers

- **Tier A — screen-level component tests**, living in each feature module's own `androidTest`
  (e.g. `feature/feature-reminder/src/androidTest`). Mount a single screen composable directly
  with `createComposeRule()` (no Activity, no DB, no Koin) against a fake/preview `State` and a
  captured lambda for each callback. Fast, isolated, and matches the project's "stateless
  composable driven by a ViewModel" convention from `CLAUDE.md` — you're testing that the
  composable renders the state correctly and calls the right callback, not that the ViewModel or
  DB behave correctly (that's already covered by existing JVM unit tests on the ViewModel).
- **Tier B — flow-level tests**, living in `app/src/androidTest` since `app` is the only module
  that owns `BottomNavActivity`/the nav graph. Use
  `createAndroidComposeRule<BottomNavActivity>()` with a Koin test module that swaps
  `app`-owned platform seams (cloud auth, biometric, Google sign-in, ad banners) for fakes but
  keeps a **real in-memory Room DB** (`Room.inMemoryDatabaseBuilder`) — so recurrence
  calculation, save, and list-display genuinely round-trip through the real repository layer,
  the same way the reminderv2-notification-hierarchy doc's own
  `ResolveReminderV2NotificationSettingsUseCaseTest` does at the unit level, just through the UI.

### 1b. Maestro

Free, open source, YAML flow syntax, drives the compiled app like `adb`/UiAutomator under the
hood but with a far less brittle API than raw Espresso `onView` chains, and — critically for
this task — has built-in helpers for the notification shade and permission dialogs that Compose
UI tests can't reach. Flows live in `.maestro/` at repo root, organized by feature, one YAML per
journey, tagged (`smoke`, `recurrence`, `notifications`, `settings`, `pro-only`) so CI can run a
fast `smoke`-tagged subset per PR and the full suite nightly/on `master`.

```
.maestro/
  config.yaml                # appId, default flow tags/env
  smoke/
    create_simple_reminder.yaml
  recurrence/
    once.yaml
    daily_repeat.yaml
    countdown.yaml
    weekly.yaml
    monthly.yaml
    yearly.yaml
    location_enter.yaml
    location_exit.yaml
    icalendar_custom_rrule.yaml
  notifications/
    quiet_hours_suppresses_low_priority.yaml
    quiet_hours_ignore_all.yaml
    bypass_dnd_high_priority.yaml
    notification_permission_denied.yaml
    tap_notification_opens_reminder.yaml
  settings/
    notification_defaults_hierarchy.yaml
    biometric_lock.yaml
```

---

## 2. Gradle wiring needed

New version-catalog entries (none of these exist yet in `gradle/libs.versions.toml`):

```toml
[versions]
androidx-test-ext-junit = "1.3.0"
androidx-test-orchestrator = "1.6.1"
espresso-core = "3.7.0"
mockk-android = "1.14.5"   # match existing mockk version used in unit tests

[libraries]
androidx-test-ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidx-test-ext-junit" }
androidx-test-orchestrator = { group = "androidx.test", name = "orchestrator", version.ref = "androidx-test-orchestrator" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso-core" }
mockk-android = { group = "io.mockk", name = "mockk-android", version.ref = "mockk-android" }
```

`app/build.gradle.kts`:

```kotlin
android {
  defaultConfig {
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    testInstrumentationRunnerArguments["clearPackageData"] = "true"
  }
  testOptions {
    execution = "ANDROIDX_TEST_ORCHESTRATOR"
    animationsDisabled = true
  }
}

dependencies {
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.rules)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.espresso.core)
  androidTestImplementation(libs.compose.ui.test.junit4)
  androidTestImplementation(libs.mockk.android)
  androidTestImplementation(platform(libs.koin.bom)) // reuse whatever BOM app already uses
  androidTestImplementation(libs.koin.test)
  androidTestUtil(libs.androidx.test.orchestrator)
  debugImplementation(libs.compose.ui.test.manifest) // already present
}
```

Same `androidTestImplementation(libs.compose.ui.test.junit4)` block (minus Espresso/orchestrator,
which only make sense with a hosting Activity) goes into any `feature:*` module gaining Tier-A
tests — put the shared bits (Room in-memory DB helper, Koin test-module builder, common fakes for
`ReminderNotifier`/`NotificationApi`/`DateTimeManager`) in `core:testing` so both tiers reuse them
instead of each module rolling its own; `core:testing` is currently JVM-only unit-test helpers
(`BaseTest`, `TestDispatcherProvider`), so this is a genuinely new Android-instrumented surface
for that module, not an extension of an existing one.

A fake `DateTimeManager`/clock is worth calling out specifically: recurrence math
(`RecurrenceRuleCalculator`, `RecurrenceCalculator`) is date-relative, so Tier-B tests that don't
control "now" will be flaky around month/year boundaries (e.g. a Monthly test seeded with
`dayOfMonth = 31` behaves differently in February). Inject a fixed/fake clock rather than relying
on the real device date.

---

## 3. GitHub Actions job

Reuses the existing `.github/actions/setup-build` composite action (JDK 17, `gradlew` perms,
`google-services.json` secrets) already used by the three jobs in `build_and_test.yml`. Add this
as a fourth job:

```yaml
  test_e2e:
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        api-level: [29, 30, 33, 34, 35]   # 29 = project minSdk, 35 ≈ current stable
    steps:

    - name: Clone repo
      uses: actions/checkout@v7

    - name: Setup build
      uses: ./.github/actions/setup-build
      with:
        google-services-pro: ${{ secrets.GOOGLE_SERVICES }}
        google-services-free: ${{ secrets.GOOGLE_SERVICES_FREE }}
        google-services-cloudtestadmin: ${{ secrets.GOOGLE_SERVICES }}

    - name: Enable KVM
      run: |
        echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
        sudo udevadm control --reload-rules
        sudo udevadm trigger --name-match=kvm

    - name: Cache AVD
      uses: actions/cache@v4
      with:
        path: |
          ~/.android/avd/*
          ~/.android/adb*
        key: avd-${{ matrix.api-level }}-v1

    - name: Build debug APK and test APK
      run: ./gradlew :app:assembleProDebug :app:assembleProDebugAndroidTest

    - name: Install Maestro
      run: |
        curl -fsSL "https://get.maestro.mobile.dev" | bash
        echo "$HOME/.maestro/bin" >> "$GITHUB_PATH"

    - name: Run instrumented tests + Maestro flows
      uses: reactivecircus/android-emulator-runner@v2
      with:
        api-level: ${{ matrix.api-level }}
        target: google_apis
        arch: x86_64
        disable-animations: true
        script: |
          adb shell pm grant com.cray.software.justreminderpro android.permission.POST_NOTIFICATIONS || true
          ./gradlew connectedProDebugAndroidTest --continue
          adb install -r app/build/outputs/apk/pro/debug/app-pro-debug.apk
          maestro test --tags smoke,recurrence,notifications,settings .maestro/

    - name: Publish Compose UI Test Report
      uses: mikepenz/action-junit-report@v6
      if: always()
      with:
        report_paths: '**/build/outputs/androidTest-results/connected/**/*.xml'
        detailed_summary: true
        include_passed: false
        check_name: 'E2E Test Results (API ${{ matrix.api-level }})'
        summary: true

    - name: Upload Maestro artifacts (screenshots/logs on failure)
      uses: actions/upload-artifact@v7
      if: failure()
      with:
        name: maestro-failure-${{ matrix.api-level }}
        path: ~/.maestro/tests/
        retention-days: 14
```

Notes:

- **`ubuntu-latest` + KVM is sufficient** — GitHub enabled hardware-accelerated Android
  virtualization on Linux hosted runners in April 2024, so the older advice to use pricier
  `macos-*` runners for emulator acceleration no longer applies.
- **Cost**: this uses standard GitHub Actions minutes only (2,000 free/month on private repos,
  unlimited on public repos) — no paid device-farm account is required for this job. A 5-cell
  API-level matrix does multiply runtime/minutes by 5, though — consider running only the
  `smoke`-tagged Maestro subset + Tier-A/B Compose tests on every PR (one API level, e.g. 34),
  and the full matrix + full Maestro tag set on `master`-merge / nightly, to keep PR feedback
  fast.
- **Always use the `pro` flavor debug variant** here per this repo's existing convention (never
  `free`, unless the change is free-flavor-specific) — matches
  `applicationId = "com.cray.software.justreminderpro"`.
- `connectedProDebugAndroidTest` at the root will pick up Tier-A tests from any `feature:*`
  module and Tier-B tests from `app` in one invocation, as long as each module's `androidTest`
  targets a variant Gradle can resolve against (library modules have no flavor dimension, so
  their `androidTest` runs regardless of the `Pro` in the task name).

---

## 4. Notification testing: mechanics and a wiring caveat

**Read this before writing any notification-delivery assertion.**
[`reminderv2-notification-hierarchy.md`](reminderv2-notification-hierarchy.md) documents that
`ResolveReminderV2NotificationSettingsUseCase` — the function that merges
Reminder→Group→Settings overrides into one effective `NotificationSettings` — **is not called
anywhere in the delivery path yet** ("Nothing reads the resolved `NotificationSettings` at
notification-fire time"). Separately, `Notifier.reminderChannelId(settings: NotificationSettings)`
*does* already take a `NotificationSettings` and create/derive a channel keyed on
`vibrate|vibrationPattern|priority|bypassDoNotDisturb` (see `Notifier.kt`) — but confirm at
implementation time exactly which `NotificationSettings` instance is passed in (raw
per-reminder-with-defaults vs. the fully resolved 3-level merge) before asserting that a Group- or
Settings-level override actually changes a fired notification's channel/behavior. Until that's
confirmed wired end-to-end, §C below should assert on the **UI round-trip** (does Settings/Group/
Reminder correctly show/persist "Inherited: X" vs. an explicit override) rather than on the fired
notification's actual channel importance/vibration for Group- and Settings-level overrides
specifically. Reminder-level overrides and the app-wide quiet-hours DND (§D) are already fully
wired (`DoNotDisturbManager`, `Prefs`) and safe to assert on end-to-end today.

Practical mechanics for the tests in §D:

- **Don't sleep-wait for real due times.** Seed a reminder with a *Countdown* recurrence (exists
  exactly for this: `RecurrenceRule.Countdown(after = 65_000)`) or a Date+Time a minute in the
  future, then use Maestro's `extendedWaitUntil` (timeout ~90s) rather than a real calendar-day
  wait. This is the only recurrence type worth using purely as a delivery-timing vehicle; the
  recurrence-correctness tests in §A should still cover the other types independently at the
  scheduling-calculation level (Tier B, asserting the computed next-occurrence date, not waiting
  for it to actually fire).
- **Grant `POST_NOTIFICATIONS` before the run** (Android 13+/API 33+): `adb shell pm grant
  <applicationId> android.permission.POST_NOTIFICATIONS` (shown in the workflow above) so tests
  aren't blocked on a permission dialog that varies by OEM. Test the *denied* path (§D-62)
  separately and explicitly, without the grant.
- **Reading the notification shade**: Maestro can swipe open the shade (`swipe` from the top
  edge) and assert on visible text (`assertVisible: "<reminder summary>"`), or use
  `adb shell dumpsys notification --noredact` from a `runScript`/shell step for structured
  assertions (channel id, which — per `Notifier.channelSuffix` — encodes
  vibrate/vibrationPattern/priority/bypassDoNotDisturb as a hash, useful for confirming a
  Reminder-level override actually produced a *different* channel than the default one).
- **App-level quiet hours** (`DoNotDisturbManager`, `Prefs.isDoNotDisturbEnabled` /
  `doNotDisturbFrom` / `doNotDisturbTo` / `doNotDisturbIgnore`) is fully in-app and doesn't touch
  system DND, so it's CI-safe: `doNotDisturbIgnore` is a 0–5 priority threshold where `5` means
  "ignore everything regardless of priority" and `0–4` means "suppress only if the reminder's
  priority is below this value" — §D below covers both branches.

---

## 5. E2E test list

Legend: **Tier A** = Compose screen-level (feature module `androidTest`) · **Tier B** = Compose
flow-level (`app` `androidTest`) · **Maestro** = black-box flow · **P0** = must have before this
initiative is considered "done" · **P1** = important, can land after initial rollout.

### A. Reminder recurrence types

Every row maps 1:1 to a `RecurrenceRule` variant (`core/domain/.../reminder/v2/RecurrenceRule.kt`)
via the exact builder-item combination `RecurrenceRuleCalculator` requires — verified by reading
that calculator, not guessed from the UI.

| # | Test | Builder items used | `RecurrenceRule` produced | Tool | Pri |
|---|---|---|---|---|---|
| A1 | One-time reminder | Date + Time | `Once` | Tier B | P0 |
| A2 | Repeat-from-date, fixed millis interval | Date + Time + RepeatTime | `Daily(repeatInterval)` | Tier B | P0 |
| A3 | Simple countdown timer | Timer only | `Countdown(after)` | Tier B, Maestro | P0 |
| A4 | Countdown timer + repeat interval + repeat limit | Timer + RepeatTime + RepeatLimit | `Countdown(after, repeatInterval, repeatLimit)` | Tier B | P0 |
| A5 | Countdown timer + exclusion window | Timer + TimerExclusion | `Countdown` + exclusion applied to next-fire calc | Tier B | P1 |
| A6 | Weekly, single weekday | Time + DaysOfWeek(1 day) | `Weekly(weekdays=[x])` | Tier B, Maestro | P0 |
| A7 | Weekly, multiple weekdays | Time + DaysOfWeek(3+ days) | `Weekly(weekdays=[...])` | Tier B | P0 |
| A8 | Weekly + repeat limit | Time + DaysOfWeek + RepeatLimit | `Weekly(..., repeatLimit)` — stops firing after N occurrences | Tier B | P1 |
| A9 | Monthly, single day-of-month | Time + DayOfMonth | `Monthly(dayOfMonth)` | Tier B, Maestro | P0 |
| A10 | Monthly + repeat interval (every N months) + limit | Time + DayOfMonth + RepeatInterval + RepeatLimit | `Monthly(dayOfMonth, repeatInterval, repeatLimit)` | Tier B | P0 |
| A11 | **Edge case**: `dayOfMonth = 31` rolling into a 30-day/28-day month | Time + DayOfMonth(31) | `Monthly(31)` — verify `findNextMonthDayDateTime` behavior, not just that it doesn't crash | Tier B | P0 |
| A12 | Yearly, single day-of-year | Time + DayOfYear | `Yearly(dayOfMonth, monthOfYear)` | Tier B, Maestro | P0 |
| A13 | Yearly + repeat interval + limit | Time + DayOfYear + RepeatInterval + RepeatLimit | `Yearly(..., repeatInterval, repeatLimit)` | Tier B | P0 |
| A14 | **Edge case**: Feb 29 on a leap year, next occurrence in a non-leap year | Time + DayOfYear=60 | `Yearly(29, Feb)` — confirm fallback (Feb 28 vs Mar 1) | Tier B | P1 |
| A15 | Location — arriving (geofence enter) | ArrivingCoordinates | `LocationEnter` | Tier B | P0 |
| A16 | Location — leaving (geofence exit) | LeavingCoordinates | `LocationExit` | Tier B | P0 |
| A17 | Location with delayed fire | Arriving/LeavingCoordinates + LocationDelayDate/Time | `LocationEnter`/`LocationExit` with `LocationSettings(hasDelayedReminder=true)` | Tier B | P1 |
| A18 | Custom RRULE — `FREQ=DAILY;INTERVAL=n;COUNT=n` | ICal Freq/Interval/Count | `ICalendar(rrule)` | Tier B, Maestro | P0 |
| A19 | Custom RRULE — `FREQ=WEEKLY;BYDAY=…` (multi-day) | ICal Freq + ByDay | `ICalendar(rrule)` | Tier B | P0 |
| A20 | Custom RRULE — `FREQ=MONTHLY;BYMONTHDAY=…` | ICal Freq + ByMonthDay | `ICalendar(rrule)` | Tier B | P1 |
| A21 | Custom RRULE — `FREQ=MONTHLY;BYDAY=TU;BYSETPOS=2` ("2nd Tuesday") | ICal Freq + ByDay + BySetPos | `ICalendar(rrule)` — **this is currently the only UI-reachable way to express what the domain's `RecurrenceRule.RelativeMonthly` models; `RelativeMonthly` itself is never constructed by `RecurrenceRuleCalculator` today** — flag this test's intent as "verify the RRULE escape hatch," not "verify `RelativeMonthly`" | Tier B | P1 |
| A22 | Custom RRULE — `FREQ=YEARLY;BYMONTH=…;BYMONTHDAY=…` | ICal Freq + ByMonth + ByMonthDay | `ICalendar(rrule)` | Tier B | P1 |
| A23 | Custom RRULE with `UNTIL` date | ICal Freq + UntilDate/Time | `ICalendar(rrule)` — recurrence stops after `UNTIL` | Tier B | P1 |
| A24 | Save a custom RRULE as a "recur preset," reuse it on a second reminder | ICal builder → save preset → new reminder → apply preset | n/a (preset feature) | Maestro | P1 |
| A25 | Shopping-list reminder with no date/time | Sub-tasks only, no Date/Time/Timer | `Once` via `emptySchedule()` (no `eventDateTime`) | Tier B | P1 |
| A26 | Edit existing reminder: change recurrence type (e.g. Once → Weekly) | any → any | recalculated `RecurrenceRule` replaces the old one | Tier B | P0 |
| A27 | Complete one occurrence of a repeating reminder, confirm next occurrence schedules | Weekly/Monthly/Daily | next `eventDateTime` advances correctly; `repeatLimit`/`until` boundary stops repetition when exhausted | Tier B | P0 |
| A28 | Snooze a fired reminder (`delayMinutes`), confirm it re-fires after the window | any + DelayMinutes override | re-scheduled at `now + delayMinutes` | Tier B, Maestro | P1 |
| A29 | Delete/archive a reminder cancels its scheduled alarm | any | no notification fires after deletion (verify via §4 shade-check, absence) | Maestro | P0 |

### B. Reminder actions & customization (non-notification)

| # | Test | Tool | Pri |
|---|---|---|---|
| B1 | Phone call action — number entry + validation | Tier A | P1 |
| B2 | SMS action — number entry + validation | Tier A | P1 |
| B3 | Email action + subject field | Tier A | P1 |
| B4 | Web link action — URL validation | Tier A | P1 |
| B5 | Open-application action (SDK-gated, `maxSdk = S`) — confirm hidden above Android 12 | Tier A | P1 |
| B6 | Sub-tasks/shopping list — add/remove/check off nested items | Tier B, Maestro | P0 |
| B7 | Group assignment on create, group-based filtering on list/home | Tier B, Maestro | P0 |
| B8 | Tag assignment (add/remove), filter reminder list by tag | Tier B | P1 |
| B9 | Priority selection reflected in list sort/badge | Tier B | P1 |
| B10 | Note attachment link (create/edit reminder ↔ existing note) | Tier B | P1 |
| B11 | File attachment add/remove | Tier A | P1 |
| B12 | Google Task list linkage — **requires a signed-in Google test account; tag `manual`/skip in CI, cover with a fake-auth Tier B test of the UI wiring instead** | Tier B (fake auth) | P1 |
| B13 | Google Calendar event creation + duration field | Tier B (fake calendar API) | P1 |
| B14 | "Remind before" (before-time) field | Tier A | P1 |
| B15 | Repeat time / interval / limit fields validated together (constraint rules from `BuilderItem` `constraints {}` blocks) | Tier A | P0 |
| B16 | Constraint enforcement: selecting a blocked combination (e.g. Timer + Date) is prevented/clears the conflicting item | Tier A | P0 |

### C. Notification customization (Settings → Group → Reminder hierarchy)

One row per field from `NotificationSettingsOverride`
(`core/domain/.../reminder/v2/RecurrenceRule.kt` sibling `NotificationSettingsResolver.kt`); see
§4's caveat about what's actually wired to delivery today.

| # | Test | Level(s) | Tool | Pri |
|---|---|---|---|---|
| C1 | `vibrate` toggle: Settings default, Group override, Reminder override, "Inherit" resets to null | All 3 | Tier B | P0 |
| C2 | `vibrationPattern` preset picker at all 3 levels (**PRO-only** — confirm hidden on free flavor) | All 3 | Tier B | P0 |
| C3 | `repeatNotification` toggle at all 3 levels | All 3 | Tier B | P1 |
| C4 | `priority` picker at all 3 levels, reflected in channel importance | All 3 | Tier B, Maestro | P0 |
| C5 | `category` picker at all 3 levels | All 3 | Tier B | P1 |
| C6 | `bypassDoNotDisturb` toggle at all 3 levels | All 3 | Tier B, Maestro | P0 |
| C7 | `wakeScreen` toggle at all 3 levels | All 3 | Tier B | P1 |
| C8 | `lockScreenVisibility` picker at all 3 levels | All 3 | Tier B | P1 |
| C9 | `delayMinutes` override (switch + slider dialog, distinct UI from the other fields) at all 3 levels | All 3 | Tier B | P1 |
| C10 | LED `color` picker — Settings-level only (frozen at Group/Reminder except builder), **PRO-only** | Settings + Reminder builder | Tier B | P1 |
| C11 | "How does this work?" help screen opens from both Settings and Group editor entry points | Settings, Group | Tier A | P1 |
| C12 | Subtitle text shows `"Inherited: <effective value>"` vs the explicit override label correctly after each save | Group, Reminder | Tier B | P0 |

### D. Notification delivery under different app settings

| # | Test | Tool | Pri |
|---|---|---|---|
| D1 | Quiet hours disabled → notification fires normally at due time | Maestro | P0 |
| D2 | Quiet hours enabled, due time inside window, priority below `doNotDisturbIgnore` → suppressed | Maestro | P0 |
| D3 | Quiet hours enabled, due time inside window, priority ≥ `doNotDisturbIgnore` → still fires | Maestro | P0 |
| D4 | Quiet hours `doNotDisturbIgnore = 5` ("ignore all") → suppressed regardless of priority | Maestro | P0 |
| D5 | Quiet hours enabled but due time outside the window → fires normally | Maestro | P1 |
| D6 | `repeatNotification` enabled → re-alerts at interval until dismissed/opened | Maestro | P1 |
| D7 | `POST_NOTIFICATIONS` permission denied (Android 13+) → app doesn't crash; in-app prompt shown; no system notification posted | Maestro | P0 |
| D8 | Permission granted after initial denial (via deep-linked system Settings) → subsequent reminders fire | Maestro | P1 |
| D9 | Tapping a fired notification opens the correct reminder preview/edit screen (deep link) | Maestro | P0 |
| D10 | Dismissing (swipe-away) vs. tapping a notification action updates reminder state accordingly | Maestro | P1 |
| D11 | Wear/companion notification (`WEAR_NOTIFICATION` pref) posts a secondary notification when enabled | Maestro | P2 |
| D12 | Global default priority applied to a new reminder created with no explicit override | Tier B | P1 |

### E. Settings screens

Covers the settings surface currently mid-migration into `feature/featuresettings` (per this
branch's in-flight work) — write these against whichever module owns the screen by the time this
lands.

| # | Test | Tool | Pri |
|---|---|---|---|
| E1 | General settings: theme/color-scheme change reflected app-wide (light/dark, dynamic color) | Maestro | P1 |
| E2 | Security settings: enable biometric/PIN lock → relaunch → lock gate appears before content | Maestro | P0 |
| E3 | Location settings: default geofence radius, permission prompt flow | Tier A | P1 |
| E4 | Calendar settings: first-day-of-week affects Calendar screen layout | Tier A | P2 |
| E5 | Cloud backup entry points (Google Drive / Dropbox) render; **full OAuth sign-in is out of scope for CI — cover only the UI entry state, flag full sign-in as a manual/physical-device check** | Tier A | P1 |
| E6 | Local encrypted backup (PRO): export with passphrase → restore with correct passphrase succeeds | Maestro | P1 |
| E7 | Local encrypted backup: restore with wrong passphrase / corrupted file rejected cleanly, no crash | Maestro | P0 |
| E8 | Troubleshooting: cache-clear action completes without error | Tier A | P2 |
| E9 | iCalendar (`.ics`) import/export round-trip preserves reminder data | Tier B | P1 |

### F. Cross-cutting / regression-prone

| # | Test | Tool | Pri |
|---|---|---|---|
| F1 | Free vs. Pro flavor gating: PRO-only builder items (LED color, vibration pattern, Other Params, Insights) hidden/disabled on `free` flavor build | Tier A (run against `assembleFreeDebug` build separately — see `CLAUDE.md`'s free-flavor caveat) | P0 |
| F2 | Reminder list smart filters: Today / Overdue / This week / No group | Tier B | P0 |
| F3 | Calendar view surfaces reminders + birthdays + Google Task due dates for a given day | Tier B | P1 |
| F4 | Global search finds a reminder/note/birthday/task by text | Tier B | P1 |
| F5 | Rotation/config-change mid-builder doesn't lose in-progress input | Tier B | P1 |
| F6 | Multi-select bulk actions on reminder list (long-press → select → bulk delete/complete, per [multiselect.md](multiselect.md)) | Tier B, Maestro | P0 |
| F7 | Widget configuration screens open without crashing (actual home-screen widget rendering is out of scope — Maestro/Espresso can't drive the launcher) | Tier A | P2 |

---

## 6. Rollout suggestion

1. Land the Gradle/module wiring (§2) and the GitHub Actions job (§3) with just **A1–A4, A6, A9,
   A12** (one Tier-B test per core recurrence family) and **D1–D4** (quiet-hours) to prove the
   pipeline end-to-end before writing the full matrix.
2. Fill in the rest of §A (edge cases) and §C (notification hierarchy) next — these are the areas
   explicitly called out as needing coverage and are where `RecurrenceRuleCalculator` and the
   Settings/Group/Reminder override resolution have the most surface area for regressions.
3. §B/§E/§F can land incrementally as each screen stabilizes, especially given `feature-reminder`
   and the settings feature module are both still under active extraction into their own Gradle
   modules on this branch — a screen's exact package/module home may move before these tests are
   written.

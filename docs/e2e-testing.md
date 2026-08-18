# End-to-End (E2E) UI Testing

This document proposes how to add real device/emulator UI testing to this repo — Compose UI
tests for fast, in-process screen/flow verification, and [Maestro](https://maestro.dev) for
true black-box user journeys (including notification-tray checks) — wired into GitHub Actions
across multiple Android versions.

**Status: infrastructure and a first wave of coverage are live.** `app/src/androidTest/kotlin/com/elementary/tasks/e2e/ReminderRecurrenceE2ETest.kt`
(Tier B, ~30 tests) and `.maestro/notifications/*.yaml` (6 flows) exist and run — Gradle wiring
(§2), the nightly CI job (§3), and most of §A/§B/§D below are implemented and verified on a real
device. §5's tables now carry a **Status** column per row so you can tell what's done, skipped
(with why), deferred, or still just planned, without cross-referencing the test file. Nothing in
Tier A exists yet — every Tier-B-labeled row below that's done lives in the one flow-level test
file; no `feature:*` module has its own `androidTest` source set, so `core:testing` hasn't
actually needed the Android-instrumented split §2 originally proposed for it.

The rollout notes in §6 are the fastest way to see where things actually stand and what's likely
next.

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

The layout below is the original proposal (organized by test-category folder: `smoke/`,
`recurrence/`, `settings/`, etc.) — **what actually exists today is narrower**, just the
`notifications/` folder, since that's the only category with flows written so far:

```
.maestro/
  config.yaml                     # appId
  notifications/
    quiet_hours_disabled_fires.yaml              # D1
    quiet_hours_low_priority_suppressed.yaml     # D2
    quiet_hours_high_priority_fires.yaml         # D3
    quiet_hours_ignore_all_suppressed.yaml       # D4
    notification_permission_denied.yaml          # D7
    tap_notification_opens_reminder.yaml         # D9
    flows/                        # reusable subflows, not runnable top-level tests themselves
      create_countdown_reminder.yaml
      enable_quiet_hours.yaml
```

The rest of this section's proposed layout (`smoke/`, `recurrence/`, `settings/`) is still just
a proposal — nothing under those categories has been written yet (recurrence coverage so far is
all Tier B/Compose instead, per §A's Status column).

---

## 2. Gradle wiring needed

**Status: done**, for `app` only — matches the shape proposed below (orchestrator execution,
`clearPackageData = "true"`, `compose.ui.test.junit4`, `mockk.android`, `koin.test`, confirmed
against the checked-in `app/build.gradle.kts`). `clearPackageData` only resets state once per
`connectedProDebugAndroidTest` invocation, not per test method within the run — worth knowing
before assuming any given test starts from a truly clean install (see the Privacy Policy banner
caveat on A26 in `ReminderRecurrenceE2ETest.kt` for a concrete case this affected). No `feature:*`
module has gained its own `androidTest` source set (no Tier-A tests exist yet), so the "same block
goes into any `feature:*` module" step below and the `core:testing` Android-split plan are both
still purely aspirational.

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

**Status: done, simplified.** `test_e2e` exists in `build_and_test.yml` as a fourth job, but
schedule-triggered only (nightly, not per-PR/push) and pinned to a single API level (34) rather
than the illustrative 5-cell matrix below — see the "Nightly failure notification" bullet under
Notes for what actually ships today, including the GitHub Issue it keeps open while the job is
red. The YAML below is left as the original fuller proposal (multi-API-level, PR-triggered) in
case that's ever worth reintroducing; don't assume it matches the checked-in workflow verbatim.

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
- **Nightly failure notification**: `test_e2e` (the job as actually implemented in
  `build_and_test.yml` — single API level 34, schedule-triggered only, not the illustrative 5-cell
  matrix above) keeps one persistent GitHub Issue labeled `e2e-nightly-failure` as its status: a
  failing run opens/reopens it with a comment linking the run and the commit range since the last
  green nightly run (via the Actions API, filtered to `event: schedule`, `status: success`); the
  next passing run comments and closes it. This is how "did last night's merge break e2e" gets
  surfaced without anyone having to read through Actions run history — there's no separate
  historical trend dashboard, since GitHub's own Actions run list already shows that chronologically
  per-run.
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
  aren't blocked on a permission dialog that varies by OEM. Test the *denied* path (D7)
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

**Status** column values: **Done** (implemented and verified on a real device — see
`ReminderRecurrenceE2ETest.kt`/`.maestro/notifications/`, test method names roughly follow this
doc's row IDs) · **Skipped** (investigated and deliberately not implemented — reason inline or in
a note below the table) · **Deferred** (explicitly postponed to a later batch, not abandoned) ·
blank (not started).

### A. Reminder recurrence types

Every row maps 1:1 to a `RecurrenceRule` variant (`core/domain/.../reminder/v2/RecurrenceRule.kt`)
via the exact builder-item combination `RecurrenceRuleCalculator` requires — verified by reading
that calculator, not guessed from the UI.

| # | Test | Builder items used | `RecurrenceRule` produced | Tool | Pri | Status |
|---|---|---|---|---|---|---|
| A1 | One-time reminder | Date + Time | `Once` | Tier B | P0 | Done |
| A2 | Repeat-from-date, fixed millis interval | Date + Time + RepeatTime | `Daily(repeatInterval)` | Tier B | P0 | Done |
| A3 | Simple countdown timer | Timer only | `Countdown(after)` | Tier B, Maestro | P0 | Done |
| A4 | Countdown timer + repeat interval + repeat limit | Timer + RepeatTime + RepeatLimit | `Countdown(after, repeatInterval, repeatLimit)` | Tier B | P0 | Done |
| A5 | Countdown timer + exclusion window | Timer + TimerExclusion | `Countdown` + exclusion applied to next-fire calc | Tier B | P1 | Done — scope note below |
| A6 | Weekly, single weekday | Time + DaysOfWeek(1 day) | `Weekly(weekdays=[x])` | Tier B, Maestro | P0 | Done |
| A7 | Weekly, multiple weekdays | Time + DaysOfWeek(3+ days) | `Weekly(weekdays=[...])` | Tier B | P0 | Done |
| A8 | Weekly + repeat limit | Time + DaysOfWeek + RepeatLimit | `Weekly(..., repeatLimit)` — stops firing after N occurrences | Tier B | P1 | Done |
| A9 | Monthly, single day-of-month | Time + DayOfMonth | `Monthly(dayOfMonth)` | Tier B, Maestro | P0 | Done |
| A10 | Monthly + repeat interval (every N months) + limit | Time + DayOfMonth + RepeatInterval + RepeatLimit | `Monthly(dayOfMonth, repeatInterval, repeatLimit)` | Tier B | P0 | Done |
| A11 | **Edge case**: `dayOfMonth = 31` rolling into a 30-day/28-day month | Time + DayOfMonth(31) | `Monthly(31)` — verify `findNextMonthDayDateTime` behavior, not just that it doesn't crash | Tier B | P0 | Done — redefined, see note below |
| A12 | Yearly, single day-of-year | Time + DayOfYear | `Yearly(dayOfMonth, monthOfYear)` | Tier B, Maestro | P0 | Done |
| A13 | Yearly + repeat interval + limit | Time + DayOfYear + RepeatInterval + RepeatLimit | `Yearly(..., repeatInterval, repeatLimit)` | Tier B | P0 | Done |
| A14 | **Edge case**: Feb 29 on a leap year, next occurrence in a non-leap year | Time + DayOfYear=60 | `Yearly(29, Feb)` — confirm fallback (Feb 28 vs Mar 1) | Tier B | P1 | Done |
| A15 | Location — arriving (geofence enter) | ArrivingCoordinates | `LocationEnter` | Tier B | P0 | |
| A16 | Location — leaving (geofence exit) | LeavingCoordinates | `LocationExit` | Tier B | P0 | |
| A17 | Location with delayed fire | Arriving/LeavingCoordinates + LocationDelayDate/Time | `LocationEnter`/`LocationExit` with `LocationSettings(hasDelayedReminder=true)` | Tier B | P1 | |
| A18 | Custom RRULE — `FREQ=DAILY;INTERVAL=n;COUNT=n` | ICal Freq/Interval/Count | `ICalendar(rrule)` | Tier B, Maestro | P0 | Deferred |
| A19 | Custom RRULE — `FREQ=WEEKLY;BYDAY=…` (multi-day) | ICal Freq + ByDay | `ICalendar(rrule)` | Tier B | P0 | Deferred |
| A20 | Custom RRULE — `FREQ=MONTHLY;BYMONTHDAY=…` | ICal Freq + ByMonthDay | `ICalendar(rrule)` | Tier B | P1 | Deferred |
| A21 | Custom RRULE — `FREQ=MONTHLY;BYDAY=TU;BYSETPOS=2` ("2nd Tuesday") | ICal Freq + ByDay + BySetPos | `ICalendar(rrule)` — **this is currently the only UI-reachable way to express what the domain's `RecurrenceRule.RelativeMonthly` models; `RelativeMonthly` itself is never constructed by `RecurrenceRuleCalculator` today** — flag this test's intent as "verify the RRULE escape hatch," not "verify `RelativeMonthly`" | Tier B | P1 | Deferred |
| A22 | Custom RRULE — `FREQ=YEARLY;BYMONTH=…;BYMONTHDAY=…` | ICal Freq + ByMonth + ByMonthDay | `ICalendar(rrule)` | Tier B | P1 | Deferred |
| A23 | Custom RRULE with `UNTIL` date | ICal Freq + UntilDate/Time | `ICalendar(rrule)` — recurrence stops after `UNTIL` | Tier B | P1 | Deferred |
| A24 | Save a custom RRULE as a "recur preset," reuse it on a second reminder | ICal builder → save preset → new reminder → apply preset | n/a (preset feature) | Maestro | P1 | Deferred — depends on A18-A23's RRULE work |
| A25 | Shopping-list reminder with no date/time | Sub-tasks only, no Date/Time/Timer | `Once` via `emptySchedule()` (no `eventDateTime`) | Tier B | P1 | Done |
| A26 | Edit existing reminder: change recurrence type (e.g. Once → Weekly) | any → any | recalculated `RecurrenceRule` replaces the old one | Tier B | P0 | Done |
| A27 | Complete one occurrence of a repeating reminder, confirm next occurrence schedules | Weekly/Monthly/Daily | next `eventDateTime` advances correctly; `repeatLimit`/`until` boundary stops repetition when exhausted | Tier B | P0 | Skipped — no safe UI path, see note below |
| A28 | Snooze a fired reminder (`delayMinutes`), confirm it re-fires after the window | any + DelayMinutes override | re-scheduled at `now + delayMinutes` | Tier B, Maestro | P1 | |
| A29 | Delete/archive a reminder cancels its scheduled alarm | any | no notification fires after deletion (verify via §4 shade-check, absence) | Maestro | P0 | |

Notes on the non-obvious Status values above:

- **A5** is done, but scoped down from the row's literal description: `RecurrenceRuleCalculator
  .fromTimer()` never reads the exclusion window at all (confirmed by reading that file) — it only
  feeds `RecurrenceCalculator.findNextTimerDateTime`'s `excludedHours` param when computing a
  *repeat's next fire*, not at initial save, the same "would need to wait for a real fire to
  observe" limitation as A27. The implemented test verifies the exclusion configuration
  (`activeHours`/`quietHoursFrom`/`quietHoursTo` on the reminder's own `NotificationSettingsOverride`)
  round-trips correctly through the builder and repository — not that a fire actually gets skipped.
- **A11** was redefined after discovering `DayOfMonthValueEditor`'s wheel only offers days 1–28
  plus a "Last day" sentinel (`dayOfMonth = 0`) — there's no way to pick a literal 29–31 through
  the real UI, so the originally-planned "31 rolling into a shorter month" scenario can't happen.
  The implemented test instead pins "now" (via a `FakeNowDateTimeProvider`) so the *next* month is
  a non-leap February, and asserts "Last day" resolves to the 28th, not March or a crash — same
  underlying `findNextMonthDayDateTime` code path, UI-reachable scenario.
- **A27** was investigated (a background agent traced every caller of `CompleteReminderUseCase`)
  and found no path reachable from `BottomNavActivity`'s own UI at all — completing an occurrence
  only happens via a fired system notification's "Done" action or `ReminderActionActivity` (a
  separate Activity, normally only reached from a notification). Driving that Activity directly
  mid-test was flagged as the closest option but unverified without a device; skipped rather than
  guessed at. Worth revisiting now that real-device verification is established practice in this
  test suite.

### B. Reminder actions & customization (non-notification)

| # | Test | Tool | Pri | Status |
|---|---|---|---|---|
| B1 | Phone call action — number entry + validation | Tier A | P1 | Done (as Tier B, not Tier A — see §5 intro) |
| B2 | SMS action — number entry + validation | Tier A | P1 | Done (Tier B) |
| B3 | Email action + subject field | Tier A | P1 | Done (Tier B) |
| B4 | Web link action — URL validation | Tier A | P1 | Done (Tier B) |
| B5 | Open-application action (SDK-gated, `maxSdk = S`) — confirm hidden above Android 12 | Tier A | P1 | Skipped — needs an API 31+ device, see note below |
| B6 | Sub-tasks/shopping list — add/remove/check off nested items | Tier B, Maestro | P0 | Done — add, check, and remove, see note below |
| B7 | Group assignment on create, group-based filtering on list/home | Tier B, Maestro | P0 | Done — assignment only, see note below |
| B8 | Tag assignment (add/remove), filter reminder list by tag | Tier B | P1 | Done — assignment only, see note below |
| B9 | Priority selection reflected in list sort/badge | Tier B | P1 | Done — selection only, see note below |
| B10 | Note attachment link (create/edit reminder ↔ existing note) | Tier B | P1 | Done |
| B11 | File attachment add/remove | Tier A | P1 | Skipped — real system file picker, see note below |
| B12 | Google Task list linkage — **requires a signed-in Google test account; tag `manual`/skip in CI, cover with a fake-auth Tier B test of the UI wiring instead** | Tier B (fake auth) | P1 | |
| B13 | Google Calendar event creation + duration field (`GoogleCalendarBuilderItem` + `GoogleCalendarDurationBuilderItem`'s `CalendarDuration{allDay, millis}`) | Tier B (fake calendar API) | P1 | |
| B14 | "Remind before" (before-time) field | Tier A | P1 | Done (Tier B) |
| B15 | Repeat time / interval / limit fields validated together (constraint rules from `BuilderItem` `constraints {}` blocks) | Tier A | P0 | Done (Tier B) — one constraint, see note below |
| B16 | Constraint enforcement: selecting a blocked combination (e.g. Timer + Date) is prevented/clears the conflicting item | Tier A | P0 | Done (Tier B) |
| B17 | Description field (`DescriptionBuilderItem`) persists free-text body, independent of Summary | Tier A | P2 | Done — bundled into B8's test, see note below |

Notes on the non-obvious Status values above:

- **All of B1–B4/B6/B7/B9/B10/B14/B15/B16 were implemented as Tier B**, not the Tier A this
  section originally specified — no `feature:*` module has ever gained its own `androidTest`
  source set (see §2's status note), so every test so far lives in the one Tier-B flow-level file
  and drives the full app instead of mounting an isolated composable. Functionally equivalent
  coverage, just a heavier/slower test than originally planned.
- **B5** wasn't implemented: its actual point is confirming the item is *hidden above* Android 12
  (`maxSdk = S` on `ApplicationBuilderItem`), but the real device used for this suite so far is
  API 30 — below the gate — so it can only ever prove the item is present, not that the gating
  itself works. Needs an API 31+ device.
- **B6** now covers add, check-off, and remove. The remove case needed a real (not semantics-faked)
  focus change first — `ShopItemRow`'s remove button only composes once its row is both focused
  *and* non-empty — done by tapping directly on the target row's own text, the same gesture a user
  would use, which reliably refocuses it via the real Compose focus system. `shopItemCheckTestTag`/
  `shopItemRemoveTestTag` (`SubTasksValueEditor.kt`) had already been scaffolded ahead of this by
  whoever wrote the add/check-off test; `shopItemRemoveTestTag` had no caller anywhere until now.
- **B7** covers "assignment on create" only, not "group-based filtering on list/home" — that half
  needs its own Home/list-screen investigation, folded into the new §G below rather than done here.
- **B9** covers persisted selection only, not "reflected in list sort/badge" — same reasoning as
  B7, that's a list/Home-screen rendering concern, see §G.
- **B11** wasn't implemented: `AttachmentsValueEditor`'s `onPickFiles` opens the real Android
  system file picker, a different app/Activity entirely — same category as B12/B13's Google-auth
  flows, already flagged above as out of scope for CI.
- **B15** covers one constraint (`RepeatLimitBuilderItem`'s `requiresAny`), not the full "repeat
  time/interval/limit validated together" scope implied by the row — a reasonable next slice if
  this area gets revisited.
- **B8 covers "assignment on create" only**, not "filter reminder list by tag" — same split already
  applied to B7/B9, that half is a list/Home-screen rendering concern, see §G. Tags also have no
  corresponding `BuilderItem` — unlike every other row in this section, they aren't added via the
  item picker at all. `BuildReminderScreen.kt`'s `TagsRow` is a fixed row rendered inside the same
  `LazyColumn` as the picked builder items (backed by `TagChipPicker`/`onManageTagsClick`), wired
  directly in `BuildReminderViewModel`/`BuildReminderState` — so it only composes once at least one
  builder item has been added (the empty-builder state renders `BuilderEmptyState` instead, with no
  `LazyColumn` at all). Needed new `factory<TagRepository>`/`factory<TagAssignmentRepository>`
  entries in `testRepositoryModule` (plus a no-op `TagSyncTrigger` fake — its one real
  implementation lives in `app` and schedules cloud-sync work irrelevant here), same reasoning as
  B10's new `NoteRepository` entry.
- **B17** ended up bundled into B8's test rather than a dedicated one, per this note's own original
  suggestion — `SummaryBuilderItem`/`DescriptionBuilderItem` were exercised constantly already as
  test scaffolding (every existing test sets a unique Summary so its row can be found later), but
  neither one's own persistence had ever been the subject of an assertion until now.

### C. Notification customization (Settings → Group → Reminder hierarchy)

One row per field from `NotificationSettingsOverride`
(`core/domain/.../reminder/v2/RecurrenceRule.kt` sibling `NotificationSettingsResolver.kt`); see
§4's caveat about what's actually wired to delivery today.

| # | Test | Level(s) | Tool | Pri | Status |
|---|---|---|---|---|---|
| C1 | `vibrate` toggle: Settings default, Group override, Reminder override, "Inherit" resets to null | All 3 | Tier B | P0 | Deferred |
| C2 | `vibrationPattern` preset picker at all 3 levels (**PRO-only** — confirm hidden on free flavor) | All 3 | Tier B | P0 | Deferred |
| C3 | `repeatNotification` toggle at all 3 levels | All 3 | Tier B | P1 | Deferred |
| C4 | `priority` picker at all 3 levels, reflected in channel importance | All 3 | Tier B, Maestro | P0 | Deferred |
| C5 | `category` picker at all 3 levels | All 3 | Tier B | P1 | Deferred |
| C6 | `bypassDoNotDisturb` toggle at all 3 levels | All 3 | Tier B, Maestro | P0 | Deferred |
| C7 | `wakeScreen` toggle at all 3 levels | All 3 | Tier B | P1 | Deferred |
| C8 | `lockScreenVisibility` picker at all 3 levels | All 3 | Tier B | P1 | Deferred |
| C9 | `delayMinutes` override (switch + slider dialog, distinct UI from the other fields) at all 3 levels | All 3 | Tier B | P1 | Deferred |
| C10 | LED `color` picker — Settings-level only (frozen at Group/Reminder except builder), **PRO-only** | Settings + Reminder builder | Tier B | P1 | Deferred |
| C11 | "How does this work?" help screen opens from both Settings and Group editor entry points | Settings, Group | Tier A | P1 | Deferred |
| C12 | Subtitle text shows `"Inherited: <effective value>"` vs the explicit override label correctly after each save | Group, Reminder | Tier B | P0 | Deferred |

Whole section deferred by project decision, together with §A's custom-RRULE rows (A18–A24) — not
started, waiting to be picked up as one batch.

**Note for whoever picks this batch up**: on the *builder* side (Reminder-level override), C1's
`vibrate` and C3's `repeatNotification` aren't two separate builder items — both are written by the
single PRO-only `OtherParamsBuilderItem` (`BiGroup.EXTRA`), whose `OtherParamsModifier.putInto` only
copies `vibrate`/`repeatNotification` onto `reminder.notification` when its `useGlobal` flag is
`false` (`useGlobal = true` is the "Inherit" state C1/C12 refer to). That same item's `OtherParams`
value also carries a `notifyByVoice` field that's captured in the UI but **never read by
`putInto`** — it doesn't persist anywhere. Don't write a C-series assertion expecting a voice-
notification override to round-trip; confirm with whoever owns this screen whether that's a known
dead field or a bug before doing anything else with it.

### D. Notification delivery under different app settings

| # | Test | Tool | Pri | Status |
|---|---|---|---|---|
| D1 | Quiet hours disabled → notification fires normally at due time | Maestro | P0 | Done |
| D2 | Quiet hours enabled, due time inside window, priority below `doNotDisturbIgnore` → suppressed | Maestro | P0 | Done |
| D3 | Quiet hours enabled, due time inside window, priority ≥ `doNotDisturbIgnore` → still fires | Maestro | P0 | Done |
| D4 | Quiet hours `doNotDisturbIgnore = 5` ("ignore all") → suppressed regardless of priority | Maestro | P0 | Done |
| D5 | Quiet hours enabled but due time outside the window → fires normally | Maestro | P1 | |
| D6 | `repeatNotification` enabled → re-alerts at interval until dismissed/opened | Maestro | P1 | |
| D7 | `POST_NOTIFICATIONS` permission denied (Android 13+) → app doesn't crash; in-app prompt shown; no system notification posted | Maestro | P0 | Done — written, not verifiable on the API 30 device used so far, see note below |
| D8 | Permission granted after initial denial (via deep-linked system Settings) → subsequent reminders fire | Maestro | P1 | |
| D9 | Tapping a fired notification opens the correct reminder preview/edit screen (deep link) | Maestro | P0 | Done — corrected target screen, see note below |
| D10 | Dismissing (swipe-away) vs. tapping a notification action updates reminder state accordingly | Maestro | P1 | |
| D11 | Wear/companion notification (`WEAR_NOTIFICATION` pref) posts a secondary notification when enabled | Maestro | P2 | |
| D12 | Global default priority applied to a new reminder created with no explicit override | Tier B | P1 | |

Notes on the non-obvious Status values above:

- **D1–D4** were also tightened after implementation: the flows originally asserted on the generic
  app-name text every reminder's notification shows (so a run could pass even if some other, wrong
  reminder's notification fired). They now seed each reminder with a unique Summary and assert on
  that instead — a real device run confirmed the original assertion was too weak to actually prove
  which notification fired.
- **D7** (`notification_permission_denied.yaml`) is written and structurally sound, but this
  suite's real device is API 30, where `POST_NOTIFICATIONS` isn't a runtime permission at all —
  there's no dialog for it to deny, so the flow's actual "Don't allow" step has never run for real.
  It'll get genuine coverage the next time this runs against CI's API 34 target.
- **D9** (`tap_notification_opens_reminder.yaml`) corrects an assumption this row's own description
  made: reading `ReminderNotificationHandler.contentPendingIntent` shows tapping a notification
  actually opens `ReminderActionActivity` (the Complete/Snooze action picker), not a preview/edit
  screen. The implemented flow asserts against that real target instead.

### E. Settings screens

**Status: not started** (no Status column below — every row is still just planned).

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

**Status: not started** (no Status column below — every row is still just planned).

| # | Test | Tool | Pri |
|---|---|---|---|
| F1 | Free vs. Pro flavor gating: PRO-only builder items (LED color, vibration pattern, Other Params, Insights) hidden/disabled on `free` flavor build | Tier A (run against `assembleFreeDebug` build separately — see `CLAUDE.md`'s free-flavor caveat) | P0 |
| F2 | Reminder list smart filters: Today / Overdue / This week / No group | Tier B | P0 |
| F3 | Calendar view surfaces reminders + birthdays + Google Task due dates for a given day | Tier B | P1 |
| F4 | Global search finds a reminder/note/birthday/task by text | Tier B | P1 |
| F5 | Rotation/config-change mid-builder doesn't lose in-progress input | Tier B | P1 |
| F6 | Multi-select bulk actions on reminder list (long-press → select → bulk delete/complete, per [multiselect.md](multiselect.md)) | Tier B, Maestro | P0 |
| F7 | Widget configuration screens open without crashing (actual home-screen widget rendering is out of scope — Maestro/Espresso can't drive the launcher) | Tier A | P2 |

**F4 correction**: there is no separate global-search screen — the only search UI found is the
`SearchBar` embedded directly in `AgendaScreen`/`RemindersArchiveScreen`, each filtering that
screen's own list. F4 should be read as "search within Agenda/Archive," not a cross-content
search surface; confirm against `AgendaViewModel.onSearchQueryChange` before writing it.

### G. Reminder appearance across screens

**Status: not started.** Everything in §A/§B/§D so far asserts on what got *persisted* through
the builder — nothing yet asserts on what a reminder actually *looks like* once it exists, across
the different screens that render one. This section was added once enough of the rest existed to
make that gap obvious, and is grounded in reading the actual screens/adapters rather than guessed
— see the notes after the table for specifics and open questions.

Several screens turn out to share one underlying text/row adapter
(`UiReminderListAdapterImpl.createV2()` → `UiReminderList`), so a bug in that adapter would show
up identically on Home, Agenda, and the Group details screen — worth keeping in mind when
prioritizing: G5/G7 below are as much a regression check on that shared adapter as they are
screen-specific.

| # | Test | Tool | Pri |
|---|---|---|---|
| G1 | Home: a reminder with a Summary set shows that text as the row's title (`GetActiveEventsForTheDayUseCase.createMainText`) | Tier B | P0 |
| G2 | Home: a reminder with no Summary (e.g. shopping-list-only) falls back to the `"(description)"` placeholder text | Tier B | P1 |
| G3 | Home only shows today's active, non-removed reminders — one scheduled for tomorrow, already completed/inactive, or removed doesn't appear (`observeActiveInRange`'s date range + `active`/`removed` filters) | Tier B | P0 |
| G4 | Agenda screen (the "reminder list" §F2 already refers to): smart-list filters (Today / Overdue / This week / No group) each show the correct subset | Tier B | P0 |
| G5 | Agenda screen: a reminder row renders summary/placeholder text, due-date/place secondary text, and repeat/remaining/group tag chips correctly, plus an "Enabled" status chip and its Open/Edit/Archive/Skip/Turn-off row menu | Tier B | P1 |
| G6 | Agenda screen: its embedded search filters the list by reminder text (see the F4 correction above — this *is* what "global search" means today) | Tier B | P1 |
| G7 | Group details screen: a member reminder's row renders the same summary/date/tag chips as Agenda (same underlying `UiReminderList` adapter) but with **no status chip and no row menu** — click-only navigation to open it | Tier B | P1 |
| G8 | Archive screen: a removed reminder's row renders summary/date/tags with **only Edit and Delete** in its menu, no status chip, and — confirmed by reading `RemindersArchiveViewModel`/`ArchiveReminderRow` — **no restore/un-archive action exists anywhere on the row at all**; the only way back to active is via Edit. Worth a test asserting that absence explicitly, since it's easy to accidentally "fix" as a bug later without realizing it's the current intended behavior | Tier B | P1 |
| G9 | Preview screen: Enabled/Disabled status text + toggle button reflects and changes the reminder's active state; button is disabled (non-interactive) once the reminder is removed | Tier B | P0 |
| G10 | Preview screen: Details section (summary, description, due date, remind-before, repeat text, remaining time, group title, priority title, tags, ID) renders correctly for a fully-populated reminder | Tier B | P1 |
| G11 | Preview screen: action-target section renders contact name + raw phone number for Call/SMS, contact name + email + subject for Email, resolved app name for App, and the URL for Link — correctly hidden for Shopping/None | Tier B | P1 |
| G12 | Preview screen: attachments (image thumbnail vs. file-type icon), sub-tasks (checked count, strikethrough), map/place address, note-link card, Google Task card, and calendar-event cards each render when the reminder has that data | Tier B | P1 |
| G13 | Preview screen: overflow menu (Edit/Share/Copy/Delete) — Copy only appears when `state.canCopy`, and the delete confirmation dialog's text switches between "Delete" and "Move to the archive" depending on `state.canDelete` | Tier B | P1 |
| G14 | Action screen (`ReminderActionActivity`, opened from a fired notification or in-app): header renders contact photo/icon + name + phone number for Call/SMS, email + subject for Email, resolved app name (not raw target) for App, and URL text for Link — summary-only for Shopping/None | Tier B | P1 |
| G15 | Action screen: main + secondary action buttons render correctly — a single full-width button when only one action is available, a split button with an overflow menu when there are several | Tier B | P1 |

**Notes on G9–G13 (Preview screen), added after re-reading the screen post-redesign** (`PreviewReminderScreen.kt`,
`PreviewReminderState.kt`, `PreviewReminderViewModel.kt` — reflects `d0e7845c6`/PR #496, "Remove switch
from the reminder details screen"):
- **G9's row previously said "toggle switch"** — corrected above. The status control is now a dot +
  text + `FilledTonalButton` ("Turn on"/"Turn off"), not a `Switch`. The button's `enabled` is
  `status.canToggle`, which is `!removed` (`UiReminderStatus.kt`) — so a test for the removed-reminder
  case must reach Preview via the **Archive screen** row tap (`RemindersArchiveScreen.kt`), not Home,
  since Home never lists removed reminders (§G3).
- **`UiReminderStatus.title` is computed but never read by `StatusRow`** — the composable derives its
  text purely from `status.active` (`"Enabled"`/`"Disabled"`), even though `title` is separately
  `"Deleted"` for a removed reminder. A removed reminder therefore still reads "Disabled" on this
  screen, not "Deleted". Don't write a G9 assertion expecting `"Deleted"` text — that's not what
  renders today. Flagging this as a possible (unconfirmed) inconsistency rather than a bug to fix as
  part of test-writing.
- **G11 originally omitted the App target** (`UiReminderType.Kind.APP`, open-application action) —
  `targetInfoItems()` in `PreviewReminderScreen.kt` branches on it exactly like Call/SMS/Email/Link
  (resolved app name via `UiAppTarget.name`, falling back to the raw target string), so it belongs in
  this row's scope too. Note per §B5: the *builder* item that creates an App target is hidden above
  API 31 (`maxSdk = S`) — on this suite's real device (API 30) it's still creatable through the UI, so
  no repository-seeding workaround is needed for this one specifically, unlike most of G12 below.
- **G12's data sources mostly have no in-scope UI path to create them** (attachments = system file
  picker, Google Task/Calendar = OAuth-gated — same exclusions already applied to B11/B12/B13; places
  = location builder items, §A15–A17, still blank/not started; sub-tasks and note-linking *are*
  UI-buildable per B6/B10). The practical path for G12 tests is to seed the missing pieces directly
  through their repositories (`reminderV2Repository`/`noteRepository`/`googleTaskRepository`/
  `googleTaskListRepository`/`calendarEventRepository`, mirroring how B10 already seeds a `Note`
  directly) and use real UI only to navigate to and assert on Preview — consistent with this section's
  stated intent ("what a reminder actually *looks like* once it exists"), not re-proving the
  create-flow itself.

**Open question — resolve before writing a test for it, don't guess**: does `ReminderActionScreen`
need to suppress Snooze for location reminders the way the *fired system notification* already
does? `ReminderNotificationHandler.extraActions` excludes Snooze when `places.isNotEmpty()`, but
`GetReminderActionsUseCase` (which drives this in-app screen) filters only on
`isActive`/`isRemoved` — no location check at all. So a location reminder opened from its
notification shows no Snooze button, but the same reminder reached another way would. This may be
intentional (notification vs. in-app context genuinely differ) or a gap — confirm intent with
whoever owns this screen before treating either behavior as "correct."

**Two more render paths exist but are out of scope here**, consistent with how §F already treats
similar cases:
- **Calendar day view** (`feature/feature-calendar`) reuses the same `UiReminderListAdapter` output
  as Agenda (confirmed via `GetDayEventItemsUseCase`), so it's very likely covered by the same
  underlying adapter bug surface as G5/G7 — but its own rendering composable wasn't confirmed here
  and would need its own look before writing a row.
- **Home-screen widget list** (`extensions/appwidgets`) uses a completely separate,
  RemoteViews-based adapter (`UiReminderWidgetListAdapter`), not the Compose one everything else in
  this section shares — already out of scope per F7 ("actual home-screen widget rendering is out
  of scope — Maestro/Espresso can't drive the launcher").

---

## 6. Rollout suggestion

1. ~~Land the Gradle/module wiring (§2) and the GitHub Actions job (§3) with just **A1–A4, A6, A9,
   A12** (one Tier-B test per core recurrence family) and **D1–D4** (quiet-hours) to prove the
   pipeline end-to-end before writing the full matrix.~~ **Done.**
2. Fill in the rest of §A (edge cases) and §C (notification hierarchy) next — these are the areas
   explicitly called out as needing coverage and are where `RecurrenceRuleCalculator` and the
   Settings/Group/Reminder override resolution have the most surface area for regressions.
   **Partially done**: §A's edge cases and most of its P0/P1 rows are covered (see the Status
   column in §5), including A25 now, other than location (A15–A17), custom RRULE (A18–A24,
   explicitly deferred by project decision), and a handful of others (A27 skipped, A28, A29).
   **§C (notification hierarchy) hasn't been started at all** — still deferred by the same
   decision as the RRULE rows, waiting to be picked up together in one batch per §4's wiring
   caveat.
3. §B/§E/§F can land incrementally as each screen stabilizes, especially given `feature-reminder`
   and the settings feature module are both still under active extraction into their own Gradle
   modules on this branch — a screen's exact package/module home may move before these tests are
   written. **Most of §B is now done** (see its Status column), including B6's remove case, B8
   (tags), and B17 (description) — B12/B13 (Google linkage) and the file/app-picker rows (B5, B11)
   remain, all already flagged as needing external accounts/devices this CI setup doesn't have.
   **§E and §F haven't been started.**
4. **New, not in the original plan**: §G below (reminder appearance across Home, list, Preview,
   and the notification Action screen) — added once enough of §A/§B/§D existed to reveal this as
   a real gap: none of the tests so far assert on what a reminder actually *looks like* once
   created, only on what got persisted. Not yet started.

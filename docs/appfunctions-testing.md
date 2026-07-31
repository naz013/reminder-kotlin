# Testing the Gemini AppFunctions integration

## What this covers, and what it doesn't

The `:appfunctions` module (see [architecture.md](architecture.md)) has never been run on a real
device or against real Gemini in this environment — everything so far is verified at the
compile/lint/manifest-merge level only (`./gradlew :appfunctions:test`, `:appfunctions:detekt`,
`:app:compileProDebugKotlin`, and manually inspecting the merged manifest). This document is the
missing piece: how to actually exercise the functions on a device, without needing real Gemini
(which is still in private preview and may not be available to you).

Three layers, cheapest first:

1. **Unit tests** — already exist, run in seconds, no device needed. Covers the use-case logic
   (date math, field mapping, error-vs-success branching).
2. **`adb` invocation** — runs a function exactly as the OS would, through the real
   `AppFunctionService`, on a real/emulated device. This is the layer that actually proves the
   feature works, and doesn't require Gemini at all.
3. **Real Gemini** — the actual end-to-end experience. Only possible if your device/account has
   the (currently private-preview) Gemini AppFunctions integration enabled.

Commands below are given for **macOS/Linux/Git Bash** and **Windows PowerShell**. The differences
are mechanical, not semantic:

- Gradle: `./gradlew` (Unix) vs `.\gradlew.bat` (Windows) — from `cmd.exe` instead drop the leading
  `.\`.
- Line continuation in the longer `adb` commands: trailing `\` (Unix) vs trailing `` ` `` (PowerShell,
  must be the last character on the line, no trailing spaces after it). You can also just delete the
  continuation characters and put each command on one line in either shell — it's exactly equivalent.
- Piping to a text filter: `grep` (Unix) vs `Select-String` (PowerShell) — `Select-String` is
  case-insensitive by default, matching bash `grep -i`.
- `adb` itself and the JSON `--parameters '...'` strings are identical either way; `adb.exe` is a
  plain executable, not shell-specific, and accepts forward-slash paths fine on Windows too.

## Prerequisites

- **A device or emulator running Android 16 (API 36) or newer.** The `AppFunctionService` classes
  are `@RequiresApi(36)` and won't be indexed by the OS below that. If you're on an emulator, use a
  **Google Play** system image, not AOSP — the `app_function` shell service and AppSearch indexing
  are Play-system-image features.
  - Note: some Google documentation states the `adb shell cmd app_function` debug commands
    specifically require **Android 17**. If `list-app-functions` below returns
    `Can't find service: app_function` on an API 36 device, this is the likely reason — try a newer
    system image before assuming something in this app is broken.
- **The PRO flavor**, since AppFunctions is gated to `BuildInfo.isPro` and the `:appfunctions`
  module isn't even compiled into the `free` APK (see "Flavor-gated modules" in
  [architecture.md](architecture.md)).
- **For Google Tasks functions only**: sign in under the app's Google Tasks screen first
  (`GoogleTasksAuthManager.isAuthorized()` gates those three functions — see
  [BaseGoogleTaskAppFunctionService.kt](../appfunctions/src/main/kotlin/com/github/naz013/appfunctions/BaseGoogleTaskAppFunctionService.kt)).
  Without it you should get a clean `AppFunctionNotSupportedException`, not a crash — that's an
  intentional path to test, not just a precondition.

## 1. Unit tests

macOS/Linux/Git Bash:

```bash
./gradlew :appfunctions:test
```

Windows PowerShell:

```powershell
.\gradlew.bat :appfunctions:test
```

Runs all 24 use-case tests (reminders, notes, birthdays, Google Tasks) with MockK — no device
needed. Run this after any change to `appfunctions/src/main/kotlin/**` before moving on to on-device
testing; it's the fast feedback loop and will catch most logic bugs before you burn time on `adb`.

Also worth running for anything else you touch in this area:

```bash
./gradlew :appfunctions:detekt
./gradlew :app:testProDebugUnitTest
```

```powershell
.\gradlew.bat :appfunctions:detekt
.\gradlew.bat :app:testProDebugUnitTest
```

## 2. Build and install the PRO debug build

macOS/Linux/Git Bash:

```bash
./gradlew :app:assembleProDebug
adb install -r app/build/outputs/apk/pro/debug/app-pro-debug.apk
```

Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleProDebug
adb install -r app\build\outputs\apk\pro\debug\app-pro-debug.apk
```

The PRO applicationId is `com.cray.software.justreminderpro` (see `app/build.gradle.kts`,
`productFlavors.pro`) — that's the `--package` value for every `adb` command below.

## 3. Confirm the functions are registered

macOS/Linux/Git Bash:

```bash
adb shell cmd app_function list-app-functions | grep com.cray.software.justreminderpro
```

Windows PowerShell:

```powershell
adb shell cmd app_function list-app-functions | Select-String com.cray.software.justreminderpro
```

This should list all 10 functions across the four services. If it comes back empty:

- Confirm the app was actually installed as the **pro** build:
  - Bash: `adb shell pm list packages | grep justreminder`
  - PowerShell: `adb shell pm list packages | Select-String justreminder`

  should show `com.cray.software.justreminderpro`, not the free `com.cray.software.justreminder`.
- Confirm the device is API 36+ with a Google Play system image (see Prerequisites).
- Check `assets/reminder_app_function_service.xml`, `note_app_function_service.xml`,
  `birthday_app_function_service.xml`, `google_task_app_function_service.xml` exist inside the APK
  — these are generated by the KSP compiler at build time (`appFunctionXmlFileName` in each
  `@AppFunctionServiceEntryPoint`); if they're missing, the KSP step didn't run or its output wasn't
  packaged.
  - Bash: `unzip -l app-pro-debug.apk | grep app_function_service`
  - PowerShell (no native `unzip`, but Windows 10+ ships `tar.exe`, which reads zip-based archives
    including APKs): `tar -tf app-pro-debug.apk | Select-String app_function_service`
- Watch for indexing errors while relaunching the app:
  - Bash: `adb logcat | grep -i appfunction`
  - PowerShell: `adb logcat | Select-String appfunction`

## 4. Invoke a function directly with `adb`

This is the important part — it exercises the real `AppFunctionService`, argument marshalling, and
your validation/exception logic, all without Gemini.

macOS/Linux/Git Bash:

```bash
adb shell cmd app_function execute-app-function \
    --package com.cray.software.justreminderpro \
    --function '<SERVICE_CLASS>#<FUNCTION_NAME>' \
    --parameters '<JSON>'
```

Windows PowerShell:

```powershell
adb shell cmd app_function execute-app-function `
    --package com.cray.software.justreminderpro `
    --function '<SERVICE_CLASS>#<FUNCTION_NAME>' `
    --parameters '<JSON>'
```

`<SERVICE_CLASS>` is the **generated** service class (the `serviceName` passed to
`@AppFunctionServiceEntryPoint`, not the hand-written `Base*` class), fully qualified. `<JSON>`
field names must match the `@AppFunctionSerializable` param class's Kotlin property names exactly.

> `java.time.LocalDateTime`/`LocalDate` params (`dueDateTime`, `date`) are shown below as ISO-8601
> strings (`"2026-08-01T09:30:00"` / `"2026-08-01"`), which is the conventional wire format — but
> this hasn't been confirmed against the real marshaller in this environment. If a call fails with
> a parse/type error on the date field, that's the first thing to re-check; `list-app-functions`
> output includes the generated parameter schema per function and is the fastest way to confirm the
> exact expected shape.

The rest of this section gives the macOS/Linux form for brevity — swap `\` line-continuations for
`` ` `` (or drop them and put each command on one line) to run the same thing from PowerShell.

### Reminders — `com.github.naz013.appfunctions.ReminderAppFunctionService`

```bash
# createReminder
adb shell cmd app_function execute-app-function \
    --package com.cray.software.justreminderpro \
    --function 'com.github.naz013.appfunctions.ReminderAppFunctionService#createReminder' \
    --parameters '{"title": "Pay rent", "dueDateTime": "2026-08-01T09:30:00", "notes": "before 5pm"}'

# listUpcomingReminders
adb shell cmd app_function execute-app-function \
    --package com.cray.software.justreminderpro \
    --function 'com.github.naz013.appfunctions.ReminderAppFunctionService#listUpcomingReminders' \
    --parameters '{"withinDays": 7}'

# completeReminder / deleteReminder - id comes from the createReminder or listUpcomingReminders result
adb shell cmd app_function execute-app-function \
    --package com.cray.software.justreminderpro \
    --function 'com.github.naz013.appfunctions.ReminderAppFunctionService#completeReminder' \
    --parameters '{"id": "<uuid-from-a-previous-result>"}'

adb shell cmd app_function execute-app-function \
    --package com.cray.software.justreminderpro \
    --function 'com.github.naz013.appfunctions.ReminderAppFunctionService#deleteReminder' \
    --parameters '{"id": "<uuid-from-a-previous-result>"}'
```

PowerShell, single-line form (easiest to copy-paste one at a time):

```powershell
adb shell cmd app_function execute-app-function --package com.cray.software.justreminderpro --function 'com.github.naz013.appfunctions.ReminderAppFunctionService#createReminder' --parameters '{"title": "Pay rent", "dueDateTime": "2026-08-01T09:30:00", "notes": "before 5pm"}'

adb shell cmd app_function execute-app-function --package com.cray.software.justreminderpro --function 'com.github.naz013.appfunctions.ReminderAppFunctionService#listUpcomingReminders' --parameters '{"withinDays": 7}'

adb shell cmd app_function execute-app-function --package com.cray.software.justreminderpro --function 'com.github.naz013.appfunctions.ReminderAppFunctionService#completeReminder' --parameters '{"id": "<uuid-from-a-previous-result>"}'

adb shell cmd app_function execute-app-function --package com.cray.software.justreminderpro --function 'com.github.naz013.appfunctions.ReminderAppFunctionService#deleteReminder' --parameters '{"id": "<uuid-from-a-previous-result>"}'
```

After `createReminder`, open the app's reminder list and confirm it actually shows up there — the
`adb` call proves the AppFunction layer works, but the point is that it's the same
`ReminderV2Repository` the UI reads from, so this is also your end-to-end proof that the plumbing
is real and not just returning a fake success.

### Notes — `com.github.naz013.appfunctions.NoteAppFunctionService`

```bash
adb shell cmd app_function execute-app-function \
    --package com.cray.software.justreminderpro \
    --function 'com.github.naz013.appfunctions.NoteAppFunctionService#createNote' \
    --parameters '{"title": "Wi-Fi password", "content": "hunter2"}'

adb shell cmd app_function execute-app-function \
    --package com.cray.software.justreminderpro \
    --function 'com.github.naz013.appfunctions.NoteAppFunctionService#searchNotes' \
    --parameters '{"query": "wifi"}'
```

PowerShell:

```powershell
adb shell cmd app_function execute-app-function --package com.cray.software.justreminderpro --function 'com.github.naz013.appfunctions.NoteAppFunctionService#createNote' --parameters '{"title": "Wi-Fi password", "content": "hunter2"}'

adb shell cmd app_function execute-app-function --package com.cray.software.justreminderpro --function 'com.github.naz013.appfunctions.NoteAppFunctionService#searchNotes' --parameters '{"query": "wifi"}'
```

### Birthdays — `com.github.naz013.appfunctions.BirthdayAppFunctionService`

```bash
adb shell cmd app_function execute-app-function \
    --package com.cray.software.justreminderpro \
    --function 'com.github.naz013.appfunctions.BirthdayAppFunctionService#createBirthday' \
    --parameters '{"name": "Ada", "date": "1999-10-03", "ignoreYear": false}'

adb shell cmd app_function execute-app-function \
    --package com.cray.software.justreminderpro \
    --function 'com.github.naz013.appfunctions.BirthdayAppFunctionService#listUpcomingBirthdays' \
    --parameters '{"withinDays": 30}'
```

PowerShell:

```powershell
adb shell cmd app_function execute-app-function --package com.cray.software.justreminderpro --function 'com.github.naz013.appfunctions.BirthdayAppFunctionService#createBirthday' --parameters '{"name": "Ada", "date": "1999-10-03", "ignoreYear": false}'

adb shell cmd app_function execute-app-function --package com.cray.software.justreminderpro --function 'com.github.naz013.appfunctions.BirthdayAppFunctionService#listUpcomingBirthdays' --parameters '{"withinDays": 30}'
```

Worth a dedicated check: create a birthday on **Feb 29** with a past leap year, then
`listUpcomingBirthdays` around a non-leap Feb/Mar boundary — this exercises
`BirthdayDateCalculator.getNextOccurrence` (see [architecture.md](architecture.md) /
`ListUpcomingBirthdaysUseCase`), which exists specifically to get that edge case right.

### Google Tasks — `com.github.naz013.appfunctions.GoogleTaskAppFunctionService`

Requires being signed in under the app's Google Tasks screen first (see Prerequisites).

```bash
adb shell cmd app_function execute-app-function \
    --package com.cray.software.justreminderpro \
    --function 'com.github.naz013.appfunctions.GoogleTaskAppFunctionService#createGoogleTask' \
    --parameters '{"title": "Buy milk", "notes": "2%", "dueDateTime": "2026-08-01T09:00:00"}'

adb shell cmd app_function execute-app-function \
    --package com.cray.software.justreminderpro \
    --function 'com.github.naz013.appfunctions.GoogleTaskAppFunctionService#listGoogleTasks' \
    --parameters '{"includeCompleted": false}'

adb shell cmd app_function execute-app-function \
    --package com.cray.software.justreminderpro \
    --function 'com.github.naz013.appfunctions.GoogleTaskAppFunctionService#completeGoogleTask' \
    --parameters '{"id": "<taskId-from-createGoogleTask-result>"}'
```

PowerShell:

```powershell
adb shell cmd app_function execute-app-function --package com.cray.software.justreminderpro --function 'com.github.naz013.appfunctions.GoogleTaskAppFunctionService#createGoogleTask' --parameters '{"title": "Buy milk", "notes": "2%", "dueDateTime": "2026-08-01T09:00:00"}'

adb shell cmd app_function execute-app-function --package com.cray.software.justreminderpro --function 'com.github.naz013.appfunctions.GoogleTaskAppFunctionService#listGoogleTasks' --parameters '{"includeCompleted": false}'

adb shell cmd app_function execute-app-function --package com.cray.software.justreminderpro --function 'com.github.naz013.appfunctions.GoogleTaskAppFunctionService#completeGoogleTask' --parameters '{"id": "<taskId-from-createGoogleTask-result>"}'
```

Unlike the other three services, `createGoogleTask`/`completeGoogleTask` make a **live network
call** to the real Google Tasks API before returning (see the "Sync behavior" rationale in the
`CreateGoogleTaskUseCase`/`CompleteGoogleTaskUseCase` KDoc) — after running these, check the actual
Google Tasks app or tasks.google.com to confirm the task really landed there, not just in this
app's local database.

**Negative test worth running deliberately**: sign out of Google Tasks in the app, then run
`createGoogleTask` again — expect a clean `AppFunctionNotSupportedException` (visible in the `adb`
output as an error, not a crash or a silently-created-but-unsynced task).

## 5. Confirm PRO/FREE gating

macOS/Linux/Git Bash:

```bash
./gradlew :app:assembleFreeDebug
adb install -r app/build/outputs/apk/free/debug/app-free-debug.apk
adb shell cmd app_function list-app-functions | grep com.cray.software.justreminder
```

Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleFreeDebug
adb install -r app\build\outputs\apk\free\debug\app-free-debug.apk
adb shell cmd app_function list-app-functions | Select-String com.cray.software.justreminder
```

Should return **nothing** for the free package (`com.cray.software.justreminder`, no `pro` suffix)
— the `:appfunctions` module isn't compiled into that APK at all. This was already confirmed at
build time (`grep -c "AppFunctionService\"" ` against the merged manifest returned `0`), but
confirming it on-device closes the loop on the one thing static analysis can't fully prove: that
the OS itself sees no functions for that package.

## 6. Testing the Settings screen

Manual, no `adb` needed: Settings → Other → "Gemini functions" (only visible on the PRO build).
Confirm it opens [app_functions.html](../app/src/main/assets/files/app_functions.html) and renders
correctly in both light and dark mode (Settings → General → theme, or system dark mode toggle).

## 7. Testing with real Gemini

Only possible if your device/Google account has access to the private-preview Gemini AppFunctions
integration — this is out of this project's control. If you do have access: install the PRO build,
make sure Gemini is set as the assistant, and try prompts like the ones listed in
[app_functions.html](../app/src/main/assets/files/app_functions.html) ("Remind me to call the
dentist tomorrow at 9am", "What's on my reminders for the next week?"). If Gemini doesn't pick up
the functions, `adb shell cmd app_function list-app-functions` (step 3) is still the right first
diagnostic — if the OS doesn't see the functions, Gemini won't either, and the problem isn't
Gemini-specific.

## Known gaps

- No instrumented/Robolectric tests exist for the `Base*AppFunctionService` classes themselves
  (argument validation, exception mapping) — only the use cases underneath them are unit-tested.
  The `adb execute-app-function` flow above is currently the only way to exercise that layer.
- The exact JSON wire format for `java.time.LocalDateTime`/`LocalDate` params is a best guess (see
  the callout in section 4), not confirmed against a real device.
- `AppFunctionManagerCompat.setAppFunctionEnabled(...)` (runtime enable/disable) was deliberately
  not implemented — functions are enabled by default and rely on the PRO/FREE module split plus the
  in-code `BuildInfo.isPro`/`GoogleTasksAuthManager.isAuthorized()` checks for gating.

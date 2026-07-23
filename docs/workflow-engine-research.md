# Local-Only Workflow Automation: Research and Proposed Architecture

This documents the research behind adding "workflow" automations (trigger → condition → action
rules, like a mini Tasker/IFTTT) on top of `ReminderV2`/`GroupV2`, entirely on-device — no backend,
no third-party automation service, consistent with the app's privacy-first positioning. It exists
so the eventual implementation plan (and whoever picks up a specific workflow later) starts from a
shared understanding of what already exists and what doesn't.

## Constraint: what "local-only" rules out

Every trigger must be an in-process event, a local OS signal (time, location, connectivity/battery,
or another app's intent), and every action must be a local write or a local Android API call. This
rules out the entire Zapier/IFTTT-as-a-service model (cloud webhooks, a rules server, third-party
automation platforms) — the workflow engine's job is to compose primitives that already exist
inside the app or the OS, never to call out to the network for automation logic. The app's existing
cloud backup (Drive/Dropbox) is dumb file storage the user explicitly triggers — it is not, and
must not become, a workflow trigger or action source.

## What already exists (audited before designing anything new)

### 1. A write-event bus exists, wired into every repository, with no real subscriber

- [`TableChangeNotifier`](../repository/src/main/java/com/github/naz013/repository/observer/TableChangeNotifier.kt) —
  every `RepositoryImpl.save()/delete()/updateSyncState()` (including `ReminderV2RepositoryImpl`,
  `GroupV2RepositoryImpl`) calls `tableChangeNotifier.notify(table)`, broadcasting the changed
  [`Table`](../repository-api/src/main/java/com/github/naz013/repository/table/Table.kt) enum value
  via `LocalBroadcastManager`.
- [`TableChangeListenerFactory`](../repository-api/src/main/java/com/github/naz013/repository/observer/TableChangeListenerFactory.kt) /
  `TableChangeListener` — the subscribe-side interface, implemented in
  `repository/src/main/java/com/github/naz013/repository/observer/TableChangeListenerFactoryImpl.kt`.
- **The only consumer, `observeTable()` in `app/src/main/java/com/elementary/tasks/core/data/CoroutineScopeExtensions.kt`, has zero call sites anywhere in the app.** This is real, tested infrastructure sitting unused — the natural trigger source for "when a ReminderV2/GroupV2 row changes," not something to reinvent.
- **Caveat**: `LocalBroadcastManager` is in-process only and non-durable. If the app process is dead
  when a write happens, nothing observes it. Any trigger that must survive process death (e.g.
  "reminder snoozed 3 times" tracked across app restarts) needs a periodic re-check, not just this
  bus alone.

### 2. Action execution has a reusable, generic facade

- [`BackgroundTask`](../work-api/src/main/kotlin/com/github/naz013/workapi/BackgroundTask.kt) —
  `fun interface BackgroundTask { suspend fun run(input: TaskData, progress: TaskProgressReporter): TaskResult }`.
- [`WorkScheduler`](../work-api/src/main/kotlin/com/github/naz013/workapi/WorkScheduler.kt) — facade
  over WorkManager: `enqueue`/`enqueueUnique`/`enqueuePeriodic`, `cancelByTag`/`cancelUniqueWork`,
  `observeUniqueWork`. [`WorkRequest`](../work-api/src/main/kotlin/com/github/naz013/workapi/WorkRequest.kt)/
  [`PeriodicWorkRequest`](../work-api/src/main/kotlin/com/github/naz013/workapi/PeriodicWorkRequest.kt)
  carry `networkRequirement` ([`NetworkRequirement`](../work-api/src/main/kotlin/com/github/naz013/workapi/NetworkRequirement.kt):
  `NONE/CONNECTED/UNMETERED/METERED`) and `requiresBatteryNotLow`.
- One concrete Worker (`work/src/main/kotlin/com/github/naz013/work/GenericTaskWorker.kt`) resolves
  the actual `BackgroundTask` by a Koin DI qualifier (`taskKey`) — already used identically for
  cloud sync, birthday checks, and Google Tasks sync (12 registrations across the app). This is
  proven, generic "run this named unit of deferred/constrained work" scaffolding — reuse it for any
  workflow action that needs deferral, retry, or connectivity/battery constraints.

### 3. A small existing action vocabulary to model after, not to reuse directly

[`ResolvedEventAction`](../app/src/main/java/com/elementary/tasks/eventaction/ResolvedEventAction.kt)
(sealed: `MakeCall`, `SendSms`, `OpenApp`, `OpenLink`, `SendEmail`) plus
`ResolveReminderEventActionUseCase` → `DispatchEventActionUseCase` is the closest thing to a
reusable "action" building block today. It's reminder-type-specific and only 5 cases, but the shape
— sealed action type, a resolver, a dispatcher — is the right template for a workflow engine's
(much broader) action vocabulary.

### 4. No rule/condition engine exists at all

The only "condition → behavior" logic anywhere in the codebase is
[`DoNotDisturbManager`](../app/src/main/java/com/elementary/tasks/core/utils/datetime/DoNotDisturbManager.kt)'s
single hardcoded priority-threshold check. `RecurrenceRule` models *when* a reminder fires;
nothing today models *what happens as a consequence* of an event. A grep for
`trigger|automation|rule|workflow|condition` across the codebase turns up only unrelated UI-menu
"trigger" terminology and doc-comment prose — no groundwork to build on.

### 5. Location "geofencing" is homemade distance polling, not the Android Geofencing API

`GeolocationService` (foreground service) + `LocationTracker` (dual legacy-`LocationManager`/
`FusedLocationProviderClient` polling) + `CheckLocationReminderUseCase` (plain `Location.distanceTo()`
math against every active GPS-type reminder on every location update). No `GeofencingClient` usage
anywhere. A location-triggered workflow either extends this existing polling loop (cheap — it
already fans out over every reminder on each update) or introduces real Play Services geofencing as
genuinely new surface area.

## Proposed architecture

```
domain             WorkflowRule, WorkflowTrigger (sealed), WorkflowCondition, WorkflowAction (sealed)
repository-api      WorkflowRuleRepository interface
repository          WorkflowRuleEntity/Dao/RepositoryImpl (Room), own migration
usecase:workflows   WorkflowEngine (evaluate + dispatch), one usecase per trigger-check
app                 TableChangeListener-based reactive trigger wiring, a periodic BackgroundTask
                    for polling-style conditions, action executors (some inline writes, some
                    routed through WorkScheduler/BackgroundTask)
```

- **`WorkflowRule`** (domain): `trigger: WorkflowTrigger`, `condition: WorkflowCondition?`,
  `action: WorkflowAction`, plus a scope field mirroring the 3-level hierarchy already built for
  notifications — a rule can attach to a specific `ReminderV2`, to a `GroupV2` (applies to every
  reminder in that group), or be global. This isn't a new concept to invent: it's the same
  "reminder ?: group ?: global" instinct from the notification-customization hierarchy, applied to
  *which rules apply* instead of *which settings apply*. See
  [reminderv2-notification-hierarchy.md](reminderv2-notification-hierarchy.md) for that prior art.
- **`WorkflowTrigger`** (sealed): `ReminderCompleted`, `ReminderSnoozedNTimes(count)`,
  `GroupAllCompleted`, `LocationEntered`/`LocationExited`, `ScheduleReached`, `AgeExceeded(days)`
  (for archival-style rules) — extensible, one variant per distinct triggering event.
- **`WorkflowAction`** (sealed): `SetNotificationOverride(fields)`, `CreateReminder(template)`,
  `ArchiveReminder`, `CompleteReminder`, `RunBackgroundTask(taskKey, input)`, … — the broader,
  decoupled successor to `ResolvedEventAction`.
- **Trigger sourcing**: reactive triggers (reminder/group state changes) subscribe via
  `TableChangeListenerFactory` — finally giving it a real subscriber. Polling-style triggers
  (snooze counts, age-based archival, anything that must survive process death) run as a periodic
  `BackgroundTask` via `WorkScheduler.enqueuePeriodic`. Location triggers extend
  `CheckLocationReminderUseCase`'s existing per-update fan-out rather than duplicating a second
  distance-check loop.
- **Execution**: a `WorkflowEngine` use case evaluates a rule's condition against current
  `ReminderV2`/`GroupV2` state and either applies the action inline (e.g., writing an override
  field through the existing repositories) or enqueues it via `WorkScheduler`/`BackgroundTask` when
  the action itself needs deferral or constraints (e.g., "back this up" — though per the local-only
  constraint above, no workflow action should ever target the cloud backup path automatically).

## Candidate workflow catalog

Every idea below is expressible as one `WorkflowRule` (trigger + optional condition + action) in the
architecture above — this list is the target surface for the phased implementation plan, not a
flat backlog to build in one pass.

**Reminder lifecycle**
- Chained/dependent reminders — completing one auto-activates the next
- Escalation on repeated snooze/dismiss — bumps `priority`/`bypassDoNotDisturb`/`wakeScreen` via the
  existing `NotificationSettingsOverride` hierarchy
- Auto-archive completed reminders after N days

**Group-level**
- "All reminders in group completed" → mark group inactive / notify
- Bulk group action ("vacation mode") — temporary group-level notification override
- Rule-based auto-grouping by keyword/tag match (pure local string matching)

**Location** (reusing the existing distance-polling service)
- Arrive home → auto-complete a shopping-list reminder
- Enter/leave a geofence → temporarily swap a group's notification override

**Notification/escalation**
- Unacknowledged high-priority reminder for N minutes → bypass DND
- Auto-apply a group's quiet hours during a schedule window

**System integration** (still fully local)
- Widgets/App Shortcuts for one-tap complete/snooze
- Tasker plugin intents — lets privacy-conscious power users build their own local automations
  without the app needing to build every rule itself
- Wear OS complications via the on-device Wearable Data Layer API

**Privacy-specific data hygiene**
- Auto-purge completed reminders (and attached places/shopping data) older than N days
- Auto re-lock (PIN/biometric) after backgrounding
- Fully-local weekly completion summary notification

## What this document is not

This is the research/architecture reference, not the implementation plan. See the plan file for
the phased build-out (foundation engine first, then individual workflows layered on top, roughly
ordered by how much new infrastructure each needs beyond what's audited above).

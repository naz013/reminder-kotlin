# Chained reminders

A reminder can be set to **start after another reminder**. It is saved inactive ("waiting"), and when its
predecessor is completed it becomes active and is scheduled normally. Works in the free and PRO flavors and is
independent of the workflow engine (the workflow `ActivateReminder` action is untouched).

## Data

`ReminderV2.startAfterReminderId: String?` (null = not chained). It is carried through every layer that owns a
copy of the reminder:

- Room: `ReminderV2Entity` (indexed column), `ReminderV2Mapper`, `MIGRATION_44_45`, `AppDb` version 45.
- Backup and cloud sync: `ReminderV2Json.startAfterReminderId` (null default for old backups) and both
  directions in `DataConverterImpl`.
- `ReminderV2Repository.getByStartAfterReminderId(predecessorId)` returns the waiting successors.

## Logic (`logic:logic-reminder`)

- `ReminderChainValidator`: pure cycle check (`wouldCreateCycle`) for self references and loops of any depth.
- `ResolveChainedStartUseCase`: decides whether a link is usable. It keeps the link and forces `isActive = false`
  while the predecessor exists, is not archived, and is active or itself waiting. Otherwise (missing, archived,
  finished, self reference, cycle) the link is dropped and the reminder behaves normally. Both
  `SaveReminderUseCase` and `ActivateReminderUseCase` call it, so no entry point can persist an unlinked-looking
  waiting reminder or schedule one by mistake.
- `ActivateChainedRemindersUseCase(predecessorId)`: the single place that starts successors. For each one it
  clears the link (one-shot) and calls `ActivateReminderUseCase` with `startAnyway = true`. A recurring successor
  whose time is stale first gets its next occurrence recalculated; a one-time successor whose date has already
  passed fires immediately. Archived successors are only unlinked.
- Callers: `CompleteReminderUseCase` (every completion path, including the notification Done action, snooze with
  a non-positive time, the action dialog and the workflow `CompleteReminder` action), `DeleteReminderUseCase`,
  `DeleteAllReminderUseCase`, `MoveReminderToArchiveUseCase`, and the App Functions complete and delete use
  cases. A recurring predecessor starts its successors on the first completion only, because the link is cleared.
- Toggling a waiting reminder on manually ("start now") clears the link first.
- `FindStartAfterCandidatesUseCase`: candidates for the builder picker (active or waiting, not archived, not
  itself, no cycle). `ResolveWaitingTitlesUseCase`: predecessor titles for list rows.

## UI

- Builder (`feature:feature-reminder`): "Start after another reminder" row with a picker sheet and clear button.
  State and filtering live in `BuildReminderViewModel`; saving a cycle is rejected with an error message.
- Lists (`ui:ui-reminder`, Agenda): a "Waiting for <title>" tag from `UiReminderListAdapter.createV2`. Waiting
  reminders are inactive, so they are never overdue and are absent from the active Today and calendar queries.
- Preview screen: a "Waiting for <title>" detail row; the overdue timing rows are hidden.

## Known limits

- A manually paused (toggled-off) predecessor with no link counts as finished, so a link to it is dropped on save.
- Group, tag, archive and calendar agenda lists show no "Waiting for" tag; only the Agenda list and the preview do.
- Cloud sync, restore and group deletion remove reminders without releasing their successors.
- A waiting reminder is parked with a plain save and its alarms cancelled; its Google Task export is not completed until it is later activated.

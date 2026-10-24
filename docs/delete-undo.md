# Delete / archive with undo

Policy: reversible deletes, archives and unarchives show an undo Snackbar with no confirm dialog.
Confirm only for delete-all and for deleting a group or tag that is in use. The helper below works with or without a prior
confirm. Undo of an archive/unarchive leaves the item as it was, since the database is only touched
on commit.

## Pieces

- `UndoDeleteController` (`core:feature-common`) - owns a `PendingDeleteTracker`. Exposes
  `pendingIds: StateFlow<Set<String>>`, `undoEvents: Flow<UndoEvent(batchKey, message)>`,
  `markPending(batchKey, ids, message, onCommit)`, `undo(batchKey)`, `commit(batchKey)` and
  `commitAllDetached(dispatcher)`.
- `UndoDeleteHost` (`ui:ui-common`, next to `UndoSnackbarDispatcher`) - composable that shows the
  Snackbar for each `UndoEvent`; action tap calls `undo`, timeout calls `commit`.

## Usage in a list screen

1. ViewModel: `val undoDelete = UndoDeleteController(viewModelScope, dispatcherProvider.io())`.
2. Combine `undoDelete.pendingIds` into the list flow and filter those ids out.
3. On delete/archive (after the confirm, if one is required) call `undoDelete.markPending(...)` with the
   real repository work in `onCommit`.
4. `override fun onCleared() { undoDelete.commitAllDetached(dispatcherProvider.io()) }`.
5. Screen: `UndoDeleteHost(controller = viewModel.undoDelete, snackbarHostState = snackbarHostState)`.

Migrated screens: Agenda, Birthdays, Reminders archive, Notes (delete and archive/unarchive, single and
bulk; list and archive each own a controller and a `SnackbarHostState`), Places list (undo only, no
confirm), Tags list and Groups list (confirm only when in use, cascade runs on commit), Workflow rules (gallery,
group and reminder scopes; undo only, no confirm). Preview/edit screens are covered in the section
below.

Bulk selection flows: see [multiselect.md](multiselect.md).

Cascade/picker caveat: pending groups/tags are only hidden from their own list. Pickers elsewhere may still
show them during the undo window, and a reminder assigned to a pending group in that window loses the group
when the delete commits.

Workflow caveat: a pending-deleted rule may still fire during the undo window (the engine reads rules live).

## Deleting from preview/edit screens

A delete started on a preview/edit screen must survive that screen's ViewModel being cleared and show
its undo Snackbar on the list the user returns to. Screens use an app-scoped coordinator instead of
their own controller.

- `PendingDeleteCoordinator` (`core:feature-common`, Koin `single`) - one `UndoDeleteController` per
  `PendingEntity` (REMINDER, BIRTHDAY, NOTE, GROUP, TAG, PLACE), so types stay independent. API:
  `controller(type)`, `pendingIds(type)`, `markPending(type, id, message, onCommit)`, `commitAll()`,
  `hostCount(type)`, `registerHost(type)`, `unregisterHost(type)`. The batch key is the item id.
- `PendingDeleteHost(coordinator, type, snackbarHostState)` (`ui:ui-common`) - wraps `UndoDeleteHost`
  for the type's controller and registers itself in `hostCount(type)`, so a fallback host can tell
  that no list is showing undo UI for that type. Use one host per type.
- `PendingDeleteFallbackHost(coordinator)` (`ui:ui-common`, composed once around `NavDisplay` in
  `AppNavGraph`) - shows the undo Snackbar for a type only while no `PendingDeleteHost` is registered for
  it (`fallbackActive(type)`: zero hosts for `FALLBACK_SETTLE_MS`, so the list under a popped preview can
  register first). Covers previews opened from Home, the Calendar side panel or a details screen.
- `pendingIdsOf(vararg types)` merges pending ids when a screen lists several entity kinds.
- Watchdog: `markPending` also starts a timer of `UNDO_WINDOW_MS + UNDO_WATCHDOG_GRACE_MS` that commits
  the exact event if nobody resolved it (no host ever appeared). Commit is idempotent. `UNDO_WINDOW_MS`
  (4 s) mirrors `SnackbarDuration.Short` used by `UndoSnackbarDispatcher`; keep them in sync.
  There is one watchdog job per event, cancelled as soon as the event is undone or committed.
  `UndoDeleteHost` dismisses its shown Snackbar when the controller's `latestEvent` becomes null
  (resolved by the watchdog or `commitAll()`), so a stale Undo button never lingers.
- Commit on background: `ReminderApp` observes `ProcessLifecycleOwner` and calls `commitAll()` on
  `ON_STOP`, so no delete stays hidden-but-uncommitted if the process is killed.
- Navigation: `MutableList<NavKey>.popDeleted(isSameItem)` (`core:navigation-api`) removes trailing
  back stack entries of the deleted item (always keeping one) so the user lands on the list.

### Usage in a preview/edit screen

1. ViewModel: inject `PendingDeleteCoordinator` and call `markPending(type, id, message, onCommit)` with
   the real delete in `onCommit`; the lambda outlives the ViewModel. Guard with
   `id in coordinator.pendingIds(type).value` so a repeat tap does nothing, then emit a `Deleted(id)` event.
   Preview screens that load a single item skip updates while the id is pending.
2. Nav graph: on `Deleted(id)` call `backStack.popDeleted { key -> key is Preview/Edit/Details && key.id == id }`.
3. The list the user lands on: expose `val pendingDeleteCoordinator` from its ViewModel, filter
   `pendingIds(type)` out of the list flow (next to its own `undoDelete` filter) and compose a
   `PendingDeleteHost` with the list's `SnackbarHostState`.
4. Any other screen that can show the item (Home, Agenda, tag/group details) filters the same ids out;
   it needs no host, the root fallback covers it.

Filtered besides the owning lists: Agenda (reminders, birthdays; also hosts both), Home schedule and
day events (reminders, birthdays), tag details (reminders, notes, birthdays) and group details
(reminders). Not filtered: Calendar (month, timeline, history) and widgets - a deleted item stays
visible there until its undo window closes, then disappears on the next data change.

Migrated: Reminder preview (archived reminders only; moving an active reminder to the archive keeps its
confirm), Birthday preview and edit, Note preview and edit, Group edit and details, Tag edit and details,
Place edit. The rest delete without a confirm. Group and tag screens (list, details, edit, and bulk
delete) confirm only when the item is in use, with an explanation of the effect (reminders are left
without a group, the tag is removed from every item); unused ones delete straight away with undo. "In
use" is `IsGroupInUseUseCase` (`logic:logic-group`, active reminders in the group; group details uses
its loaded reminder list) and `IsTagInUseUseCase` (`logic:logic-tag`, any assignment of any item type).
The cascade runs inside `onCommit` either way, so undo restores everything.

Tag deletion everywhere goes through `DeleteTagWithAssignmentsUseCase` (`logic:logic-tag`).

Not migrated: Routine, Habit and Google Task previews/editors have no list-level undo home yet, so they
keep their confirm dialogs and delete immediately.

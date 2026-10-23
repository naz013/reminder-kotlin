# Delete / archive with undo

Policy: reversible deletes, archives and unarchives show an undo Snackbar with no confirm dialog.
Confirm only for delete-all and cascading deletes. The helper below works with or without a prior
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
confirm), Tags list and Groups list (confirm kept, cascade runs on commit), Workflow rules (gallery,
group and reminder scopes; undo only, no confirm). Still on confirm until card 5/6: the preview and
edit screens. Other remaining screens: see the unified delete-undo Trello series.

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

The preview/edit screens adopt this in following chunks; none use it yet.

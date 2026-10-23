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

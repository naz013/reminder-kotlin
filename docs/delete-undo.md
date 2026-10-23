# Delete / archive with undo

Policy: reversible deletes and archives show an undo Snackbar with no confirm dialog. Confirm only for
delete-all and cascading deletes. The helper below works with or without a prior confirm.

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

Migrated screens: Agenda, Birthdays, Reminders archive, Notes (list and archive each own a controller
and a `SnackbarHostState`), Places list (undo only, no confirm). Remaining screens and docs: see the unified delete-undo Trello series.

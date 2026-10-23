package com.github.naz013.feature.common.viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Request to show an undo Snackbar for the pending batch identified by [batchKey]. */
data class UndoEvent(
  val batchKey: String,
  val message: String,
)

/**
 * ViewModel-side entry point for reversible deletes/archives: hides items via [pendingIds],
 * announces the undo window via [undoEvents], then the UI reports back with [undo] or [commit].
 * Works the same with or without a prior confirm dialog - call [markPending] once the action is
 * confirmed (or immediately, when the policy needs no confirmation).
 *
 * [commit] runs on [scope] (typically `viewModelScope`) using [commitDispatcher]; call
 * [commitAllDetached] from `onCleared()` so nothing pending is lost.
 */
class UndoDeleteController(
  private val scope: CoroutineScope,
  private val commitDispatcher: CoroutineDispatcher,
) {
  private val tracker = PendingDeleteTracker()
  private val events = Channel<UndoEvent>(Channel.BUFFERED)

  val pendingIds: StateFlow<Set<String>> = tracker.pendingIds
  val undoEvents: Flow<UndoEvent> = events.receiveAsFlow()

  fun markPending(
    batchKey: String,
    ids: Set<String>,
    message: String,
    onCommit: suspend () -> Unit,
  ) {
    tracker.markPending(batchKey = batchKey, ids = ids, onCommit = onCommit)
    events.trySend(UndoEvent(batchKey = batchKey, message = message))
  }

  fun undo(batchKey: String) {
    tracker.undo(batchKey)
  }

  fun commit(batchKey: String) {
    scope.launch(commitDispatcher) { tracker.commit(batchKey) }
  }

  fun commitAllDetached(dispatcher: CoroutineDispatcher) {
    tracker.commitAllDetached(dispatcher)
  }
}

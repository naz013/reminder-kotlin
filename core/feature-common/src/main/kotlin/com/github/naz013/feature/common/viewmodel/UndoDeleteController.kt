package com.github.naz013.feature.common.viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * Request to show an undo Snackbar for the pending batch identified by [batchKey]. [token] is unique
 * per mark, so a stale Snackbar result can only resolve its own batch.
 */
data class UndoEvent(
  val batchKey: String,
  val message: String,
  val token: Long = 0L,
)

/**
 * ViewModel-side entry point for reversible deletes/archives: hides items via [pendingIds],
 * announces the undo window via [undoEvents], then the UI reports back with [undo] or [commit].
 * Works the same with or without a prior confirm dialog - call [markPending] once the action is
 * confirmed (or immediately, when the policy needs no confirmation).
 *
 * [commit] runs on [scope] (typically `viewModelScope`) using [commitDispatcher]; it is a no-op once
 * the scope is cancelled because [commitAllDetached] in `onCleared()` covers everything pending.
 *
 * A newer mark commits any still-unresolved older one (a new Snackbar replaces the old one). The
 * latest unresolved event is replayed to every new [undoEvents] collector, so a host recreated by a
 * configuration change or navigation shows the undo UI again with a fresh window.
 */
class UndoDeleteController(
  private val scope: CoroutineScope,
  private val commitDispatcher: CoroutineDispatcher,
) {
  private val tracker = PendingDeleteTracker()
  private val lock = Any()
  private var lastToken = 0L
  private val unresolved = LinkedHashMap<Long, UndoEvent>()
  private val latest = MutableStateFlow<UndoEvent?>(null)

  val pendingIds: StateFlow<Set<String>> = tracker.pendingIds

  /** Exactly one `UndoDeleteHost` should collect this per controller. */
  val undoEvents: Flow<UndoEvent> = latest.filterNotNull()

  fun markPending(
    batchKey: String,
    ids: Set<String>,
    message: String,
    onCommit: suspend () -> Unit,
  ) {
    val replaced: UndoEvent?
    synchronized(lock) {
      val event = UndoEvent(batchKey = batchKey, message = message, token = ++lastToken)
      tracker.markPending(internalKey(event), ids, onCommit)
      replaced = latest.value
      unresolved[event.token] = event
      latest.value = event
    }
    replaced?.let { commit(it) }
  }

  fun undo(event: UndoEvent) {
    if (resolve(event)) tracker.undo(internalKey(event))
  }

  fun commit(event: UndoEvent) {
    if (resolve(event)) scope.launch(commitDispatcher) { tracker.commit(internalKey(event)) }
  }

  fun undo(batchKey: String) {
    unresolvedFor(batchKey).forEach { undo(it) }
  }

  fun commit(batchKey: String) {
    unresolvedFor(batchKey).forEach { commit(it) }
  }

  fun commitAllDetached(dispatcher: CoroutineDispatcher) {
    synchronized(lock) {
      unresolved.clear()
      latest.value = null
    }
    tracker.commitAllDetached(dispatcher)
  }

  private fun resolve(event: UndoEvent): Boolean =
    synchronized(lock) {
      val removed = unresolved.remove(event.token) != null
      if (latest.value == event) latest.value = null
      removed
    }

  private fun unresolvedFor(batchKey: String): List<UndoEvent> =
    synchronized(lock) { unresolved.values.filter { it.batchKey == batchKey } }

  private fun internalKey(event: UndoEvent) = "${event.batchKey}#${event.token}"
}

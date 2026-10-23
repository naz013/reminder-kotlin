package com.github.naz013.feature.common.viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Backs an "undo delete" Snackbar for a list screen. [markPending] hides [ids] from the UI
 * immediately via [pendingIds] - combine it into the screen's reactively-observed list so deleted
 * items disappear right away without actually touching the repository. The caller must follow up
 * with exactly one of [undo] (the item reappears, [onCommit] never runs) or [commit] (runs
 * [onCommit], e.g. the real repository delete) once the Snackbar's undo window closes - typically
 * driven by the result of Compose's `SnackbarHostState.showSnackbar`, not a timer owned here.
 *
 * Batches are keyed by [markPending]'s `batchKey` so a single-item delete and a bulk delete can be
 * pending at the same time without clobbering each other. Re-marking a key that is still pending
 * merges into the existing batch (ids are united, both commit actions run) so no delete is dropped.
 *
 * Thread-safe: a batch is removed atomically before its commit action runs, so it runs at most once.
 */
class PendingDeleteTracker {
  private val lock = Any()
  private val _pendingIds = MutableStateFlow<Set<String>>(emptySet())
  val pendingIds: StateFlow<Set<String>> = _pendingIds

  private val pendingBatches = mutableMapOf<String, PendingBatch>()

  fun markPending(
    batchKey: String,
    ids: Set<String>,
    onCommit: suspend () -> Unit,
  ) {
    synchronized(lock) {
      val previous = pendingBatches[batchKey]
      pendingBatches[batchKey] = if (previous == null) {
        PendingBatch(ids, onCommit)
      } else {
        PendingBatch(previous.ids + ids) {
          previous.onCommit()
          onCommit()
        }
      }
      publish()
    }
  }

  fun undo(batchKey: String) {
    synchronized(lock) {
      pendingBatches.remove(batchKey) ?: return
      publish()
    }
  }

  suspend fun commit(batchKey: String) {
    val batch = synchronized(lock) {
      val removed = pendingBatches.remove(batchKey) ?: return
      publish()
      removed
    }
    batch.onCommit()
  }

  suspend fun commitAll() {
    val keys = synchronized(lock) { pendingBatches.keys.toList() }
    keys.forEach { commit(it) }
  }

  /**
   * For `ViewModel.onCleared()`: viewModelScope is already cancelled there, so pending batches are
   * committed on a standalone scope that outlives the ViewModel.
   */
  fun commitAllDetached(dispatcher: CoroutineDispatcher) {
    CoroutineScope(SupervisorJob() + dispatcher).launch { commitAll() }
  }

  private fun publish() {
    _pendingIds.value = pendingBatches.values.flatMapTo(mutableSetOf()) { it.ids }
  }

  private class PendingBatch(
    val ids: Set<String>,
    val onCommit: suspend () -> Unit,
  )
}

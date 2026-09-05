package com.github.naz013.feature.common.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Backs an "undo delete" Snackbar for a list screen. [markPending] hides [ids] from the UI
 * immediately via [pendingIds] - combine it into the screen's reactively-observed list so deleted
 * items disappear right away without actually touching the repository. The caller must follow up
 * with exactly one of [undo] (the item reappears, [onCommit] never runs) or [commit] (runs
 * [onCommit], e.g. the real repository delete) once the Snackbar's undo window closes - typically
 * driven by the result of Compose's `SnackbarHostState.showSnackbar`, not a timer owned here.
 *
 * Batches are keyed by [markPending]'s `batchKey` so a single-item delete and a bulk delete can be
 * pending at the same time without clobbering each other.
 */
class PendingDeleteTracker {
  private val _pendingIds = MutableStateFlow<Set<String>>(emptySet())
  val pendingIds: StateFlow<Set<String>> = _pendingIds

  private val pendingBatches = mutableMapOf<String, PendingBatch>()

  fun markPending(
    batchKey: String,
    ids: Set<String>,
    onCommit: suspend () -> Unit,
  ) {
    val previous = pendingBatches.put(batchKey, PendingBatch(ids, onCommit))
    _pendingIds.update { (it - (previous?.ids ?: emptySet())) + ids }
  }

  fun undo(batchKey: String) {
    val batch = pendingBatches.remove(batchKey) ?: return
    _pendingIds.update { it - batch.ids }
  }

  suspend fun commit(batchKey: String) {
    val batch = pendingBatches.remove(batchKey) ?: return
    _pendingIds.update { it - batch.ids }
    batch.onCommit()
  }

  private class PendingBatch(
    val ids: Set<String>,
    val onCommit: suspend () -> Unit,
  )
}

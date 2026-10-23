package com.github.naz013.feature.common.viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.EnumMap
import java.util.concurrent.ConcurrentHashMap

/** Length of the undo window; the undo Snackbar's `SnackbarDuration.Short` is 4 seconds too. */
const val UNDO_WINDOW_MS = 4_000L

/** Extra time past [UNDO_WINDOW_MS] before the watchdog commits a delete nobody resolved. */
const val UNDO_WATCHDOG_GRACE_MS = 3_000L

enum class PendingEntity { REMINDER, BIRTHDAY, NOTE, GROUP, TAG, PLACE }

/**
 * App-scoped owner of pending deletes started on preview/edit screens: unlike a screen ViewModel it
 * outlives the screen, so the list the user returns to can show the undo Snackbar and hide the item.
 * One [UndoDeleteController] per [PendingEntity] keeps entity types independent.
 */
interface PendingDeleteCoordinator {
  fun controller(type: PendingEntity): UndoDeleteController

  fun pendingIds(type: PendingEntity): StateFlow<Set<String>>

  /** Hides [id] and starts the undo window; [onCommit] runs unless the user undoes in time. */
  fun markPending(
    type: PendingEntity,
    id: String,
    message: String,
    onCommit: suspend () -> Unit,
  )

  /** Commits every unresolved delete of every type, e.g. when the app goes to the background. */
  fun commitAll()

  fun hostCount(type: PendingEntity): StateFlow<Int>

  fun registerHost(type: PendingEntity)

  fun unregisterHost(type: PendingEntity)
}

internal class PendingDeleteCoordinatorImpl(
  private val scope: CoroutineScope,
  private val commitDispatcher: CoroutineDispatcher,
) : PendingDeleteCoordinator {
  private val controllers = EnumMap<PendingEntity, UndoDeleteController>(PendingEntity::class.java)
  private val hosts = EnumMap<PendingEntity, MutableStateFlow<Int>>(PendingEntity::class.java)

  private val watchdogs = ConcurrentHashMap<Pair<PendingEntity, Long>, Job>()

  init {
    PendingEntity.entries.forEach { type ->
      controllers[type] = UndoDeleteController(scope, commitDispatcher) { event ->
        watchdogs.remove(type to event.token)?.cancel()
      }
      hosts[type] = MutableStateFlow(0)
    }
  }

  override fun controller(type: PendingEntity): UndoDeleteController = controllers.getValue(type)

  override fun pendingIds(type: PendingEntity): StateFlow<Set<String>> = controller(type).pendingIds

  override fun markPending(
    type: PendingEntity,
    id: String,
    message: String,
    onCommit: suspend () -> Unit,
  ) {
    val controller = controller(type)
    val event = controller.markPending(id, setOf(id), message, onCommit)
    val key = type to event.token
    watchdogs[key] = scope.launch {
      delay(UNDO_WINDOW_MS + UNDO_WATCHDOG_GRACE_MS)
      watchdogs.remove(key)
      controller.commit(event)
    }
  }

  override fun commitAll() {
    controllers.values.forEach { it.commitAllDetached(commitDispatcher) }
  }

  override fun hostCount(type: PendingEntity): StateFlow<Int> = hosts.getValue(type)

  override fun registerHost(type: PendingEntity) {
    hosts.getValue(type).update { it + 1 }
  }

  override fun unregisterHost(type: PendingEntity) {
    hosts.getValue(type).update { (it - 1).coerceAtLeast(0) }
  }
}

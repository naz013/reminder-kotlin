package com.github.naz013.logic.reminder.usecase

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.reminderNow
import com.github.naz013.logic.reminder.reminderUtcToLocal
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.scheduler.JobSchedulerApi
import org.threeten.bp.LocalDateTime

/**
 * Captures and restores the schedule state that a bulk reschedule changes, so it can be undone.
 * Restoring does not go through activation, which would count an extra event.
 */
class RestoreReminderStatesUseCase(
  private val reminderRepository: ReminderV2Repository,
  private val saveReminderUseCase: SaveReminderUseCase,
  private val jobScheduler: JobSchedulerApi,
  private val dateTimeManager: DateTimeManager,
) {
  data class State(
    val id: String,
    val eventDateTime: LocalDateTime?,
    val eventCount: Long,
    val isActive: Boolean,
  )

  suspend fun capture(ids: List<String>): List<State> =
    ids.distinct().mapNotNull { id ->
      reminderRepository.getById(id)?.let {
        State(
          id = it.uuId,
          eventDateTime = it.schedule.eventDateTime,
          eventCount = it.eventCount,
          isActive = it.isActive,
        )
      }
    }

  suspend operator fun invoke(states: List<State>) {
    states.forEach { restoreOne(it) }
    Logger.i(TAG, "Restored ${states.size} reminder states")
  }

  private suspend fun restoreOne(state: State) {
    val current = reminderRepository.getById(state.id) ?: return
    val restored = current.copy(
      schedule = current.schedule.copy(eventDateTime = state.eventDateTime),
      eventCount = state.eventCount,
      isActive = state.isActive,
      sync = current.sync.copy(version = current.sync.version + 1, syncState = SyncState.WaitingForUpload),
    )
    saveReminderUseCase(restored)
    jobScheduler.cancelReminder(restored.uniqueId)
    if (restored.isActive && isInFuture(restored)) {
      jobScheduler.scheduleReminder(restored)
    }
  }

  private fun isInFuture(reminder: ReminderV2): Boolean {
    val event = reminder.schedule.eventDateTime ?: return false
    return dateTimeManager.reminderUtcToLocal(reminder, event).isAfter(dateTimeManager.reminderNow(reminder))
  }

  companion object {
    private const val TAG = "RestoreReminderStatesUseCase"
  }
}

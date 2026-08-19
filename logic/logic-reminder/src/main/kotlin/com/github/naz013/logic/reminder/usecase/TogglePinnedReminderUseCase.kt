package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger

/**
 * Toggles whether a reminder is pinned to the top of the Agenda list.
 */
class TogglePinnedReminderUseCase(
  private val saveReminderUseCase: SaveReminderUseCase,
) {
  suspend operator fun invoke(reminder: ReminderV2): ReminderV2 {
    val updated = reminder.copy(isPinned = !reminder.isPinned)
    Logger.i(TAG, "Toggling pinned state for id=${reminder.uuId}, isPinned=${updated.isPinned}")
    saveReminderUseCase(updated)
    return updated
  }

  companion object {
    private const val TAG = "TogglePinnedReminderUseCase"
  }
}

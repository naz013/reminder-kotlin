package com.github.naz013.logic.notificationaction.reminder

import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderV2Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Re-fires a repeating reminder alert. Whether to reschedule *again* after this fire is decided
 * by [ReminderActionProcessor.process] itself (it owns the repeat-count cap), not here - so this
 * processor can't drift out of sync with that cap.
 */
class ReminderRepeatProcessor(
  private val dispatcherProvider: DispatcherProvider,
  private val reminderV2Repository: ReminderV2Repository,
  private val reminderActionProcessor: ReminderActionProcessor,
) {
  private val scope = CoroutineScope(dispatcherProvider.default())

  fun process(
    id: String,
    repeatCount: Int,
  ) {
    Logger.d(TAG, "process: $id, repeatCount=$repeatCount")
    scope.launch {
      val reminder = reminderV2Repository.getById(id) ?: return@launch
      reminderActionProcessor.process(reminder.uuId, repeatCount)
    }
  }

  companion object {
    private const val TAG = "ReminderRepeatProcessor"
  }
}

package com.elementary.tasks.reminder.scheduling.usecase.google

import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.domain.reminder.v2.ReminderV2

/**
 * Saves reminder to Google Calendar if needed.
 */
class SaveReminderToGoogleCalendarUseCase(
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val prefs: Prefs,
) {
  suspend operator fun invoke(reminder: ReminderV2) {
    if (reminder.calendarExport != null && prefs.isCalendarEnabled) {
      googleCalendarUtils.addEvent(reminder)
    }
  }

  companion object {
    private const val TAG = "SaveReminderToGoogleCalendarUseCase"
  }
}

package com.elementary.tasks.reminder.scheduling.usecase.google

import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.domain.Reminder

/**
 * Saves reminder to Google Calendar if needed.
 */
class SaveReminderToGoogleCalendarUseCase(
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val prefs: Prefs
) {

  suspend operator fun invoke(reminder: Reminder) {
    if (reminder.exportToCalendar) {
      if (prefs.isCalendarEnabled || reminder.jsonSchemaVersion == Reminder.Version.V3) {
        googleCalendarUtils.addEvent(reminder)
      }
    }
  }

  companion object {
    private const val TAG = "SaveReminderToGoogleCalendarUseCase"
  }
}

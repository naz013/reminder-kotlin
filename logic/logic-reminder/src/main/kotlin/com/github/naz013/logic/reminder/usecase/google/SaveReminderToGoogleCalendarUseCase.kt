package com.github.naz013.logic.reminder.usecase.google

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.googlecalendar.GoogleCalendarApi
import com.github.naz013.logic.reminder.ReminderPreferences

/**
 * Saves reminder to Google Calendar if needed.
 */
class SaveReminderToGoogleCalendarUseCase(
  private val googleCalendarApi: GoogleCalendarApi,
  private val reminderPreferences: ReminderPreferences,
) {
  suspend operator fun invoke(reminder: ReminderV2) {
    if (reminder.calendarExport != null && reminderPreferences.isCalendarEnabled) {
      googleCalendarApi.addEvent(reminder)
    }
  }

  companion object {
    private const val TAG = "SaveReminderToGoogleCalendarUseCase"
  }
}

package com.elementary.tasks.module.logicreminder

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logic.reminder.ReminderPreferences

class ReminderPreferencesImpl(
  private val prefs: Prefs,
) : ReminderPreferences {
  override val numberOfReminderOccurrences: Int
    get() = prefs.numberOfReminderOccurrences

  override val isSbNotificationEnabled: Boolean
    get() = prefs.isSbNotificationEnabled

  override val isCalendarEnabled: Boolean
    get() = prefs.isCalendarEnabled
}

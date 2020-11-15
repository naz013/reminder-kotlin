package com.elementary.tasks.core.controller

import android.content.Context
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Prefs

abstract class EventManager(
  val reminder: Reminder,
  protected val db: AppDb,
  protected val prefs: Prefs,
  protected val calendarUtils: CalendarUtils,
  protected val context: Context
) : EventControl {

  protected fun save() {
    db.reminderDao().insert(reminder)
    UpdatesHelper.updateWidget(context)
    if (prefs.isSbNotificationEnabled) {
      Notifier.updateReminderPermanent(context, PermanentReminderReceiver.ACTION_SHOW)
    }
  }

  protected fun remove() {
    db.reminderDao().delete(reminder)
    UpdatesHelper.updateWidget(context)
    if (prefs.isSbNotificationEnabled) {
      Notifier.updateReminderPermanent(context, PermanentReminderReceiver.ACTION_SHOW)
    }
  }
}

package com.elementary.tasks.core.controller

import android.content.Context
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Prefs
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class EventManager(val reminder: Reminder) : EventControl, KoinComponent {

    protected val db: AppDb by inject()
    protected val prefs: Prefs by inject()
    protected val notifier: Notifier by inject()
    protected val calendarUtils: CalendarUtils by inject()
    protected val context: Context by inject()

    protected fun save() {
        db.reminderDao().insert(reminder)
        UpdatesHelper.updateWidget(context)
        if (prefs.isSbNotificationEnabled) {
            notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_SHOW)
        }
    }

    protected fun remove() {
        db.reminderDao().delete(reminder)
        UpdatesHelper.updateWidget(context)
        if (prefs.isSbNotificationEnabled) {
            notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_SHOW)
        }
    }
}

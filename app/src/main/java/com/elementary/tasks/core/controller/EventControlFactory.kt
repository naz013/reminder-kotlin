package com.elementary.tasks.core.controller

import android.content.Context
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.elementary.tasks.core.utils.params.Prefs
import timber.log.Timber

class EventControlFactory(
  private val prefs: Prefs,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val context: Context,
  private val notifier: Notifier,
  private val jobScheduler: JobScheduler,
  private val updatesHelper: UpdatesHelper,
  private val textProvider: TextProvider,
  private val reminderDao: ReminderDao,
  private val googleTasksDao: GoogleTasksDao,
  private val dateTimeManager: DateTimeManager,
  private val recurEventManager: RecurEventManager
) {

  fun getController(reminder: Reminder): EventControl {
    val type = UiReminderType(reminder.type)
    return when {
      type.isSame(Reminder.BY_DATE_SHOP) && !reminder.hasReminder -> {
        ShoppingEvent(
          reminder,
          reminderDao,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider,
          dateTimeManager,
          googleTasksDao
        )
      }

      type.isBase(UiReminderType.Base.DATE) -> {
        DateEvent(
          reminder,
          reminderDao,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider,
          dateTimeManager,
          googleTasksDao
        )
      }

      type.isGpsType() -> {
        LocationEvent(
          reminder,
          reminderDao,
          prefs,
          context,
          notifier,
          jobScheduler,
          updatesHelper,
          dateTimeManager
        )
      }

      type.isBase(UiReminderType.Base.MONTHLY) -> {
        MonthlyEvent(
          reminder,
          reminderDao,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider,
          dateTimeManager,
          googleTasksDao
        )
      }

      type.isBase(UiReminderType.Base.WEEKDAY) -> {
        WeeklyEvent(
          reminder,
          reminderDao,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider,
          dateTimeManager,
          googleTasksDao
        )
      }

      type.isBase(UiReminderType.Base.TIMER) -> {
        TimerEvent(
          reminder,
          reminderDao,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider,
          dateTimeManager,
          googleTasksDao
        )
      }

      type.isBase(UiReminderType.Base.YEARLY) ->
        YearlyEvent(
          reminder,
          reminderDao,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider,
          dateTimeManager,
          googleTasksDao
        )

      type.isBase(UiReminderType.Base.RECUR) ->
        RecurEvent(
          reminder,
          reminderDao,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider,
          dateTimeManager,
          googleTasksDao,
          recurEventManager
        )

      else -> DateEvent(
        reminder,
        reminderDao,
        prefs,
        googleCalendarUtils,
        notifier,
        jobScheduler,
        updatesHelper,
        textProvider,
        dateTimeManager,
        googleTasksDao
      )
    }.also {
      Timber.d("getController: $it")
    }
  }
}

package com.elementary.tasks.core.controller

import android.content.Context
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.Reminder
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
    return when {
      Reminder.isSame(reminder.type, Reminder.BY_DATE_SHOP) -> {
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
      Reminder.isBase(reminder.type, Reminder.BY_DATE) -> {
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

      Reminder.isBase(reminder.type, Reminder.BY_LOCATION) ||
        Reminder.isBase(reminder.type, Reminder.BY_OUT) ||
        Reminder.isBase(reminder.type, Reminder.BY_PLACES) -> {
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
      Reminder.isBase(reminder.type, Reminder.BY_MONTH) -> {
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
      Reminder.isBase(reminder.type, Reminder.BY_WEEK) -> {
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
      Reminder.isSame(reminder.type, Reminder.BY_TIME) -> {
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
      Reminder.isBase(reminder.type, Reminder.BY_DAY_OF_YEAR) ->
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
      Reminder.isBase(reminder.type, Reminder.BY_RECUR) ->
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

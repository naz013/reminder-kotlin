package com.elementary.tasks.core.controller

import android.content.Context
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.TextProvider
import timber.log.Timber

class EventControlFactory(
  private val appDb: AppDb,
  private val prefs: Prefs,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val context: Context,
  private val notifier: Notifier,
  private val jobScheduler: JobScheduler,
  private val updatesHelper: UpdatesHelper,
  private val textProvider: TextProvider
) {

  fun getController(reminder: Reminder): EventControl {
    return when {
      Reminder.isSame(reminder.type, Reminder.BY_DATE_SHOP) ->
        ShoppingEvent(
          reminder,
          appDb,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider
        )

      Reminder.isBase(reminder.type, Reminder.BY_DATE) ->
        DateEvent(
          reminder,
          appDb,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider
        )

      Reminder.isBase(reminder.type, Reminder.BY_LOCATION) ->
        LocationEvent(reminder, appDb, prefs, context, notifier, jobScheduler, updatesHelper)

      Reminder.isBase(reminder.type, Reminder.BY_MONTH) ->
        MonthlyEvent(
          reminder,
          appDb,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider
        )

      Reminder.isBase(reminder.type, Reminder.BY_WEEK) ->
        WeeklyEvent(
          reminder,
          appDb,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider
        )

      Reminder.isBase(reminder.type, Reminder.BY_OUT) ->
        LocationEvent(reminder, appDb, prefs, context, notifier, jobScheduler, updatesHelper)

      Reminder.isBase(reminder.type, Reminder.BY_PLACES) ->
        LocationEvent(reminder, appDb, prefs, context, notifier, jobScheduler, updatesHelper)

      Reminder.isSame(reminder.type, Reminder.BY_TIME) ->
        TimerEvent(
          reminder,
          appDb,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider
        )

      Reminder.isBase(reminder.type, Reminder.BY_DAY_OF_YEAR) ->
        YearlyEvent(
          reminder,
          appDb,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider
        )

      else -> DateEvent(
        reminder,
        appDb,
        prefs,
        googleCalendarUtils,
        notifier,
        jobScheduler,
        updatesHelper,
        textProvider
      )
    }.also {
      Timber.d("getController: $it")
    }
  }
}

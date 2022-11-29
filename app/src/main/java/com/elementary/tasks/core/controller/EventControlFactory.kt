package com.elementary.tasks.core.controller

import android.content.Context
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Prefs
import timber.log.Timber

class EventControlFactory(
  private val appDb: AppDb,
  private val prefs: Prefs,
  private val calendarUtils: CalendarUtils,
  private val context: Context,
  private val notifier: Notifier
) {

  fun getController(reminder: Reminder): EventControl {
    return when {
      Reminder.isSame(reminder.type, Reminder.BY_DATE_SHOP) ->
        ShoppingEvent(reminder, appDb, prefs, calendarUtils, context, notifier)
      Reminder.isBase(reminder.type, Reminder.BY_DATE) ->
        DateEvent(reminder, appDb, prefs, calendarUtils, context, notifier)
      Reminder.isBase(reminder.type, Reminder.BY_LOCATION) ->
        LocationEvent(reminder, appDb, prefs, calendarUtils, context, notifier)
      Reminder.isBase(reminder.type, Reminder.BY_MONTH) ->
        MonthlyEvent(reminder, appDb, prefs, calendarUtils, context, notifier)
      Reminder.isBase(reminder.type, Reminder.BY_WEEK) ->
        WeeklyEvent(reminder, appDb, prefs, calendarUtils, context, notifier)
      Reminder.isBase(reminder.type, Reminder.BY_OUT) ->
        LocationEvent(reminder, appDb, prefs, calendarUtils, context, notifier)
      Reminder.isBase(reminder.type, Reminder.BY_PLACES) ->
        LocationEvent(reminder, appDb, prefs, calendarUtils, context, notifier)
      Reminder.isSame(reminder.type, Reminder.BY_TIME) ->
        TimerEvent(reminder, appDb, prefs, calendarUtils, context, notifier)
      Reminder.isBase(reminder.type, Reminder.BY_DAY_OF_YEAR) ->
        YearlyEvent(reminder, appDb, prefs, calendarUtils, context, notifier)
      else -> DateEvent(reminder, appDb, prefs, calendarUtils, context, notifier)
    }.also {
      Timber.d("getController: $it")
    }
  }
}

package com.elementary.tasks.core.controller

import android.content.Context
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.ReminderRepository

class EventControlFactory(
  private val prefs: Prefs,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val context: Context,
  private val notifier: Notifier,
  private val jobScheduler: JobScheduler,
  private val updatesHelper: UpdatesHelper,
  private val textProvider: TextProvider,
  private val reminderRepository: ReminderRepository,
  private val googleTaskRepository: GoogleTaskRepository,
  private val dateTimeManager: DateTimeManager,
  private val recurEventManager: RecurEventManager
) {

  fun getController(reminder: Reminder): EventControl {
    val type = UiReminderType(reminder.type)
    return when {
      type.isSame(Reminder.BY_DATE_SHOP) && !reminder.hasReminder -> {
        ShoppingEvent(
          reminder,
          reminderRepository,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider,
          dateTimeManager,
          googleTaskRepository
        )
      }

      type.isBase(UiReminderType.Base.DATE) -> {
        DateEvent(
          reminder,
          reminderRepository,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider,
          dateTimeManager,
          googleTaskRepository
        )
      }

      type.isGpsType() -> {
        LocationEvent(
          reminder,
          reminderRepository,
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
          reminderRepository,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider,
          dateTimeManager,
          googleTaskRepository
        )
      }

      type.isBase(UiReminderType.Base.WEEKDAY) -> {
        WeeklyEvent(
          reminder,
          reminderRepository,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider,
          dateTimeManager,
          googleTaskRepository
        )
      }

      type.isBase(UiReminderType.Base.TIMER) -> {
        TimerEvent(
          reminder,
          reminderRepository,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider,
          dateTimeManager,
          googleTaskRepository
        )
      }

      type.isBase(UiReminderType.Base.YEARLY) ->
        YearlyEvent(
          reminder,
          reminderRepository,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider,
          dateTimeManager,
          googleTaskRepository
        )

      type.isBase(UiReminderType.Base.RECUR) ->
        RecurEvent(
          reminder,
          reminderRepository,
          prefs,
          googleCalendarUtils,
          notifier,
          jobScheduler,
          updatesHelper,
          textProvider,
          dateTimeManager,
          googleTaskRepository,
          recurEventManager
        )

      else -> DateEvent(
        reminder,
        reminderRepository,
        prefs,
        googleCalendarUtils,
        notifier,
        jobScheduler,
        updatesHelper,
        textProvider,
        dateTimeManager,
        googleTaskRepository
      )
    }.also {
      Logger.d("getController: $it")
    }
  }
}

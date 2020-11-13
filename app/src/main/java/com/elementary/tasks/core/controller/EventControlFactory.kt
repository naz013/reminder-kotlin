package com.elementary.tasks.core.controller

import com.elementary.tasks.core.data.models.Reminder
import timber.log.Timber

object EventControlFactory {

  fun getController(reminder: Reminder): EventControl {
    val control: EventControl = when {
      Reminder.isSame(reminder.type, Reminder.BY_DATE_SHOP) -> ShoppingEvent(reminder)
      Reminder.isBase(reminder.type, Reminder.BY_DATE) -> DateEvent(reminder)
      Reminder.isBase(reminder.type, Reminder.BY_LOCATION) -> LocationEvent(reminder)
      Reminder.isBase(reminder.type, Reminder.BY_MONTH) -> MonthlyEvent(reminder)
      Reminder.isBase(reminder.type, Reminder.BY_WEEK) -> WeeklyEvent(reminder)
      Reminder.isBase(reminder.type, Reminder.BY_OUT) -> LocationEvent(reminder)
      Reminder.isBase(reminder.type, Reminder.BY_PLACES) -> LocationEvent(reminder)
      Reminder.isSame(reminder.type, Reminder.BY_TIME) -> TimerEvent(reminder)
      Reminder.isBase(reminder.type, Reminder.BY_DAY_OF_YEAR) -> YearlyEvent(reminder)
      else -> DateEvent(reminder)
    }
    Timber.d("getController: $control")
    return control
  }
}

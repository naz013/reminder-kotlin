package com.elementary.tasks.day_view

import com.elementary.tasks.core.data.adapter.UiBirthdayListAdapter
import com.elementary.tasks.core.data.adapter.UiReminderListAdapter
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.plusMillis
import com.elementary.tasks.day_view.day.EventModel

class DayViewProvider(
  private val uiBirthdayListAdapter: UiBirthdayListAdapter,
  private val uiReminderListAdapter: UiReminderListAdapter,
  private val dateTimeManager: DateTimeManager
) {

  fun loadReminders(isFuture: Boolean, reminders: List<Reminder>): List<EventModel> {
    val data = mutableListOf<EventModel>()
    val filtered = reminders.filterNot { UiReminderType(it.type).isGpsType() }
    for (reminder in filtered) {
      val type = reminder.type
      val eventTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: continue
      val repeatTime = reminder.repeatInterval
      val limit = reminder.repeatLimit.toLong()
      val count = reminder.eventCount

      data.add(
        EventModel(
          reminder.viewType,
          uiReminderListAdapter.create(reminder),
          eventTime.dayOfMonth,
          eventTime.monthValue,
          eventTime.year,
          0
        )
      )
      if (isFuture) {
        var dateTime = dateTimeManager.fromGmtToLocal(reminder.startTime) ?: continue
        if (Reminder.isBase(type, Reminder.BY_WEEK)) {
          var days: Long = 0
          var max = Configs.MAX_DAYS_COUNT
          if (reminder.isLimited()) {
            max = limit - count
          }
          val weekdays = reminder.weekdays
          val baseTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: continue
          do {
            dateTime = dateTime.plusDays(1)
            if (dateTime == baseTime) {
              continue
            }
            val weekDay = dateTimeManager.localDayOfWeekToOld(eventTime.dayOfWeek)
            if (weekdays[weekDay - 1] == 1) {
              days++
              val localItem = Reminder(reminder, true).apply {
                this.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
              }
              data.add(
                EventModel(
                  reminder.viewType,
                  uiReminderListAdapter.create(localItem),
                  dateTime.dayOfMonth,
                  dateTime.monthValue,
                  dateTime.year,
                  0
                )
              )
            }
          } while (days < max)
        } else if (Reminder.isBase(type, Reminder.BY_MONTH)) {
          var days: Long = 0
          var max = Configs.MAX_DAYS_COUNT
          if (reminder.isLimited()) {
            max = limit - count
          }
          val baseTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: continue
          var localItem = reminder
          do {
            dateTime = dateTimeManager.getNewNextMonthDayTime(localItem, eventTime)
            if (dateTime == baseTime) {
              continue
            }
            days++
            localItem = Reminder(localItem, true).apply {
              this.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
            }
            data.add(
              EventModel(
                reminder.viewType,
                uiReminderListAdapter.create(localItem),
                dateTime.dayOfMonth,
                dateTime.monthValue,
                dateTime.year,
                0
              )
            )
          } while (days < max)
        } else {
          if (repeatTime == 0L) {
            continue
          }
          var days: Long = 0
          var max = Configs.MAX_DAYS_COUNT
          if (reminder.isLimited()) {
            max = limit - count
          }
          do {
            dateTime = dateTime.plusMillis(repeatTime)
            if (eventTime == dateTime) {
              continue
            }
            days++
            val localItem = Reminder(reminder, true).apply {
              this.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
            }
            data.add(
              EventModel(
                reminder.viewType,
                uiReminderListAdapter.create(localItem),
                dateTime.dayOfMonth,
                dateTime.monthValue,
                dateTime.year,
                0
              )
            )
          } while (days < max)
        }
      }
    }
    return data
  }

  fun toEventModel(birthday: Birthday): EventModel {
    val birthdayListItem = uiBirthdayListAdapter.convert(birthday)
    val dateTime = dateTimeManager.fromMillis(birthdayListItem.nextBirthdayDate)
    return EventModel(
      EventModel.BIRTHDAY,
      birthdayListItem,
      dateTime.dayOfMonth,
      dateTime.monthValue,
      dateTime.year,
      0
    )
  }
}

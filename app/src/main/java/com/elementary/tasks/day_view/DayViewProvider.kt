package com.elementary.tasks.day_view

import android.app.AlarmManager
import com.elementary.tasks.birthdays.list.BirthdayModelAdapter
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.day_view.day.EventModel
import com.github.naz013.calendarext.getDayOfMonth
import com.github.naz013.calendarext.getMonth
import com.github.naz013.calendarext.getYear
import com.github.naz013.calendarext.newCalendar
import java.util.*

class DayViewProvider(
  private val currentStateHolder: CurrentStateHolder,
  private val birthdayModelAdapter: BirthdayModelAdapter
) {

  fun loadReminders(isFuture: Boolean, reminders: List<Reminder>): List<EventModel> {
    val data = mutableListOf<EventModel>()
    for (item in reminders) {
      val mType = item.type
      if (!Reminder.isGpsType(mType)) {
        var eventTime = item.dateTime
        val repeatTime = item.repeatInterval
        val limit = item.repeatLimit.toLong()
        val count = item.eventCount
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = eventTime
        var mDay = calendar.get(Calendar.DAY_OF_MONTH)
        var mMonth = calendar.get(Calendar.MONTH)
        var mYear = calendar.get(Calendar.YEAR)
        if (eventTime > 0) {
          data.add(EventModel(item.viewType, item, mDay, mMonth, mYear, eventTime, 0))
        } else {
          continue
        }
        if (isFuture) {
          calendar.timeInMillis = item.startDateTime
          if (Reminder.isBase(mType, Reminder.BY_WEEK)) {
            var days: Long = 0
            var max = Configs.MAX_DAYS_COUNT
            if (item.isLimited()) {
              max = limit - count
            }
            val weekdays = item.weekdays
            val baseTime = item.dateTime
            do {
              calendar.timeInMillis = calendar.timeInMillis + AlarmManager.INTERVAL_DAY
              eventTime = calendar.timeInMillis
              if (eventTime == baseTime) {
                continue
              }
              val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
              if (weekdays[weekDay - 1] == 1 && eventTime > 0) {
                mDay = calendar.get(Calendar.DAY_OF_MONTH)
                mMonth = calendar.get(Calendar.MONTH)
                mYear = calendar.get(Calendar.YEAR)
                days++
                val localItem = Reminder(item, true).apply {
                  this.eventTime = TimeUtil.getGmtFromDateTime(eventTime)
                }
                data.add(EventModel(item.viewType, localItem, mDay, mMonth, mYear, eventTime, 0))
              }
            } while (days < max)
          } else if (Reminder.isBase(mType, Reminder.BY_MONTH)) {
            var days: Long = 0
            var max = Configs.MAX_DAYS_COUNT
            if (item.isLimited()) {
              max = limit - count
            }
            val baseTime = item.dateTime
            var localItem = item
            do {
              eventTime = TimeCount.getNextMonthDayTime(localItem, calendar.timeInMillis)
              calendar.timeInMillis = eventTime
              if (eventTime == baseTime) {
                continue
              }
              mDay = calendar.get(Calendar.DAY_OF_MONTH)
              mMonth = calendar.get(Calendar.MONTH)
              mYear = calendar.get(Calendar.YEAR)
              if (eventTime > 0) {
                days++
                localItem = Reminder(localItem, true).apply {
                  this.eventTime = TimeUtil.getGmtFromDateTime(eventTime)
                }
                data.add(EventModel(item.viewType, localItem, mDay, mMonth, mYear, eventTime, 0))
              }
            } while (days < max)
          } else {
            if (repeatTime == 0L) {
              continue
            }
            var days: Long = 0
            var max = Configs.MAX_DAYS_COUNT
            if (item.isLimited()) {
              max = limit - count
            }
            do {
              calendar.timeInMillis = calendar.timeInMillis + repeatTime
              eventTime = calendar.timeInMillis
              if (eventTime == item.dateTime) {
                continue
              }
              mDay = calendar.get(Calendar.DAY_OF_MONTH)
              mMonth = calendar.get(Calendar.MONTH)
              mYear = calendar.get(Calendar.YEAR)
              if (eventTime > 0) {
                days++
                val localItem = Reminder(item, true).apply {
                  this.eventTime = TimeUtil.getGmtFromDateTime(eventTime)
                }
                data.add(EventModel(item.viewType, localItem, mDay, mMonth, mYear, eventTime, 0))
              }
            } while (days < max)
          }
        }
      }
    }
    return data
  }

  fun toEventModel(birthday: Birthday): EventModel {
    val birthdayListItem = birthdayModelAdapter.convert(birthday)
    val calendar = newCalendar(birthdayListItem.nextBirthdayDate)
    return EventModel(
      EventModel.BIRTHDAY,
      birthdayListItem,
      calendar.getDayOfMonth(),
      calendar.getMonth(),
      calendar.getYear(),
      calendar.timeInMillis,
      0
    )
  }
}

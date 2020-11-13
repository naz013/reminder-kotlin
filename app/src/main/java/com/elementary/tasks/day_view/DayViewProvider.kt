package com.elementary.tasks.day_view

import android.app.AlarmManager
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.day_view.day.EventModel
import java.util.*

object DayViewProvider {

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

  fun loadBirthdays(birthTime: Long, list: List<Birthday>): List<EventModel> {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = birthTime
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val data = mutableListOf<EventModel>()
    for (item in list) {
      var date: Date? = null
      try {
        date = TimeUtil.BIRTH_DATE_FORMAT.parse(item.date)
      } catch (e: Exception) {
        e.printStackTrace()
      }

      if (date != null) {
        val calendar1 = Calendar.getInstance()
        calendar1.time = date
        val bDay = calendar1.get(Calendar.DAY_OF_MONTH)
        val bMonth = calendar1.get(Calendar.MONTH)
        val bYear = calendar1.get(Calendar.YEAR)
        calendar1.timeInMillis = System.currentTimeMillis()
        calendar1.set(Calendar.MONTH, bMonth)
        calendar1.set(Calendar.DAY_OF_MONTH, bDay)
        calendar1.set(Calendar.HOUR_OF_DAY, hour)
        calendar1.set(Calendar.MINUTE, minute)
        data.add(EventModel(EventModel.BIRTHDAY, item, bDay, bMonth, bYear, calendar1.timeInMillis, 0))
      }
    }
    return data
  }
}

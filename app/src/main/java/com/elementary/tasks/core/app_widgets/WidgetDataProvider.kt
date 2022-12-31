package com.elementary.tasks.core.app_widgets

import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.repository.BirthdayRepository
import com.elementary.tasks.core.data.repository.ReminderRepository
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.plusMillis
import org.threeten.bp.LocalTime

class WidgetDataProvider(
  private val dateTimeManager: DateTimeManager,
  private val reminderRepository: ReminderRepository,
  private val birthdayRepository: BirthdayRepository
) {

  private val data: MutableList<Item> = ArrayList()
  private var birthdayTime: LocalTime = LocalTime.now()
  private var isFeature: Boolean = false

  enum class WidgetType {
    BIRTHDAY,
    REMINDER
  }

  fun setTime(birthdayTime: LocalTime) {
    this.birthdayTime = birthdayTime
  }

  fun setFeature(isFeature: Boolean) {
    this.isFeature = isFeature
  }

  fun getData(): List<Item> {
    return data
  }

  fun getItem(position: Int): Item {
    return data[position]
  }

  fun hasReminder(day: Int, month: Int, year: Int): Boolean {
    var res = false
    for (item in data) {
      if (res) {
        break
      }
      res = item.day == day && item.month == month && item.year == year &&
        item.type == WidgetType.REMINDER
    }
    return res
  }

  fun hasBirthday(day: Int, month: Int): Boolean {
    var res = false
    for (item in data) {
      if (item.day == day && item.month == month && item.type == WidgetType.BIRTHDAY) {
        res = true
        break
      }
    }
    return res
  }

  fun prepare() {
    data.clear()
    loadBirthdays()
    loadReminders()
  }

  private fun loadReminders() {
    val reminders = reminderRepository.getActiveWithoutGpsTypes()
    for (item in reminders) {
      val mType = item.type
      var eventTime = dateTimeManager.fromGmtToLocal(item.eventTime) ?: continue
      data.add(
        Item(
          eventTime.dayOfMonth,
          eventTime.monthValue,
          eventTime.year,
          WidgetType.REMINDER
        )
      )
      val repeatTime = item.repeatInterval
      val limit = item.repeatLimit.toLong()
      val count = item.eventCount
      val isLimited = limit > 0
      if (isFeature) {
        if (Reminder.isBase(mType, Reminder.BY_WEEK)) {
          var days: Long = 0
          var max = Configs.MAX_DAYS_COUNT
          if (isLimited) {
            max = limit - count
          }
          val list = item.weekdays
          do {
            eventTime = eventTime.plusDays(1)

            val weekDay = dateTimeManager.localDayOfWeekToOld(eventTime.dayOfWeek)
            if (list[weekDay - 1] == 1) {
              days++
              data.add(
                Item(
                  eventTime.dayOfMonth,
                  eventTime.monthValue,
                  eventTime.year,
                  WidgetType.REMINDER
                )
              )
            }
          } while (days < max)
        } else if (Reminder.isBase(mType, Reminder.BY_MONTH)) {
          var days: Long = 0
          var max = Configs.MAX_DAYS_COUNT
          if (isLimited) {
            max = limit - count
          }
          do {
            item.eventTime = dateTimeManager.getGmtFromDateTime(eventTime)
            eventTime = dateTimeManager.getNewNextMonthDayTime(item)
            days++
            data.add(
              Item(
                eventTime.dayOfMonth,
                eventTime.monthValue,
                eventTime.year,
                WidgetType.REMINDER
              )
            )
          } while (days < max)
        } else {
          if (repeatTime == 0L) {
            continue
          }
          var days: Long = 0
          var max = Configs.MAX_DAYS_COUNT
          if (isLimited) {
            max = limit - count
          }
          do {
            eventTime = eventTime.plusMillis(repeatTime)
            days++
            data.add(
              Item(
                eventTime.dayOfMonth,
                eventTime.monthValue,
                eventTime.year,
                WidgetType.REMINDER
              )
            )
          } while (days < max)
        }
      }
    }
  }

  private fun loadBirthdays() {
    val birthdays = birthdayRepository.getAll()
    for (item in birthdays) {
      val date = dateTimeManager.parseBirthdayDate(item.date) ?: continue
      data.add(Item(date.dayOfMonth, date.monthValue, 0, WidgetType.BIRTHDAY))
    }
  }

  class Item(var day: Int, var month: Int, var year: Int, val type: WidgetType)
}

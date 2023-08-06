package com.elementary.tasks.core.appwidgets

import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.repository.BirthdayRepository
import com.elementary.tasks.core.data.repository.ReminderRepository
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceDateTimeTag
import com.elementary.tasks.core.utils.datetime.recurrence.RecurrenceManager
import com.elementary.tasks.core.utils.datetime.recurrence.TagType
import com.elementary.tasks.core.utils.plusMillis
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class WidgetDataProvider(
  private val dateTimeManager: DateTimeManager,
  private val reminderRepository: ReminderRepository,
  private val birthdayRepository: BirthdayRepository,
  private val recurrenceManager: RecurrenceManager
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

  fun setFuture(isFeature: Boolean) {
    this.isFeature = isFeature
  }

  fun getData(): List<Item> {
    return data
  }

  fun getItem(position: Int): Item {
    return data[position]
  }

  fun hasReminder(date: LocalDate): Boolean {
    var res = false
    for (item in data) {
      if (res) {
        break
      }
      res = item.date == date && item.type == WidgetType.REMINDER
    }
    return res
  }

  fun hasBirthday(day: Int, month: Int): Boolean {
    var res = false
    for (item in data) {
      if (item.date.dayOfMonth == day && item.date.monthValue == month &&
        item.type == WidgetType.BIRTHDAY
      ) {
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
      data.add(Item(eventTime.toLocalDate(), WidgetType.REMINDER))
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
              data.add(Item(eventTime.toLocalDate(), WidgetType.REMINDER))
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
            data.add(Item(eventTime.toLocalDate(), WidgetType.REMINDER))
          } while (days < max)
        } else if (Reminder.isBase(mType, Reminder.BY_RECUR)) {
          val dates = runCatching {
            recurrenceManager.parseObject(item.recurDataObject)
          }.getOrNull()?.getTagOrNull<RecurrenceDateTimeTag>(TagType.RDATE)?.values

          val baseTime = dateTimeManager.fromGmtToLocal(item.eventTime)

          dates?.mapNotNull { it.dateTime }
            ?.forEach { localDateTime ->
              if (baseTime != localDateTime) {
                data.add(Item(localDateTime.toLocalDate(), WidgetType.REMINDER))
              }
            }
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
            data.add(Item(eventTime.toLocalDate(), WidgetType.REMINDER))
          } while (days < max)
        }
      }
    }
  }

  private fun loadBirthdays() {
    val birthdays = birthdayRepository.getAll()
    for (item in birthdays) {
      val date = dateTimeManager.parseBirthdayDate(item.date) ?: continue
      data.add(Item(date, WidgetType.BIRTHDAY))
    }
  }

  data class Item(
    val date: LocalDate,
    val type: WidgetType
  )
}

package com.github.naz013.appwidgets.calendar

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.datetime.plusMillis
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.coroutine.invokeSuspend
import com.github.naz013.icalendar.ICalendarApi
import com.github.naz013.icalendar.RecurrenceDateTimeTag
import com.github.naz013.icalendar.TagType
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter
import com.github.naz013.usecase.birthdays.GetAllBirthdaysUseCase
import com.github.naz013.usecase.reminders.GetActiveRemindersWithoutGpsUseCase
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

internal class WidgetDataProvider(
  private val dateTimeManager: DateTimeManager,
  private val modelDateTimeFormatter: ModelDateTimeFormatter,
  private val getActiveRemindersWithoutGpsUseCase: GetActiveRemindersWithoutGpsUseCase,
  private val getAllBirthdaysUseCase: GetAllBirthdaysUseCase,
  private val iCalendarApi: ICalendarApi
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
    val reminders = invokeSuspend { getActiveRemindersWithoutGpsUseCase() }
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
          var max = MAX_DAYS_COUNT
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
          var max = MAX_DAYS_COUNT
          if (isLimited) {
            max = limit - count
          }
          do {
            item.eventTime = dateTimeManager.getGmtFromDateTime(eventTime)
            eventTime = modelDateTimeFormatter.getNewNextMonthDayTime(item)
            days++
            data.add(Item(eventTime.toLocalDate(), WidgetType.REMINDER))
          } while (days < max)
        } else if (Reminder.isBase(mType, Reminder.BY_RECUR)) {
          val dates = runCatching {
            iCalendarApi.parseObject(item.recurDataObject)
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
          var max = MAX_DAYS_COUNT
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
    val birthdays = invokeSuspend { getAllBirthdaysUseCase() }
    for (item in birthdays) {
      val date = dateTimeManager.parseBirthdayDate(item.date) ?: continue
      data.add(Item(date, WidgetType.BIRTHDAY))
    }
  }

  data class Item(
    val date: LocalDate,
    val type: WidgetType
  )

  companion object {
    const val MAX_DAYS_COUNT: Long = 240
  }
}

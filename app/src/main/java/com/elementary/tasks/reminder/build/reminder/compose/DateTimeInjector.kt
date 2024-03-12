package com.elementary.tasks.reminder.build.reminder.compose

import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.IntervalUtil
import com.elementary.tasks.reminder.build.DateBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayDateBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayTimeBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.bi.BiType
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import timber.log.Timber

class DateTimeInjector(
  private val dateTimeManager: DateTimeManager,
  private val iCalDateTimeInjector: ICalDateTimeInjector
) {

  operator fun invoke(
    reminder: Reminder,
    processedBuilderItems: ProcessedBuilderItems
  ) {
    val type = UiReminderType(reminder.type)

    val itemsMap = processedBuilderItems.typeMap
    val date = (itemsMap[BiType.DATE] as? DateBuilderItem)?.modifier?.getValue()
    val time = (itemsMap[BiType.TIME] as? TimeBuilderItem)?.modifier?.getValue()

    when {
      type.isSubTasks() -> {
        if (date != null && time != null) {
          val dateTime = LocalDateTime.of(date, time)
          val startTime = dateTimeManager.getGmtFromDateTime(dateTime)
          Timber.d("invoke: put date time $dateTime for sub tasks")
          reminder.eventTime = startTime
          reminder.startTime = startTime
          reminder.hasReminder = true
          dateTime
        } else {
          reminder.eventTime = ""
          reminder.startTime = ""
          reminder.hasReminder = false
          null
        }
      }

      type.isByDate() && date != null && time != null -> {
        LocalDateTime.of(date, time)
      }

      type.isTimer() && reminder.after != 0L -> {
        dateTimeManager.generateNextTimer(reminder, true)
      }

      type.isByWeekday() && IntervalUtil.isWeekday(reminder.weekdays) && time != null -> {
        reminder.eventTime =
          dateTimeManager.getGmtFromDateTime(LocalDateTime.of(LocalDate.now(), time))
        dateTimeManager.getNextWeekdayTime(reminder)
      }

      type.isMonthly() && time != null -> {
        reminder.eventTime =
          dateTimeManager.getGmtFromDateTime(LocalDateTime.of(LocalDate.now(), time))
        dateTimeManager.getNewNextMonthDayTime(reminder)
      }

      type.isYearly() && time != null -> {
        reminder.eventTime =
          dateTimeManager.getGmtFromDateTime(LocalDateTime.of(LocalDate.now(), time))
        dateTimeManager.getNextYearDayTime(reminder)
      }

      type.isGpsType() -> {
        val delayDate = (itemsMap[BiType.LOCATION_DELAY_DATE] as? LocationDelayDateBuilderItem)
          ?.modifier?.getValue()
        val delayTime = (itemsMap[BiType.LOCATION_DELAY_TIME] as? LocationDelayTimeBuilderItem)
          ?.modifier?.getValue()
        val delayDateTime = when {
          delayDate != null && delayTime != null -> {
            LocalDateTime.of(delayDate, delayTime)
          }
          delayDate != null -> {
            LocalDateTime.of(delayDate, LocalTime.now())
          }
          delayTime != null -> {
            LocalDateTime.of(LocalDate.now(), delayTime)
          }
          else -> null
        }
        if (delayDateTime != null && dateTimeManager.isCurrent(delayDateTime)) {
          val startTime = delayDateTime.let { dateTimeManager.getGmtFromDateTime(it) }
          Timber.d("invoke: put delay date time $delayDateTime")
          reminder.eventTime = startTime
          reminder.startTime = startTime
          reminder.hasReminder = true
          delayDateTime
        } else {
          Timber.d("invoke: no delay date time needed for ${reminder.type}")
          reminder.eventTime = ""
          reminder.startTime = ""
          reminder.hasReminder = false
          null
        }
      }

      type.isRecur() && processedBuilderItems.groupMap.isNotEmpty() -> {
        iCalDateTimeInjector(reminder, processedBuilderItems)
      }

      else -> {
        Timber.d("invoke: no date time needed for ${reminder.type}")
        reminder.eventTime = ""
        reminder.startTime = ""
        null
      }
    }?.also { dateTime ->
      val startTime = dateTime.let { dateTimeManager.getGmtFromDateTime(it) }
      Timber.d("invoke: put date time $dateTime")
      reminder.eventTime = startTime
      reminder.startTime = startTime
    }
  }
}

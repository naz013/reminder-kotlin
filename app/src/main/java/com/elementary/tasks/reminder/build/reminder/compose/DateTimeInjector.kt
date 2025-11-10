package com.elementary.tasks.reminder.build.reminder.compose

import com.elementary.tasks.core.utils.datetime.IntervalUtil
import com.elementary.tasks.reminder.build.DateBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayDateBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayTimeBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems
import com.elementary.tasks.reminder.scheduling.recurrence.RecurrenceCalculator
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class DateTimeInjector(
  private val dateTimeManager: DateTimeManager,
  private val iCalDateTimeInjector: ICalDateTimeInjector,
  private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculator(),
) {

  operator fun invoke(
    reminder: Reminder,
    processedBuilderItems: ProcessedBuilderItems
  ) {
    val type = reminder.readType()

    val itemsMap = processedBuilderItems.typeMap
    val date = (itemsMap[BiType.DATE] as? DateBuilderItem)?.modifier?.getValue()
    val time = (itemsMap[BiType.TIME] as? TimeBuilderItem)?.modifier?.getValue()

    when {
      type.hasSubTasks() -> {
        if (date != null && time != null) {
          val dateTime = LocalDateTime.of(date, time)
          val startTime = dateTimeManager.getGmtFromDateTime(dateTime)
          Logger.i(TAG, "Put date time $dateTime for sub tasks")
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

      type.isDateTime() && date != null && time != null -> {
        LocalDateTime.of(date, time).also {
          Logger.i(TAG, "Put date time $it")
        }
      }

      type.isCountdown() && reminder.after != 0L -> {
        recurrenceCalculator.getStartTimerDateTime(
          countdownTimeInMillis = reminder.after
        ).also {
          Logger.i(TAG, "Put countdown date time $it")
        }
      }

      type.isByDayOfWeek() && IntervalUtil.isWeekday(reminder.weekdays) && time != null -> {
        val dateTime = LocalDateTime.of(LocalDate.now(), time)
        val nextDateTime = recurrenceCalculator.findNextDayOfWeekDateTime(
          eventDateTime = dateTime,
          weekdays = reminder.weekdays,
          afterOrEqualDateTime = dateTimeManager.getCurrentDateTime()
        ).also {
          Logger.i(TAG, "Put date time $it for the By Day Of Week")
        }
        reminder.eventTime = dateTimeManager.getGmtFromDateTime(nextDateTime)
        nextDateTime
      }

      type.isByDayOfMonth() && time != null -> {
        val dateTime = LocalDateTime.of(LocalDate.now(), time)
        val nextDateTime = recurrenceCalculator.findNextMonthDayDateTime(
          eventDateTime = dateTime,
          dayOfMonth = reminder.dayOfMonth,
          interval = reminder.repeatInterval,
          afterOrEqualDateTime = dateTimeManager.getCurrentDateTime()
        ).also {
          Logger.i(TAG, "Put date time $it for the By Day Of Month")
        }
        reminder.eventTime = dateTimeManager.getGmtFromDateTime(nextDateTime)
        nextDateTime
      }

      type.isByDayOfYear() && time != null -> {
        val dateTime = LocalDateTime.of(LocalDate.now(), time)
        val nextDateTime = recurrenceCalculator.findNextYearDayDateTime(
          eventDateTime = dateTime,
          monthOfYear = reminder.monthOfYear,
          dayOfMonth = reminder.dayOfMonth,
          interval = reminder.repeatInterval,
          afterOrEqualDateTime = dateTimeManager.getCurrentDateTime()
        ).also {
          Logger.i(TAG, "Put date time $it for the By Day Of Year")
        }
        reminder.eventTime = dateTimeManager.getGmtFromDateTime(nextDateTime)
        nextDateTime
      }

      type.isGpsType() -> {
        val delayDate = (itemsMap[BiType.LOCATION_DELAY_DATE] as? LocationDelayDateBuilderItem)
          ?.modifier?.getValue()
        val delayTime = (itemsMap[BiType.LOCATION_DELAY_TIME] as? LocationDelayTimeBuilderItem)
          ?.modifier?.getValue()
        var hasDelay = false
        val delayDateTime = when {
          delayDate != null && delayTime != null -> {
            hasDelay = true
            LocalDateTime.of(delayDate, delayTime)
          }

          delayDate != null || delayTime != null -> {
            hasDelay = true
            null
          }

          else -> null
        }
        if (delayDateTime != null && dateTimeManager.isCurrent(delayDateTime)) {
          val startTime = delayDateTime.let { dateTimeManager.getGmtFromDateTime(it) }
          Logger.i(TAG, "Put the delay date time $delayDateTime")
          reminder.eventTime = startTime
          reminder.startTime = startTime
          reminder.hasReminder = true
          delayDateTime
        } else if (hasDelay && delayDateTime == null) {
          reminder.eventTime = ""
          reminder.startTime = ""
          reminder.hasReminder = true
          null
        } else {
          reminder.eventTime = ""
          reminder.startTime = ""
          reminder.hasReminder = false
          null
        }
      }

      type.isICalendar() && processedBuilderItems.groupMap.isNotEmpty() -> {
        iCalDateTimeInjector(reminder, processedBuilderItems).also {
          Logger.i(TAG, "Put date time $it for the ICalendar")
        }
      }

      else -> {
        Logger.i(TAG, "No date time to put for type $type")
        reminder.eventTime = ""
        reminder.startTime = ""
        null
      }
    }?.also { dateTime ->
      val startTime = dateTime.let { dateTimeManager.getGmtFromDateTime(it) }
      reminder.eventTime = startTime
      reminder.startTime = startTime
    }
  }

  companion object {
    private const val TAG = "DateTimeInjector"
  }
}

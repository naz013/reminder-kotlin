package com.github.naz013.feature.reminder.build.reminder.compose

import com.github.naz013.feature.reminder.util.IntervalUtil
import com.github.naz013.feature.reminder.build.ArrivingCoordinatesBuilderItem
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.DateBuilderItem
import com.github.naz013.feature.reminder.build.DayOfMonthBuilderItem
import com.github.naz013.feature.reminder.build.DayOfYearBuilderItem
import com.github.naz013.feature.reminder.build.DaysOfWeekBuilderItem
import com.github.naz013.feature.reminder.build.LeavingCoordinatesBuilderItem
import com.github.naz013.feature.reminder.build.LocationDelayDateBuilderItem
import com.github.naz013.feature.reminder.build.LocationDelayTimeBuilderItem
import com.github.naz013.feature.reminder.build.RepeatIntervalBuilderItem
import com.github.naz013.feature.reminder.build.RepeatLimitBuilderItem
import com.github.naz013.feature.reminder.build.RepeatTimeBuilderItem
import com.github.naz013.feature.reminder.build.TimeBuilderItem
import com.github.naz013.feature.reminder.build.TimerBuilderItem
import com.github.naz013.feature.reminder.build.bi.BiGroup
import com.github.naz013.feature.reminder.build.bi.ProcessedBuilderItems
import com.github.naz013.feature.reminder.build.reminder.ICalDateTimeCalculator
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.RecurrenceCalculator
import com.github.naz013.domain.Place
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.LocationSettings
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.logging.Logger
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

data class ComposedRecurrence(
  val rule: RecurrenceRule,
  val schedule: ReminderSchedule,
  val places: List<Place> = emptyList(),
  val location: LocationSettings? = null,
)

class RecurrenceRuleCalculator(
  private val dateTimeManager: DateTimeManager,
  private val iCalDateTimeCalculator: ICalDateTimeCalculator,
  private val recurrenceCalculator: RecurrenceCalculator,
) {
  operator fun invoke(processedBuilderItems: ProcessedBuilderItems): ComposedRecurrence? {
    val itemsMap = processedBuilderItems.typeMap

    val hasDateTime = itemsMap[BiType.DATE] != null && itemsMap[BiType.TIME] != null
    val hasTimer = itemsMap[BiType.COUNTDOWN_TIMER] != null
    val hasWeekdays = itemsMap[BiType.DAYS_OF_WEEK] != null
    val hasMonthDay = itemsMap[BiType.DAY_OF_MONTH] != null
    val hasDayOfYear = itemsMap[BiType.DAY_OF_YEAR] != null

    val isOnlyOneActive = isOnlyOneActive(hasDateTime, hasTimer, hasWeekdays, hasMonthDay, hasDayOfYear)

    val result =
      if (isOnlyOneActive) {
        when {
          hasDateTime -> fromDate(itemsMap)
          hasTimer -> fromTimer(itemsMap)
          hasWeekdays -> fromWeekdays(itemsMap)
          hasMonthDay -> fromMonthDay(itemsMap)
          hasDayOfYear -> fromDayOfYear(itemsMap)
          else -> null
        }
      } else if (isAllFalse(hasDateTime, hasTimer, hasWeekdays, hasMonthDay, hasDayOfYear)) {
        val hasLocationIn = itemsMap[BiType.ARRIVING_COORDINATES] != null
        val hasLocationOut = itemsMap[BiType.LEAVING_COORDINATES] != null
        val hasICalRecur = processedBuilderItems.groupMap.containsKey(BiGroup.ICAL)
        val hasLocationDelay =
          itemsMap[BiType.LOCATION_DELAY_DATE] != null || itemsMap[BiType.LOCATION_DELAY_TIME] != null
        val hasDate = itemsMap[BiType.DATE] != null
        val hasTime = itemsMap[BiType.TIME] != null
        val hasShop = itemsMap[BiType.SUB_TASKS] != null

        when {
          hasLocationIn -> fromLocation(itemsMap, RecurrenceRule.LocationEnter)
          hasLocationOut -> fromLocation(itemsMap, RecurrenceRule.LocationExit)
          hasLocationDelay -> fromLocation(itemsMap, RecurrenceRule.LocationEnter)
          hasICalRecur -> fromICalendar(processedBuilderItems)
          hasDate || hasTime || hasShop -> emptySchedule()
          else -> null
        }
      } else {
        null
      }

    Logger.i(TAG, "Calculated recurrence = ${result?.rule}")
    return result
  }

  private fun fromDate(itemsMap: Map<BiType, BuilderItem<*>>): ComposedRecurrence? {
    val date = (itemsMap[BiType.DATE] as? DateBuilderItem)?.modifier?.getValue() ?: return null
    val time = (itemsMap[BiType.TIME] as? TimeBuilderItem)?.modifier?.getValue() ?: return null
    val localDateTime = LocalDateTime.of(date, time)
    val utcDateTime = dateTimeManager.localToUtc(localDateTime)

    val repeatInterval = (itemsMap[BiType.REPEAT_TIME] as? RepeatTimeBuilderItem)?.modifier?.getValue()
    val repeatLimit = readRepeatLimit(itemsMap)

    val rule =
      if (repeatInterval != null && repeatInterval > 0) {
        RecurrenceRule.Daily(repeatInterval = repeatInterval, repeatLimit = repeatLimit)
      } else {
        RecurrenceRule.Once
      }

    return ComposedRecurrence(
      rule = rule,
      schedule = ReminderSchedule(startDateTime = utcDateTime, eventDateTime = utcDateTime),
    )
  }

  private fun fromTimer(itemsMap: Map<BiType, BuilderItem<*>>): ComposedRecurrence? {
    val after = (itemsMap[BiType.COUNTDOWN_TIMER] as? TimerBuilderItem)?.modifier?.getValue() ?: return null
    if (after == 0L) return null

    val repeatInterval = (itemsMap[BiType.REPEAT_TIME] as? RepeatTimeBuilderItem)?.modifier?.getValue() ?: 0L
    val repeatLimit = readRepeatLimit(itemsMap)

    val localDateTime = recurrenceCalculator.getStartTimerDateTime(countdownTimeInMillis = after)
    val utcDateTime = dateTimeManager.localToUtc(localDateTime)

    return ComposedRecurrence(
      rule = RecurrenceRule.Countdown(after = after, repeatInterval = repeatInterval, repeatLimit = repeatLimit),
      schedule = ReminderSchedule(startDateTime = utcDateTime, eventDateTime = utcDateTime),
    )
  }

  private fun fromWeekdays(itemsMap: Map<BiType, BuilderItem<*>>): ComposedRecurrence? {
    val time = (itemsMap[BiType.TIME] as? TimeBuilderItem)?.modifier?.getValue() ?: return null
    val weekdays = (itemsMap[BiType.DAYS_OF_WEEK] as? DaysOfWeekBuilderItem)?.modifier?.getValue()
    if (!IntervalUtil.isWeekday(weekdays)) return null
    val weekdaysNonNull = weekdays ?: return null

    val repeatLimit = readRepeatLimit(itemsMap)

    val localDateTime = LocalDateTime.of(LocalDate.now(), time)
    val nextLocal =
      recurrenceCalculator.findNextDayOfWeekDateTime(
        eventDateTime = localDateTime,
        weekdays = weekdaysNonNull,
        afterOrEqualDateTime = dateTimeManager.getCurrentDateTime(),
      )
    val utcDateTime = dateTimeManager.localToUtc(nextLocal)

    return ComposedRecurrence(
      rule = RecurrenceRule.Weekly(weekdays = weekdaysNonNull, repeatLimit = repeatLimit),
      schedule = ReminderSchedule(startDateTime = utcDateTime, eventDateTime = utcDateTime),
    )
  }

  private fun fromMonthDay(itemsMap: Map<BiType, BuilderItem<*>>): ComposedRecurrence? {
    val time = (itemsMap[BiType.TIME] as? TimeBuilderItem)?.modifier?.getValue() ?: return null
    val dayOfMonth = (itemsMap[BiType.DAY_OF_MONTH] as? DayOfMonthBuilderItem)?.modifier?.getValue() ?: return null

    val repeatInterval = (itemsMap[BiType.REPEAT_INTERVAL] as? RepeatIntervalBuilderItem)?.modifier?.getValue() ?: 1L
    val repeatLimit = readRepeatLimit(itemsMap)

    val localDateTime = LocalDateTime.of(LocalDate.now(), time)
    val nextLocal =
      recurrenceCalculator.findNextMonthDayDateTime(
        eventDateTime = localDateTime,
        dayOfMonth = dayOfMonth,
        interval = repeatInterval,
        afterOrEqualDateTime = dateTimeManager.getCurrentDateTime(),
      )
    val utcDateTime = dateTimeManager.localToUtc(nextLocal)

    return ComposedRecurrence(
      rule =
        RecurrenceRule.Monthly(
          dayOfMonth = dayOfMonth,
          repeatInterval = repeatInterval,
          repeatLimit = repeatLimit,
        ),
      schedule = ReminderSchedule(startDateTime = utcDateTime, eventDateTime = utcDateTime),
    )
  }

  private fun fromDayOfYear(itemsMap: Map<BiType, BuilderItem<*>>): ComposedRecurrence? {
    val time = (itemsMap[BiType.TIME] as? TimeBuilderItem)?.modifier?.getValue() ?: return null
    val dayOfYear = (itemsMap[BiType.DAY_OF_YEAR] as? DayOfYearBuilderItem)?.modifier?.getValue() ?: return null
    val derivedDate = runCatching { LocalDate.now().withDayOfYear(dayOfYear) }.getOrNull() ?: return null

    val repeatInterval = (itemsMap[BiType.REPEAT_INTERVAL] as? RepeatIntervalBuilderItem)?.modifier?.getValue() ?: 1L
    val repeatLimit = readRepeatLimit(itemsMap)

    val localDateTime = LocalDateTime.of(LocalDate.now(), time)
    val nextLocal =
      recurrenceCalculator.findNextYearDayDateTime(
        eventDateTime = localDateTime,
        monthOfYear = derivedDate.monthValue - 1,
        dayOfMonth = derivedDate.dayOfMonth,
        interval = repeatInterval,
        afterOrEqualDateTime = dateTimeManager.getCurrentDateTime(),
      )
    val utcDateTime = dateTimeManager.localToUtc(nextLocal)

    return ComposedRecurrence(
      rule =
        RecurrenceRule.Yearly(
          dayOfMonth = derivedDate.dayOfMonth,
          monthOfYear = derivedDate.monthValue - 1,
          repeatInterval = repeatInterval,
          repeatLimit = repeatLimit,
        ),
      schedule = ReminderSchedule(startDateTime = utcDateTime, eventDateTime = utcDateTime),
    )
  }

  private fun fromLocation(itemsMap: Map<BiType, BuilderItem<*>>, rule: RecurrenceRule): ComposedRecurrence {
    val place =
      (itemsMap[BiType.ARRIVING_COORDINATES] as? ArrivingCoordinatesBuilderItem)?.modifier?.getValue()
        ?: (itemsMap[BiType.LEAVING_COORDINATES] as? LeavingCoordinatesBuilderItem)?.modifier?.getValue()

    val delayDate = (itemsMap[BiType.LOCATION_DELAY_DATE] as? LocationDelayDateBuilderItem)?.modifier?.getValue()
    val delayTime = (itemsMap[BiType.LOCATION_DELAY_TIME] as? LocationDelayTimeBuilderItem)?.modifier?.getValue()
    val hasDelay = delayDate != null || delayTime != null
    val delayLocal = if (delayDate != null && delayTime != null) LocalDateTime.of(delayDate, delayTime) else null

    val nowUtc = dateTimeManager.localToUtc(dateTimeManager.getCurrentDateTime())

    return if (delayLocal != null && dateTimeManager.isCurrent(delayLocal)) {
      val delayUtc = dateTimeManager.localToUtc(delayLocal)
      ComposedRecurrence(
        rule = rule,
        schedule = ReminderSchedule(startDateTime = delayUtc, eventDateTime = delayUtc),
        places = listOfNotNull(place),
        location = LocationSettings(hasDelayedReminder = true),
      )
    } else {
      ComposedRecurrence(
        rule = rule,
        schedule = ReminderSchedule(startDateTime = nowUtc, eventDateTime = null),
        places = listOfNotNull(place),
        location = LocationSettings(hasDelayedReminder = hasDelay),
      )
    }
  }

  /** Neither a full date+time nor GPS/iCal recurrence was selected - either a partial date/time
   * pick (invalid, [EventTimeValidator] rejects it) or a shopping-list-only reminder with no due
   * date (valid, [EventTimeValidator] doesn't require a time for [ReminderAction.Shopping]). */
  private fun emptySchedule(): ComposedRecurrence {
    val nowUtc = dateTimeManager.localToUtc(dateTimeManager.getCurrentDateTime())
    return ComposedRecurrence(
      rule = RecurrenceRule.Once,
      schedule = ReminderSchedule(startDateTime = nowUtc, eventDateTime = null),
    )
  }

  private fun fromICalendar(processedBuilderItems: ProcessedBuilderItems): ComposedRecurrence? {
    val eventData = iCalDateTimeCalculator(processedBuilderItems) ?: return null
    val utcDateTime = dateTimeManager.localToUtc(eventData.startDateTime)

    return ComposedRecurrence(
      rule = RecurrenceRule.ICalendar(rrule = eventData.recurObject),
      schedule = ReminderSchedule(startDateTime = utcDateTime, eventDateTime = utcDateTime),
    )
  }

  private fun readRepeatLimit(itemsMap: Map<BiType, BuilderItem<*>>): Int =
    (itemsMap[BiType.REPEAT_LIMIT] as? RepeatLimitBuilderItem)?.modifier?.getValue() ?: -1

  private fun isOnlyOneActive(vararg booleans: Boolean): Boolean = booleans.filter { it }.size == 1

  private fun isAllFalse(vararg booleans: Boolean): Boolean = booleans.none { it }

  companion object {
    private const val TAG = "RecurrenceRuleCalculator"
  }
}

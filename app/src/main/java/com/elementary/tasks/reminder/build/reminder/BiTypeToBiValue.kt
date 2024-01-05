@file:Suppress("UNCHECKED_CAST")

package com.elementary.tasks.reminder.build.reminder

import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.datetime.recurrence.DayValue
import com.elementary.tasks.core.utils.datetime.recurrence.FreqType
import com.elementary.tasks.reminder.build.bi.BiType
import com.elementary.tasks.reminder.build.bi.CalendarDuration
import com.elementary.tasks.reminder.build.bi.OtherParams
import com.elementary.tasks.reminder.build.bi.TimerExclusion
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class BiTypeToBiValue {

  operator fun <V> invoke(biType: BiType, value: String): V? {
    println("BiTypeToBiValueClass: in = $value, type = $biType")
    if (value.isEmpty()) return null
    return when (biType) {
      BiType.DATE,
      BiType.LOCATION_DELAY_DATE,
      BiType.ICAL_START_DATE,
      BiType.ICAL_UNTIL_DATE -> LocalDate.parse(value) as? V

      BiType.TIME,
      BiType.LOCATION_DELAY_TIME,
      BiType.ICAL_START_TIME,
      BiType.ICAL_UNTIL_TIME -> LocalTime.parse(value) as? V

      BiType.DAYS_OF_WEEK,
      BiType.ICAL_BYMONTH,
      BiType.ICAL_BYMONTHDAY,
      BiType.ICAL_BYHOUR,
      BiType.ICAL_BYMINUTE,
      BiType.ICAL_BYYEARDAY,
      BiType.ICAL_BYWEEKNO,
      BiType.ICAL_BYSETPOS -> parseIntList(value) as? V

      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.REPEAT_LIMIT,
      BiType.ICAL_INTERVAL,
      BiType.ICAL_COUNT,
      BiType.PRIORITY,
      BiType.LED_COLOR,
      BiType.WINDOW_TYPE -> value.toInt() as? V

      BiType.COUNTDOWN_TIMER,
      BiType.BEFORE_TIME,
      BiType.REPEAT_TIME,
      BiType.REPEAT_INTERVAL -> value.toLong() as? V

      BiType.ARRIVING_COORDINATES,
      BiType.LEAVING_COORDINATES -> parsePlace(value) as? V

      BiType.SUMMARY,
      BiType.DESCRIPTION,
      BiType.PHONE_CALL,
      BiType.SMS,
      BiType.LINK,
      BiType.APPLICATION,
      BiType.EMAIL,
      BiType.EMAIL_SUBJECT,
      BiType.MELODY -> value as? V

      BiType.COUNTDOWN_TIMER_EXCLUSION -> parseTimerExclusion(value) as? V
      BiType.ICAL_FREQ -> parseFreqType(value) as? V
      BiType.ICAL_BYDAY -> parseDayValueList(value) as? V
      BiType.ICAL_WEEKSTART -> parseDayValue(value) as? V
      BiType.SUB_TASKS -> parseShopItemList(value) as? V
      BiType.GROUP -> parseUiGroupList(value) as? V
      BiType.ATTACHMENTS -> parseStringList(value) as? V
      BiType.OTHER_PARAMS -> parseOtherParams(value) as? V
      BiType.GOOGLE_TASK_LIST -> parseGoogleTaskList(value) as? V
      BiType.GOOGLE_CALENDAR -> parseCalendarItem(value) as? V
      BiType.GOOGLE_CALENDAR_DURATION -> parseCalendarDuration(value) as? V
    }
  }

  private fun parseCalendarDuration(value: String): CalendarDuration? {
    if (value.isEmpty()) {
      return null
    }
    return Gson().fromJson(value, object : TypeToken<CalendarDuration>() {}.type)
  }

  private fun parseCalendarItem(value: String): GoogleCalendarUtils.CalendarItem? {
    if (value.isEmpty()) {
      return null
    }
    return Gson().fromJson(value, object : TypeToken<GoogleCalendarUtils.CalendarItem>() {}.type)
  }

  private fun parseGoogleTaskList(value: String): GoogleTaskList? {
    if (value.isEmpty()) {
      return null
    }
    return Gson().fromJson(value, object : TypeToken<GoogleTaskList>() {}.type)
  }

  private fun parseOtherParams(value: String): OtherParams? {
    if (value.isEmpty()) {
      return null
    }
    return Gson().fromJson(value, object : TypeToken<OtherParams>() {}.type)
  }

  private fun parseUiGroupList(value: String): UiGroupList? {
    if (value.isEmpty()) {
      return null
    }
    return Gson().fromJson(value, object : TypeToken<UiGroupList>() {}.type)
  }

  private fun parseShopItemList(value: String): List<ShopItem> {
    return Gson().fromJson(value, object : TypeToken<List<ShopItem>>() {}.type)
  }

  private fun parseDayValue(value: String): DayValue? {
    return runCatching { DayValue(value) }.getOrNull()
  }

  private fun parseDayValueList(value: String): List<DayValue> {
    return Gson().fromJson(value, object : TypeToken<List<DayValue>>() {}.type)
  }

  private fun parseFreqType(value: String): FreqType {
    if (value.isEmpty()) {
      return FreqType.DAILY
    }
    return value.toIntOrNull()?.let { FreqType.entries[it] } ?: FreqType.DAILY
  }

  private fun parseTimerExclusion(value: String): TimerExclusion? {
    if (value.isEmpty()) {
      return null
    }
    return Gson().fromJson(value, object : TypeToken<TimerExclusion>() {}.type)
  }

  private fun parsePlace(value: String): Place? {
    if (value.isEmpty()) {
      return null
    }
    return Gson().fromJson(value, object : TypeToken<Place>() {}.type)
  }

  private fun parseIntList(value: String): List<Int> {
    return value.split(',').mapNotNull { it.toIntOrNull() }
  }

  private fun parseStringList(value: String): List<String> {
    return value.split(',')
  }
}

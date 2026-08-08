@file:Suppress("UNCHECKED_CAST")

package com.elementary.tasks.reminder.build.reminder

import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.reminder.build.bi.CalendarDuration
import com.elementary.tasks.reminder.build.bi.OtherParams
import com.elementary.tasks.reminder.build.bi.TimerExclusion
import com.elementary.tasks.reminder.build.preset.data.CalendarItemJson
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.domain.Place
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.googlecalendar.CalendarItem
import com.github.naz013.icalendar.DayValue
import com.github.naz013.icalendar.FreqType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class BiTypeToBiValue {
  operator fun <V> invoke(
    biType: BiType,
    value: String,
  ): V? {
    if (value.isEmpty()) return null
    return when (biType) {
      BiType.DATE,
      BiType.LOCATION_DELAY_DATE,
      BiType.ICAL_START_DATE,
      BiType.ICAL_UNTIL_DATE,
      -> LocalDate.parse(value) as? V

      BiType.TIME,
      BiType.LOCATION_DELAY_TIME,
      BiType.ICAL_START_TIME,
      BiType.ICAL_UNTIL_TIME,
      -> LocalTime.parse(value) as? V

      BiType.DAYS_OF_WEEK,
      BiType.ICAL_BYMONTH,
      BiType.ICAL_BYMONTHDAY,
      BiType.ICAL_BYHOUR,
      BiType.ICAL_BYMINUTE,
      BiType.ICAL_BYYEARDAY,
      BiType.ICAL_BYWEEKNO,
      BiType.ICAL_BYSETPOS,
      -> parseIntList(value) as? V

      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.REPEAT_LIMIT,
      BiType.ICAL_INTERVAL,
      BiType.ICAL_COUNT,
      BiType.PRIORITY,
      BiType.LED_COLOR,
      BiType.CATEGORY,
      BiType.LOCK_SCREEN_VISIBILITY,
      BiType.DELAY_MINUTES,
      -> value.toInt() as? V

      BiType.COUNTDOWN_TIMER,
      BiType.BEFORE_TIME,
      BiType.REPEAT_TIME,
      BiType.REPEAT_INTERVAL,
      -> value.toLong() as? V

      BiType.BYPASS_DND,
      BiType.WAKE_SCREEN,
      -> value.toBoolean() as? V

      BiType.VIBRATION_PATTERN,
      -> parseLongList(value) as? V

      BiType.ARRIVING_COORDINATES,
      BiType.LEAVING_COORDINATES,
      -> parsePlace(value) as? V

      BiType.SUMMARY,
      BiType.DESCRIPTION,
      BiType.PHONE_CALL,
      BiType.SMS,
      BiType.LINK,
      BiType.APPLICATION,
      BiType.EMAIL,
      BiType.EMAIL_SUBJECT,
      -> value as? V

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
      BiType.NOTE -> parseUiNoteList(value) as? V
    }
  }

  private fun parseCalendarDuration(value: String): CalendarDuration? {
    if (value.isEmpty()) {
      return null
    }
    return Gson().fromJson(value, object : TypeToken<CalendarDuration>() {}.type)
  }

  private fun parseCalendarItem(value: String): CalendarItem? {
    if (value.isEmpty()) {
      return null
    }
    return Gson().fromJson<CalendarItemJson>(value, object : TypeToken<CalendarItemJson>() {}.type)?.let {
      CalendarItem(
        id = it.id,
        name = it.name,
      )
    }
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

  private fun parseUiNoteList(value: String): UiNoteList? {
    if (value.isEmpty()) {
      return null
    }
    return Gson().fromJson(value, object : TypeToken<UiNoteList>() {}.type)
  }

  private fun parseShopItemList(value: String): List<ShopItem> = Gson().fromJson(value, object : TypeToken<List<ShopItem>>() {}.type)

  private fun parseDayValue(value: String): DayValue? = runCatching { DayValue(value) }.getOrNull()

  private fun parseDayValueList(value: String): List<DayValue> = Gson().fromJson(value, object : TypeToken<List<DayValue>>() {}.type)

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

  private fun parseIntList(value: String): List<Int> = value.split(',').mapNotNull { it.toIntOrNull() }

  private fun parseLongList(value: String): List<Long> = value.split(',').mapNotNull { it.toLongOrNull() }

  private fun parseStringList(value: String): List<String> = value.split(',')
}

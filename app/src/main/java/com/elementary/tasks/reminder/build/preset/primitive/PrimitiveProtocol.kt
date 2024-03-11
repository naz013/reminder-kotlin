package com.elementary.tasks.reminder.build.preset.primitive

import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.datetime.recurrence.DayValue
import com.elementary.tasks.core.utils.datetime.recurrence.FreqType
import com.elementary.tasks.reminder.build.bi.CalendarDuration
import com.elementary.tasks.reminder.build.bi.OtherParams
import com.elementary.tasks.reminder.build.bi.TimerExclusion
import com.google.gson.Gson
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class PrimitiveProtocol {

  fun asString(any: Any): String {
    return when (any) {
      is Int -> any.asString()
      is Long -> any.asString()
      is Boolean -> any.asString()
      is LocalTime -> any.asString()
      is LocalDate -> any.asString()
      is List<*> -> any.asString()
      is Place -> any.asString()
      is TimerExclusion -> any.asString()
      is FreqType -> any.asString()
      is DayValue -> any.asString()
      is UiGroupList -> any.asString()
      is OtherParams -> any.asString()
      is GoogleTaskList -> any.asString()
      is GoogleCalendarUtils.CalendarItem -> any.asString()
      is CalendarDuration -> any.asString()
      is String -> any
      else -> ""
    }
  }

  private fun CalendarDuration.asString(): String {
    return Gson().toJson(this)
  }

  private fun GoogleCalendarUtils.CalendarItem.asString(): String {
    return Gson().toJson(this)
  }

  private fun GoogleTaskList.asString(): String {
    return Gson().toJson(this)
  }

  private fun OtherParams.asString(): String {
    return Gson().toJson(this)
  }

  private fun UiGroupList.asString(): String {
    return Gson().toJson(this)
  }

  private fun DayValue.asString(): String {
    return value
  }

  private fun FreqType.asString(): String {
    return ordinal.asString()
  }

  private fun TimerExclusion.asString(): String {
    return Gson().toJson(this)
  }

  private fun Place.asString(): String {
    return Gson().toJson(this)
  }

  private fun List<*>.asString(): String {
    return joinToString(",") { asString(it ?: "") }
  }

  private fun LocalTime.asString(): String {
    return toString()
  }

  private fun LocalDate.asString(): String {
    return toString()
  }

  private fun Int.asString(): String {
    return toString()
  }

  private fun Long.asString(): String {
    return toString()
  }

  private fun Boolean.asString(): String {
    return toString()
  }
}

package com.elementary.tasks.reminder.build.preset.primitive

import com.github.naz013.ui.group.UiGroupList
import com.elementary.tasks.reminder.build.bi.CalendarDuration
import com.elementary.tasks.reminder.build.bi.OtherParams
import com.elementary.tasks.reminder.build.bi.TimerExclusion
import com.elementary.tasks.reminder.build.preset.data.CalendarItemJson
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.domain.Place
import com.github.naz013.googlecalendar.CalendarItem
import com.github.naz013.icalendar.DayValue
import com.github.naz013.icalendar.FreqType
import com.google.gson.Gson
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class PrimitiveProtocol {
  fun asString(any: Any): String =
    when (any) {
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
      is CalendarItem -> any.toJson().asString()
      is CalendarDuration -> any.asString()
      is String -> any
      else -> ""
    }

  private fun CalendarDuration.asString(): String = Gson().toJson(this)

  private fun CalendarItemJson.asString(): String = Gson().toJson(this)

  private fun GoogleTaskList.asString(): String = Gson().toJson(this)

  private fun OtherParams.asString(): String = Gson().toJson(this)

  private fun UiGroupList.asString(): String = Gson().toJson(this)

  private fun DayValue.asString(): String = value

  private fun FreqType.asString(): String = ordinal.asString()

  private fun TimerExclusion.asString(): String = Gson().toJson(this)

  private fun Place.asString(): String = Gson().toJson(this)

  private fun List<*>.asString(): String = joinToString(",") { asString(it ?: "") }

  private fun LocalTime.asString(): String = toString()

  private fun LocalDate.asString(): String = toString()

  private fun Int.asString(): String = toString()

  private fun Long.asString(): String = toString()

  private fun Boolean.asString(): String = toString()

  private fun CalendarItem.toJson(): CalendarItemJson {
    return CalendarItemJson(
      name = this.name,
      id = this.id,
    )
  }
}

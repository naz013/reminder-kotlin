package com.elementary.tasks.reminder.create.fragments.recur.adapter

import com.elementary.tasks.R
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.icalendar.Day
import com.github.naz013.icalendar.DayValue
import com.github.naz013.icalendar.FreqType
import com.github.naz013.icalendar.RecurParamType
import com.github.naz013.icalendar.UtcDateTime
import com.elementary.tasks.reminder.create.fragments.recur.BuilderParam
import com.github.naz013.common.TextProvider

class ParamToTextAdapter(
  private val textProvider: TextProvider,
  private val dateTimeManager: DateTimeManager
) {

  fun createTextWithValues(param: BuilderParam<*>): String {
    return "${getTypeText(param.recurParamType)}: ${getValueText(param.value)}"
  }

  fun createText(param: BuilderParam<*>): String {
    return getTypeText(param.recurParamType)
  }

  fun getTypeText(recurParamType: RecurParamType): String {
    return when (recurParamType) {
      RecurParamType.COUNT -> textProvider.getText(R.string.recur_count)
      RecurParamType.INTERVAL -> textProvider.getText(R.string.recur_interval)
      RecurParamType.FREQ -> textProvider.getText(R.string.recur_frequency)
      RecurParamType.UNTIL -> textProvider.getText(R.string.recur_until)
      RecurParamType.BYDAY -> textProvider.getText(R.string.recur_day_s)
      RecurParamType.BYMONTH -> textProvider.getText(R.string.recur_month_s)
      RecurParamType.BYMONTHDAY -> textProvider.getText(R.string.recur_day_s_of_month)
      RecurParamType.BYHOUR -> textProvider.getText(R.string.recur_hour_s)
      RecurParamType.BYMINUTE -> textProvider.getText(R.string.recur_minute_s)
      RecurParamType.BYYEARDAY -> textProvider.getText(R.string.recur_day_s_of_year)
      RecurParamType.BYWEEKNO -> textProvider.getText(R.string.recur_week_number_s)
      RecurParamType.BYSETPOS -> textProvider.getText(R.string.recur_set_pos)
      RecurParamType.WEEKSTART -> textProvider.getText(R.string.recur_week_start)
    }
  }

  private fun getValueText(value: Any?): String {
    if (value == null) return ""

    return when (value) {
      is FreqType -> getFreqText(value)
      is Int -> getIntText(value)
      is UtcDateTime -> getDateTimeText(value)
      is List<*> -> getListText(value)
      is DayValue -> getDayText(value)
      else -> value.toString()
    }
  }

  private fun getListText(list: List<*>): String {
    return list.joinToString(",") {
      when (it) {
        is Int -> getIntText(it)
        is DayValue -> getDayText(it)
        else -> it.toString()
      }
    }
  }

  private fun getDateTimeText(utcDateTime: UtcDateTime): String {
    return utcDateTime.dateTime?.let {
      dateTimeManager.getFullDateTime(it)
    } ?: textProvider.getText(R.string.recur_not_set)
  }

  private fun getIntText(int: Int): String {
    return int.toString()
  }

  private fun getDayText(dayValue: DayValue): String {
    return dayValue.value
  }

  fun getDayFullText(dayValue: DayValue): String {
    return when (dayValue.day) {
      Day.MO -> textProvider.getText(R.string.recur_monday)
      Day.TU -> textProvider.getText(R.string.recur_tuesday)
      Day.WE -> textProvider.getText(R.string.recur_wednesday)
      Day.TH -> textProvider.getText(R.string.recur_thursday)
      Day.FR -> textProvider.getText(R.string.recur_friday)
      Day.SA -> textProvider.getText(R.string.recur_saturday)
      Day.SU -> textProvider.getText(R.string.recur_sunday)
      else -> dayValue.value
    }
  }

  fun getFreqText(freqType: FreqType): String {
    return when (freqType) {
      FreqType.DAILY -> textProvider.getText(R.string.recur_daily)
      FreqType.WEEKLY -> textProvider.getText(R.string.recur_weekly)
      FreqType.YEARLY -> textProvider.getText(R.string.recur_yearly)
      FreqType.MONTHLY -> textProvider.getText(R.string.recur_monthly)
      FreqType.HOURLY -> textProvider.getText(R.string.recur_hourly)
      FreqType.MINUTELY -> textProvider.getText(R.string.recur_minutely)
    }
  }
}

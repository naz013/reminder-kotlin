package com.elementary.tasks.reminder.build.adapter

import com.elementary.tasks.R
import com.github.naz013.common.TextProvider
import com.github.naz013.icalendar.Day
import com.github.naz013.icalendar.DayValue
import com.github.naz013.icalendar.FreqType
import com.github.naz013.icalendar.RecurParamType

class ParamToTextAdapter(
  private val textProvider: TextProvider
) {

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

package com.elementary.tasks.reminder.build.adapter

import android.content.Context
import com.elementary.tasks.R
import com.github.naz013.icalendar.RecurParamType
import com.elementary.tasks.reminder.build.bi.BiGroup
import com.github.naz013.domain.reminder.BiType
import com.elementary.tasks.reminder.create.fragments.recur.adapter.ParamToTextAdapter

class BiTypeForUiAdapter(
  private val context: Context,
  private val paramToTextAdapter: ParamToTextAdapter
) {

  fun getUiString(biType: BiType): String {
    return when (biType) {
      BiType.DATE -> context.getString(R.string.builder_date)
      BiType.TIME -> context.getString(R.string.time)
      BiType.SUMMARY -> context.getString(R.string.builder_summary)
      BiType.DESCRIPTION -> context.getString(R.string.builder_details)
      BiType.DAY_OF_YEAR -> context.getString(R.string.builder_day_of_year)
      BiType.DAY_OF_MONTH -> context.getString(R.string.day_of_month)
      BiType.DAYS_OF_WEEK -> context.getString(R.string.builder_days_of_week)
      BiType.COUNTDOWN_TIMER -> context.getString(R.string.builder_countdown)
      BiType.COUNTDOWN_TIMER_EXCLUSION -> context.getString(R.string.builder_countdown_exclusion)
      BiType.GROUP -> context.getString(R.string.group)
      BiType.BEFORE_TIME -> context.getString(R.string.before_time)
      BiType.REPEAT_TIME -> context.getString(R.string.repeat)
      BiType.REPEAT_INTERVAL -> context.getString(R.string.builder_repeat_interval)
      BiType.REPEAT_LIMIT -> context.getString(R.string.repeat_limit)
      BiType.PRIORITY -> context.getString(R.string.priority)
      BiType.LED_COLOR -> context.getString(R.string.led_color)
      BiType.ATTACHMENTS -> context.getString(R.string.builder_attachments)
      BiType.PHONE_CALL -> context.getString(R.string.make_call)
      BiType.SMS -> context.getString(R.string.send_sms)
      BiType.GOOGLE_TASK_LIST -> context.getString(R.string.add_to_google_tasks)
      BiType.GOOGLE_CALENDAR -> context.getString(R.string.add_to_calendar)
      BiType.GOOGLE_CALENDAR_DURATION -> context.getString(R.string.event_duration)
      BiType.EMAIL -> context.getString(R.string.e_mail)
      BiType.LINK -> context.getString(R.string.builder_web_address)
      BiType.EMAIL_SUBJECT -> context.getString(R.string.subject)
      BiType.APPLICATION -> context.getString(R.string.application)
      BiType.OTHER_PARAMS -> context.getString(R.string.builder_additional_parameters)
      BiType.SUB_TASKS -> context.getString(R.string.builder_sub_tasks)
      BiType.ARRIVING_COORDINATES -> {
        context.getString(R.string.builder_arriving_destination)
      }

      BiType.LEAVING_COORDINATES -> {
        context.getString(R.string.builder_leaving_place)
      }

      BiType.LOCATION_DELAY_DATE -> {
        context.getString(R.string.builder_delay_date)
      }

      BiType.LOCATION_DELAY_TIME -> {
        context.getString(R.string.builder_delay_time)
      }

      BiType.ICAL_START_TIME -> {
        iCalendarPrefix() + " " + context.getString(R.string.builder_start_time)
      }

      BiType.ICAL_START_DATE -> {
        iCalendarPrefix() + " " + context.getString(R.string.builder_start_date)
      }

      BiType.ICAL_UNTIL_DATE -> {
        iCalendarPrefix() + " " + context.getString(R.string.builder_until_date)
      }

      BiType.ICAL_UNTIL_TIME -> {
        iCalendarPrefix() + " " + context.getString(R.string.builder_until_time)
      }

      BiType.NOTE -> {
        context.getString(R.string.note)
      }

      else -> {
        val recurParamType = biType.toRecurParamType()
        if (BiGroup.ICAL.types.contains(biType) && recurParamType != null) {
          iCalendarPrefix() + " " + paramToTextAdapter.getTypeText(recurParamType)
        } else {
          "NA"
        }
      }
    }
  }

  private fun iCalendarPrefix(): String {
    return context.getString(R.string.builder_icalendar)
  }

  private fun BiType.toRecurParamType(): RecurParamType? {
    return when (this) {
      BiType.ICAL_BYWEEKNO -> RecurParamType.BYWEEKNO
      BiType.ICAL_BYYEARDAY -> RecurParamType.BYYEARDAY
      BiType.ICAL_BYHOUR -> RecurParamType.BYHOUR
      BiType.ICAL_BYMINUTE -> RecurParamType.BYMINUTE
      BiType.ICAL_BYDAY -> RecurParamType.BYDAY
      BiType.ICAL_WEEKSTART -> RecurParamType.WEEKSTART
      BiType.ICAL_BYMONTH -> RecurParamType.BYMONTH
      BiType.ICAL_BYMONTHDAY -> RecurParamType.BYMONTHDAY
      BiType.ICAL_COUNT -> RecurParamType.COUNT
      BiType.ICAL_INTERVAL -> RecurParamType.INTERVAL
      BiType.ICAL_BYSETPOS -> RecurParamType.BYSETPOS
      BiType.ICAL_FREQ -> RecurParamType.FREQ
      else -> null
    }
  }
}

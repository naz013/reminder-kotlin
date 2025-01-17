package com.elementary.tasks.reminder.build.adapter

import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.bi.BiGroup
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.icalendar.RecurParamType

class BiTypeForUiAdapter(
  private val textProvider: TextProvider,
  private val paramToTextAdapter: ParamToTextAdapter
) {

  fun getUiString(biType: BiType): String {
    return when (biType) {
      BiType.DATE -> textProvider.getString(R.string.builder_date)
      BiType.TIME -> textProvider.getString(R.string.time)
      BiType.SUMMARY -> textProvider.getString(R.string.builder_summary)
      BiType.DESCRIPTION -> textProvider.getString(R.string.builder_details)
      BiType.DAY_OF_YEAR -> textProvider.getString(R.string.builder_day_of_year)
      BiType.DAY_OF_MONTH -> textProvider.getString(R.string.day_of_month)
      BiType.DAYS_OF_WEEK -> textProvider.getString(R.string.builder_days_of_week)
      BiType.COUNTDOWN_TIMER -> textProvider.getString(R.string.builder_countdown)
      BiType.COUNTDOWN_TIMER_EXCLUSION -> {
        textProvider.getString(R.string.builder_countdown_exclusion)
      }

      BiType.GROUP -> textProvider.getString(R.string.group)
      BiType.BEFORE_TIME -> textProvider.getString(R.string.before_time)
      BiType.REPEAT_TIME -> textProvider.getString(R.string.repeat)
      BiType.REPEAT_INTERVAL -> textProvider.getString(R.string.builder_repeat_interval)
      BiType.REPEAT_LIMIT -> textProvider.getString(R.string.repeat_limit)
      BiType.PRIORITY -> textProvider.getString(R.string.priority)
      BiType.LED_COLOR -> textProvider.getString(R.string.led_color)
      BiType.ATTACHMENTS -> textProvider.getString(R.string.builder_attachments)
      BiType.PHONE_CALL -> textProvider.getString(R.string.make_call)
      BiType.SMS -> textProvider.getString(R.string.send_sms)
      BiType.GOOGLE_TASK_LIST -> textProvider.getString(R.string.add_to_google_tasks)
      BiType.GOOGLE_CALENDAR -> textProvider.getString(R.string.add_to_calendar)
      BiType.GOOGLE_CALENDAR_DURATION -> textProvider.getString(R.string.event_duration)
      BiType.EMAIL -> textProvider.getString(R.string.e_mail)
      BiType.LINK -> textProvider.getString(R.string.builder_web_address)
      BiType.EMAIL_SUBJECT -> textProvider.getString(R.string.subject)
      BiType.APPLICATION -> textProvider.getString(R.string.application)
      BiType.OTHER_PARAMS -> textProvider.getString(R.string.builder_additional_parameters)
      BiType.SUB_TASKS -> textProvider.getString(R.string.builder_sub_tasks)
      BiType.ARRIVING_COORDINATES -> {
        textProvider.getString(R.string.builder_arriving_destination)
      }

      BiType.LEAVING_COORDINATES -> {
        textProvider.getString(R.string.builder_leaving_place)
      }

      BiType.LOCATION_DELAY_DATE -> {
        textProvider.getString(R.string.builder_delay_date)
      }

      BiType.LOCATION_DELAY_TIME -> {
        textProvider.getString(R.string.builder_delay_time)
      }

      BiType.ICAL_START_TIME -> {
        iCalendarPrefix() + " " + textProvider.getString(R.string.builder_start_time)
      }

      BiType.ICAL_START_DATE -> {
        iCalendarPrefix() + " " + textProvider.getString(R.string.builder_start_date)
      }

      BiType.ICAL_UNTIL_DATE -> {
        iCalendarPrefix() + " " + textProvider.getString(R.string.builder_until_date)
      }

      BiType.ICAL_UNTIL_TIME -> {
        iCalendarPrefix() + " " + textProvider.getString(R.string.builder_until_time)
      }

      BiType.NOTE -> {
        textProvider.getString(R.string.note)
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
    return textProvider.getString(R.string.builder_icalendar)
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

package com.elementary.tasks.reminder.build.preset

import com.elementary.tasks.core.data.models.PresetBuilderScheme
import com.elementary.tasks.core.data.models.RecurPreset
import com.elementary.tasks.reminder.build.ApplicationBuilderItem
import com.elementary.tasks.reminder.build.ArrivingCoordinatesBuilderItem
import com.elementary.tasks.reminder.build.AttachmentsBuilderItem
import com.elementary.tasks.reminder.build.BeforeTimeBuilderItem
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.DateBuilderItem
import com.elementary.tasks.reminder.build.DayOfMonthBuilderItem
import com.elementary.tasks.reminder.build.DayOfYearBuilderItem
import com.elementary.tasks.reminder.build.DaysOfWeekBuilderItem
import com.elementary.tasks.reminder.build.DescriptionBuilderItem
import com.elementary.tasks.reminder.build.EmailBuilderItem
import com.elementary.tasks.reminder.build.EmailSubjectBuilderItem
import com.elementary.tasks.reminder.build.GoogleCalendarBuilderItem
import com.elementary.tasks.reminder.build.GoogleCalendarDurationBuilderItem
import com.elementary.tasks.reminder.build.GoogleTaskListBuilderItem
import com.elementary.tasks.reminder.build.GroupBuilderItem
import com.elementary.tasks.reminder.build.ICalByDayBuilderItem
import com.elementary.tasks.reminder.build.ICalByHourBuilderItem
import com.elementary.tasks.reminder.build.ICalByMinuteBuilderItem
import com.elementary.tasks.reminder.build.ICalByMonthBuilderItem
import com.elementary.tasks.reminder.build.ICalByMonthDayBuilderItem
import com.elementary.tasks.reminder.build.ICalBySetPosBuilderItem
import com.elementary.tasks.reminder.build.ICalByWeekNoBuilderItem
import com.elementary.tasks.reminder.build.ICalByYearDayBuilderItem
import com.elementary.tasks.reminder.build.ICalCountBuilderItem
import com.elementary.tasks.reminder.build.ICalFrequencyBuilderItem
import com.elementary.tasks.reminder.build.ICalIntervalBuilderItem
import com.elementary.tasks.reminder.build.ICalStartDateBuilderItem
import com.elementary.tasks.reminder.build.ICalStartTimeBuilderItem
import com.elementary.tasks.reminder.build.ICalUntilDateBuilderItem
import com.elementary.tasks.reminder.build.ICalUntilTimeBuilderItem
import com.elementary.tasks.reminder.build.ICalWeekStartBuilderItem
import com.elementary.tasks.reminder.build.LeavingCoordinatesBuilderItem
import com.elementary.tasks.reminder.build.LedColorBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayDateBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayTimeBuilderItem
import com.elementary.tasks.reminder.build.NoteBuilderItem
import com.elementary.tasks.reminder.build.OtherParamsBuilderItem
import com.elementary.tasks.reminder.build.PhoneCallBuilderItem
import com.elementary.tasks.reminder.build.PriorityBuilderItem
import com.elementary.tasks.reminder.build.RepeatIntervalBuilderItem
import com.elementary.tasks.reminder.build.RepeatLimitBuilderItem
import com.elementary.tasks.reminder.build.RepeatTimeBuilderItem
import com.elementary.tasks.reminder.build.SmsBuilderItem
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.elementary.tasks.reminder.build.SummaryBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.TimerBuilderItem
import com.elementary.tasks.reminder.build.TimerExclusionBuilderItem
import com.elementary.tasks.reminder.build.WebAddressBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.bi.BiType.APPLICATION
import com.elementary.tasks.reminder.build.bi.BiType.ARRIVING_COORDINATES
import com.elementary.tasks.reminder.build.bi.BiType.ATTACHMENTS
import com.elementary.tasks.reminder.build.bi.BiType.BEFORE_TIME
import com.elementary.tasks.reminder.build.bi.BiType.COUNTDOWN_TIMER
import com.elementary.tasks.reminder.build.bi.BiType.COUNTDOWN_TIMER_EXCLUSION
import com.elementary.tasks.reminder.build.bi.BiType.DATE
import com.elementary.tasks.reminder.build.bi.BiType.DAYS_OF_WEEK
import com.elementary.tasks.reminder.build.bi.BiType.DAY_OF_MONTH
import com.elementary.tasks.reminder.build.bi.BiType.DAY_OF_YEAR
import com.elementary.tasks.reminder.build.bi.BiType.DESCRIPTION
import com.elementary.tasks.reminder.build.bi.BiType.EMAIL
import com.elementary.tasks.reminder.build.bi.BiType.EMAIL_SUBJECT
import com.elementary.tasks.reminder.build.bi.BiType.GOOGLE_CALENDAR
import com.elementary.tasks.reminder.build.bi.BiType.GOOGLE_CALENDAR_DURATION
import com.elementary.tasks.reminder.build.bi.BiType.GOOGLE_TASK_LIST
import com.elementary.tasks.reminder.build.bi.BiType.GROUP
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_BYDAY
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_BYHOUR
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_BYMINUTE
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_BYMONTH
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_BYMONTHDAY
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_BYSETPOS
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_BYWEEKNO
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_BYYEARDAY
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_COUNT
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_FREQ
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_INTERVAL
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_START_DATE
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_START_TIME
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_UNTIL_DATE
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_UNTIL_TIME
import com.elementary.tasks.reminder.build.bi.BiType.ICAL_WEEKSTART
import com.elementary.tasks.reminder.build.bi.BiType.LEAVING_COORDINATES
import com.elementary.tasks.reminder.build.bi.BiType.LED_COLOR
import com.elementary.tasks.reminder.build.bi.BiType.LINK
import com.elementary.tasks.reminder.build.bi.BiType.LOCATION_DELAY_DATE
import com.elementary.tasks.reminder.build.bi.BiType.LOCATION_DELAY_TIME
import com.elementary.tasks.reminder.build.bi.BiType.NOTE
import com.elementary.tasks.reminder.build.bi.BiType.OTHER_PARAMS
import com.elementary.tasks.reminder.build.bi.BiType.PHONE_CALL
import com.elementary.tasks.reminder.build.bi.BiType.PRIORITY
import com.elementary.tasks.reminder.build.bi.BiType.REPEAT_INTERVAL
import com.elementary.tasks.reminder.build.bi.BiType.REPEAT_LIMIT
import com.elementary.tasks.reminder.build.bi.BiType.REPEAT_TIME
import com.elementary.tasks.reminder.build.bi.BiType.SMS
import com.elementary.tasks.reminder.build.bi.BiType.SUB_TASKS
import com.elementary.tasks.reminder.build.bi.BiType.SUMMARY
import com.elementary.tasks.reminder.build.bi.BiType.TIME
import com.elementary.tasks.reminder.build.reminder.BiTypeToBiValue

class BuilderPresetToBiAdapter(
  private val biFactory: BiFactory,
  private val biTypeToBiValue: BiTypeToBiValue
) {

  operator fun invoke(preset: RecurPreset): List<BuilderItem<*>> {
    return preset.builderScheme.mapNotNull { tryToBuilderItem(it) }
  }

  private fun tryToBuilderItem(scheme: PresetBuilderScheme): BuilderItem<*>? {
    return runCatching { toBuilderItem(scheme) }.getOrNull()
  }

  private fun toBuilderItem(scheme: PresetBuilderScheme): BuilderItem<*>? {
    val type = scheme.type
    return when (type) {
      DATE -> create(scheme, DateBuilderItem::class.java)
      TIME -> create(scheme, TimeBuilderItem::class.java)
      DAYS_OF_WEEK -> create(scheme, DaysOfWeekBuilderItem::class.java)
      DAY_OF_MONTH -> create(scheme, DayOfMonthBuilderItem::class.java)
      DAY_OF_YEAR -> create(scheme, DayOfYearBuilderItem::class.java)
      COUNTDOWN_TIMER -> create(scheme, TimerBuilderItem::class.java)
      ARRIVING_COORDINATES -> create(scheme, ArrivingCoordinatesBuilderItem::class.java)
      LEAVING_COORDINATES -> create(scheme, LeavingCoordinatesBuilderItem::class.java)
      SUMMARY -> create(scheme, SummaryBuilderItem::class.java)
      COUNTDOWN_TIMER_EXCLUSION -> create(scheme, TimerExclusionBuilderItem::class.java)
      BEFORE_TIME -> create(scheme, BeforeTimeBuilderItem::class.java)
      REPEAT_TIME -> create(scheme, RepeatTimeBuilderItem::class.java)
      REPEAT_INTERVAL -> create(scheme, RepeatIntervalBuilderItem::class.java)
      REPEAT_LIMIT -> create(scheme, RepeatLimitBuilderItem::class.java)
      LOCATION_DELAY_DATE -> create(scheme, LocationDelayDateBuilderItem::class.java)
      LOCATION_DELAY_TIME -> create(scheme, LocationDelayTimeBuilderItem::class.java)
      ICAL_START_DATE -> create(scheme, ICalStartDateBuilderItem::class.java)
      ICAL_START_TIME -> create(scheme, ICalStartTimeBuilderItem::class.java)
      ICAL_FREQ -> create(scheme, ICalFrequencyBuilderItem::class.java)
      ICAL_INTERVAL -> create(scheme, ICalIntervalBuilderItem::class.java)
      ICAL_COUNT -> create(scheme, ICalCountBuilderItem::class.java)
      ICAL_UNTIL_DATE -> create(scheme, ICalUntilDateBuilderItem::class.java)
      ICAL_UNTIL_TIME -> create(scheme, ICalUntilTimeBuilderItem::class.java)
      ICAL_BYMONTH -> create(scheme, ICalByMonthBuilderItem::class.java)
      ICAL_BYDAY -> create(scheme, ICalByDayBuilderItem::class.java)
      ICAL_BYMONTHDAY -> create(scheme, ICalByMonthDayBuilderItem::class.java)
      ICAL_BYHOUR -> create(scheme, ICalByHourBuilderItem::class.java)
      ICAL_BYMINUTE -> create(scheme, ICalByMinuteBuilderItem::class.java)
      ICAL_BYYEARDAY -> create(scheme, ICalByYearDayBuilderItem::class.java)
      ICAL_BYWEEKNO -> create(scheme, ICalByWeekNoBuilderItem::class.java)
      ICAL_BYSETPOS -> create(scheme, ICalBySetPosBuilderItem::class.java)
      ICAL_WEEKSTART -> create(scheme, ICalWeekStartBuilderItem::class.java)
      DESCRIPTION -> create(scheme, DescriptionBuilderItem::class.java)
      SUB_TASKS -> create(scheme, SubTasksBuilderItem::class.java)
      PHONE_CALL -> create(scheme, PhoneCallBuilderItem::class.java)
      SMS -> create(scheme, SmsBuilderItem::class.java)
      LINK -> create(scheme, WebAddressBuilderItem::class.java)
      APPLICATION -> create(scheme, ApplicationBuilderItem::class.java)
      EMAIL -> create(scheme, EmailBuilderItem::class.java)
      EMAIL_SUBJECT -> create(scheme, EmailSubjectBuilderItem::class.java)
      GROUP -> create(scheme, GroupBuilderItem::class.java)
      PRIORITY -> create(scheme, PriorityBuilderItem::class.java)
      LED_COLOR -> create(scheme, LedColorBuilderItem::class.java)
      ATTACHMENTS -> create(scheme, AttachmentsBuilderItem::class.java)
      OTHER_PARAMS -> create(scheme, OtherParamsBuilderItem::class.java)
      GOOGLE_TASK_LIST -> create(scheme, GoogleTaskListBuilderItem::class.java)
      GOOGLE_CALENDAR -> create(scheme, GoogleCalendarBuilderItem::class.java)
      GOOGLE_CALENDAR_DURATION -> create(scheme, GoogleCalendarDurationBuilderItem::class.java)
      NOTE -> create(scheme, NoteBuilderItem::class.java)
    }
  }

  private inline fun <reified V, reified T : BuilderItem<V>> create(
    scheme: PresetBuilderScheme,
    clazz: Class<T>
  ): T? {
    val type = scheme.type
    return biFactory.createWithValue(
      biType = type,
      value = biTypeToBiValue(type, scheme.value),
      clazz = clazz
    )
  }
}

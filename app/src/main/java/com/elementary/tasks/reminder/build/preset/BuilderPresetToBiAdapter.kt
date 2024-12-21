package com.elementary.tasks.reminder.build.preset

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
import com.elementary.tasks.reminder.build.reminder.BiTypeToBiValue
import com.github.naz013.domain.PresetBuilderScheme
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.reminder.BiType.APPLICATION
import com.github.naz013.domain.reminder.BiType.ARRIVING_COORDINATES
import com.github.naz013.domain.reminder.BiType.ATTACHMENTS
import com.github.naz013.domain.reminder.BiType.BEFORE_TIME
import com.github.naz013.domain.reminder.BiType.COUNTDOWN_TIMER
import com.github.naz013.domain.reminder.BiType.COUNTDOWN_TIMER_EXCLUSION
import com.github.naz013.domain.reminder.BiType.DATE
import com.github.naz013.domain.reminder.BiType.DAYS_OF_WEEK
import com.github.naz013.domain.reminder.BiType.DAY_OF_MONTH
import com.github.naz013.domain.reminder.BiType.DAY_OF_YEAR
import com.github.naz013.domain.reminder.BiType.DESCRIPTION
import com.github.naz013.domain.reminder.BiType.EMAIL
import com.github.naz013.domain.reminder.BiType.EMAIL_SUBJECT
import com.github.naz013.domain.reminder.BiType.GOOGLE_CALENDAR
import com.github.naz013.domain.reminder.BiType.GOOGLE_CALENDAR_DURATION
import com.github.naz013.domain.reminder.BiType.GOOGLE_TASK_LIST
import com.github.naz013.domain.reminder.BiType.GROUP
import com.github.naz013.domain.reminder.BiType.ICAL_BYDAY
import com.github.naz013.domain.reminder.BiType.ICAL_BYHOUR
import com.github.naz013.domain.reminder.BiType.ICAL_BYMINUTE
import com.github.naz013.domain.reminder.BiType.ICAL_BYMONTH
import com.github.naz013.domain.reminder.BiType.ICAL_BYMONTHDAY
import com.github.naz013.domain.reminder.BiType.ICAL_BYSETPOS
import com.github.naz013.domain.reminder.BiType.ICAL_BYWEEKNO
import com.github.naz013.domain.reminder.BiType.ICAL_BYYEARDAY
import com.github.naz013.domain.reminder.BiType.ICAL_COUNT
import com.github.naz013.domain.reminder.BiType.ICAL_FREQ
import com.github.naz013.domain.reminder.BiType.ICAL_INTERVAL
import com.github.naz013.domain.reminder.BiType.ICAL_START_DATE
import com.github.naz013.domain.reminder.BiType.ICAL_START_TIME
import com.github.naz013.domain.reminder.BiType.ICAL_UNTIL_DATE
import com.github.naz013.domain.reminder.BiType.ICAL_UNTIL_TIME
import com.github.naz013.domain.reminder.BiType.ICAL_WEEKSTART
import com.github.naz013.domain.reminder.BiType.LEAVING_COORDINATES
import com.github.naz013.domain.reminder.BiType.LED_COLOR
import com.github.naz013.domain.reminder.BiType.LINK
import com.github.naz013.domain.reminder.BiType.LOCATION_DELAY_DATE
import com.github.naz013.domain.reminder.BiType.LOCATION_DELAY_TIME
import com.github.naz013.domain.reminder.BiType.NOTE
import com.github.naz013.domain.reminder.BiType.OTHER_PARAMS
import com.github.naz013.domain.reminder.BiType.PHONE_CALL
import com.github.naz013.domain.reminder.BiType.PRIORITY
import com.github.naz013.domain.reminder.BiType.REPEAT_INTERVAL
import com.github.naz013.domain.reminder.BiType.REPEAT_LIMIT
import com.github.naz013.domain.reminder.BiType.REPEAT_TIME
import com.github.naz013.domain.reminder.BiType.SMS
import com.github.naz013.domain.reminder.BiType.SUB_TASKS
import com.github.naz013.domain.reminder.BiType.SUMMARY
import com.github.naz013.domain.reminder.BiType.TIME

class BuilderPresetToBiAdapter(
  private val biFactory: BiFactory,
  private val biTypeToBiValue: BiTypeToBiValue
) {

  suspend operator fun invoke(preset: RecurPreset): List<BuilderItem<*>> {
    return preset.builderScheme.mapNotNull { tryToBuilderItem(it) }
  }

  private suspend fun tryToBuilderItem(scheme: PresetBuilderScheme): BuilderItem<*>? {
    return runCatching { toBuilderItem(scheme) }.getOrNull()
  }

  private suspend fun toBuilderItem(scheme: PresetBuilderScheme): BuilderItem<*>? {
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

  private suspend inline fun <reified V, reified T : BuilderItem<V>> create(
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

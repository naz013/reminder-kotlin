package com.github.naz013.feature.reminder.build.preset

import com.github.naz013.feature.reminder.build.ApplicationBuilderItem
import com.github.naz013.feature.reminder.build.ArrivingCoordinatesBuilderItem
import com.github.naz013.feature.reminder.build.AttachmentsBuilderItem
import com.github.naz013.feature.reminder.build.BeforeTimeBuilderItem
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.BypassDndBuilderItem
import com.github.naz013.feature.reminder.build.CategoryBuilderItem
import com.github.naz013.feature.reminder.build.DateBuilderItem
import com.github.naz013.feature.reminder.build.DayOfMonthBuilderItem
import com.github.naz013.feature.reminder.build.DayOfYearBuilderItem
import com.github.naz013.feature.reminder.build.DaysOfWeekBuilderItem
import com.github.naz013.feature.reminder.build.DelayMinutesBuilderItem
import com.github.naz013.feature.reminder.build.DescriptionBuilderItem
import com.github.naz013.feature.reminder.build.EmailBuilderItem
import com.github.naz013.feature.reminder.build.EmailSubjectBuilderItem
import com.github.naz013.feature.reminder.build.GoogleCalendarBuilderItem
import com.github.naz013.feature.reminder.build.GoogleCalendarDurationBuilderItem
import com.github.naz013.feature.reminder.build.GoogleTaskListBuilderItem
import com.github.naz013.feature.reminder.build.GroupBuilderItem
import com.github.naz013.feature.reminder.build.ICalByDayBuilderItem
import com.github.naz013.feature.reminder.build.ICalByHourBuilderItem
import com.github.naz013.feature.reminder.build.ICalByMinuteBuilderItem
import com.github.naz013.feature.reminder.build.ICalByMonthBuilderItem
import com.github.naz013.feature.reminder.build.ICalByMonthDayBuilderItem
import com.github.naz013.feature.reminder.build.ICalBySetPosBuilderItem
import com.github.naz013.feature.reminder.build.ICalByWeekNoBuilderItem
import com.github.naz013.feature.reminder.build.ICalByYearDayBuilderItem
import com.github.naz013.feature.reminder.build.ICalCountBuilderItem
import com.github.naz013.feature.reminder.build.ICalFrequencyBuilderItem
import com.github.naz013.feature.reminder.build.ICalIntervalBuilderItem
import com.github.naz013.feature.reminder.build.ICalStartDateBuilderItem
import com.github.naz013.feature.reminder.build.ICalStartTimeBuilderItem
import com.github.naz013.feature.reminder.build.ICalUntilDateBuilderItem
import com.github.naz013.feature.reminder.build.ICalUntilTimeBuilderItem
import com.github.naz013.feature.reminder.build.ICalWeekStartBuilderItem
import com.github.naz013.feature.reminder.build.LeavingCoordinatesBuilderItem
import com.github.naz013.feature.reminder.build.LedColorBuilderItem
import com.github.naz013.feature.reminder.build.LocationDelayDateBuilderItem
import com.github.naz013.feature.reminder.build.LocationDelayTimeBuilderItem
import com.github.naz013.feature.reminder.build.LockScreenVisibilityBuilderItem
import com.github.naz013.feature.reminder.build.NoteBuilderItem
import com.github.naz013.feature.reminder.build.OtherParamsBuilderItem
import com.github.naz013.feature.reminder.build.PhoneCallBuilderItem
import com.github.naz013.feature.reminder.build.PriorityBuilderItem
import com.github.naz013.feature.reminder.build.RepeatIntervalBuilderItem
import com.github.naz013.feature.reminder.build.RepeatLimitBuilderItem
import com.github.naz013.feature.reminder.build.RepeatTimeBuilderItem
import com.github.naz013.feature.reminder.build.SmsBuilderItem
import com.github.naz013.feature.reminder.build.SubTasksBuilderItem
import com.github.naz013.feature.reminder.build.SummaryBuilderItem
import com.github.naz013.feature.reminder.build.TimeBuilderItem
import com.github.naz013.feature.reminder.build.TimerBuilderItem
import com.github.naz013.feature.reminder.build.TimerExclusionBuilderItem
import com.github.naz013.feature.reminder.build.VibrationPatternBuilderItem
import com.github.naz013.feature.reminder.build.WakeScreenBuilderItem
import com.github.naz013.feature.reminder.build.WebAddressBuilderItem
import com.github.naz013.feature.reminder.build.bi.BiFactory
import com.github.naz013.feature.reminder.build.reminder.BiTypeToBiValue
import com.github.naz013.domain.PresetBuilderScheme
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.BiType.APPLICATION
import com.github.naz013.domain.reminder.BiType.ARRIVING_COORDINATES
import com.github.naz013.domain.reminder.BiType.ATTACHMENTS
import com.github.naz013.domain.reminder.BiType.BEFORE_TIME
import com.github.naz013.domain.reminder.BiType.BYPASS_DND
import com.github.naz013.domain.reminder.BiType.CATEGORY
import com.github.naz013.domain.reminder.BiType.COUNTDOWN_TIMER
import com.github.naz013.domain.reminder.BiType.COUNTDOWN_TIMER_EXCLUSION
import com.github.naz013.domain.reminder.BiType.DATE
import com.github.naz013.domain.reminder.BiType.DAYS_OF_WEEK
import com.github.naz013.domain.reminder.BiType.DAY_OF_MONTH
import com.github.naz013.domain.reminder.BiType.DAY_OF_YEAR
import com.github.naz013.domain.reminder.BiType.DELAY_MINUTES
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
import com.github.naz013.domain.reminder.BiType.LOCK_SCREEN_VISIBILITY
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
import com.github.naz013.domain.reminder.BiType.VIBRATION_PATTERN
import com.github.naz013.domain.reminder.BiType.WAKE_SCREEN

class BuilderPresetToBiAdapter(
  private val biFactory: BiFactory,
  private val biTypeToBiValue: BiTypeToBiValue,
) {
  suspend operator fun invoke(preset: RecurPreset): List<BuilderItem<*>> = preset.builderScheme.mapNotNull { tryToBuilderItem(it) }

  private suspend fun tryToBuilderItem(scheme: PresetBuilderScheme): BuilderItem<*>? = runCatching { toBuilderItem(scheme) }.getOrNull()

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
      else -> toNotificationOrIntegrationBuilderItem(scheme, type)
    }
  }

  // Split out of toBuilderItem() to stay under the LongMethod line threshold - the notification
  // override fields and third-party-integration items, which don't share any grouping with the
  // core/params/action items above beyond both being flat BiType -> BuilderItem lookups.
  private suspend fun toNotificationOrIntegrationBuilderItem(
    scheme: PresetBuilderScheme,
    type: BiType,
  ): BuilderItem<*>? =
    when (type) {
      CATEGORY -> create(scheme, CategoryBuilderItem::class.java)
      LOCK_SCREEN_VISIBILITY -> create(scheme, LockScreenVisibilityBuilderItem::class.java)
      BYPASS_DND -> create(scheme, BypassDndBuilderItem::class.java)
      WAKE_SCREEN -> create(scheme, WakeScreenBuilderItem::class.java)
      VIBRATION_PATTERN -> create(scheme, VibrationPatternBuilderItem::class.java)
      DELAY_MINUTES -> create(scheme, DelayMinutesBuilderItem::class.java)
      GOOGLE_TASK_LIST -> create(scheme, GoogleTaskListBuilderItem::class.java)
      GOOGLE_CALENDAR -> create(scheme, GoogleCalendarBuilderItem::class.java)
      GOOGLE_CALENDAR_DURATION -> create(scheme, GoogleCalendarDurationBuilderItem::class.java)
      NOTE -> create(scheme, NoteBuilderItem::class.java)
      else -> error("Unknown biType: $type")
    }

  private suspend inline fun <reified V, reified T : BuilderItem<V>> create(
    scheme: PresetBuilderScheme,
    clazz: Class<T>,
  ): T? {
    val type = scheme.type
    return biFactory.createWithValue(
      biType = type,
      value = biTypeToBiValue(type, scheme.value),
      clazz = clazz,
    )
  }
}

package com.elementary.tasks.reminder.build

import android.os.Build
import androidx.annotation.DrawableRes
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.GTasks
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.reminder.ShopItem
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.datetime.recurrence.ByDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByHourRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMinuteRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMonthDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMonthRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.BySetPosRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByWeekNumberRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByYearDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.CountRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.Day
import com.elementary.tasks.core.utils.datetime.recurrence.DayValue
import com.elementary.tasks.core.utils.datetime.recurrence.FreqRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.FreqType
import com.elementary.tasks.core.utils.datetime.recurrence.IntervalRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.RecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.WeekStartRecurParam
import com.elementary.tasks.reminder.build.bi.BiGroup
import com.github.naz013.domain.reminder.BiType
import com.elementary.tasks.reminder.build.bi.BuilderModifier
import com.elementary.tasks.reminder.build.bi.CalendarDuration
import com.elementary.tasks.reminder.build.bi.DateModifier
import com.elementary.tasks.reminder.build.bi.DefaultStringModifier
import com.elementary.tasks.reminder.build.bi.EmailModifier
import com.elementary.tasks.reminder.build.bi.FormattedStringModifier
import com.elementary.tasks.reminder.build.bi.GoogleCalendarDurationModifier
import com.elementary.tasks.reminder.build.bi.GoogleCalendarModifier
import com.elementary.tasks.reminder.build.bi.GoogleTaskListModifier
import com.elementary.tasks.reminder.build.bi.GroupModifier
import com.elementary.tasks.reminder.build.bi.IntModifier
import com.elementary.tasks.reminder.build.bi.ListIntModifier
import com.elementary.tasks.reminder.build.bi.ListStringModifier
import com.elementary.tasks.reminder.build.bi.LongModifier
import com.elementary.tasks.reminder.build.bi.NoteModifier
import com.elementary.tasks.reminder.build.bi.OtherParams
import com.elementary.tasks.reminder.build.bi.OtherParamsModifier
import com.elementary.tasks.reminder.build.bi.PhoneNumberModifier
import com.elementary.tasks.reminder.build.bi.PlaceModifier
import com.elementary.tasks.reminder.build.bi.RecurParamModifier
import com.elementary.tasks.reminder.build.bi.ShopItemsModifier
import com.elementary.tasks.reminder.build.bi.StringModifier
import com.elementary.tasks.reminder.build.bi.SummaryModifier
import com.elementary.tasks.reminder.build.bi.TimeModifier
import com.elementary.tasks.reminder.build.bi.TimerExclusion
import com.elementary.tasks.reminder.build.bi.TimerExclusionModifier
import com.elementary.tasks.reminder.build.bi.WebAddressModifier
import com.elementary.tasks.reminder.build.bi.constraint.BiConstraint
import com.elementary.tasks.reminder.build.bi.constraint.constraints
import com.elementary.tasks.reminder.build.formatter.ApplicationFormatter
import com.elementary.tasks.reminder.build.formatter.AttachmentsFormatter
import com.elementary.tasks.reminder.build.formatter.CalendarDurationFormatter
import com.elementary.tasks.reminder.build.formatter.Formatter
import com.elementary.tasks.reminder.build.formatter.LedColorFormatter
import com.elementary.tasks.reminder.build.formatter.OtherParamsFormatter
import com.elementary.tasks.reminder.build.formatter.PriorityFormatter
import com.elementary.tasks.reminder.build.formatter.RepeatLimitFormatter
import com.elementary.tasks.reminder.build.formatter.TimerExclusionFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.BeforeTimeFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.DateFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.DayOfMonthFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.DayOfYearFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.RepeatIntervalFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.RepeatTimeFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.TimeFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.TimerFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.WeekdayArrayFormatter
import com.elementary.tasks.reminder.build.formatter.ical.ICalDayValueFormatter
import com.elementary.tasks.reminder.build.formatter.ical.ICalFreqFormatter
import com.elementary.tasks.reminder.build.formatter.ical.ICalListDayValueFormatter
import com.elementary.tasks.reminder.build.formatter.`object`.NoteFormatter
import com.elementary.tasks.reminder.build.formatter.`object`.PlaceFormatter
import com.elementary.tasks.reminder.build.formatter.`object`.ShopItemsFormatter
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

abstract class BuilderItem<T> {
  @get:DrawableRes
  abstract val iconRes: Int
  abstract val title: String
  abstract val description: String?
  abstract val isForPro: Boolean
  abstract val modifier: BuilderModifier<T>
  abstract val biType: BiType
  abstract val biGroup: BiGroup
  open val isEnabled: Boolean = true
  open val minSdk: Int = Module.minSdk
  open val maxSdk: Int = Module.maxSdk
  open val constraints: List<BiConstraint<*>> = constraints { }
}

abstract class StringBuilderItem : BuilderItem<String>()

data class SummaryBuilderItem(
  override val title: String,
  override val description: String?
) : StringBuilderItem() {
  override val iconRes: Int = R.drawable.ic_fluent_text
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<String> = SummaryModifier()
  override val biType: BiType = BiType.SUMMARY
  override val biGroup: BiGroup = BiGroup.PARAMS
}

data class DescriptionBuilderItem(
  override val title: String,
  override val description: String?
) : StringBuilderItem() {
  override val iconRes: Int = R.drawable.ic_builder_details
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<String> = object : StringModifier() {
    override fun putInto(reminder: Reminder) {
      reminder.description = storage.value
    }
  }
  override val biType: BiType = BiType.DESCRIPTION
  override val biGroup: BiGroup = BiGroup.EXTRA
}

data class DateBuilderItem(
  override val title: String,
  override val description: String?,
  private val dateFormatter: DateFormatter
) : BuilderItem<LocalDate>() {
  override val iconRes: Int = R.drawable.ic_builder_by_monthday
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<LocalDate> = DateModifier(dateFormatter)
  override val biType: BiType = BiType.DATE
  override val biGroup: BiGroup = BiGroup.CORE
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.TIME)
    blockedBy(
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES,
      BiType.LOCATION_DELAY_DATE,
      BiType.LOCATION_DELAY_TIME
    )
    blockedBy(BiGroup.ICAL)
  }
}

data class TimeBuilderItem(
  override val title: String,
  override val description: String?,
  private val timeFormatter: TimeFormatter
) : BuilderItem<LocalTime>() {
  override val iconRes: Int = R.drawable.ic_builder_time
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<LocalTime> = TimeModifier(timeFormatter)
  override val biType: BiType = BiType.TIME
  override val biGroup: BiGroup = BiGroup.CORE
  override val constraints: List<BiConstraint<*>> = constraints {
    blockedBy(
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES,
      BiType.LOCATION_DELAY_DATE,
      BiType.LOCATION_DELAY_TIME
    )
    mandatoryIf(
      BiType.DATE,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR
    )
    blockedBy(BiGroup.ICAL)
  }
}

data class DaysOfWeekBuilderItem(
  override val title: String,
  override val description: String?,
  private val weekdayArrayFormatter: WeekdayArrayFormatter
) : BuilderItem<List<Int>>() {
  override val iconRes: Int = R.drawable.ic_builder_weekday
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<List<Int>> =
    object : ListIntModifier(weekdayArrayFormatter) {
      override fun putInto(reminder: Reminder) {
        storage.value?.let { reminder.weekdays = it }
      }
    }
  override val biType: BiType = BiType.DAYS_OF_WEEK
  override val biGroup: BiGroup = BiGroup.CORE
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.TIME)
    blockedBy(
      BiType.DATE,
      BiType.DAY_OF_YEAR,
      BiType.DAY_OF_MONTH,
      BiType.COUNTDOWN_TIMER,
      BiType.REPEAT_TIME,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES,
      BiType.LOCATION_DELAY_DATE,
      BiType.LOCATION_DELAY_TIME
    )
    blockedBy(BiGroup.ICAL)
  }
}

data class DayOfMonthBuilderItem(
  override val title: String,
  override val description: String?,
  private val dayOfMonthFormatter: DayOfMonthFormatter
) : BuilderItem<Int>() {
  override val iconRes: Int = R.drawable.ic_builder_by_monthday
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<Int> = object : IntModifier(dayOfMonthFormatter) {
    override fun putInto(reminder: Reminder) {
      storage.value?.let { reminder.dayOfMonth = it }
    }
  }
  override val biType: BiType = BiType.DAY_OF_MONTH
  override val biGroup: BiGroup = BiGroup.CORE
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.TIME)
    blockedBy(
      BiType.DATE,
      BiType.DAY_OF_YEAR,
      BiType.DAYS_OF_WEEK,
      BiType.COUNTDOWN_TIMER,
      BiType.REPEAT_TIME,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES,
      BiType.LOCATION_DELAY_DATE,
      BiType.LOCATION_DELAY_TIME
    )
    blockedBy(BiGroup.ICAL)
  }
}

data class DayOfYearBuilderItem(
  override val title: String,
  override val description: String?,
  private val dayOfYearFormatter: DayOfYearFormatter
) : BuilderItem<Int>() {
  override val iconRes: Int = R.drawable.ic_builder_by_yearday
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<Int> = object : IntModifier(dayOfYearFormatter) {
    override fun putInto(reminder: Reminder) {
      storage.value?.let { LocalDate.now().withDayOfYear(it) }?.let {
        reminder.monthOfYear = it.monthValue - 1
        reminder.dayOfMonth = it.dayOfMonth
      }
    }
  }
  override val biType: BiType = BiType.DAY_OF_YEAR
  override val biGroup: BiGroup = BiGroup.CORE
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.TIME)
    blockedBy(
      BiType.DATE,
      BiType.DAY_OF_MONTH,
      BiType.DAYS_OF_WEEK,
      BiType.COUNTDOWN_TIMER,
      BiType.REPEAT_TIME,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES,
      BiType.LOCATION_DELAY_DATE,
      BiType.LOCATION_DELAY_TIME
    )
    blockedBy(BiGroup.ICAL)
  }
}

data class TimerBuilderItem(
  override val title: String,
  override val description: String?,
  private val timerFormatter: TimerFormatter
) : BuilderItem<Long>() {
  override val iconRes: Int = R.drawable.ic_builder_timer
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<Long> = object : LongModifier(timerFormatter) {
    override fun putInto(reminder: Reminder) {
      storage.value?.let { reminder.after = it }
    }
  }
  override val biType: BiType = BiType.COUNTDOWN_TIMER
  override val biGroup: BiGroup = BiGroup.CORE
  override val constraints: List<BiConstraint<*>> = constraints {
    blockedBy(
      BiType.DATE,
      BiType.DAY_OF_MONTH,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_YEAR,
      BiType.TIME,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES,
      BiType.LOCATION_DELAY_DATE,
      BiType.LOCATION_DELAY_TIME
    )
    blockedBy(BiGroup.ICAL)
  }
}

data class TimerExclusionBuilderItem(
  override val title: String,
  override val description: String?,
  private val timerExclusionFormatter: TimerExclusionFormatter
) : BuilderItem<TimerExclusion>() {
  override val iconRes: Int = R.drawable.ic_builder_timer_exclusion
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<TimerExclusion> =
    TimerExclusionModifier(timerExclusionFormatter)
  override val biType: BiType = BiType.COUNTDOWN_TIMER_EXCLUSION
  override val biGroup: BiGroup = BiGroup.EXTRA
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.COUNTDOWN_TIMER)
    blockedBy(
      BiType.DATE,
      BiType.DAY_OF_MONTH,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_YEAR,
      BiType.TIME,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES,
      BiType.LOCATION_DELAY_DATE,
      BiType.LOCATION_DELAY_TIME
    )
    blockedBy(BiGroup.ICAL)
  }
}

data class GroupBuilderItem(
  override val title: String,
  override val description: String?,
  val groups: List<UiGroupList>,
  val defaultGroup: UiGroupList?
) : BuilderItem<UiGroupList>() {
  override val iconRes: Int = R.drawable.ic_builder_group
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<UiGroupList> = GroupModifier(defaultGroup)
  override val biType: BiType = BiType.GROUP
  override val biGroup: BiGroup = BiGroup.PARAMS
}

data class BeforeTimeBuilderItem(
  override val title: String,
  override val description: String?,
  private val beforeTimeFormatter: BeforeTimeFormatter
) : BuilderItem<Long>() {
  override val iconRes: Int = R.drawable.ic_builder_before_time
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<Long> = object : LongModifier(beforeTimeFormatter) {
    override fun putInto(reminder: Reminder) {
      storage.value?.let { reminder.remindBefore = it }
    }
  }
  override val biType: BiType = BiType.BEFORE_TIME
  override val biGroup: BiGroup = BiGroup.PARAMS
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAny(
      BiType.COUNTDOWN_TIMER,
      BiType.TIME,
      BiType.DAY_OF_YEAR,
      BiType.DAY_OF_MONTH,
      BiType.DAYS_OF_WEEK,
      BiType.DATE
    )
    blockedBy(
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES,
      BiType.LOCATION_DELAY_DATE,
      BiType.LOCATION_DELAY_TIME
    )
    blockedBy(BiGroup.ICAL)
  }
}

data class RepeatTimeBuilderItem(
  override val title: String,
  override val description: String?,
  private val repeatTimeFormatter: RepeatTimeFormatter
) : BuilderItem<Long>() {
  override val iconRes: Int = R.drawable.ic_fluent_arrow_repeat_all
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<Long> = object : LongModifier(repeatTimeFormatter) {
    override fun putInto(reminder: Reminder) {
      storage.value?.let { reminder.repeatInterval = it }
    }
  }
  override val biType: BiType = BiType.REPEAT_TIME
  override val biGroup: BiGroup = BiGroup.PARAMS
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAny(
      BiType.COUNTDOWN_TIMER,
      BiType.TIME,
      BiType.DATE
    )
    blockedBy(
      BiType.DAY_OF_YEAR,
      BiType.DAY_OF_MONTH,
      BiType.DAYS_OF_WEEK,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES,
      BiType.LOCATION_DELAY_DATE,
      BiType.LOCATION_DELAY_TIME
    )
    blockedBy(BiGroup.ICAL)
  }
}

data class RepeatIntervalBuilderItem(
  override val title: String,
  override val description: String?,
  private val repeatIntervalFormatter: RepeatIntervalFormatter
) : BuilderItem<Long>() {
  override val iconRes: Int = R.drawable.ic_builder_interval
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<Long> =
    object : LongModifier(repeatIntervalFormatter, 1L) {
      override fun putInto(reminder: Reminder) {
        storage.value?.let { reminder.repeatInterval = it }
      }
    }
  override val biType: BiType = BiType.REPEAT_INTERVAL
  override val biGroup: BiGroup = BiGroup.PARAMS
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAny(
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR
    )
    blockedBy(
      BiType.COUNTDOWN_TIMER,
      BiType.DATE,
      BiType.DAYS_OF_WEEK,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES,
      BiType.LOCATION_DELAY_DATE,
      BiType.LOCATION_DELAY_TIME
    )
    blockedBy(BiGroup.ICAL)
  }
}

data class RepeatLimitBuilderItem(
  override val title: String,
  override val description: String?,
  val repeatLimitFormatter: RepeatLimitFormatter
) : BuilderItem<Int>() {
  override val iconRes: Int = R.drawable.ic_builder_repeat_limit
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<Int> = object : IntModifier(repeatLimitFormatter) {
    override fun putInto(reminder: Reminder) {
      storage.value?.let { reminder.repeatLimit = it }
    }
  }
  override val biType: BiType = BiType.REPEAT_LIMIT
  override val biGroup: BiGroup = BiGroup.EXTRA
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAny(
      BiType.DAY_OF_YEAR,
      BiType.DAY_OF_MONTH,
      BiType.DAYS_OF_WEEK,
      BiType.REPEAT_TIME
    )
    blockedBy(
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES,
      BiType.LOCATION_DELAY_DATE,
      BiType.LOCATION_DELAY_TIME
    )
    blockedBy(BiGroup.ICAL)
  }
}

data class PriorityBuilderItem(
  override val title: String,
  override val description: String?,
  private val priorityFormatter: PriorityFormatter
) : BuilderItem<Int>() {
  override val iconRes: Int = R.drawable.ic_fluent_star
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<Int> = object : IntModifier(priorityFormatter, 2) {
    override fun putInto(reminder: Reminder) {
      storage.value?.let { reminder.priority = it }
    }
  }
  override val biType: BiType = BiType.PRIORITY
  override val biGroup: BiGroup = BiGroup.EXTRA
}

data class LedColorBuilderItem(
  override val title: String,
  override val description: String?,
  private val ledColorFormatter: LedColorFormatter
) : BuilderItem<Int>() {
  override val iconRes: Int = R.drawable.ic_builder_led_color
  override val isForPro: Boolean = true
  override val modifier: BuilderModifier<Int> = object : IntModifier(ledColorFormatter, LED.BLUE) {
    override fun putInto(reminder: Reminder) {
      storage.value?.let { reminder.color = it }
    }
  }
  override val biType: BiType = BiType.LED_COLOR
  override val biGroup: BiGroup = BiGroup.EXTRA
}

data class AttachmentsBuilderItem(
  override val title: String,
  override val description: String?,
  private val attachmentsFormatter: AttachmentsFormatter
) : BuilderItem<List<String>>() {
  override val iconRes: Int = R.drawable.ic_builder_attach
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<List<String>> =
    object : ListStringModifier(attachmentsFormatter) {
      override fun putInto(reminder: Reminder) {
        storage.value?.let { reminder.attachmentFiles = it }
      }
    }
  override val biType: BiType = BiType.ATTACHMENTS
  override val biGroup: BiGroup = BiGroup.EXTRA
}

data class PhoneCallBuilderItem(
  override val title: String,
  override val description: String?
) : StringBuilderItem() {
  override val iconRes: Int = R.drawable.ic_builder_add_call
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<String> = PhoneNumberModifier()
  override val biType: BiType = BiType.PHONE_CALL
  override val biGroup: BiGroup = BiGroup.ACTION
  override val constraints: List<BiConstraint<*>> = constraints {
    permission(Permissions.CALL_PHONE)
    requiresAny(
      BiType.COUNTDOWN_TIMER,
      BiType.DATE,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.ARRIVING_COORDINATES,
      BiType.LEAVING_COORDINATES
    )
    requiresAny(BiGroup.ICAL)
    blockedBy(
      BiType.SMS,
      BiType.EMAIL,
      BiType.LINK,
      BiType.APPLICATION,
      BiType.SUB_TASKS
    )
  }
}

data class SmsBuilderItem(
  override val title: String,
  override val description: String?
) : StringBuilderItem() {
  override val iconRes: Int = R.drawable.ic_builder_send_message
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<String> = PhoneNumberModifier()
  override val biType: BiType = BiType.SMS
  override val biGroup: BiGroup = BiGroup.ACTION
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAny(
      BiType.COUNTDOWN_TIMER,
      BiType.DATE,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.ARRIVING_COORDINATES,
      BiType.LEAVING_COORDINATES
    )
    requiresAny(BiGroup.ICAL)
    blockedBy(
      BiType.PHONE_CALL,
      BiType.EMAIL,
      BiType.LINK,
      BiType.APPLICATION,
      BiType.SUB_TASKS
    )
  }
}

data class GoogleTaskListBuilderItem(
  override val title: String,
  override val description: String?,
  val taskLists: List<GoogleTaskList>,
  private val gTasks: GTasks
) : BuilderItem<GoogleTaskList>() {
  override val iconRes: Int = R.drawable.ic_builder_google_task_list
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<GoogleTaskList> = GoogleTaskListModifier()
  override val biType: BiType = BiType.GOOGLE_TASK_LIST
  override val biGroup: BiGroup = BiGroup.EXTRA
  override val isEnabled: Boolean
    get() = gTasks.isLogged
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAny(
      BiType.COUNTDOWN_TIMER,
      BiType.DATE,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR
    )
    requiresAny(BiGroup.ICAL)
    blockedBy(
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES,
      BiType.LOCATION_DELAY_DATE,
      BiType.LOCATION_DELAY_TIME
    )
  }
}

data class GoogleCalendarBuilderItem(
  override val title: String,
  override val description: String?
) : BuilderItem<GoogleCalendarUtils.CalendarItem>() {
  override val iconRes: Int = R.drawable.ic_builder_google_calendar_add
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<GoogleCalendarUtils.CalendarItem> =
    GoogleCalendarModifier()
  override val biType: BiType = BiType.GOOGLE_CALENDAR
  override val biGroup: BiGroup = BiGroup.EXTRA
  override val constraints: List<BiConstraint<*>> = constraints {
    permission(
      Permissions.READ_CALENDAR,
      Permissions.WRITE_CALENDAR
    )
    requiresAny(
      BiType.COUNTDOWN_TIMER,
      BiType.DATE,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR
    )
    requiresAny(BiGroup.ICAL)
    blockedBy(
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES,
      BiType.LOCATION_DELAY_DATE,
      BiType.LOCATION_DELAY_TIME
    )
  }
}

data class GoogleCalendarDurationBuilderItem(
  override val title: String,
  override val description: String?,
  private val calendarDurationFormatter: CalendarDurationFormatter
) : BuilderItem<CalendarDuration>() {
  override val iconRes: Int = R.drawable.ic_builder_by_minute
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<CalendarDuration> = GoogleCalendarDurationModifier(
    calendarDurationFormatter,
    CalendarDuration(false, 0L)
  )
  override val biType: BiType = BiType.GOOGLE_CALENDAR_DURATION
  override val biGroup: BiGroup = BiGroup.EXTRA
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.GOOGLE_CALENDAR)
    blockedBy(
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES,
      BiType.LOCATION_DELAY_DATE,
      BiType.LOCATION_DELAY_TIME
    )
  }
}

data class EmailBuilderItem(
  override val title: String,
  override val description: String?
) : StringBuilderItem() {
  override val iconRes: Int = R.drawable.ic_builder_email_address
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<String> = EmailModifier()
  override val biType: BiType = BiType.EMAIL
  override val biGroup: BiGroup = BiGroup.ACTION
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAny(
      BiType.COUNTDOWN_TIMER,
      BiType.DATE,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.ARRIVING_COORDINATES,
      BiType.LEAVING_COORDINATES
    )
    requiresAny(BiGroup.ICAL)
    blockedBy(
      BiType.SMS,
      BiType.PHONE_CALL,
      BiType.LINK,
      BiType.APPLICATION,
      BiType.SUB_TASKS
    )
  }
}

data class EmailSubjectBuilderItem(
  override val title: String,
  override val description: String?
) : StringBuilderItem() {
  override val iconRes: Int = R.drawable.ic_builder_email_subject
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<String> = object : DefaultStringModifier() {
    override fun putInto(reminder: Reminder) {
      reminder.subject = storage.value ?: ""
    }
  }
  override val biType: BiType = BiType.EMAIL_SUBJECT
  override val biGroup: BiGroup = BiGroup.PARAMS
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.EMAIL)
    blockedBy(
      BiType.SMS,
      BiType.PHONE_CALL,
      BiType.LINK,
      BiType.APPLICATION,
      BiType.SUB_TASKS
    )
  }
}

data class WebAddressBuilderItem(
  override val title: String,
  override val description: String?
) : StringBuilderItem() {
  override val iconRes: Int = R.drawable.ic_builder_web_address
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<String> = WebAddressModifier()
  override val biType: BiType = BiType.LINK
  override val biGroup: BiGroup = BiGroup.ACTION
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAny(
      BiType.COUNTDOWN_TIMER,
      BiType.DATE,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.ARRIVING_COORDINATES,
      BiType.LEAVING_COORDINATES
    )
    requiresAny(BiGroup.ICAL)
    blockedBy(
      BiType.SMS,
      BiType.PHONE_CALL,
      BiType.EMAIL,
      BiType.APPLICATION,
      BiType.SUB_TASKS
    )
  }
}

data class ApplicationBuilderItem(
  override val title: String,
  override val description: String?,
  private val applicationFormatter: ApplicationFormatter
) : StringBuilderItem() {
  override val iconRes: Int = R.drawable.ic_builder_add_app
  override val isForPro: Boolean = false
  override val maxSdk: Int = Build.VERSION_CODES.S
  override val modifier: BuilderModifier<String> = object : FormattedStringModifier(
    applicationFormatter
  ) {
    override fun putInto(reminder: Reminder) {
      super.putInto(reminder)
      storage.value?.also {
        reminder.target = it
      }
    }
  }
  override val biType: BiType = BiType.APPLICATION
  override val biGroup: BiGroup = BiGroup.ACTION
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAny(
      BiType.COUNTDOWN_TIMER,
      BiType.DATE,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.ARRIVING_COORDINATES,
      BiType.LEAVING_COORDINATES
    )
    requiresAny(BiGroup.ICAL)
    blockedBy(
      BiType.SMS,
      BiType.PHONE_CALL,
      BiType.EMAIL,
      BiType.LINK,
      BiType.SUB_TASKS
    )
  }
}

data class OtherParamsBuilderItem(
  override val title: String,
  override val description: String?,
  private val otherParamsFormatter: OtherParamsFormatter
) : BuilderItem<OtherParams>() {
  override val iconRes: Int = R.drawable.ic_builder_more_options
  override val isForPro: Boolean = true
  override val modifier: BuilderModifier<OtherParams> = OtherParamsModifier(otherParamsFormatter)
  override val biType: BiType = BiType.OTHER_PARAMS
  override val biGroup: BiGroup = BiGroup.EXTRA
}

data class SubTasksBuilderItem(
  override val title: String,
  override val description: String?,
  private val shopItemsFormatter: ShopItemsFormatter
) : BuilderItem<List<ShopItem>>() {
  override val iconRes: Int = R.drawable.ic_builder_sub_task_list
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<List<ShopItem>> = ShopItemsModifier(shopItemsFormatter)
  override val biType: BiType = BiType.SUB_TASKS
  override val biGroup: BiGroup = BiGroup.PARAMS
  override val constraints: List<BiConstraint<*>> = constraints {
    blockedBy(
      BiType.APPLICATION,
      BiType.EMAIL,
      BiType.LINK,
      BiType.PHONE_CALL,
      BiType.SMS
    )
  }
}

data class ArrivingCoordinatesBuilderItem(
  override val title: String,
  override val description: String?,
  private val placeFormatter: PlaceFormatter
) : BuilderItem<Place>() {
  override val iconRes: Int = R.drawable.ic_builder_arriving_type
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<Place> = PlaceModifier(placeFormatter)
  override val biType: BiType = BiType.ARRIVING_COORDINATES
  override val biGroup: BiGroup = BiGroup.CORE
  override val constraints: List<BiConstraint<*>> = constraints {
    permission(
      Permissions.ACCESS_COARSE_LOCATION,
      Permissions.ACCESS_FINE_LOCATION,
      Permissions.BACKGROUND_LOCATION,
      Permissions.FOREGROUND_SERVICE,
      Permissions.FOREGROUND_SERVICE_LOCATION
    )
    blockedBy(
      BiType.COUNTDOWN_TIMER,
      BiType.DATE,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.LEAVING_COORDINATES
    )
    blockedBy(BiGroup.ICAL)
  }
}

data class LeavingCoordinatesBuilderItem(
  override val title: String,
  override val description: String?,
  private val placeFormatter: PlaceFormatter
) : BuilderItem<Place>() {
  override val iconRes: Int = R.drawable.ic_builder_leaving_type
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<Place> = PlaceModifier(placeFormatter)
  override val biType: BiType = BiType.LEAVING_COORDINATES
  override val biGroup: BiGroup = BiGroup.CORE
  override val constraints: List<BiConstraint<*>> = constraints {
    permission(
      Permissions.ACCESS_COARSE_LOCATION,
      Permissions.ACCESS_FINE_LOCATION,
      Permissions.BACKGROUND_LOCATION,
      Permissions.FOREGROUND_SERVICE,
      Permissions.FOREGROUND_SERVICE_LOCATION
    )
    blockedBy(
      BiType.COUNTDOWN_TIMER,
      BiType.DATE,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.ARRIVING_COORDINATES
    )
    blockedBy(BiGroup.ICAL)
  }
}

data class LocationDelayDateBuilderItem(
  override val title: String,
  override val description: String?,
  private val dateFormatter: DateFormatter
) : BuilderItem<LocalDate>() {
  override val iconRes: Int = R.drawable.ic_builder_by_monthday
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<LocalDate> = DateModifier(dateFormatter)
  override val biType: BiType = BiType.LOCATION_DELAY_DATE
  override val biGroup: BiGroup = BiGroup.PARAMS
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.LOCATION_DELAY_TIME)
    requiresAny(
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER
    )
    blockedBy(BiGroup.ICAL)
    mandatoryIf(BiType.LOCATION_DELAY_TIME)
  }
}

data class LocationDelayTimeBuilderItem(
  override val title: String,
  override val description: String?,
  private val timeFormatter: TimeFormatter
) : BuilderItem<LocalTime>() {
  override val iconRes: Int = R.drawable.ic_builder_time_picker
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<LocalTime> = TimeModifier(timeFormatter)
  override val biType: BiType = BiType.LOCATION_DELAY_TIME
  override val biGroup: BiGroup = BiGroup.PARAMS
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.LOCATION_DELAY_DATE)
    requiresAny(
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER
    )
    blockedBy(BiGroup.ICAL)
    mandatoryIf(BiType.LOCATION_DELAY_DATE)
  }
}

data class NoteBuilderItem(
  override val title: String,
  override val description: String?,
  val notes: List<UiNoteList>,
  private val noteFormatter: NoteFormatter
) : BuilderItem<UiNoteList>() {
  override val iconRes: Int = R.drawable.ic_fluent_note
  override val isForPro: Boolean = false
  override val modifier: BuilderModifier<UiNoteList> = NoteModifier(noteFormatter)
  override val biType: BiType = BiType.NOTE
  override val biGroup: BiGroup = BiGroup.EXTRA
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(
      BiType.DATE,
      BiType.TIME
    )
  }
}

sealed class ICalBuilderItem<T>(
  initValue: T,
  formatter: Formatter<T>? = null
) : BuilderItem<T>() {
  override val isForPro: Boolean = true
  override val modifier: BuilderModifier<T> = RecurParamModifier(initValue, formatter)
  override val biGroup: BiGroup = BiGroup.ICAL

  fun getRecurParam(): RecurParam? {
    return modifier.getValue()?.let { createRecurParam(it) }
  }

  open fun createRecurParam(value: T): RecurParam? {
    return null
  }
}

data class ICalFrequencyBuilderItem(
  override val title: String,
  override val description: String?,
  private val formatter: ICalFreqFormatter
) : ICalBuilderItem<FreqType>(FreqType.DAILY, formatter) {
  override val iconRes: Int = R.drawable.ic_builder_frequency
  override val biType: BiType = BiType.ICAL_FREQ
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.ICAL_COUNT)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }

  override fun createRecurParam(value: FreqType): RecurParam {
    return FreqRecurParam(value)
  }
}

sealed class ICalIntBuilderItem(
  initValue: Int,
  open val formatter: Formatter<Int>
) : ICalBuilderItem<Int>(initValue, formatter) {
  abstract val minValue: Int
  abstract val maxValue: Int
}

data class ICalIntervalBuilderItem(
  override val title: String,
  override val description: String?,
  override val formatter: Formatter<Int>
) : ICalIntBuilderItem(1, formatter) {
  override val iconRes: Int = R.drawable.ic_builder_interval
  override val biType: BiType = BiType.ICAL_INTERVAL
  override val minValue: Int = 0
  override val maxValue: Int = 366
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.ICAL_FREQ, BiType.ICAL_COUNT)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }

  override fun createRecurParam(value: Int): RecurParam {
    return IntervalRecurParam(value)
  }
}

data class ICalCountBuilderItem(
  override val title: String,
  override val description: String?,
  override val formatter: Formatter<Int>
) : ICalIntBuilderItem(1, formatter) {
  override val iconRes: Int = R.drawable.ic_builder_count
  override val biType: BiType = BiType.ICAL_COUNT
  override val minValue: Int = 0
  override val maxValue: Int = 500
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.ICAL_FREQ)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }

  override fun createRecurParam(value: Int): RecurParam {
    return CountRecurParam(value)
  }
}

data class ICalUntilDateBuilderItem(
  override val title: String,
  override val description: String?,
  private val formatter: DateFormatter
) : ICalBuilderItem<LocalDate>(LocalDate.now(), formatter) {
  override val iconRes: Int = R.drawable.ic_builder_by_monthday
  override val biType: BiType = BiType.ICAL_UNTIL_DATE
  override val constraints: List<BiConstraint<*>> = constraints {
    mandatoryIf(BiType.ICAL_UNTIL_TIME)
    requiresAll(BiType.ICAL_UNTIL_TIME)
    requiresAll(BiType.ICAL_FREQ)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }
}

data class ICalUntilTimeBuilderItem(
  override val title: String,
  override val description: String?,
  private val formatter: TimeFormatter
) : ICalBuilderItem<LocalTime>(LocalTime.now(), formatter) {
  override val iconRes: Int = R.drawable.ic_builder_time_picker
  override val biType: BiType = BiType.ICAL_UNTIL_TIME
  override val constraints: List<BiConstraint<*>> = constraints {
    mandatoryIf(BiType.ICAL_UNTIL_DATE)
    requiresAll(BiType.ICAL_UNTIL_DATE)
    requiresAll(BiType.ICAL_FREQ)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }
}

data class ICalStartDateBuilderItem(
  override val title: String,
  override val description: String?,
  private val formatter: DateFormatter
) : ICalBuilderItem<LocalDate>(LocalDate.now(), formatter) {
  override val iconRes: Int = R.drawable.ic_builder_by_monthday
  override val biType: BiType = BiType.ICAL_START_DATE
  override val constraints: List<BiConstraint<*>> = constraints {
    mandatoryIf(BiType.ICAL_START_TIME)
    requiresAll(BiType.ICAL_START_TIME, BiType.ICAL_COUNT, BiType.ICAL_FREQ)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }
}

data class ICalStartTimeBuilderItem(
  override val title: String,
  override val description: String?,
  private val formatter: TimeFormatter
) : ICalBuilderItem<LocalTime>(LocalTime.now(), formatter) {
  override val iconRes: Int = R.drawable.ic_builder_time_picker
  override val biType: BiType = BiType.ICAL_START_TIME
  override val constraints: List<BiConstraint<*>> = constraints {
    mandatoryIf(BiType.ICAL_START_DATE)
    requiresAll(BiType.ICAL_START_DATE, BiType.ICAL_COUNT, BiType.ICAL_FREQ)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }
}

sealed class ICalListIntBuilderItem(
  initValue: List<Int>,
  val minValue: Int,
  val maxValue: Int,
  val excludedValues: IntArray = intArrayOf(),
  formatter: Formatter<List<Int>>? = null
) : ICalBuilderItem<List<Int>>(initValue, formatter)

data class ICalByMonthBuilderItem(
  override val title: String,
  override val description: String?,
  private val formatter: Formatter<List<Int>>
) : ICalListIntBuilderItem(
  initValue = emptyList(),
  minValue = 1,
  maxValue = 12,
  formatter = formatter
) {
  override val iconRes: Int = R.drawable.ic_builder_by_month
  override val biType: BiType = BiType.ICAL_BYMONTH
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.ICAL_FREQ, BiType.ICAL_COUNT)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }

  override fun createRecurParam(value: List<Int>): RecurParam {
    return ByMonthRecurParam(value)
  }
}

data class ICalByDayBuilderItem(
  override val title: String,
  override val description: String?,
  private val formatter: ICalListDayValueFormatter
) : ICalBuilderItem<List<DayValue>>(emptyList(), formatter) {
  override val iconRes: Int = R.drawable.ic_builder_by_day
  override val biType: BiType = BiType.ICAL_BYDAY
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.ICAL_FREQ, BiType.ICAL_COUNT)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }

  override fun createRecurParam(value: List<DayValue>): RecurParam {
    return ByDayRecurParam(value)
  }
}

data class ICalByMonthDayBuilderItem(
  override val title: String,
  override val description: String?,
  private val formatter: Formatter<List<Int>>
) : ICalListIntBuilderItem(
  initValue = emptyList(),
  minValue = -31,
  maxValue = 31,
  excludedValues = intArrayOf(0),
  formatter = formatter
) {
  override val iconRes: Int = R.drawable.ic_builder_by_monthday
  override val biType: BiType = BiType.ICAL_BYMONTHDAY
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.ICAL_FREQ, BiType.ICAL_COUNT)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }

  override fun createRecurParam(value: List<Int>): RecurParam {
    return ByMonthDayRecurParam(value)
  }
}

data class ICalByHourBuilderItem(
  override val title: String,
  override val description: String?,
  private val formatter: Formatter<List<Int>>
) : ICalListIntBuilderItem(
  initValue = emptyList(),
  minValue = 0,
  maxValue = 23,
  formatter = formatter
) {
  override val iconRes: Int = R.drawable.ic_builder_by_hour
  override val biType: BiType = BiType.ICAL_BYHOUR
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.ICAL_FREQ, BiType.ICAL_COUNT)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }

  override fun createRecurParam(value: List<Int>): RecurParam {
    return ByHourRecurParam(value)
  }
}

data class ICalByMinuteBuilderItem(
  override val title: String,
  override val description: String?,
  private val formatter: Formatter<List<Int>>
) : ICalListIntBuilderItem(
  initValue = emptyList(),
  minValue = 0,
  maxValue = 59,
  formatter = formatter
) {
  override val iconRes: Int = R.drawable.ic_builder_by_minute
  override val biType: BiType = BiType.ICAL_BYMINUTE
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.ICAL_FREQ, BiType.ICAL_COUNT)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }

  override fun createRecurParam(value: List<Int>): RecurParam {
    return ByMinuteRecurParam(value)
  }
}

data class ICalByYearDayBuilderItem(
  override val title: String,
  override val description: String?,
  private val formatter: Formatter<List<Int>>
) : ICalListIntBuilderItem(
  initValue = emptyList(),
  minValue = -366,
  maxValue = 366,
  excludedValues = intArrayOf(0),
  formatter = formatter
) {
  override val iconRes: Int = R.drawable.ic_builder_by_yearday
  override val biType: BiType = BiType.ICAL_BYYEARDAY
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.ICAL_FREQ, BiType.ICAL_COUNT)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }

  override fun createRecurParam(value: List<Int>): RecurParam {
    return ByYearDayRecurParam(value)
  }
}

data class ICalByWeekNoBuilderItem(
  override val title: String,
  override val description: String?,
  private val formatter: Formatter<List<Int>>
) : ICalListIntBuilderItem(
  initValue = emptyList(),
  minValue = -53,
  maxValue = 53,
  excludedValues = intArrayOf(0),
  formatter = formatter
) {
  override val iconRes: Int = R.drawable.ic_builder_week_no
  override val biType: BiType = BiType.ICAL_BYWEEKNO
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.ICAL_FREQ, BiType.ICAL_COUNT)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }

  override fun createRecurParam(value: List<Int>): RecurParam {
    return ByWeekNumberRecurParam(value)
  }
}

data class ICalBySetPosBuilderItem(
  override val title: String,
  override val description: String?,
  private val formatter: Formatter<List<Int>>
) : ICalListIntBuilderItem(
  initValue = emptyList(),
  minValue = -366,
  maxValue = 366,
  excludedValues = intArrayOf(0),
  formatter = formatter
) {
  override val iconRes: Int = R.drawable.ic_builder_bysetpos
  override val biType: BiType = BiType.ICAL_BYSETPOS
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.ICAL_FREQ, BiType.ICAL_COUNT)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }

  override fun createRecurParam(value: List<Int>): RecurParam {
    return BySetPosRecurParam(value)
  }
}

data class ICalWeekStartBuilderItem(
  override val title: String,
  override val description: String?,
  private val formatter: ICalDayValueFormatter
) : ICalBuilderItem<DayValue>(DayValue(Day.MO), formatter) {
  override val iconRes: Int = R.drawable.ic_builder_week_start
  override val biType: BiType = BiType.ICAL_WEEKSTART
  override val constraints: List<BiConstraint<*>> = constraints {
    requiresAll(BiType.ICAL_FREQ, BiType.ICAL_COUNT)
    blockedBy(
      BiType.DATE,
      BiType.TIME,
      BiType.DAYS_OF_WEEK,
      BiType.DAY_OF_MONTH,
      BiType.DAY_OF_YEAR,
      BiType.COUNTDOWN_TIMER,
      BiType.LEAVING_COORDINATES,
      BiType.ARRIVING_COORDINATES
    )
  }

  override fun createRecurParam(value: DayValue): RecurParam {
    return WeekStartRecurParam(value)
  }
}

package com.elementary.tasks.reminder.build.preset

import com.elementary.tasks.core.utils.datetime.recurrence.ByDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByHourRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMinuteRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMonthDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByMonthRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.BySetPosRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByWeekNumberRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.ByYearDayRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.CountRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.FreqRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.IntervalRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.RecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.UntilRecurParam
import com.elementary.tasks.core.utils.datetime.recurrence.WeekStartRecurParam
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.ICalBuilderItem
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
import com.elementary.tasks.reminder.build.ICalUntilDateBuilderItem
import com.elementary.tasks.reminder.build.ICalUntilTimeBuilderItem
import com.elementary.tasks.reminder.build.ICalWeekStartBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.bi.BiType

class RecurParamsToBiAdapter(
  private val biFactory: BiFactory
) {

  operator fun invoke(params: List<RecurParam>): List<BuilderItem<*>> {
    return params.map { it.toBuilderItem() }.flatten()
  }

  private fun RecurParam.toBuilderItem(): List<ICalBuilderItem<*>> {
    return when (this) {
      is CountRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(BiType.ICAL_COUNT, value, ICalCountBuilderItem::class.java)
        )
      }
      is IntervalRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(
            BiType.ICAL_INTERVAL,
            value,
            ICalIntervalBuilderItem::class.java
          )
        )
      }
      is FreqRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(BiType.ICAL_FREQ, value, ICalFrequencyBuilderItem::class.java)
        )
      }
      is UntilRecurParam -> {
        value.dateTime?.let {
          listOfNotNull(
            biFactory.createWithValue(
              BiType.ICAL_UNTIL_DATE,
              it.toLocalDate(),
              ICalUntilDateBuilderItem::class.java
            ),
            biFactory.createWithValue(
              BiType.ICAL_UNTIL_TIME,
              it.toLocalTime(),
              ICalUntilTimeBuilderItem::class.java
            )
          )
        } ?: emptyList()
      }
      is ByDayRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(BiType.ICAL_BYDAY, value, ICalByDayBuilderItem::class.java)
        )
      }
      is ByMonthRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(BiType.ICAL_BYMONTH, value, ICalByMonthBuilderItem::class.java)
        )
      }
      is ByMonthDayRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(
            BiType.ICAL_BYMONTHDAY,
            value,
            ICalByMonthDayBuilderItem::class.java
          )
        )
      }
      is ByHourRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(BiType.ICAL_BYHOUR, value, ICalByHourBuilderItem::class.java)
        )
      }
      is ByMinuteRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(
            BiType.ICAL_BYMINUTE,
            value,
            ICalByMinuteBuilderItem::class.java
          )
        )
      }
      is ByYearDayRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(
            BiType.ICAL_BYYEARDAY,
            value,
            ICalByYearDayBuilderItem::class.java
          )
        )
      }
      is ByWeekNumberRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(
            BiType.ICAL_BYWEEKNO,
            value,
            ICalByWeekNoBuilderItem::class.java
          )
        )
      }
      is BySetPosRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(
            BiType.ICAL_BYSETPOS,
            value,
            ICalBySetPosBuilderItem::class.java
          )
        )
      }
      is WeekStartRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(
            BiType.ICAL_WEEKSTART,
            value,
            ICalWeekStartBuilderItem::class.java
          )
        )
      }
    }
  }
}

package com.elementary.tasks.reminder.build.preset

import com.github.naz013.icalendar.ByDayRecurParam
import com.github.naz013.icalendar.ByHourRecurParam
import com.github.naz013.icalendar.ByMinuteRecurParam
import com.github.naz013.icalendar.ByMonthDayRecurParam
import com.github.naz013.icalendar.ByMonthRecurParam
import com.github.naz013.icalendar.BySetPosRecurParam
import com.github.naz013.icalendar.ByWeekNumberRecurParam
import com.github.naz013.icalendar.ByYearDayRecurParam
import com.github.naz013.icalendar.CountRecurParam
import com.github.naz013.icalendar.FreqRecurParam
import com.github.naz013.icalendar.IntervalRecurParam
import com.github.naz013.icalendar.RecurParam
import com.github.naz013.icalendar.UntilRecurParam
import com.github.naz013.icalendar.WeekStartRecurParam
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
import com.github.naz013.domain.reminder.BiType

class RecurParamsToBiAdapter(
  private val biFactory: BiFactory
) {

  suspend operator fun invoke(params: List<RecurParam>): List<BuilderItem<*>> {
    return params.map { it.toBuilderItem() }.flatten()
  }

  private suspend fun RecurParam.toBuilderItem(): List<ICalBuilderItem<*>> {
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

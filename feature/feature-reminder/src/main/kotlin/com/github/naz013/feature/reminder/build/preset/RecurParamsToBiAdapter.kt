package com.github.naz013.feature.reminder.build.preset

import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.ICalBuilderItem
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
import com.github.naz013.feature.reminder.build.ICalUntilDateBuilderItem
import com.github.naz013.feature.reminder.build.ICalUntilTimeBuilderItem
import com.github.naz013.feature.reminder.build.ICalWeekStartBuilderItem
import com.github.naz013.feature.reminder.build.bi.BiFactory
import com.github.naz013.domain.reminder.BiType
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

internal class RecurParamsToBiAdapter(
  private val biFactory: BiFactory,
) {
  suspend operator fun invoke(params: List<RecurParam>): List<BuilderItem<*>> = params.map { it.toBuilderItem() }.flatten()

  private suspend fun RecurParam.toBuilderItem(): List<ICalBuilderItem<*>> =
    when (this) {
      is CountRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(BiType.ICAL_COUNT, value, ICalCountBuilderItem::class.java),
        )
      }
      is IntervalRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(
            BiType.ICAL_INTERVAL,
            value,
            ICalIntervalBuilderItem::class.java,
          ),
        )
      }
      is FreqRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(BiType.ICAL_FREQ, value, ICalFrequencyBuilderItem::class.java),
        )
      }
      is UntilRecurParam -> {
        value.dateTime?.let {
          listOfNotNull(
            biFactory.createWithValue(
              BiType.ICAL_UNTIL_DATE,
              it.toLocalDate(),
              ICalUntilDateBuilderItem::class.java,
            ),
            biFactory.createWithValue(
              BiType.ICAL_UNTIL_TIME,
              it.toLocalTime(),
              ICalUntilTimeBuilderItem::class.java,
            ),
          )
        } ?: emptyList()
      }
      is ByDayRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(BiType.ICAL_BYDAY, value, ICalByDayBuilderItem::class.java),
        )
      }
      is ByMonthRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(BiType.ICAL_BYMONTH, value, ICalByMonthBuilderItem::class.java),
        )
      }
      is ByMonthDayRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(
            BiType.ICAL_BYMONTHDAY,
            value,
            ICalByMonthDayBuilderItem::class.java,
          ),
        )
      }
      is ByHourRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(BiType.ICAL_BYHOUR, value, ICalByHourBuilderItem::class.java),
        )
      }
      is ByMinuteRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(
            BiType.ICAL_BYMINUTE,
            value,
            ICalByMinuteBuilderItem::class.java,
          ),
        )
      }
      is ByYearDayRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(
            BiType.ICAL_BYYEARDAY,
            value,
            ICalByYearDayBuilderItem::class.java,
          ),
        )
      }
      is ByWeekNumberRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(
            BiType.ICAL_BYWEEKNO,
            value,
            ICalByWeekNoBuilderItem::class.java,
          ),
        )
      }
      is BySetPosRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(
            BiType.ICAL_BYSETPOS,
            value,
            ICalBySetPosBuilderItem::class.java,
          ),
        )
      }
      is WeekStartRecurParam -> {
        listOfNotNull(
          biFactory.createWithValue(
            BiType.ICAL_WEEKSTART,
            value,
            ICalWeekStartBuilderItem::class.java,
          ),
        )
      }
    }
}

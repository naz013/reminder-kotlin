package com.elementary.tasks.reminder.build.bi

import android.content.Context
import com.elementary.tasks.R
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.BuilderItem
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
import com.elementary.tasks.reminder.build.adapter.BiTypeForUiAdapter
import com.elementary.tasks.reminder.build.formatter.datetime.DateFormatter
import com.elementary.tasks.reminder.build.formatter.datetime.TimeFormatter
import com.elementary.tasks.reminder.build.formatter.ical.ICalByMonthFormatter
import com.elementary.tasks.reminder.build.formatter.ical.ICalDayValueFormatter
import com.elementary.tasks.reminder.build.formatter.ical.ICalFreqFormatter
import com.elementary.tasks.reminder.build.formatter.ical.ICalGenericIntFormatter
import com.elementary.tasks.reminder.build.formatter.ical.ICalGenericListIntFormatter
import com.elementary.tasks.reminder.build.formatter.ical.ICalListDayValueFormatter
import com.elementary.tasks.reminder.create.fragments.recur.adapter.ParamToTextAdapter
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.common.ContextProvider

class BiFactoryICal(
  contextProvider: ContextProvider,
  private val biTypeForUiAdapter: BiTypeForUiAdapter,
  private val dateTimeManager: DateTimeManager,
  private val paramToTextAdapter: ParamToTextAdapter
) {

  private val context: Context = contextProvider.themedContext

  fun create(biType: BiType): BuilderItem<*> {
    return when (biType) {
      BiType.ICAL_BYSETPOS -> {
        ICalBySetPosBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_by_setpos_description),
          formatter = ICalGenericListIntFormatter(context)
        )
      }

      BiType.ICAL_BYDAY -> {
        ICalByDayBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_by_day_description),
          formatter = ICalListDayValueFormatter(context, paramToTextAdapter)
        )
      }

      BiType.ICAL_BYMONTH -> {
        ICalByMonthBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_by_month_description),
          formatter = ICalByMonthFormatter(context, dateTimeManager)
        )
      }

      BiType.ICAL_BYMONTHDAY -> {
        ICalByMonthDayBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_by_monthday_description),
          formatter = ICalGenericListIntFormatter(context)
        )
      }

      BiType.ICAL_BYYEARDAY -> {
        ICalByYearDayBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_by_yearday_description),
          formatter = ICalGenericListIntFormatter(context)
        )
      }

      BiType.ICAL_BYHOUR -> {
        ICalByHourBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_by_hour_description),
          formatter = ICalGenericListIntFormatter(context)
        )
      }

      BiType.ICAL_BYMINUTE -> {
        ICalByMinuteBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_by_minute_description),
          formatter = ICalGenericListIntFormatter(context)
        )
      }

      BiType.ICAL_BYWEEKNO -> {
        ICalByWeekNoBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_week_number_description),
          formatter = ICalGenericListIntFormatter(context)
        )
      }

      BiType.ICAL_FREQ -> {
        ICalFrequencyBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_frequency_description),
          formatter = ICalFreqFormatter(paramToTextAdapter)
        )
      }

      BiType.ICAL_COUNT -> {
        ICalCountBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_count_description),
          formatter = ICalGenericIntFormatter()
        )
      }

      BiType.ICAL_INTERVAL -> {
        ICalIntervalBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_interval_description),
          formatter = ICalGenericIntFormatter()
        )
      }

      BiType.ICAL_UNTIL_DATE -> {
        ICalUntilDateBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_until_date_description),
          formatter = DateFormatter(dateTimeManager)
        )
      }

      BiType.ICAL_UNTIL_TIME -> {
        ICalUntilTimeBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_until_time_description),
          formatter = TimeFormatter(dateTimeManager)
        )
      }

      BiType.ICAL_START_DATE -> {
        ICalStartDateBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_start_date_description),
          formatter = DateFormatter(dateTimeManager)
        )
      }

      BiType.ICAL_START_TIME -> {
        ICalStartTimeBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_start_time_description),
          formatter = TimeFormatter(dateTimeManager)
        )
      }

      BiType.ICAL_WEEKSTART -> {
        ICalWeekStartBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_week_start_description),
          formatter = ICalDayValueFormatter(paramToTextAdapter)
        )
      }

      else -> {
        throw IllegalArgumentException("Unknown biType: $biType")
      }
    }
  }
}

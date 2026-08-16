package com.github.naz013.feature.reminder.build.bi

import android.content.Context
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.build.BuilderItem
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
import com.github.naz013.feature.reminder.build.adapter.BiTypeForUiAdapter
import com.github.naz013.feature.reminder.build.adapter.ParamToTextAdapter
import com.github.naz013.feature.reminder.build.formatter.datetime.DateFormatter
import com.github.naz013.feature.reminder.build.formatter.datetime.TimeFormatter
import com.github.naz013.feature.reminder.build.formatter.ical.ICalByMonthFormatter
import com.github.naz013.feature.reminder.build.formatter.ical.ICalDayValueFormatter
import com.github.naz013.feature.reminder.build.formatter.ical.ICalFreqFormatter
import com.github.naz013.feature.reminder.build.formatter.ical.ICalGenericIntFormatter
import com.github.naz013.feature.reminder.build.formatter.ical.ICalGenericListIntFormatter
import com.github.naz013.feature.reminder.build.formatter.ical.ICalListDayValueFormatter
import com.github.naz013.common.ContextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.BiType

class BiFactoryICal(
  contextProvider: ContextProvider,
  private val biTypeForUiAdapter: BiTypeForUiAdapter,
  private val dateTimeManager: DateTimeManager,
  private val paramToTextAdapter: ParamToTextAdapter,
) {
  private val context: Context = contextProvider.themedContext

  fun create(biType: BiType): BuilderItem<*> =
    when (biType) {
      BiType.ICAL_BYSETPOS -> {
        ICalBySetPosBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_by_setpos_description),
          formatter = ICalGenericListIntFormatter(context),
        )
      }

      BiType.ICAL_BYDAY -> {
        ICalByDayBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_by_day_description),
          formatter = ICalListDayValueFormatter(context, paramToTextAdapter),
        )
      }

      BiType.ICAL_BYMONTH -> {
        ICalByMonthBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_by_month_description),
          formatter = ICalByMonthFormatter(context, dateTimeManager),
        )
      }

      BiType.ICAL_BYMONTHDAY -> {
        ICalByMonthDayBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_by_monthday_description),
          formatter = ICalGenericListIntFormatter(context),
        )
      }

      BiType.ICAL_BYYEARDAY -> {
        ICalByYearDayBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_by_yearday_description),
          formatter = ICalGenericListIntFormatter(context),
        )
      }

      BiType.ICAL_BYHOUR -> {
        ICalByHourBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_by_hour_description),
          formatter = ICalGenericListIntFormatter(context),
        )
      }

      BiType.ICAL_BYMINUTE -> {
        ICalByMinuteBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_by_minute_description),
          formatter = ICalGenericListIntFormatter(context),
        )
      }

      BiType.ICAL_BYWEEKNO -> {
        ICalByWeekNoBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_week_number_description),
          formatter = ICalGenericListIntFormatter(context),
        )
      }

      BiType.ICAL_FREQ -> {
        ICalFrequencyBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_frequency_description),
          formatter = ICalFreqFormatter(paramToTextAdapter),
        )
      }

      BiType.ICAL_COUNT -> {
        ICalCountBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_count_description),
          formatter = ICalGenericIntFormatter(),
        )
      }

      BiType.ICAL_INTERVAL -> {
        ICalIntervalBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_interval_description),
          formatter = ICalGenericIntFormatter(),
        )
      }

      BiType.ICAL_UNTIL_DATE -> {
        ICalUntilDateBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_until_date_description),
          formatter = DateFormatter(dateTimeManager),
        )
      }

      BiType.ICAL_UNTIL_TIME -> {
        ICalUntilTimeBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_until_time_description),
          formatter = TimeFormatter(dateTimeManager),
        )
      }

      BiType.ICAL_START_DATE -> {
        ICalStartDateBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_start_date_description),
          formatter = DateFormatter(dateTimeManager),
        )
      }

      BiType.ICAL_START_TIME -> {
        ICalStartTimeBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_start_time_description),
          formatter = TimeFormatter(dateTimeManager),
        )
      }

      BiType.ICAL_WEEKSTART -> {
        ICalWeekStartBuilderItem(
          title = biTypeForUiAdapter.getUiString(biType),
          description = context.getString(R.string.builder_week_start_description),
          formatter = ICalDayValueFormatter(paramToTextAdapter),
        )
      }

      else -> {
        throw IllegalArgumentException("Unknown biType: $biType")
      }
    }
}

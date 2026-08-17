package com.github.naz013.feature.reminder.build.quickstart

import android.app.AlarmManager
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.DateBuilderItem
import com.github.naz013.feature.reminder.build.DayOfMonthBuilderItem
import com.github.naz013.feature.reminder.build.DayOfYearBuilderItem
import com.github.naz013.feature.reminder.build.DaysOfWeekBuilderItem
import com.github.naz013.feature.reminder.build.SubTasksBuilderItem
import com.github.naz013.feature.reminder.build.SummaryBuilderItem
import com.github.naz013.feature.reminder.build.TimeBuilderItem
import com.github.naz013.feature.reminder.build.TimerBuilderItem
import com.github.naz013.feature.reminder.build.bi.BiFactory
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.WeekDaysProtocol
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.ShopItem
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

/**
 * Builds the [BuilderItem] list for each [QuickStartOption] - a fresh, unsaved template rather
 * than a [com.github.naz013.domain.RecurPreset]: it doesn't need to survive as a user-manageable
 * preset, and its label is independent from the generic preset/type names shown elsewhere (e.g.
 * "Alarm clock" in Manage Presets), so it stays available even if the user edits or deletes their
 * saved presets.
 */
internal class QuickStartItemsProvider(
  private val biFactory: BiFactory,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider,
) {
  suspend fun itemsFor(option: QuickStartOption): List<BuilderItem<*>> =
    when (option) {
      QuickStartOption.ONE_TIME ->
        listOfNotNull(
          summary(),
          biFactory.createWithValue(BiType.DATE, LocalDate.now(), DateBuilderItem::class.java),
          biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java),
        )

      QuickStartOption.EVERY_WEEKDAY ->
        listOfNotNull(
          summary(),
          biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java),
          biFactory.createWithValue(
            BiType.DAYS_OF_WEEK,
            WeekDaysProtocol.getWorkDays(),
            DaysOfWeekBuilderItem::class.java,
          ),
        )

      QuickStartOption.EVERY_MONTH_DAY ->
        listOfNotNull(
          summary(),
          biFactory.createWithValue(
            biType = BiType.DAY_OF_MONTH,
            value = LocalDate.now().dayOfMonth,
            clazz = DayOfMonthBuilderItem::class.java,
          ),
          biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java),
        )

      QuickStartOption.EVERY_YEAR ->
        listOfNotNull(
          summary(),
          biFactory.createWithValue(
            biType = BiType.DAY_OF_YEAR,
            value = LocalDate.now().dayOfYear,
            clazz = DayOfYearBuilderItem::class.java,
          ),
          biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java),
        )

      QuickStartOption.COUNTDOWN_TIMER ->
        listOfNotNull(
          summary(),
          biFactory.createWithValue(
            BiType.COUNTDOWN_TIMER,
            AlarmManager.INTERVAL_HOUR,
            TimerBuilderItem::class.java,
          ),
        )

      QuickStartOption.SHOPPING_LIST ->
        listOfNotNull(
          biFactory.createWithValue(BiType.SUMMARY,
            textProvider.getString(R.string.preset_buy_groceries), SummaryBuilderItem::class.java),
          biFactory.createWithValue(
            biType = BiType.SUB_TASKS,
            value =
              listOf(
                ShopItem(summary = textProvider.getString(R.string.preset_bread), position = 0, createTime = dateTimeManager.getNowGmtDateTime()),
                ShopItem(summary = textProvider.getString(R.string.preset_juice), position = 1, createTime = dateTimeManager.getNowGmtDateTime()),
                ShopItem(summary = textProvider.getString(R.string.preset_cookies), position = 2, createTime = dateTimeManager.getNowGmtDateTime()),
              ),
            clazz = SubTasksBuilderItem::class.java,
          ),
        )
    }

  private suspend fun summary(): SummaryBuilderItem? =
    biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java)
}

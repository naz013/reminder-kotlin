package com.elementary.tasks.reminder.build.preset

import android.app.AlarmManager
import com.elementary.tasks.R
import com.elementary.tasks.config.DayOfMonth
import com.github.naz013.domain.PresetBuilderScheme
import com.github.naz013.domain.PresetType
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.reminder.ShopItem
import com.elementary.tasks.core.protocol.WeekDaysProtocol
import com.github.naz013.feature.common.android.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.ApplicationBuilderItem
import com.elementary.tasks.reminder.build.ArrivingCoordinatesBuilderItem
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.DateBuilderItem
import com.elementary.tasks.reminder.build.DayOfMonthBuilderItem
import com.elementary.tasks.reminder.build.DayOfYearBuilderItem
import com.elementary.tasks.reminder.build.DaysOfWeekBuilderItem
import com.elementary.tasks.reminder.build.EmailBuilderItem
import com.elementary.tasks.reminder.build.EmailSubjectBuilderItem
import com.elementary.tasks.reminder.build.LeavingCoordinatesBuilderItem
import com.elementary.tasks.reminder.build.RepeatTimeBuilderItem
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.elementary.tasks.reminder.build.SummaryBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.TimerBuilderItem
import com.elementary.tasks.reminder.build.WebAddressBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.github.naz013.domain.reminder.BiType
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class DefaultPresetsGenerateUseCase(
  private val builderItemsToBuilderPresetAdapter: BuilderItemsToBuilderPresetAdapter,
  private val biFactory: BiFactory,
  private val dateTimeManager: DateTimeManager,
  private val textProvider: TextProvider
) {

  suspend operator fun invoke(): List<RecurPreset> {
    return listOf(
      createPreset(
        name = textProvider.getText(R.string.by_date),
        description = textProvider.getText(R.string.reminder_by_date_explanations),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(BiType.DATE, LocalDate.now(), DateBuilderItem::class.java),
            biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java),
            biFactory.createWithValue(
              biType = BiType.REPEAT_TIME,
              value = 0L,
              clazz = RepeatTimeBuilderItem::class.java
            )
          )
        )
      ),
      createPreset(
        name = textProvider.getText(R.string.timer),
        description = textProvider.getText(R.string.reminder_by_timer_explanations),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(
              BiType.COUNTDOWN_TIMER,
              AlarmManager.INTERVAL_HOUR,
              TimerBuilderItem::class.java
            )
          )
        )
      ),
      createPreset(
        name = textProvider.getText(R.string.alarm),
        description = textProvider.getText(R.string.reminder_by_weekday_explanations),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java),
            biFactory.createWithValue(
              BiType.DAYS_OF_WEEK,
              WeekDaysProtocol.getWorkDays(),
              DaysOfWeekBuilderItem::class.java
            )
          )
        )
      ),
      createPreset(
        name = textProvider.getText(R.string.e_mail),
        description = textProvider.getText(R.string.reminder_email_explanations),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(BiType.DATE, LocalDate.now(), DateBuilderItem::class.java),
            biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java),
            biFactory.createWithValue(BiType.EMAIL, "", EmailBuilderItem::class.java),
            biFactory.createWithValue(
              biType = BiType.EMAIL_SUBJECT,
              value = "",
              clazz = EmailSubjectBuilderItem::class.java
            )
          )
        )
      ),
      createPreset(
        name = textProvider.getText(R.string.application),
        description = textProvider.getText(R.string.launch_application),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(BiType.DATE, LocalDate.now(), DateBuilderItem::class.java),
            biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java),
            biFactory.createWithValue(
              biType = BiType.APPLICATION,
              value = "",
              clazz = ApplicationBuilderItem::class.java
            )
          )
        )
      ),
      createPreset(
        name = textProvider.getText(R.string.builder_web_address),
        description = textProvider.getText(R.string.open_link),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(BiType.DATE, LocalDate.now(), DateBuilderItem::class.java),
            biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java),
            biFactory.createWithValue(
              biType = BiType.LINK,
              value = "",
              clazz = WebAddressBuilderItem::class.java
            )
          )
        )
      ),
      createPreset(
        name = textProvider.getText(R.string.day_of_month),
        description = textProvider.getText(R.string.reminder_by_month_explanations),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(
              biType = BiType.DAY_OF_MONTH,
              value = LocalDate.now().dayOfMonth,
              clazz = DayOfMonthBuilderItem::class.java
            ),
            biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java)
          )
        )
      ),
      createPreset(
        name = textProvider.getText(R.string.builder_last_day_of_month),
        description = textProvider.getText(R.string.reminder_by_month_explanations),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(
              biType = BiType.DAY_OF_MONTH,
              value = DayOfMonth.LastDayOfMonth,
              clazz = DayOfMonthBuilderItem::class.java
            ),
            biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java)
          )
        )
      ),
      createPreset(
        name = textProvider.getText(R.string.yearly),
        description = textProvider.getText(R.string.reminder_by_year_explanations),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(
              biType = BiType.DAY_OF_YEAR,
              value = LocalDate.now().dayOfYear,
              clazz = DayOfYearBuilderItem::class.java
            ),
            biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java)
          )
        )
      ),
      createPreset(
        name = textProvider.getText(R.string.shopping_list),
        description = textProvider.getText(R.string.reminder_shopping_explanations),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(
              biType = BiType.SUMMARY,
              value = "Buy groceries",
              clazz = SummaryBuilderItem::class.java
            ),
            biFactory.createWithValue(
              biType = BiType.SUB_TASKS,
              value = listOf(
                ShopItem(
                  summary = "Bread",
                  position = 0,
                  createTime = dateTimeManager.getNowGmtDateTime()
                ),
                ShopItem(
                  summary = "Juice",
                  position = 1,
                  createTime = dateTimeManager.getNowGmtDateTime()
                ),
                ShopItem(
                  summary = "Cookies",
                  position = 2,
                  createTime = dateTimeManager.getNowGmtDateTime()
                )
              ),
              clazz = SubTasksBuilderItem::class.java
            )
          )
        )
      ),
      createPreset(
        name = textProvider.getText(R.string.builder_arriving_destination),
        description = textProvider.getText(R.string.reminder_by_location_explanations),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(
              biType = BiType.ARRIVING_COORDINATES,
              value = null,
              clazz = ArrivingCoordinatesBuilderItem::class.java
            )
          )
        )
      ),
      createPreset(
        name = textProvider.getText(R.string.builder_leaving_place),
        description = textProvider.getText(R.string.reminder_by_location_explanations),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(
              biType = BiType.LEAVING_COORDINATES,
              value = null,
              clazz = LeavingCoordinatesBuilderItem::class.java
            )
          )
        )
      )
    )
  }

  private fun createPreset(
    name: String,
    description: String?,
    scheme: List<PresetBuilderScheme>
  ): RecurPreset {
    return RecurPreset(
      name = name,
      recurObject = "",
      type = PresetType.BUILDER,
      createdAt = LocalDateTime.now(),
      useCount = 0,
      builderScheme = scheme,
      description = description
    )
  }

  private fun createScheme(items: List<BuilderItem<*>>): List<PresetBuilderScheme> {
    return builderItemsToBuilderPresetAdapter(items)
  }
}

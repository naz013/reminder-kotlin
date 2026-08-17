package com.github.naz013.feature.reminder.build.preset

import android.app.AlarmManager
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.build.ArrivingCoordinatesBuilderItem
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.DateBuilderItem
import com.github.naz013.feature.reminder.build.DaysOfWeekBuilderItem
import com.github.naz013.feature.reminder.build.PhoneCallBuilderItem
import com.github.naz013.feature.reminder.build.RepeatTimeBuilderItem
import com.github.naz013.feature.reminder.build.SubTasksBuilderItem
import com.github.naz013.feature.reminder.build.SummaryBuilderItem
import com.github.naz013.feature.reminder.build.TimeBuilderItem
import com.github.naz013.feature.reminder.build.TimerBuilderItem
import com.github.naz013.feature.reminder.build.WebAddressBuilderItem
import com.github.naz013.feature.reminder.build.bi.BiFactory
import com.github.naz013.common.ContextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.WeekDaysProtocol
import com.github.naz013.domain.Place
import com.github.naz013.domain.PresetBuilderScheme
import com.github.naz013.domain.PresetType
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.domain.sync.SyncState
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

internal class BuilderPresetsGenerateUseCase(
  private val contextProvider: ContextProvider,
  private val builderItemsToBuilderPresetAdapter: BuilderItemsToBuilderPresetAdapter,
  private val biFactory: BiFactory,
  private val dateTimeManager: DateTimeManager,
) {
  private val context by lazy { contextProvider.context }

  suspend operator fun invoke(): List<RecurPreset> =
    listOf(
      createPreset(
        name = context.getString(R.string.builder_preset_remind_at_exact_date_and_time),
        scheme =
          createScheme(
            listOfNotNull(
              biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
              biFactory.createWithValue(BiType.DATE, LocalDate.now(), DateBuilderItem::class.java),
              biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java),
            ),
          ),
      ),
      createPreset(
        name =
          context.getString(
            R.string.builder_preset_remind_at_exact_date_time_with_custom_repeat,
          ),
        scheme =
          createScheme(
            listOfNotNull(
              biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
              biFactory.createWithValue(BiType.DATE, LocalDate.now(), DateBuilderItem::class.java),
              biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java),
              biFactory.createWithValue(
                BiType.REPEAT_TIME,
                AlarmManager.INTERVAL_DAY * 2,
                RepeatTimeBuilderItem::class.java,
              ),
            ),
          ),
      ),
      createPreset(
        name =
          context.getString(
            R.string.builder_preset_remind_at_exact_time_and_repeat_from_monday_till_friday,
          ),
        scheme =
          createScheme(
            listOfNotNull(
              biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
              biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java),
              biFactory.createWithValue(
                BiType.DAYS_OF_WEEK,
                WeekDaysProtocol.getWorkDays(),
                DaysOfWeekBuilderItem::class.java,
              ),
            ),
          ),
      ),
      createPreset(
        name = context.getString(R.string.builder_preset_make_call_in_3_hours),
        scheme =
          createScheme(
            listOfNotNull(
              biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
              biFactory.createWithValue(
                BiType.COUNTDOWN_TIMER,
                AlarmManager.INTERVAL_HOUR * 3,
                TimerBuilderItem::class.java,
              ),
              biFactory.createWithValue(
                BiType.PHONE_CALL,
                "111222333",
                PhoneCallBuilderItem::class.java,
              ),
            ),
          ),
      ),
      createPreset(
        name = context.getString(R.string.builder_preset_open_link_in_browser),
        scheme =
          createScheme(
            listOfNotNull(
              biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
              biFactory.createWithValue(
                BiType.LINK,
                "https://changethis.com",
                WebAddressBuilderItem::class.java,
              ),
              biFactory.createWithValue(
                BiType.ARRIVING_COORDINATES,
                Place(
                  radius = 150,
                  latitude = 37.422131,
                  longitude = -122.084801,
                  name = "Googleplex, Mountain View, CA, USA",
                  syncState = SyncState.WaitingForUpload,
                ),
                ArrivingCoordinatesBuilderItem::class.java,
              ),
            ),
          ),
      ),
      createPreset(
        name =
          context.getString(
            R.string.builder_preset_permanent_reminder_with_sub_tasks_shopping_list,
          ),
        scheme =
          createScheme(
            listOfNotNull(
              biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
              biFactory.createWithValue(
                BiType.SUB_TASKS,
                listOf(
                  ShopItem(
                    summary = "Bread",
                    position = 0,
                    createTime = dateTimeManager.getNowGmtDateTime(),
                  ),
                  ShopItem(
                    summary = "Juice",
                    position = 1,
                    createTime = dateTimeManager.getNowGmtDateTime(),
                  ),
                  ShopItem(
                    summary = "Cookies",
                    position = 2,
                    createTime = dateTimeManager.getNowGmtDateTime(),
                  ),
                ),
                SubTasksBuilderItem::class.java,
              ),
            ),
          ),
      ),
    )

  private fun createPreset(
    name: String,
    scheme: List<PresetBuilderScheme>,
  ): RecurPreset =
    RecurPreset(
      name = name,
      recurObject = "",
      type = PresetType.BUILDER,
      createdAt = LocalDateTime.now(),
      useCount = 0,
      builderScheme = scheme,
      description = null,
      isDefault = false,
      recurItemsToAdd = null,
    )

  private fun createScheme(items: List<BuilderItem<*>>): List<PresetBuilderScheme> = builderItemsToBuilderPresetAdapter(items)
}

package com.elementary.tasks.reminder.build.preset

import android.app.AlarmManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.PresetBuilderScheme
import com.elementary.tasks.core.data.models.PresetType
import com.elementary.tasks.core.data.models.RecurPreset
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.protocol.WeekDaysProtocol
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.ArrivingCoordinatesBuilderItem
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.DateBuilderItem
import com.elementary.tasks.reminder.build.DaysOfWeekBuilderItem
import com.elementary.tasks.reminder.build.PhoneCallBuilderItem
import com.elementary.tasks.reminder.build.RepeatTimeBuilderItem
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.elementary.tasks.reminder.build.SummaryBuilderItem
import com.elementary.tasks.reminder.build.TimeBuilderItem
import com.elementary.tasks.reminder.build.TimerBuilderItem
import com.elementary.tasks.reminder.build.WebAddressBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.bi.BiType
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class BuilderPresetInitializer(
  private val contextProvider: ContextProvider,
  private val builderItemsToBuilderPresetAdapter: BuilderItemsToBuilderPresetAdapter,
  private val biFactory: BiFactory,
  private val dateTimeManager: DateTimeManager
) {

  private val context by lazy { contextProvider.context }

  operator fun invoke(): List<RecurPreset> {
    return listOf(
      createPreset(
        name = context.getString(R.string.builder_preset_remind_at_exact_date_and_time),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(BiType.DATE, LocalDate.now(), DateBuilderItem::class.java),
            biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java)
          )
        )
      ),
      createPreset(
        name = context.getString(
          R.string.builder_preset_remind_at_exact_date_time_with_custom_repeat
        ),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(BiType.DATE, LocalDate.now(), DateBuilderItem::class.java),
            biFactory.createWithValue(BiType.TIME, LocalTime.now(), TimeBuilderItem::class.java),
            biFactory.createWithValue(
              BiType.REPEAT_TIME,
              AlarmManager.INTERVAL_DAY * 2,
              RepeatTimeBuilderItem::class.java
            )
          )
        )
      ),
      createPreset(
        name = context.getString(
          R.string.builder_preset_remind_at_exact_time_and_repeat_from_monday_till_friday
        ),
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
        name = context.getString(R.string.builder_preset_make_call_in_3_hours),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(
              BiType.COUNTDOWN_TIMER,
              AlarmManager.INTERVAL_HOUR * 3,
              TimerBuilderItem::class.java
            ),
            biFactory.createWithValue(
              BiType.PHONE_CALL,
              "111222333",
              PhoneCallBuilderItem::class.java
            )
          )
        )
      ),
      createPreset(
        name = context.getString(R.string.builder_preset_open_link_in_browser),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(
              BiType.LINK,
              "https://changethis.com",
              WebAddressBuilderItem::class.java
            ),
            biFactory.createWithValue(
              BiType.ARRIVING_COORDINATES,
              Place(
                radius = 150,
                latitude = 37.422131,
                longitude = -122.084801,
                name = "Googleplex, Mountain View, CA, USA"
              ),
              ArrivingCoordinatesBuilderItem::class.java
            )
          )
        )
      ),
      createPreset(
        name = context.getString(
          R.string.builder_preset_permanent_reminder_with_sub_tasks_shopping_list
        ),
        scheme = createScheme(
          listOfNotNull(
            biFactory.createWithValue(BiType.SUMMARY, "", SummaryBuilderItem::class.java),
            biFactory.createWithValue(
              BiType.SUB_TASKS,
              listOf(
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
              SubTasksBuilderItem::class.java
            )
          )
        )
      )
    )
  }

  private fun createPreset(name: String, scheme: List<PresetBuilderScheme>): RecurPreset {
    return RecurPreset(
      name = name,
      recurObject = "",
      type = PresetType.BUILDER,
      createdAt = LocalDateTime.now(),
      useCount = 0,
      builderScheme = scheme
    )
  }

  private fun createScheme(items: List<BuilderItem<*>>): List<PresetBuilderScheme> {
    return builderItemsToBuilderPresetAdapter(items)
  }
}

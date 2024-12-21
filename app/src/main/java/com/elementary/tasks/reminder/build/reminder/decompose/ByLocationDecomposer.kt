package com.elementary.tasks.reminder.build.reminder.decompose

import com.github.naz013.domain.Reminder
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.ArrivingCoordinatesBuilderItem
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.LeavingCoordinatesBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayDateBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayTimeBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.github.naz013.domain.reminder.BiType
import org.threeten.bp.LocalDateTime

class ByLocationDecomposer(
  private val dateTimeManager: DateTimeManager,
  private val biFactory: BiFactory
) {

  suspend operator fun invoke(reminder: Reminder): List<BuilderItem<*>> {
    val type = UiReminderType(reminder.type)
    val place = when {
      type.isBase(UiReminderType.Base.LOCATION_IN) -> {
        reminder.places.takeIf { it.isNotEmpty() }
          ?.firstOrNull()
          ?.let {
            biFactory.createWithValue(
              BiType.ARRIVING_COORDINATES,
              it,
              ArrivingCoordinatesBuilderItem::class.java
            )
          }
      }

      type.isBase(UiReminderType.Base.LOCATION_OUT) -> {
        reminder.places.takeIf { it.isNotEmpty() }
          ?.firstOrNull()
          ?.let {
            biFactory.createWithValue(
              BiType.LEAVING_COORDINATES,
              it,
              LeavingCoordinatesBuilderItem::class.java
            )
          }
      }

      else -> {
        null
      }
    }

    val dateTime: LocalDateTime? = if (reminder.hasReminder) {
      dateTimeManager.fromGmtToLocal(reminder.eventTime)
    } else {
      null
    }

    return listOfNotNull(
      place,
      dateTime?.toLocalDate()?.let {
        biFactory.createWithValue(
          biType = BiType.LOCATION_DELAY_DATE,
          value = it,
          clazz = LocationDelayDateBuilderItem::class.java
        )
      },
      dateTime?.toLocalTime()?.let {
        biFactory.createWithValue(
          biType = BiType.LOCATION_DELAY_TIME,
          value = it,
          clazz = LocationDelayTimeBuilderItem::class.java
        )
      }
    )
  }
}

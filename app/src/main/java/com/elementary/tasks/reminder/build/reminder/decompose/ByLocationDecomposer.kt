package com.elementary.tasks.reminder.build.reminder.decompose

import com.elementary.tasks.reminder.build.ArrivingCoordinatesBuilderItem
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.LeavingCoordinatesBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayDateBuilderItem
import com.elementary.tasks.reminder.build.LocationDelayTimeBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import org.threeten.bp.LocalDateTime

class ByLocationDecomposer(
  private val dateTimeManager: DateTimeManager,
  private val biFactory: BiFactory,
) {
  suspend operator fun invoke(reminder: ReminderV2): List<BuilderItem<*>> {
    val place =
      when (reminder.recurrence) {
        RecurrenceRule.LocationEnter -> {
          reminder.places
            .takeIf { it.isNotEmpty() }
            ?.firstOrNull()
            ?.let {
              biFactory.createWithValue(
                BiType.ARRIVING_COORDINATES,
                it,
                ArrivingCoordinatesBuilderItem::class.java,
              )
            }
        }

        RecurrenceRule.LocationExit -> {
          reminder.places
            .takeIf { it.isNotEmpty() }
            ?.firstOrNull()
            ?.let {
              biFactory.createWithValue(
                BiType.LEAVING_COORDINATES,
                it,
                LeavingCoordinatesBuilderItem::class.java,
              )
            }
        }

        else -> null
      }

    val dateTime: LocalDateTime? =
      if (reminder.location?.hasDelayedReminder == true) {
        reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) }
      } else {
        null
      }

    return listOfNotNull(
      place,
      dateTime?.toLocalDate()?.let {
        biFactory.createWithValue(
          biType = BiType.LOCATION_DELAY_DATE,
          value = it,
          clazz = LocationDelayDateBuilderItem::class.java,
        )
      },
      dateTime?.toLocalTime()?.let {
        biFactory.createWithValue(
          biType = BiType.LOCATION_DELAY_TIME,
          value = it,
          clazz = LocationDelayTimeBuilderItem::class.java,
        )
      },
    )
  }
}

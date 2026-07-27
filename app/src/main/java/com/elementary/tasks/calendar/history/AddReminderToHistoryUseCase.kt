package com.elementary.tasks.calendar.history

import com.elementary.tasks.reminder.scheduling.behavior.v2.BehaviorStrategyResolverV2
import com.elementary.tasks.reminder.scheduling.behavior.v2.LocationBasedStrategyV2
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.history.EventHistoricalRecord
import com.github.naz013.domain.history.EventHistoricalRecordType
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.repository.EventHistoryRepository
import java.util.UUID

class AddReminderToHistoryUseCase(
  private val dateTimeManager: DateTimeManager,
  private val strategyResolver: BehaviorStrategyResolverV2,
  private val eventHistoryRepository: EventHistoryRepository,
) {
  suspend operator fun invoke(reminder: ReminderV2) {
    val strategy = strategyResolver.resolve(reminder)
    if (strategy is LocationBasedStrategyV2) {
      val dateTime = dateTimeManager.getCurrentDateTime()
      eventHistoryRepository.save(
        EventHistoricalRecord(
          id = UUID.randomUUID().toString(),
          eventId = reminder.uuId,
          type = EventHistoricalRecordType.Reminder,
          date = dateTime.toLocalDate(),
          time = dateTime.toLocalTime(),
        ),
      )
      Logger.i(TAG, "Added reminder with location to history, id=${reminder.uuId}")
    } else {
      val dateTime =
        reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) }
          ?: run {
            Logger.e(TAG, "Failed to add reminder to history, can't convert time for id=${reminder.uuId}")
            return
          }
      eventHistoryRepository.save(
        EventHistoricalRecord(
          id = UUID.randomUUID().toString(),
          eventId = reminder.uuId,
          type = EventHistoricalRecordType.Reminder,
          date = dateTime.toLocalDate(),
          time = dateTime.toLocalTime(),
        ),
      )
      Logger.i(TAG, "Added reminder to history, id=${reminder.uuId}")
    }
  }

  companion object {
    private const val TAG = "AddReminderToHistoryUseCase"
  }
}

package com.elementary.tasks.calendar.history

import com.github.naz013.domain.history.EventHistoricalRecord
import com.github.naz013.logging.Logger
import com.github.naz013.repository.EventHistoryRepository
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class GetHistoryByDayUseCase(
  private val historyRepository: EventHistoryRepository,
) {
  suspend operator fun invoke(date: LocalDate): List<EventHistoricalRecord> {
    val startOfTheDay =
      LocalTime
        .now()
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
    val endOfTheDay =
      LocalTime
        .now()
        .withHour(23)
        .withMinute(59)
        .withSecond(59)
        .withNano(999_999_999)
    return historyRepository
      .getByDateAndTimeRange(
        date,
        startOfTheDay,
        endOfTheDay,
      ).also {
        Logger.d(TAG, "Fetched ${it.size} history occurrences for $date")
      }
  }

  companion object {
    private const val TAG = "GetHistoryByDayUseCase"
  }
}

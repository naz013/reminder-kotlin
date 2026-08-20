package com.github.naz013.feature.calendar.history

import com.github.naz013.domain.history.EventHistoricalRecord
import com.github.naz013.logging.Logger
import com.github.naz013.repository.EventHistoryRepository
import org.threeten.bp.LocalDate

/** Fetches history records across a date range for the 1/3/7-day timeline views. */
internal class GetHistoryByDateRangeUseCase(
  private val historyRepository: EventHistoryRepository,
) {
  suspend operator fun invoke(
    startDate: LocalDate,
    endDate: LocalDate,
  ): List<EventHistoricalRecord> =
    historyRepository.getByDateRange(startDate, endDate).also {
      Logger.d(TAG, "Fetched ${it.size} history records from $startDate to $endDate")
    }

  companion object {
    private const val TAG = "GetHistoryByDateRange"
  }
}

package com.elementary.tasks.calendar.occurrence

import com.github.naz013.domain.occurance.EventOccurrence
import com.github.naz013.logging.Logger
import com.github.naz013.repository.EventOccurrenceRepository
import org.threeten.bp.LocalDate

class GetOccurrencesByDateRangeUseCase(
  private val eventOccurrenceRepository: EventOccurrenceRepository
) {

  suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate): List<EventOccurrence> {
    return eventOccurrenceRepository.getByDateRange(startDate, endDate).also {
      Logger.i(TAG, "Fetched ${it.size} occurrences from $startDate to $endDate")
    }
  }

  companion object {
    private const val TAG = "GetOccurrencesByDateRange"
  }
}

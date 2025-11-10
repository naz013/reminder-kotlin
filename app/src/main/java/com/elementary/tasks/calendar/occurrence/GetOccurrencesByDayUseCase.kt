package com.elementary.tasks.calendar.occurrence

import com.github.naz013.domain.occurance.EventOccurrence
import com.github.naz013.logging.Logger
import com.github.naz013.repository.EventOccurrenceRepository
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class GetOccurrencesByDayUseCase(
  private val eventOccurrenceRepository: EventOccurrenceRepository
) {

  suspend operator fun invoke(date: LocalDate): List<EventOccurrence> {
    val startOfTheDay = LocalTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)
    val endOfTheDay = LocalTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999_999_999)
    return eventOccurrenceRepository.getByDateAndTimeRange(date, startOfTheDay, endOfTheDay).also {
      Logger.d(TAG, "Fetched ${it.size} occurrences for $date")
    }
  }

  companion object {
    private const val TAG = "GetOccurrencesByDayUseCase"
  }
}

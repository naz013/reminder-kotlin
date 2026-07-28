package com.github.naz013.usecase.reminders

import com.github.naz013.domain.occurance.EventOccurrence
import com.github.naz013.repository.EventOccurrenceRepository
import org.threeten.bp.LocalDate

/** Reads pre-computed occurrence dates (populated by the app module's
 * `CalculateReminderOccurrencesUseCase` background task) for a date range — shared by the app
 * module's Calendar month view and `appwidgets`' calendar widget, so both read the same
 * model-agnostic occurrence table instead of duplicating recurrence evaluation. */
class GetOccurrencesByDateRangeUseCase(
  private val eventOccurrenceRepository: EventOccurrenceRepository
) {

  suspend operator fun invoke(startDate: LocalDate, endDate: LocalDate): List<EventOccurrence> {
    return eventOccurrenceRepository.getByDateRange(startDate, endDate)
  }
}

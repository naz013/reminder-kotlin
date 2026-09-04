package com.github.naz013.feature.settings.calendar.usecase

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.GoogleCalendarEvent
import com.github.naz013.domain.occurance.EventOccurrence
import com.github.naz013.domain.occurance.OccurrenceType
import com.github.naz013.logging.Logger
import com.github.naz013.repository.EventOccurrenceRepository
import org.dmfs.rfc5545.DateTime
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException
import org.dmfs.rfc5545.recur.RecurrenceRule
import org.threeten.bp.LocalDateTime
import java.util.TimeZone
import java.util.UUID

/**
 * Expands a [GoogleCalendarEvent] into [EventOccurrence] rows the calendar/timeline screens read
 * from, the same role [com.github.naz013.logic.birthday.CalculateBirthdayOccurrencesUseCase] and
 * `CalculateReminderOccurrencesUseCase` play for their domain types. Uses lib-recur's own RRULE
 * iterator directly - rather than the coarse "interval * frequency" approximation the old
 * scan-and-create-a-reminder code used - bounded by both a max count and a max time window so a
 * rule with neither COUNT nor UNTIL (recurs forever) can't loop unbounded.
 */
internal class CalculateGoogleCalendarEventOccurrencesUseCase(
  private val eventOccurrenceRepository: EventOccurrenceRepository,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(event: GoogleCalendarEvent) {
    eventOccurrenceRepository.deleteByEventId(event.uuId)
    val localStart = dateTimeManager.utcToLocal(event.startDateTime)
    // lib-recur's iterator yields the start instant itself as its first result, so - unlike the
    // reminder/birthday calculators, which explicitly prepend the start time to a calculator that
    // only ever returns *later* occurrences - the recurring branch must not also prepend it.
    val occurrenceDateTimes =
      if (event.rrule.isBlank()) listOf(localStart) else expandRecurrence(event.rrule, localStart)
    val occurrences =
      occurrenceDateTimes.map {
        EventOccurrence(
          id = UUID.randomUUID().toString(),
          eventId = event.uuId,
          date = it.toLocalDate(),
          time = it.toLocalTime(),
          type = OccurrenceType.CalendarEvent,
        )
      }
    eventOccurrenceRepository.saveAll(occurrences)
    Logger.i(TAG, "Saved ${occurrences.size} occurrences for Google Calendar event: ${event.uuId}")
  }

  private fun expandRecurrence(
    rrule: String,
    start: LocalDateTime,
  ): List<LocalDateTime> {
    val startMillis = dateTimeManager.toMillis(start)
    return try {
      val iterator = RecurrenceRule(rrule).iterator(DateTime(TimeZone.getDefault(), startMillis))
      val result = mutableListOf<LocalDateTime>()
      var count = 0
      while (iterator.hasNext() && count < MAX_OCCURRENCES) {
        val nextMillis = iterator.nextMillis()
        if (nextMillis - startMillis > MAX_WINDOW_MILLIS) break
        result.add(dateTimeManager.fromMillis(nextMillis))
        count++
      }
      result.ifEmpty { listOf(start) }
    } catch (e: InvalidRecurrenceRuleException) {
      Logger.e(TAG, "Failed to parse recurrence rule: $rrule", e)
      listOf(start)
    }
  }

  companion object {
    private const val TAG = "CalculateGoogleCalendarEventOccurrences"
    private const val MAX_OCCURRENCES = 60
    private const val MAX_WINDOW_MILLIS = 365L * 2 * 24 * 60 * 60 * 1000
  }
}

package com.github.naz013.logic.reminder.occurrence

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.occurance.EventOccurrence
import com.github.naz013.domain.occurance.OccurrenceType
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.behavior.NoReminderStrategyV2
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.ReminderV2Repository
import java.util.UUID

class CalculateReminderOccurrencesUseCase(
  private val reminderV2Repository: ReminderV2Repository,
  private val reminderStrategyResolver: BehaviorStrategyResolverV2,
  private val reminderPreferences: ReminderPreferences,
  private val eventOccurrenceRepository: EventOccurrenceRepository,
  private val reminderOccurrenceCalculatorFactory: ReminderOccurrenceCalculatorFactoryV2,
  private val dateTimeManager: DateTimeManager,
) {
  suspend operator fun invoke(id: String) {
    val reminder =
      reminderV2Repository.getById(id) ?: run {
        Logger.e(TAG, "Reminder with id=$id not found")
        return
      }
    if (reminder.places.isNotEmpty()) {
      Logger.i(TAG, "Reminder with id=$id has places, skipping occurrence calculation")
      return
    }
    val strategy = reminderStrategyResolver.resolve(reminder)
    if (strategy is NoReminderStrategyV2) {
      Logger.i(TAG, "Reminder with id=$id uses NoReminderStrategy, skipping occurrence calculation")
      return
    }
    Logger.v(TAG, "Clearing existing occurrences for reminder id=$id")
    eventOccurrenceRepository.deleteByEventId(id)
    Logger.i(TAG, "Calculating occurrences for reminder id=$id using strategy=${strategy::class.simpleName}")
    val numberOfOccurrences = reminderPreferences.numberOfReminderOccurrences
    val calculator = reminderOccurrenceCalculatorFactory.createCalculator(strategy)
    val eventDateTime =
      reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) } ?: run {
        Logger.e(TAG, "Failed to convert event time for reminder id=$id")
        return
      }
    val occurrences =
      listOf(eventDateTime) +
        calculator.calculateOccurrences(
          reminder,
          eventDateTime,
          numberOfOccurrences,
        )
    val eventOccurrences =
      occurrences.map { occurrenceDateTime ->
        EventOccurrence(
          id = UUID.randomUUID().toString(),
          eventId = reminder.uuId,
          date = occurrenceDateTime.toLocalDate(),
          time = occurrenceDateTime.toLocalTime(),
          type = OccurrenceType.Reminder,
        )
      }
    eventOccurrenceRepository.saveAll(eventOccurrences)
    Logger.i(TAG, "Saved ${eventOccurrences.size} occurrences for reminder id=$id")
  }

  companion object {
    private const val TAG = "CalculateReminderOccurrences"
  }
}

package com.elementary.tasks.calendar.occurrence

import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.reminder.scheduling.BehaviorStrategyResolver
import com.elementary.tasks.reminder.scheduling.NoReminderStrategy
import com.elementary.tasks.reminder.scheduling.occurrence.ReminderOccurrenceCalculatorFactory
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.occurance.EventOccurrence
import com.github.naz013.domain.occurance.OccurrenceType
import com.github.naz013.logging.Logger
import com.github.naz013.repository.EventOccurrenceRepository
import com.github.naz013.repository.ReminderRepository
import java.util.UUID

class CalculateReminderOccurrencesUseCase(
  private val reminderRepository: ReminderRepository,
  private val reminderStrategyResolver: BehaviorStrategyResolver,
  private val prefs: Prefs,
  private val eventOccurrenceRepository: EventOccurrenceRepository,
  private val reminderOccurrenceCalculatorFactory: ReminderOccurrenceCalculatorFactory,
  private val dateTimeManager: DateTimeManager,
) {

  suspend operator fun invoke(id: String) {
    val reminder = reminderRepository.getById(id) ?: run {
      Logger.e(TAG, "Reminder with id=$id not found")
      return
    }
    if (reminder.places.isNotEmpty()) {
      Logger.i(TAG, "Reminder with id=$id has places, skipping occurrence calculation")
      return
    }
    val strategy = reminderStrategyResolver.resolve(reminder)
    if (strategy is NoReminderStrategy) {
      Logger.i(TAG, "Reminder with id=$id uses NoReminderStrategy, skipping occurrence calculation")
      return
    }
    Logger.i(TAG, "Calculating occurrences for reminder id=$id using strategy=${strategy::class.simpleName}")
    val numberOfOccurrences = prefs.numberOfReminderOccurrences
    val calculator = reminderOccurrenceCalculatorFactory.createCalculator(strategy)
    val eventDateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime) ?: run {
      Logger.e(TAG, "Failed to convert event time for reminder id=$id")
      return
    }
    val occurrences = listOf(eventDateTime) + calculator.calculateOccurrences(
      reminder,
      eventDateTime,
      numberOfOccurrences
    )
    val eventOccurrences = occurrences.map { occurrenceDateTime ->
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

package com.elementary.tasks.calendar.occurrence

import com.elementary.tasks.calendar.occurrence.worker.CalculateBirthdayOccurrencesTask
import com.elementary.tasks.calendar.occurrence.worker.CalculateReminderOccurrencesTask
import com.elementary.tasks.reminder.scheduling.usecase.legacy.MigrateRecurringParamsUseCase
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.workapi.WorkScheduler

class MigrateExistingEventOccurrencesUseCase(
  private val birthdayRepository: BirthdayRepository,
  private val workScheduler: WorkScheduler,
  private val reminderRepository: ReminderRepository,
  private val migrateRecurringParamsUseCase: MigrateRecurringParamsUseCase,
) {
  suspend operator fun invoke() {
    migrateRecurringParamsUseCase()
    birthdayRepository
      .getAllIds()
      .also { Logger.i(TAG, "Going to migrate ${it.size} birthdays occurrences.") }
      .forEach { id ->
        workScheduler.enqueue(CalculateBirthdayOccurrencesTask.prepareWorkRequest(id))
      }

    reminderRepository
      .getAllIds()
      .also { Logger.i(TAG, "Going to migrate ${it.size} reminders occurrences.") }
      .forEach { id ->
        workScheduler.enqueue(CalculateReminderOccurrencesTask.prepareWorkRequest(id))
      }

    Logger.i(TAG, "Scheduled occurrence calculations for existing birthdays and reminders.")
  }

  companion object {
    private const val TAG = "MigrateEventOccurrences"
  }
}

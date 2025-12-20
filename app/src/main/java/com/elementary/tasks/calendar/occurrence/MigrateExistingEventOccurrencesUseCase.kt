package com.elementary.tasks.calendar.occurrence

import com.elementary.tasks.calendar.occurrence.worker.CalculateBirthdayOccurrencesWorker
import com.elementary.tasks.calendar.occurrence.worker.CalculateReminderOccurrencesWorker
import com.elementary.tasks.core.utils.work.WorkManagerProvider
import com.elementary.tasks.reminder.scheduling.usecase.legacy.MigrateRecurringParamsUseCase
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderRepository

class MigrateExistingEventOccurrencesUseCase(
  private val birthdayRepository: BirthdayRepository,
  private val workManagerProvider: WorkManagerProvider,
  private val reminderRepository: ReminderRepository,
  private val migrateRecurringParamsUseCase: MigrateRecurringParamsUseCase,
) {

  suspend operator fun invoke() {
    migrateRecurringParamsUseCase()
    birthdayRepository.getAllIds()
      .also { Logger.i(TAG, "Going to migrate ${it.size} birthdays occurrences.") }
      .forEach { id ->
      workManagerProvider.getWorkManager()
        .enqueue(CalculateBirthdayOccurrencesWorker.prepareWork(id))
    }

    reminderRepository.getAllIds()
      .also { Logger.i(TAG, "Going to migrate ${it.size} reminders occurrences.") }
      .forEach { id ->
        workManagerProvider.getWorkManager()
          .enqueue(CalculateReminderOccurrencesWorker.prepareWork(id))
      }

    Logger.i(TAG, "Scheduled occurrence calculations for existing birthdays and reminders.")
  }

  companion object {
    private const val TAG = "MigrateEventOccurrences"
  }
}

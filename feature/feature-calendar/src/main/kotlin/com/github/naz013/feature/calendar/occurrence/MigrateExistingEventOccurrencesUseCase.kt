package com.github.naz013.feature.calendar.occurrence

import com.github.naz013.logic.birthday.CalculateBirthdayOccurrencesTask
import com.github.naz013.logic.reminder.work.CalculateReminderOccurrencesTask
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.workapi.WorkScheduler

class MigrateExistingEventOccurrencesUseCase(
  private val birthdayRepository: BirthdayRepository,
  private val workScheduler: WorkScheduler,
  private val reminderV2Repository: ReminderV2Repository,
) {
  suspend operator fun invoke() {
    birthdayRepository
      .getAllIds()
      .also { Logger.i(TAG, "Going to migrate ${it.size} birthdays occurrences.") }
      .forEach { id ->
        workScheduler.enqueue(CalculateBirthdayOccurrencesTask.prepareWorkRequest(id))
      }

    reminderV2Repository
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

package com.elementary.tasks.calendar.occurrence

import com.elementary.tasks.calendar.occurrence.worker.CalculateBirthdayOccurrencesWorker
import com.elementary.tasks.calendar.occurrence.worker.CalculateReminderOccurrencesWorker
import com.elementary.tasks.core.utils.work.WorkManagerProvider
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderRepository

class MigrateExistingEventOccurrencesUseCase(
  private val birthdayRepository: BirthdayRepository,
  private val workManagerProvider: WorkManagerProvider,
  private val reminderRepository: ReminderRepository
) {

  suspend operator fun invoke() {
    birthdayRepository.getAll().forEach { birthday ->
      workManagerProvider.getWorkManager()
        .enqueue(CalculateBirthdayOccurrencesWorker.prepareWork(birthday.uuId))
    }

    reminderRepository.getAllIds().forEach { id ->
      workManagerProvider.getWorkManager()
        .enqueue(CalculateReminderOccurrencesWorker.prepareWork(id))
    }

    Logger.i(TAG, "Scheduled occurrence calculations for existing birthdays and reminders.")
  }

  companion object {
    private const val TAG = "MigrateEventOccurrences"
  }
}

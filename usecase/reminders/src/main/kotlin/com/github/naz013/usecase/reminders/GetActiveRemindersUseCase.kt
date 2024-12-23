package com.github.naz013.usecase.reminders

import com.github.naz013.domain.Reminder
import com.github.naz013.repository.ReminderRepository

class GetActiveRemindersUseCase(
  private val reminderRepository: ReminderRepository
) {

  suspend operator fun invoke(): List<Reminder> {
    return reminderRepository.getActive()
  }
}

package com.github.naz013.usecase.reminders

import com.github.naz013.domain.Reminder
import com.github.naz013.repository.ReminderRepository

class GetReminderByIdUseCase(
  private val reminderRepository: ReminderRepository
) {

  suspend operator fun invoke(id: String): Reminder? {
    return reminderRepository.getById(id)
  }
}

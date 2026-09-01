package com.github.naz013.appfunctions.reminder

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository

class SearchRemindersUseCase(
  private val reminderV2Repository: ReminderV2Repository,
) {
  suspend operator fun invoke(query: String): List<ReminderV2> = reminderV2Repository.search(query)
}

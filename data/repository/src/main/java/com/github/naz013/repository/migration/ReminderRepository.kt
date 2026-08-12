package com.github.naz013.repository.migration

import com.github.naz013.domain.Reminder

@Deprecated("Use ReminderV2")
internal interface ReminderRepository {
  suspend fun getAll(): List<Reminder>
}

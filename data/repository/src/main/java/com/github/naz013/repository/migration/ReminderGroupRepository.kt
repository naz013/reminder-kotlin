package com.github.naz013.repository.migration

import com.github.naz013.domain.ReminderGroup

@Deprecated("Use GroupV2")
internal interface ReminderGroupRepository {
  suspend fun getAll(): List<ReminderGroup>
}

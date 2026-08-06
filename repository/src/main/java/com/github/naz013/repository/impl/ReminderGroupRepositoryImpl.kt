package com.github.naz013.repository.impl

import com.github.naz013.domain.ReminderGroup
import com.github.naz013.logging.Logger
import com.github.naz013.repository.dao.ReminderGroupDao
import com.github.naz013.repository.migration.ReminderGroupRepository

@Deprecated("Use GroupV2")
internal class ReminderGroupRepositoryImpl(
  private val dao: ReminderGroupDao,
) : ReminderGroupRepository {

  override suspend fun getAll(): List<ReminderGroup> {
    Logger.d(TAG, "Get all reminder groups")
    return dao.all().map { it.toDomain() }
  }

  companion object {
    private const val TAG = "ReminderGroupRepository"
  }
}

package com.github.naz013.repository

import com.github.naz013.domain.ReminderGroup

interface ReminderGroupRepository {
  suspend fun save(reminderGroup: ReminderGroup)
  suspend fun saveAll(reminderGroups: List<ReminderGroup>)

  suspend fun getAll(): List<ReminderGroup>
  suspend fun getById(id: String): ReminderGroup?
  suspend fun defaultGroup(isDef: Boolean = true): ReminderGroup?
  suspend fun search(query: String): List<ReminderGroup>

  suspend fun delete(id: String)
  suspend fun deleteAll()
}

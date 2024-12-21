package com.github.naz013.repository

import com.github.naz013.domain.Reminder

interface ReminderRepository {
  suspend fun save(reminder: Reminder)

  suspend fun getById(id: String): Reminder?
  suspend fun search(query: String): List<Reminder>
  suspend fun getActive(): List<Reminder>
  suspend fun getActiveWithoutGpsTypes(): List<Reminder>
  suspend fun getActiveGpsTypes(): List<Reminder>
  suspend fun getAll(active: Boolean, removed: Boolean): List<Reminder>
  suspend fun getAll(): List<Reminder>
  suspend fun getAllTypes(active: Boolean, removed: Boolean, types: IntArray): List<Reminder>
  suspend fun getByNoteKey(key: String): List<Reminder>
  suspend fun getByRemovedStatus(removed: Boolean = false): List<Reminder>
  suspend fun getActiveInRange(removed: Boolean, fromTime: String, toTime: String): List<Reminder>
  suspend fun searchBySummaryAndRemovedStatus(
    query: String,
    removed: Boolean = false
  ): List<Reminder>
  suspend fun getAllTypesInRange(
    active: Boolean,
    removed: Boolean,
    fromTime: String,
    toTime: String
  ): List<Reminder>
  suspend fun searchBySummaryAllTypes(
    query: String,
    active: Boolean,
    removed: Boolean,
    types: IntArray
  ): List<Reminder>

  suspend fun delete(id: String)
  suspend fun deleteAll()
  suspend fun deleteAll(ids: List<String>)
}

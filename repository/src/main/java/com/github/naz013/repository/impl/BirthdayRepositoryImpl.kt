package com.github.naz013.repository.impl

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.dao.BirthdaysDao
import com.github.naz013.repository.entity.BirthdayEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table

internal class BirthdayRepositoryImpl(
  private val birthdaysDao: BirthdaysDao,
  private val tableChangeNotifier: TableChangeNotifier
) : BirthdayRepository {

  private val table = Table.Birthday

  override suspend fun save(birthday: Birthday) {
    Logger.d(TAG, "Saving birthday: ${birthday.uuId}")
    birthdaysDao.insert(BirthdayEntity(birthday))
    tableChangeNotifier.notify(table)
  }

  override suspend fun updateSyncState(id: String, state: SyncState) {
    Logger.d(TAG, "Updating sync state for birthday id: $id to state: $state")
    birthdaysDao.updateSyncState(id, state.name)
    tableChangeNotifier.notify(table)
  }

  override suspend fun getById(id: String): Birthday? {
    Logger.d(TAG, "Getting birthday by id: $id")
    return birthdaysDao.getById(id)?.toDomain()
  }

  override suspend fun getByDayMonth(day: Int, month: Int): List<Birthday> {
    Logger.d(TAG, "Getting birthdays by day: $day, month: $month")
    return birthdaysDao.getAll("$day|$month").map { it.toDomain() }
  }

  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> {
    Logger.d(TAG, "Getting birthdays by sync states: $syncStates")
    return birthdaysDao.getBySyncStates(syncStates.map { it.name })
  }

  override suspend fun searchByName(query: String): List<Birthday> {
    Logger.d(TAG, "Searching birthday by name: $query")
    return birthdaysDao.searchByName(query).map { it.toDomain() }
  }

  override suspend fun getAll(): List<Birthday> {
    Logger.d(TAG, "Getting all birthdays")
    return birthdaysDao.getAll().map { it.toDomain() }
  }

  override suspend fun getAll(dayMonth: String): List<Birthday> {
    Logger.d(TAG, "Getting all birthdays by day and month: $dayMonth")
    return birthdaysDao.getAll(dayMonth).map { it.toDomain() }
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Deleting birthday by id: $id")
    birthdaysDao.delete(id)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Deleting all birthdays")
    birthdaysDao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  companion object {
    private const val TAG = "BirthdayRepository"
  }
}

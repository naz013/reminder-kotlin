package com.github.naz013.repository.impl

import com.github.naz013.domain.UsedTime
import com.github.naz013.logging.Logger
import com.github.naz013.repository.UsedTimeRepository
import com.github.naz013.repository.dao.UsedTimeDao
import com.github.naz013.repository.entity.UsedTimeEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table

internal class UsedTimeRepositoryImpl(
  private val dao: UsedTimeDao,
  private val notifier: TableChangeNotifier
) : UsedTimeRepository {

  private val table = Table.UsedTime

  override suspend fun save(usedTime: UsedTime) {
    Logger.d(TAG, "Save used time: ${usedTime.id}")
    dao.insert(UsedTimeEntity(usedTime))
    notifier.notify(table)
  }

  override suspend fun getByTimeMills(timeMills: Long): UsedTime? {
    Logger.d(TAG, "Get used time by time mills: $timeMills")
    return dao.getByTimeMills(timeMills)?.toDomain()
  }

  override suspend fun getAll(): List<UsedTime> {
    Logger.d(TAG, "Get all used times")
    return dao.getAll().map { it.toDomain() }
  }

  override suspend fun getFirst(limit: Int): List<UsedTime> {
    Logger.d(TAG, "Get first $limit used times")
    return dao.getFirst(limit).map { it.toDomain() }
  }

  override suspend fun delete(id: Long) {
    Logger.d(TAG, "Delete used time: $id")
    dao.delete(id)
    notifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all used times")
    dao.deleteAll()
    notifier.notify(table)
  }

  companion object {
    private const val TAG = "UsedTimeRepository"
  }
}

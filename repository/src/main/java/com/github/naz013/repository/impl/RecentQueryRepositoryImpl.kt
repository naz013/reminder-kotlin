package com.github.naz013.repository.impl

import com.github.naz013.domain.RecentQuery
import com.github.naz013.logging.Logger
import com.github.naz013.repository.RecentQueryRepository
import com.github.naz013.repository.dao.RecentQueryDao
import com.github.naz013.repository.entity.RecentQueryEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table

internal class RecentQueryRepositoryImpl(
  private val recentQueryDao: RecentQueryDao,
  private val tableChangeNotifier: TableChangeNotifier
) : RecentQueryRepository {

  private val table = Table.RecentQuery

  override suspend fun save(recentQuery: RecentQuery) {
    Logger.d(TAG, "Save recent query: ${recentQuery.id}")
    recentQueryDao.insert(RecentQueryEntity(recentQuery))
    tableChangeNotifier.notify(table)
  }

  override suspend fun getById(id: Long): RecentQuery? {
    Logger.d(TAG, "Get recent query by id: $id")
    return recentQueryDao.getById(id)?.toDomain()
  }

  override suspend fun getByQuery(query: String): RecentQuery? {
    Logger.d(TAG, "Get recent query by query: $query")
    return recentQueryDao.getByQuery(query)?.toDomain()
  }

  override suspend fun search(query: String): List<RecentQuery> {
    Logger.d(TAG, "Search recent query by query: $query")
    return recentQueryDao.search(query).map { it.toDomain() }
  }

  override suspend fun getAll(): List<RecentQuery> {
    Logger.d(TAG, "Get all recent queries")
    return recentQueryDao.getAll().map { it.toDomain() }
  }

  override suspend fun delete(id: Long) {
    Logger.d(TAG, "Delete recent query by id: $id")
    recentQueryDao.delete(id)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all recent queries")
    recentQueryDao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  companion object {
    private const val TAG = "RecentQueryRepository"
  }
}

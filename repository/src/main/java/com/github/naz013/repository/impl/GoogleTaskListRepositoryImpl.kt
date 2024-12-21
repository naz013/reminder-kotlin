package com.github.naz013.repository.impl

import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.dao.GoogleTaskListsDao
import com.github.naz013.repository.entity.GoogleTaskListEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table

internal class GoogleTaskListRepositoryImpl(
  private val dao: GoogleTaskListsDao,
  private val tableChangeNotifier: TableChangeNotifier
) : GoogleTaskListRepository {

  private val table = Table.GoogleTaskList
  override suspend fun save(googleTaskList: GoogleTaskList) {
    Logger.d(TAG, "Save task list: ${googleTaskList.listId}")
    dao.insert(GoogleTaskListEntity(googleTaskList))
    tableChangeNotifier.notify(table)
  }

  override suspend fun getById(id: String): GoogleTaskList? {
    Logger.d(TAG, "Get task list by id: $id")
    return dao.getById(id)?.toDomain()
  }

  override suspend fun getAll(): List<GoogleTaskList> {
    Logger.d(TAG, "Get all task lists")
    return dao.all().map { it.toDomain() }
  }

  override suspend fun defaultGoogleTaskList(): GoogleTaskList? {
    Logger.d(TAG, "Get default task list")
    return dao.defaultGoogleTaskList()?.toDomain()
  }

  override suspend fun getDefault(): List<GoogleTaskList> {
    Logger.d(TAG, "Get all default task lists")
    return dao.getDefault().map { it.toDomain() }
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete task list: $id")
    dao.delete(id)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all task lists")
    dao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  companion object {
    private const val TAG = "GoogleTaskListRepository"
  }
}

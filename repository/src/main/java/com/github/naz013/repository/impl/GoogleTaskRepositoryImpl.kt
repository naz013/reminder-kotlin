package com.github.naz013.repository.impl

import com.github.naz013.domain.GoogleTask
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.dao.GoogleTasksDao
import com.github.naz013.repository.entity.GoogleTaskEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table

internal class GoogleTaskRepositoryImpl(
  private val dao: GoogleTasksDao,
  private val tableChangeNotifier: TableChangeNotifier
) : GoogleTaskRepository {

  private val table = Table.GoogleTask

  override suspend fun save(googleTask: GoogleTask) {
    Logger.d(TAG, "Save google task: ${googleTask.taskId}")
    dao.insert(GoogleTaskEntity(googleTask))
    tableChangeNotifier.notify(table)
  }

  override suspend fun saveAll(googleTasks: List<GoogleTask>) {
    Logger.d(TAG, "Save all google tasks: ${googleTasks.size}")
    dao.insertAll(googleTasks.map { GoogleTaskEntity(it) })
    tableChangeNotifier.notify(table)
  }

  override suspend fun getById(id: String): GoogleTask? {
    Logger.d(TAG, "Get google task by id: $id")
    return dao.getById(id)?.toDomain()
  }

  override suspend fun getByReminderId(id: String): GoogleTask? {
    Logger.d(TAG, "Get google task by reminder id: $id")
    return dao.getByReminderId(id)?.toDomain()
  }

  override suspend fun getAll(): List<GoogleTask> {
    Logger.d(TAG, "Get all google tasks")
    return dao.all().map { it.toDomain() }
  }

  override suspend fun search(query: String): List<GoogleTask> {
    Logger.d(TAG, "Search google tasks: $query")
    return dao.search(query).map { it.toDomain() }
  }

  override suspend fun getAllByList(listId: String, status: String): List<GoogleTask> {
    Logger.d(TAG, "Get all google tasks by list: $listId, status: $status")
    return dao.getAllByList(listId, status).map { it.toDomain() }
  }

  override suspend fun getAllByList(listId: String): List<GoogleTask> {
    Logger.d(TAG, "Get all google tasks by list: $listId")
    return dao.getAllByList(listId).map { it.toDomain() }
  }

  override suspend fun getAttachedToReminder(): List<GoogleTask> {
    Logger.d(TAG, "Get all google tasks attached to reminder")
    return dao.getAttachedToReminder().map { it.toDomain() }
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete google task by id: $id")
    dao.delete(id)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all google tasks")
    dao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll(ids: List<String>) {
    Logger.d(TAG, "Delete all google tasks by ids: $ids")
    dao.deleteAll(ids)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll(listId: String) {
    Logger.d(TAG, "Delete all google tasks by list id: $listId")
    dao.deleteAll(listId)
    tableChangeNotifier.notify(table)
  }

  companion object {
    private const val TAG = "GoogleTaskRepository"
  }
}

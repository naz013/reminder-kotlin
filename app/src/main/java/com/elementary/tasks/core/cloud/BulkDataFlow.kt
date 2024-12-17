package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.cloud.completables.Completable
import com.elementary.tasks.core.cloud.converters.Convertible
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.repositories.Repository
import com.elementary.tasks.core.cloud.storages.DataChannel
import com.elementary.tasks.core.cloud.storages.Storage
import com.github.naz013.logging.Logger

class BulkDataFlow<T>(
  private val repository: Repository<T>,
  private val convertible: Convertible<T>,
  private val storage: Storage,
  private val completable: Completable<T>? = null
) {

  private val dataFlow = DataFlow(repository, convertible, storage, completable)

  suspend fun backup() {
    repository.all().forEach { dataFlow.backup(it) }
  }

  suspend fun restore(indexTypes: IndexTypes, deleteFile: Boolean) {
    Logger.d("restore: type=$indexTypes")
    val channel = object : DataChannel<T> {
      override suspend fun onNewData(data: T) {
        Logger.d("restore: onNewData $data")
        repository.insert(data)
        completable?.action(data)
      }
    }
    storage.restoreAll(dataFlow.getFileExt(indexTypes), deleteFile, convertible, channel)
  }
}

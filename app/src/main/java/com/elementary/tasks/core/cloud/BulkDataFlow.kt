package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.cloud.completables.Completable
import com.elementary.tasks.core.cloud.converters.Convertible
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.repositories.Repository
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.cloud.storages.Storage
import kotlinx.coroutines.channels.consumeEach
import timber.log.Timber

class BulkDataFlow<T>(
  private val repository: Repository<T>,
  private val convertible: Convertible<T>,
  private val storage: Storage,
  private val completable: Completable<T>? = null
) {

  private val dataFlow = DataFlow(repository, convertible, storage, completable)

  suspend fun backup() {
    repository.all().forEach { dataFlow.backup(it, false) }
    dataFlow.saveIndex()
    System.gc()
  }

  suspend fun restore(indexTypes: IndexTypes, deleteFile: Boolean) {
    storage.restoreAll(dataFlow.getFileExt(indexTypes), deleteFile).consumeEach {
      val item = convertible.convert(it) ?: return
      Timber.d("restore: $item")
      repository.insert(item)
      completable?.action(item)
    }
  }

  companion object {
    suspend fun fullBackup(syncManagers: SyncManagers) {
      val storage = CompositeStorage(syncManagers.storageManager)
      BulkDataFlow(
        syncManagers.repositoryManager.groupDataFlowRepository,
        syncManagers.converterManager.groupConverter,
        storage,
        completable = null
      ).backup()
      BulkDataFlow(
        syncManagers.repositoryManager.reminderDataFlowRepository,
        syncManagers.converterManager.reminderConverter,
        storage,
        syncManagers.completableManager.reminderCompletable
      ).backup()
      BulkDataFlow(
        syncManagers.repositoryManager.noteDataFlowRepository,
        syncManagers.converterManager.noteConverter,
        storage,
        completable = null
      ).backup()
      BulkDataFlow(
        syncManagers.repositoryManager.birthdayDataFlowRepository,
        syncManagers.converterManager.birthdayConverter,
        storage,
        completable = null
      ).backup()
      BulkDataFlow(
        syncManagers.repositoryManager.placeDataFlowRepository,
        syncManagers.converterManager.placeConverter,
        storage,
        completable = null
      ).backup()
      BulkDataFlow(
        syncManagers.repositoryManager.templateDataFlowRepository,
        syncManagers.converterManager.templateConverter,
        storage,
        completable = null
      ).backup()
      BulkDataFlow(
        syncManagers.repositoryManager.settingsDataFlowRepository,
        syncManagers.converterManager.settingsConverter,
        storage,
        completable = null
      ).backup()
    }
  }
}
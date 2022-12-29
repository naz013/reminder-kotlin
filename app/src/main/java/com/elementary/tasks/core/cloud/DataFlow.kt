package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.cloud.completables.Completable
import com.elementary.tasks.core.cloud.converters.Convertible
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.repositories.Repository
import com.elementary.tasks.core.cloud.storages.Storage
import com.elementary.tasks.core.cloud.storages.StorageManager
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class DataFlow<T>(
  private val repository: Repository<T>,
  private val convertible: Convertible<T>,
  private val storage: Storage,
  private val completable: Completable<T>? = null
) : KoinComponent {

  private val dateTimeManager by inject<DateTimeManager>()

  suspend fun saveIndex() {
    storage.saveIndex()
  }

  suspend fun backup(id: String, updateIndex: Boolean = true) {
    val item = repository.get(id)
    if (item == null) {
      storage.removeIndex(id)
      return
    }
    backup(item, updateIndex)
  }

  suspend fun backup(item: T, updateIndex: Boolean = true) {
    System.gc()
    val fileIndex = convertible.convert(item)
    val metadata = convertible.metadata(item)
    if (fileIndex == null) {
      storage.removeIndex(metadata.id)
      return
    }
    if (!fileIndex.readyToBackup) {
      storage.removeIndex(metadata.id)
      return
    }

    storage.backup(fileIndex, metadata)
    if (updateIndex) storage.saveIndex(fileIndex)
    completable?.action(item)
    System.gc()
    Timber.d("backup: ${metadata.id}")
  }

  suspend fun restore(id: String, type: IndexTypes) {
    val fileName = fileName(id, type)
    if (id.isEmpty() || fileName.isEmpty()) {
      return
    }
    System.gc()
    val inputStream = storage.restore(fileName) ?: return
    val item = convertible.convert(inputStream) ?: return
    val localItem = repository.get(id)
    val needUpdate = if (localItem != null) {
      val metadata = convertible.metadata(item)
      val metadataLocal = convertible.metadata(localItem)
      dateTimeManager.isAfterDate(metadata.updatedAt, metadataLocal.updatedAt)
    } else {
      true
    }
    System.gc()
    Timber.d("restore: $id, $needUpdate")
    if (needUpdate) {
      repository.insert(item)
      completable?.action(item)
    }
  }

  suspend fun delete(id: String, type: IndexTypes) {
    val fileName = fileName(id, type)
    if (id.isEmpty() || fileName.isEmpty()) {
      return
    }
    System.gc()
    Timber.d("delete: $id")
    runCatching {
      val t = repository.get(id)
      if (t != null) {
        completable?.action(t)
        repository.delete(t)
      }
    }
    storage.delete(fileName)
    storage.removeIndex(id)
    System.gc()
  }

  private fun fileName(id: String, type: IndexTypes): String {
    val ext = getFileExt(type)
    return id + ext
  }

  fun getFileExt(type: IndexTypes): String {
    return when (type) {
      IndexTypes.TYPE_REMINDER -> FileConfig.FILE_NAME_REMINDER
      IndexTypes.TYPE_NOTE -> FileConfig.FILE_NAME_NOTE
      IndexTypes.TYPE_BIRTHDAY -> FileConfig.FILE_NAME_BIRTHDAY
      IndexTypes.TYPE_GROUP -> FileConfig.FILE_NAME_GROUP
      IndexTypes.TYPE_TEMPLATE -> FileConfig.FILE_NAME_TEMPLATE
      IndexTypes.TYPE_PLACE -> FileConfig.FILE_NAME_PLACE
      IndexTypes.TYPE_SETTINGS -> FileConfig.FILE_NAME_SETTINGS_EXT
    }
  }

  companion object {
    fun availableStorageList(
      storageManager: StorageManager
    ): List<Storage> {
      return listOfNotNull(
        storageManager.gDrive.takeIf { storageManager.googleBackup },
        storageManager.dropbox.takeIf { storageManager.dropboxBackup },
        storageManager.localStorage.takeIf { storageManager.localBackup }
      )
    }
  }
}

package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.cloud.completables.Completable
import com.elementary.tasks.core.cloud.converters.Convertible
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.repositories.Repository
import com.elementary.tasks.core.cloud.storages.Storage
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DataFlow<T>(
  private val repository: Repository<T>,
  private val convertible: Convertible<T>,
  private val storage: Storage,
  private val completable: Completable<T>? = null
) : KoinComponent {

  private val dateTimeManager by inject<DateTimeManager>()

  suspend fun backup(id: String) {
    val item = repository.get(id) ?: return
    backup(item)
  }

  suspend fun backup(item: T) {
    val stream = convertible.toOutputStream(item) ?: return
    val metadata = convertible.metadata(item)
    storage.backup(stream, metadata)
    Traces.log("Backed up file with ext = ${metadata.fileExt} and id = ${metadata.id}")
  }

  suspend fun restore(id: String, type: IndexTypes) {
    val fileName = fileName(id, type)
    if (id.isEmpty() || fileName.isEmpty()) {
      return
    }
    val inputStream = storage.restore(fileName) ?: return
    val item = convertible.convert(inputStream) ?: return
    val localItem = repository.get(id)
    val metadata = convertible.metadata(item)
    val needUpdate = if (localItem != null) {
      val metadataLocal = convertible.metadata(localItem)
      dateTimeManager.isAfterDate(metadata.updatedAt, metadataLocal.updatedAt)
    } else {
      true
    }
    if (needUpdate) {
      Traces.log("Saved remote file with ext = ${metadata.fileExt} and id = $id")
      repository.insert(item)
      completable?.action(item)
    }
  }

  suspend fun delete(id: String, type: IndexTypes) {
    val fileName = fileName(id, type)
    if (id.isEmpty() || fileName.isEmpty()) {
      return
    }
    Traces.log("Delete file with type = $type and id = $id")
    runCatching {
      val t = repository.get(id)
      if (t != null) {
        completable?.action(t)
        repository.delete(t)
      }
    }
    storage.delete(fileName)
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
      IndexTypes.TYPE_PLACE -> FileConfig.FILE_NAME_PLACE
      IndexTypes.TYPE_SETTINGS -> FileConfig.FILE_NAME_SETTINGS_EXT
    }
  }
}

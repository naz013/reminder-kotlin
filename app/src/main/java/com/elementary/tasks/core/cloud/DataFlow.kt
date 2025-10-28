package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.cloud.completables.Completable
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.repositories.Repository
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.cloudapi.FileConfig
import com.github.naz013.cloudapi.legacy.Convertible
import com.github.naz013.logging.Logger
import org.koin.core.component.KoinComponent

class DataFlow<T>(
  private val repository: Repository<T>,
  private val convertible: Convertible<T>,
  private val storage: CloudFileApi,
  private val completable: Completable<T>? = null
) : KoinComponent {

  suspend fun backup(id: String) {
    val item = repository.get(id) ?: run {
      Logger.w(TAG, "Item with id = $id not found")
      return
    }
    backup(item)
  }

  suspend fun backup(item: T) {
    val stream = convertible.toOutputStream(item) ?: run {
      Logger.w(TAG, "Item stream is null")
      return
    }
    val metadata = convertible.metadata(item)
    val millis = System.currentTimeMillis()
    Logger.i(TAG, "Backup file with ext = ${metadata.fileExt} and id = ${metadata.id}")
    storage.saveFile(stream, metadata)
    val duration = System.currentTimeMillis() - millis
    Logger.i(
      TAG,
      "Backed up file with ext = ${metadata.fileExt} and id = ${metadata.id} in $duration ms"
    )
  }

  suspend fun delete(id: String, type: IndexTypes) {
    val fileName = fileName(id, type)
    if (id.isEmpty() || fileName.isEmpty()) {
      Logger.w(TAG, "Id or file name is empty, id = $id")
      return
    }
    Logger.i(TAG, "Going to delete file with ext = $fileName, id = $id")
    val millis = System.currentTimeMillis()
    runCatching {
      val t = repository.get(id)
      if (t != null) {
        completable?.action(t)
        repository.delete(t)
      }
    }
    storage.deleteFile(fileName)
    val duration = System.currentTimeMillis() - millis
    Logger.i(TAG, "Deleted file with ext = $fileName, id = $id in $duration ms")
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

  companion object {
    private const val TAG = "DataFlow"
  }
}

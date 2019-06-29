package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.cloud.converters.Convertible
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.repositories.Repository
import com.elementary.tasks.core.cloud.storages.Storage

class BulkDataFlow<T>(private val repository: Repository<T>,
                      private val convertible: Convertible<T>,
                      private val storage: Storage,
                      private val completable: Completable<T>? = null) {

    private val dataFlow = DataFlow(repository, convertible, storage, completable)

    fun backup(ids: List<String>) {
        ids.forEach {
            dataFlow.backup(it)
        }
    }

    fun restore(type: String) {
//        val fileName = fileName(id, type)
//        if (id.isEmpty() || type.isEmpty() || fileName.isEmpty()) {
//            return
//        }
//        val encrypted = storage.restore(fileName) ?: return
//        val item = convertible.convert(encrypted) ?: return
//        repository.insert(item)
//        completable?.action(item)
    }

    private fun fileName(id: String, type: String): String {
        val ext = getFileExt(type)
        return id + ext
    }

    private fun getFileExt(type: String): String {
        return when (type) {
            IndexTypes.TYPE_REMINDER -> FileConfig.FILE_NAME_REMINDER
            IndexTypes.TYPE_NOTE -> FileConfig.FILE_NAME_NOTE
            IndexTypes.TYPE_BIRTHDAY -> FileConfig.FILE_NAME_BIRTHDAY
            IndexTypes.TYPE_GROUP -> FileConfig.FILE_NAME_GROUP
            IndexTypes.TYPE_TEMPLATE -> FileConfig.FILE_NAME_TEMPLATE
            IndexTypes.TYPE_PLACE -> FileConfig.FILE_NAME_PLACE
            IndexTypes.TYPE_SETTINGS -> FileConfig.FILE_NAME_SETTINGS
            else -> ""
        }
    }
}
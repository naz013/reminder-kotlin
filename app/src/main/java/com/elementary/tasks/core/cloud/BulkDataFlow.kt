package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.cloud.converters.Convertible
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.repositories.Repository
import com.elementary.tasks.core.cloud.storages.Storage

class BulkDataFlow<T>(repository: Repository<T>,
                      convertible: Convertible<T>,
                      storage: Storage,
                      completable: Completable<T>? = null) {

    private val dataFlow = DataFlow(repository, convertible, storage, completable)

    suspend fun backup(ids: List<String>) {
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
}
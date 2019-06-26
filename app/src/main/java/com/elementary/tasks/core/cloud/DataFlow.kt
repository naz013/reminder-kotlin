package com.elementary.tasks.core.cloud

import com.elementary.tasks.core.cloud.repositories.Repository

class DataFlow<T>(private val repository: Repository<T>, private val storage: Storage<T>,
                  private val completable: Completable<T>? = null) {

    fun backup(id: String) {
        val item = repository.get(id)
        if (item == null) {
            storage.removeIndex(id)
            return
        }
        if (storage.hasIndex(id)) {
            storage.backup(item)
        }
        completable?.action(item)
    }

    fun restore(id: String) {

    }
}
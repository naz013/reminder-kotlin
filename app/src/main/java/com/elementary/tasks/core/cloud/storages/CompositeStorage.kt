package com.elementary.tasks.core.cloud.storages

import com.elementary.tasks.core.cloud.converters.Metadata

class CompositeStorage(private val storageList: List<Storage>) : Storage() {
    override fun backup(json: String, metadata: Metadata) {
        storageList.forEach { it.backup(json, metadata) }
    }

    override fun restore(fileName: String): String? {
        return null
    }

    override fun delete(fileName: String) {
        storageList.forEach { it.delete(fileName) }
    }

    override fun removeIndex(id: String) {
        storageList.forEach { it.removeIndex(id) }
    }

    override fun saveIndex(fileIndex: FileIndex) {
        storageList.forEach { it.saveIndex(fileIndex) }
    }

    override fun hasIndex(id: String): Boolean {
        return false
    }

    override fun needBackup(id: String, updatedAt: String): Boolean {
        return true
    }
}
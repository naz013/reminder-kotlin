package com.elementary.tasks.core.cloud.storages

import com.elementary.tasks.core.cloud.converters.Metadata

class LocalStorage : Storage() {
    override suspend fun backup(json: String, metadata: Metadata) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun restore(fileName: String): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun delete(fileName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeIndex(id: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveIndex(fileIndex: FileIndex) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hasIndex(id: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun needBackup(id: String, updatedAt: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
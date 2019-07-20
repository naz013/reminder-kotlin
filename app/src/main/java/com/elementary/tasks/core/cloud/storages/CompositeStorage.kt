package com.elementary.tasks.core.cloud.storages

import com.elementary.tasks.core.cloud.converters.Metadata
import com.elementary.tasks.core.utils.launchIo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach

class CompositeStorage(private val storageList: List<Storage>) : Storage() {
    override suspend fun backup(json: String, metadata: Metadata) {
        storageList.forEach { it.backup(json, metadata) }
    }

    override suspend fun restore(fileName: String): String? {
        storageList.forEach {
            val data = it.restore(fileName)
            if (data != null) return data
        }
        return null
    }

    override fun restoreAll(ext: String, deleteFile: Boolean): Channel<String> {
        val channel = Channel<String>()
        if (storageList.isEmpty()) {
            channel.cancel()
            return channel
        }
        launchIo {
            loadIndex()
            storageList.forEach {
                it.restoreAll(ext, deleteFile).consumeEach { json ->
                    channel.send(json)
                }
            }
            channel.close()
        }
        return channel
    }

    override suspend fun delete(fileName: String) {
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

    override suspend fun loadIndex() {
        launchIo {
            storageList.forEach {
                it.loadIndex()
            }
        }
    }

    override fun sendNotification(type: String, details: String) {
        storageList.forEach { it.sendNotification(type, details) }
    }
}
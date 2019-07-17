package com.elementary.tasks.core.cloud.storages

import com.elementary.tasks.core.cloud.converters.Metadata
import kotlinx.coroutines.channels.Channel

interface Backupable {
    suspend fun backup(json: String, metadata: Metadata)

    suspend fun restore(fileName: String): String?

    suspend fun delete(fileName: String)

    fun restoreAll(ext: String, deleteFile: Boolean): Channel<String>

    fun sendNotification(type: String, details: String)
}
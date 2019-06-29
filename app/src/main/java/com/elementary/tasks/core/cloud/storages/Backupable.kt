package com.elementary.tasks.core.cloud.storages

import com.elementary.tasks.core.cloud.converters.Metadata

interface Backupable {
    suspend fun backup(json: String, metadata: Metadata)

    suspend fun restore(fileName: String): String?

    suspend fun delete(fileName: String)
}
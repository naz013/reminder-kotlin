package com.elementary.tasks.core.cloud

interface Indexable {
    fun removeIndex(id: String)

    fun saveIndex(fileIndex: IndexDataFile.FileIndex)

    fun hasIndex(id: String): Boolean
}
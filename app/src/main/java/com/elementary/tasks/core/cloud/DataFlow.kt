package com.elementary.tasks.core.cloud

import android.content.Context
import com.elementary.tasks.core.cloud.completables.Completable
import com.elementary.tasks.core.cloud.converters.Convertible
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.repositories.Repository
import com.elementary.tasks.core.cloud.storages.Dropbox
import com.elementary.tasks.core.cloud.storages.GDrive
import com.elementary.tasks.core.cloud.storages.LocalStorage
import com.elementary.tasks.core.cloud.storages.Storage
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TimeUtil
import timber.log.Timber
import java.lang.Exception

class DataFlow<T>(private val repository: Repository<T>,
                  private val convertible: Convertible<T>,
                  private val storage: Storage,
                  private val completable: Completable<T>? = null) {

    suspend fun backup(id: String) {
        val item = repository.get(id)
        if (item == null) {
            storage.removeIndex(id)
            return
        }
        backup(item)
    }

    suspend fun backup(item: T) {
        val fileIndex = convertible.convert(item)
        val metadata = convertible.metadata(item)
        if (fileIndex == null) {
            storage.removeIndex(metadata.id)
            return
        }
        val json = fileIndex.json
        if (json == null) {
            storage.removeIndex(metadata.id)
            return
        }

        storage.backup(json, metadata)
        storage.saveIndex(fileIndex)
        completable?.action(item)
        Timber.d("backup: ${metadata.id}")
    }

    suspend fun restore(id: String, type: IndexTypes) {
        val fileName = fileName(id, type)
        if (id.isEmpty() || fileName.isEmpty()) {
            return
        }
        val encrypted = storage.restore(fileName) ?: return
        val item = convertible.convert(encrypted) ?: return
        val localItem = repository.get(id)
        val needUpdate = if (localItem != null) {
            val metadata = convertible.metadata(item)
            val metadataLocal = convertible.metadata(localItem)
            TimeUtil.isAfterDate(metadata.updatedAt, metadataLocal.updatedAt)
        } else {
            true
        }
        Timber.d("restore: $id, $needUpdate")
        if (needUpdate) {
            repository.insert(item)
            completable?.action(item)
        }
    }

    suspend fun delete(id: String, type: IndexTypes, notify: Boolean = false) {
        val fileName = fileName(id, type)
        if (id.isEmpty() || fileName.isEmpty()) {
            return
        }
        Timber.d("delete: $id")
        try {
            val t = repository.get(id)
            if (t != null) {
                completable?.action(t)
                repository.delete(t)
            }
        } catch (e: Exception) {
        }
        storage.delete(fileName)
        storage.removeIndex(id)
        if (notify) {
            storage.sendNotification("delete", fileName)
        }
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
            IndexTypes.TYPE_TEMPLATE -> FileConfig.FILE_NAME_TEMPLATE
            IndexTypes.TYPE_PLACE -> FileConfig.FILE_NAME_PLACE
            IndexTypes.TYPE_SETTINGS -> FileConfig.FILE_NAME_SETTINGS_EXT
        }
    }

    companion object {
        fun availableStorageList(context: Context): List<Storage> {
            val storageList = mutableListOf<Storage>()
            val googleStorage = GDrive.getInstance(context)
            if (googleStorage != null) {
                storageList.add(googleStorage)
            }
            val dropboxStorage = Dropbox()
            if (dropboxStorage.isLinked) {
                storageList.add(dropboxStorage)
            }
            if (!Module.isQ && Permissions.checkPermission(context, Permissions.WRITE_EXTERNAL, Permissions.READ_EXTERNAL)) {
                storageList.add(LocalStorage(context))
            }
            return storageList
        }
    }
}
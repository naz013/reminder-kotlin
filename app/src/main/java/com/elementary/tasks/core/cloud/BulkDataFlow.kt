package com.elementary.tasks.core.cloud

import android.content.Context
import com.elementary.tasks.core.cloud.completables.Completable
import com.elementary.tasks.core.cloud.completables.ReminderCompletable
import com.elementary.tasks.core.cloud.converters.*
import com.elementary.tasks.core.cloud.repositories.*
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.cloud.storages.Storage
import kotlinx.coroutines.channels.consumeEach

class BulkDataFlow<T>(private val repository: Repository<T>,
                      private val convertible: Convertible<T>,
                      private val storage: Storage,
                      private val completable: Completable<T>? = null) {

    private val dataFlow = DataFlow(repository, convertible, storage, completable)

    suspend fun backup() {
        repository.all().forEach {
            dataFlow.backup(it)
        }
    }

    suspend fun restore(indexTypes: IndexTypes, deleteFile: Boolean) {
        storage.restoreAll(dataFlow.getFileExt(indexTypes), deleteFile).consumeEach {
            val item = convertible.convert(it) ?: return
            repository.insert(item)
            completable?.action(item)
        }
    }

    companion object {
        suspend fun fullBackup(context: Context) {
            val storage = CompositeStorage(DataFlow.availableStorageList(context))
            BulkDataFlow(GroupRepository(), GroupConverter(), storage, null).backup()
            BulkDataFlow(ReminderRepository(), ReminderConverter(), storage, ReminderCompletable()).backup()
            BulkDataFlow(NoteRepository(), NoteConverter(), storage, null).backup()
            BulkDataFlow(BirthdayRepository(), BirthdayConverter(), storage, null).backup()
            BulkDataFlow(PlaceRepository(), PlaceConverter(), storage, null).backup()
            BulkDataFlow(TemplateRepository(), TemplateConverter(), storage, null).backup()
            BulkDataFlow(SettingsRepository(), SettingsConverter(), storage, null).backup()
        }
    }
}
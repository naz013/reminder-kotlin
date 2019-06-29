package com.elementary.tasks.login

import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.completables.ReminderCompletable
import com.elementary.tasks.core.cloud.converters.*
import com.elementary.tasks.core.cloud.repositories.*
import com.elementary.tasks.core.cloud.storages.Dropbox
import com.elementary.tasks.core.cloud.storages.GDrive
import com.elementary.tasks.core.cloud.storages.LocalStorage
import com.elementary.tasks.core.cloud.storages.Storage
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.work.BackupDataWorker
import com.elementary.tasks.groups.GroupsUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject

class LoginViewModel : ViewModel(), LifecycleObserver, KoinComponent {

    private val appDb: AppDb by inject()
    private val prefs: Prefs by inject()
    private val context: Context by inject()

    var message: MutableLiveData<Int> = MutableLiveData()
    var isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var isReady: MutableLiveData<Boolean> = MutableLiveData()

    private var job: Job? = null

    fun loadDataFromGoogle() {
        val storage = GDrive.getInstance(context)

        if (storage == null) {
            isLoading.postValue(false)
            isReady.postValue(true)
            return
        }

        loadData(storage)
    }

    fun loadDataFromDropbox() {
        val storage = Dropbox()

        if (!storage.isLinked) {
            isLoading.postValue(false)
            isReady.postValue(true)
            return
        }

        loadData(storage)
    }

    fun loadDataFromLocal() {
        if (!Permissions.checkPermission(context, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            isLoading.postValue(false)
            isReady.postValue(true)
            return
        }
        loadData(LocalStorage())
    }

    fun loadData(storage: Storage) {
        message.postValue(R.string.please_wait)
        isLoading.postValue(true)
        job = launchDefault {
            prefs.isBackupEnabled = true

            message.postValue(R.string.syncing_groups)
            BulkDataFlow(GroupRepository(), GroupConverter(), storage, null)
                    .restore(IndexTypes.TYPE_GROUP, false)

            runBlocking {
                verifyGroups()
            }

            message.postValue(R.string.syncing_reminders)
            BulkDataFlow(ReminderRepository(), ReminderConverter(), storage, ReminderCompletable())
                    .restore(IndexTypes.TYPE_REMINDER, false)

            message.postValue(R.string.syncing_notes)
            BulkDataFlow(NoteRepository(), NoteConverter(), storage, null)
                    .restore(IndexTypes.TYPE_NOTE, false)

            message.postValue(R.string.syncing_birthdays)
            BulkDataFlow(BirthdayRepository(), BirthdayConverter(), storage, null)
                    .restore(IndexTypes.TYPE_BIRTHDAY, false)

            message.postValue(R.string.syncing_places)
            BulkDataFlow(PlaceRepository(), PlaceConverter(), storage, null)
                    .restore(IndexTypes.TYPE_PLACE, false)

            message.postValue(R.string.syncing_templates)
            BulkDataFlow(TemplateRepository(), TemplateConverter(), storage, null)
                    .restore(IndexTypes.TYPE_TEMPLATE, false)

            BulkDataFlow(SettingsRepository(), SettingsConverter(), storage, null)
                    .restore(IndexTypes.TYPE_SETTINGS, false)

            withUIContext {
                isLoading.postValue(false)
                isReady.postValue(true)
            }
        }
    }

    private fun verifyGroups() {
        val list = appDb.reminderGroupDao().all()
        if (list.isEmpty()) {
            val defUiID = GroupsUtil.initDefault(context)
            val items = appDb.reminderDao().all()
            val dao = appDb.reminderDao()
            for (item in items) {
                item.groupUuId = defUiID
                dao.insert(item)
            }
            if (prefs.isBackupEnabled) {
                BackupDataWorker.schedule()
            }
        }
    }

    fun onBack() {
        job?.cancel()
    }
}
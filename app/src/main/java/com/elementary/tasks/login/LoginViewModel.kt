package com.elementary.tasks.login

import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.storages.Dropbox
import com.elementary.tasks.core.cloud.storages.GDrive
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.work.BackupDataWorker
import com.elementary.tasks.groups.GroupsUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.IOException

class LoginViewModel : ViewModel(), LifecycleObserver, KoinComponent {

    private val appDb: AppDb by inject()
    private val backupTool: BackupTool by inject()
    private val prefs: Prefs by inject()
    private val context: Context by inject()

    var message: MutableLiveData<Int> = MutableLiveData()
    var isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var isReady: MutableLiveData<Boolean> = MutableLiveData()

    private var job: Job? = null

    fun loadDataFromGoogle() {
        message.postValue(R.string.please_wait)
        isLoading.postValue(true)
        job = launchDefault {
            prefs.isBackupEnabled = true
            GDrive.getInstance(context)?.let { drive ->
                message.postValue(R.string.syncing_groups)
                try {
                    drive.downloadGroups(false)
                } catch (e: Exception) {
                }


                runBlocking {
                    verifyGroups()
                }

                message.postValue(R.string.syncing_reminders)
                try {
                    drive.downloadReminders(false)
                } catch (e: Exception) {
                }

                //export & import notes
                message.postValue(R.string.syncing_notes)
                try {
                    drive.downloadNotes(false)
                } catch (e: Exception) {
                }

                //export & import birthdays
                message.postValue(R.string.syncing_birthdays)
                try {
                    drive.downloadBirthdays(false)
                } catch (e: Exception) {
                }

                //export & import places
                message.postValue(R.string.syncing_places)
                try {
                    drive.downloadPlaces(false)
                } catch (e: Exception) {
                }

                //export & import templates
                message.postValue(R.string.syncing_templates)
                try {
                    drive.downloadTemplates(false)
                } catch (e: Exception) {
                }
                try {
                    drive.downloadSettings(false)
                } catch (e: Exception) {
                }
            }

            withUIContext {
                isLoading.postValue(false)
                isReady.postValue(true)
            }
        }
    }

    fun loadDataFromDropbox() {
        message.postValue(R.string.please_wait)
        isLoading.postValue(true)
        job = launchDefault {
            prefs.isBackupEnabled = true
            val drive = Dropbox()

            message.postValue(R.string.syncing_groups)
            drive.downloadGroups(false)

            runBlocking {
                verifyGroups()
            }

            message.postValue(R.string.syncing_reminders)
            drive.downloadReminders(false)

            //export & import notes
            message.postValue(R.string.syncing_notes)
            drive.downloadNotes(false)

            //export & import birthdays
            message.postValue(R.string.syncing_birthdays)
            drive.downloadBirthdays(false)

            //export & import places
            message.postValue(R.string.syncing_places)
            drive.downloadPlaces(false)

            //export & import templates
            message.postValue(R.string.syncing_templates)
            drive.downloadTemplates(false)
            drive.downloadSettings()

            withUIContext {
                isLoading.postValue(false)
                isReady.postValue(true)
            }
        }
    }

    fun loadDataFromLocal() {
        message.postValue(R.string.please_wait)
        isLoading.postValue(true)
        job = launchDefault {
            prefs.isBackupEnabled = true

            message.postValue(R.string.syncing_groups)
            try {
                backupTool.importGroups()
            } catch (ignored: IOException) {
            }

            runBlocking {
                verifyGroups()
            }

            message.postValue(R.string.syncing_reminders)
            try {
                backupTool.importReminders()
            } catch (ignored: IOException) {
            }

            //export & import notes
            message.postValue(R.string.syncing_notes)
            try {
                backupTool.importNotes()
            } catch (ignored: IOException) {
            }

            //export & import birthdays
            message.postValue(R.string.syncing_birthdays)
            try {
                backupTool.importBirthdays()
            } catch (ignored: IOException) {
            }

            //export & import places
            message.postValue(R.string.syncing_places)
            try {
                backupTool.importPlaces()
            } catch (ignored: IOException) {
            }

            //export & import templates
            message.postValue(R.string.syncing_templates)
            try {
                backupTool.importTemplates()
            } catch (ignored: IOException) {
            }
            prefs.loadPrefsFromFile()

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
            BackupDataWorker.schedule()
        }
    }

    fun onBack() {
        job?.cancel()
    }
}
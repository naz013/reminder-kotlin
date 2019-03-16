package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.groups.GroupsUtil
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class SyncDataWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), KoinComponent {

    private val prefs: Prefs by inject()
    private val backupTool: BackupTool by inject()
    private val appDb: AppDb by inject()

    override fun doWork(): Result {
        if (prefs.autoSyncState == 0) {
            return Result.success()
        }
        val ioHelper = IoHelper(applicationContext, prefs, backupTool)
        val syncFlags = prefs.autoSyncFlags
        launchDefault {
            if (syncFlags.contains(FLAG_REMINDER)) {
                ioHelper.restoreGroup(true)
                val list = appDb.reminderGroupDao().all()
                if (list.isEmpty()) {
                    val defUiID = GroupsUtil.initDefault(applicationContext)
                    val items = appDb.reminderDao().all()
                    for (item in items) {
                        item.groupUuId = defUiID
                    }
                    appDb.reminderDao().insertAll(items)
                }
                ioHelper.backupGroup()

                ioHelper.restoreReminder(true)
                ioHelper.backupReminder()
            }

            if (syncFlags.contains(FLAG_NOTE)) {
                ioHelper.restoreNote(true)
                ioHelper.backupNote()
            }

            if (syncFlags.contains(FLAG_BIRTHDAY)) {
                ioHelper.restoreBirthday(true)
                ioHelper.backupBirthday()
            }

            if (syncFlags.contains(FLAG_PLACE)) {
                ioHelper.restorePlaces(true)
                ioHelper.backupPlaces()
            }

            if (syncFlags.contains(FLAG_TEMPLATE)) {
                ioHelper.restoreTemplates(true)
                ioHelper.backupTemplates()
            }

            if (syncFlags.contains(FLAG_SETTINGS)) {
                ioHelper.restoreSettings()
                ioHelper.backupSettings()
            }

            withUIContext {
                UpdatesHelper.updateWidget(applicationContext)
                UpdatesHelper.updateNotesWidget(applicationContext)
            }
        }
        return Result.success()
    }

    companion object {
        private const val TAG = "SyncDataWorker"
        const val FLAG_REMINDER = "flag.reminder"
        const val FLAG_NOTE = "flag.note"
        const val FLAG_BIRTHDAY = "flag.birthday"
        const val FLAG_PLACE = "flag.place"
        const val FLAG_TEMPLATE = "flag.template"
        const val FLAG_SETTINGS = "flag.settings"

        fun schedule() {
            val work = OneTimeWorkRequest.Builder(SyncDataWorker::class.java)
                    .addTag(TAG)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }
}
package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.*
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.completables.ReminderCompletable
import com.elementary.tasks.core.cloud.converters.*
import com.elementary.tasks.core.cloud.repositories.*
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.groups.GroupsUtil
import org.koin.core.KoinComponent
import org.koin.core.inject

class SyncDataWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), KoinComponent {

    private val prefs: Prefs by inject()
    private val appDb: AppDb by inject()

    override fun doWork(): Result {
        if (prefs.autoSyncState == 0) {
            return Result.success()
        }
        val storage = CompositeStorage(DataFlow.availableStorageList(applicationContext))
        val syncFlags = prefs.autoSyncFlags
        launchDefault {
            if (syncFlags.contains(FLAG_REMINDER)) {
                BulkDataFlow(GroupRepository(), GroupConverter(), storage, null)
                        .restore(IndexTypes.TYPE_GROUP, true)
                val list = appDb.reminderGroupDao().all()
                if (list.isEmpty()) {
                    val defUiID = GroupsUtil.initDefault(applicationContext)
                    val items = appDb.reminderDao().all()
                    for (item in items) {
                        item.groupUuId = defUiID
                    }
                    appDb.reminderDao().insertAll(items)
                }
                BulkDataFlow(GroupRepository(), GroupConverter(), storage, null)
                        .backup()

                BulkDataFlow(ReminderRepository(), ReminderConverter(), storage, ReminderCompletable())
                        .restore(IndexTypes.TYPE_REMINDER, true)
                BulkDataFlow(ReminderRepository(), ReminderConverter(), storage, null)
                        .backup()
            }

            if (syncFlags.contains(FLAG_NOTE)) {
                BulkDataFlow(NoteRepository(), NoteConverter(), storage, null)
                        .restore(IndexTypes.TYPE_NOTE, true)
                BulkDataFlow(NoteRepository(), NoteConverter(), storage, null)
                        .backup()
            }

            if (syncFlags.contains(FLAG_BIRTHDAY)) {
                BulkDataFlow(BirthdayRepository(), BirthdayConverter(), storage, null)
                        .restore(IndexTypes.TYPE_BIRTHDAY, true)
                BulkDataFlow(BirthdayRepository(), BirthdayConverter(), storage, null)
                        .backup()
            }

            if (syncFlags.contains(FLAG_PLACE)) {
                BulkDataFlow(PlaceRepository(), PlaceConverter(), storage, null)
                        .restore(IndexTypes.TYPE_PLACE, true)
                BulkDataFlow(PlaceRepository(), PlaceConverter(), storage, null)
                        .backup()
            }

            if (syncFlags.contains(FLAG_TEMPLATE)) {
                BulkDataFlow(TemplateRepository(), TemplateConverter(), storage, null)
                        .restore(IndexTypes.TYPE_TEMPLATE, true)
                BulkDataFlow(TemplateRepository(), TemplateConverter(), storage, null)
                        .backup()
            }

            if (syncFlags.contains(FLAG_SETTINGS)) {
                BulkDataFlow(SettingsRepository(), SettingsConverter(), storage, null)
                        .restore(IndexTypes.TYPE_SETTINGS, true)
                BulkDataFlow(SettingsRepository(), SettingsConverter(), storage, null)
                        .backup()
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

        fun schedule(context: Context) {
            val work = OneTimeWorkRequest.Builder(SyncDataWorker::class.java)
                    .addTag(TAG)
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.UNMETERED)
                            .setRequiresBatteryNotLow(true)
                            .build())
                    .build()
            WorkManager.getInstance(context).enqueue(work)
        }
    }
}
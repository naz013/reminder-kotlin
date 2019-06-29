package com.elementary.tasks.core.work

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.completables.ReminderCompletable
import com.elementary.tasks.core.cloud.converters.*
import com.elementary.tasks.core.cloud.repositories.*
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.groups.GroupsUtil
import kotlinx.coroutines.Job

object SyncWorker {

    private var mJob: Job? = null
    private var mLastMsg: String? = null
    var onEnd: (() -> Unit)? = null
    var progress: ((String) -> Unit)? = null
        set(value) {
            field = value
            val msg = mLastMsg ?: return
            if (mJob != null) {
                value?.invoke(msg)
            }
        }
    var listener: ((Boolean) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(mJob != null)
        }

    fun sync(context: Context) {
        mJob?.cancel()
        launchSync(context)
    }

    fun unsubscribe() {
        onEnd = null
        listener = null
        progress = null
    }

    private fun launchSync(context: Context) {
        val storage = CompositeStorage(DataFlow.availableStorageList(context))
        mJob = launchIo {
            notifyMsg(context.getString(R.string.syncing_groups))
            BulkDataFlow(GroupRepository(), GroupConverter(), storage, null)
                    .restore(IndexTypes.TYPE_GROUP, true)
            val list = AppDb.getAppDatabase(context).reminderGroupDao().all()
            if (list.isEmpty()) {
                val defUiID = GroupsUtil.initDefault(context)
                val items = AppDb.getAppDatabase(context).reminderDao().all()
                for (item in items) {
                    item.groupUuId = defUiID
                }
                AppDb.getAppDatabase(context).reminderDao().insertAll(items)
            }
            BulkDataFlow(GroupRepository(), GroupConverter(), storage, null)
                    .backup()

            notifyMsg(context.getString(R.string.syncing_reminders))
            BulkDataFlow(ReminderRepository(), ReminderConverter(), storage, ReminderCompletable())
                    .restore(IndexTypes.TYPE_REMINDER, true)
            BulkDataFlow(ReminderRepository(), ReminderConverter(), storage, null)
                    .backup()

            notifyMsg(context.getString(R.string.syncing_notes))
            BulkDataFlow(NoteRepository(), NoteConverter(), storage, null)
                    .restore(IndexTypes.TYPE_NOTE, true)
            BulkDataFlow(NoteRepository(), NoteConverter(), storage, null)
                    .backup()

            notifyMsg(context.getString(R.string.syncing_birthdays))
            BulkDataFlow(BirthdayRepository(), BirthdayConverter(), storage, null)
                    .restore(IndexTypes.TYPE_BIRTHDAY, true)
            BulkDataFlow(BirthdayRepository(), BirthdayConverter(), storage, null)
                    .backup()

            notifyMsg(context.getString(R.string.syncing_places))
            BulkDataFlow(PlaceRepository(), PlaceConverter(), storage, null)
                    .restore(IndexTypes.TYPE_PLACE, true)
            BulkDataFlow(PlaceRepository(), PlaceConverter(), storage, null)
                    .backup()

            notifyMsg(context.getString(R.string.syncing_templates))
            BulkDataFlow(TemplateRepository(), TemplateConverter(), storage, null)
                    .restore(IndexTypes.TYPE_TEMPLATE, true)
            BulkDataFlow(TemplateRepository(), TemplateConverter(), storage, null)
                    .backup()

            BulkDataFlow(SettingsRepository(), SettingsConverter(), storage, null)
                    .backup()

            withUIContext {
                UpdatesHelper.updateWidget(context)
                UpdatesHelper.updateNotesWidget(context)
                onEnd?.invoke()
            }
            mJob = null
        }
    }

    private suspend fun notifyMsg(msg: String) {
        mLastMsg = msg
        withUIContext { progress?.invoke(msg) }
    }
}
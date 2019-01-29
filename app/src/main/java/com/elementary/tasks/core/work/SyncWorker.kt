package com.elementary.tasks.core.work

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.IoHelper
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

    fun sync(context: Context, ioHelper: IoHelper) {
        mJob?.cancel()
        launchSync(context, ioHelper)
    }

    fun unsubscribe() {
        onEnd = null
        listener = null
        progress = null
    }

    private fun launchSync(context: Context, ioHelper: IoHelper) {
        mJob = launchIo {
            notifyMsg(context.getString(R.string.syncing_groups))
            ioHelper.restoreGroup(true)
            val list = AppDb.getAppDatabase(context).reminderGroupDao().all()
            if (list.isEmpty()) {
                val defUiID = GroupsUtil.initDefault(context)
                val items = AppDb.getAppDatabase(context).reminderDao().all()
                for (item in items) {
                    item.groupUuId = defUiID
                }
                AppDb.getAppDatabase(context).reminderDao().insertAll(items)
            }
            ioHelper.backupGroup()

            notifyMsg(context.getString(R.string.syncing_reminders))
            ioHelper.restoreReminder(true)
            ioHelper.backupReminder()

            notifyMsg(context.getString(R.string.syncing_notes))
            ioHelper.restoreNote(true)
            ioHelper.backupNote()

            notifyMsg(context.getString(R.string.syncing_birthdays))
            ioHelper.restoreBirthday(true)
            ioHelper.backupBirthday()

            notifyMsg(context.getString(R.string.syncing_places))
            ioHelper.restorePlaces(true)
            ioHelper.backupPlaces()

            notifyMsg(context.getString(R.string.syncing_templates))
            ioHelper.restoreTemplates(true)
            ioHelper.backupTemplates()
            ioHelper.backupSettings()

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
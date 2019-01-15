package com.elementary.tasks.core.work

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.IoHelper
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.withUIContext
import kotlinx.coroutines.Job
import timber.log.Timber

object BackupWorker {

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
            Timber.d("BackupWorker: $mJob")
            value?.invoke(mJob != null)
        }

    fun backup(context: Context, ioHelper: IoHelper) {
        mJob?.cancel()
        launchSync(context, ioHelper)
    }

    private fun launchSync(context: Context, ioHelper: IoHelper) {
        mJob = launchIo {
            notifyMsg(context.getString(R.string.syncing_groups))
            ioHelper.backupGroup()

            notifyMsg(context.getString(R.string.syncing_reminders))
            ioHelper.backupReminder()

            notifyMsg(context.getString(R.string.syncing_notes))
            ioHelper.backupNote()

            notifyMsg(context.getString(R.string.syncing_birthdays))
            ioHelper.backupBirthday()

            notifyMsg(context.getString(R.string.syncing_places))
            ioHelper.backupPlaces()

            notifyMsg(context.getString(R.string.syncing_templates))
            ioHelper.backupTemplates()
            ioHelper.backupSettings()

            withUIContext {
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
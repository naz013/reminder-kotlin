package com.elementary.tasks.core.work

import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.withUIContext
import kotlinx.coroutines.Job
import timber.log.Timber
import java.io.File

object ExportAllDataWorker {

    private var mJob: Job? = null
    var onEnd: ((File?) -> Unit)? = null
    var listener: ((Boolean) -> Unit)? = null
        set(value) {
            field = value
            Timber.d("ExportAllDataWorker: $mJob")
            value?.invoke(mJob != null)
        }

    fun export(backupTool: BackupTool) {
        mJob?.cancel()
        launchSync(backupTool)
    }

    fun unsubscribe() {
        onEnd = null
        listener = null
    }

    private fun launchSync(backupTool: BackupTool) {
        mJob = launchIo {
            val file = backupTool.exportAll()
            withUIContext {
                onEnd?.invoke(file)
            }
            mJob = null
        }
    }
}
package com.elementary.tasks.core.work

import android.content.Context
import com.elementary.tasks.core.utils.IoHelper
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
            Timber.d("BackupWorker: $mJob")
            value?.invoke(mJob != null)
        }

    fun export(context: Context, ioHelper: IoHelper) {
        mJob?.cancel()
        launchSync(context, ioHelper)
    }

    private fun launchSync(context: Context, ioHelper: IoHelper) {
        mJob = launchIo {
            val file = ioHelper.exportAllToFile()
            withUIContext {
                onEnd?.invoke(file)
            }
            mJob = null
        }
    }
}
package com.elementary.tasks.core.work

import android.content.Context
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.withUIContext
import kotlinx.coroutines.Job
import timber.log.Timber
import java.io.File

class ExportAllDataWorker(
  private val context: Context,
  private val backupTool: BackupTool
) {

  private var mJob: Job? = null
  var onEnd: ((File?) -> Unit)? = null
  var listener: ((Boolean) -> Unit)? = null
    set(value) {
      field = value
      Timber.d("ExportAllDataWorker: $mJob")
      value?.invoke(mJob != null)
    }

  fun export() {
    mJob?.cancel()
    launchSync()
  }

  fun unsubscribe() {
    onEnd = null
    listener = null
  }

  private fun launchSync() {
    mJob = launchIo {
      val file = backupTool.exportAll(context)
      withUIContext {
        onEnd?.invoke(file)
      }
      mJob = null
    }
  }
}
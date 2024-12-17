package com.elementary.tasks.core.work

import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.withUIContext
import com.github.naz013.logging.Logger
import kotlinx.coroutines.Job
import java.io.File

class ExportAllDataWorker(private val backupTool: BackupTool) {

  private var mJob: Job? = null
  var onEnd: ((File?) -> Unit)? = null
  var listener: ((Boolean) -> Unit)? = null
    set(value) {
      field = value
      Logger.d("ExportAllDataWorker: $mJob")
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
      val file = backupTool.exportAll()
      withUIContext {
        onEnd?.invoke(file)
      }
      mJob = null
    }
  }
}

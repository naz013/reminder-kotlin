package com.elementary.tasks.core.utils.work

import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import com.elementary.tasks.core.utils.params.Prefs

class WorkerLauncher(
  private val prefs: Prefs,
  private val workManagerProvider: WorkManagerProvider
) {

  fun startWork(clazz: Class<out ListenableWorker>, key: String, valueTag: String) {
    startWork(clazz, Data.Builder().putString(key, valueTag).build(), valueTag)
  }

  fun startWork(clazz: Class<out ListenableWorker>, data: Data, tag: String) {
    if (prefs.isBackupEnabled) {
      val work = OneTimeWorkRequest.Builder(clazz)
        .setInputData(data)
        .addTag(tag)
        .build()
      workManagerProvider.getWorkManager().enqueue(work)
    }
  }
}
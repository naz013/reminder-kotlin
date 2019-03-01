package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.IoHelper
import com.elementary.tasks.core.utils.Prefs
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class BackupSettingsWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), KoinComponent {

    private val prefs: Prefs by inject()
    private val backupTool: BackupTool by inject()

    override fun doWork(): Result {
        IoHelper(applicationContext, prefs, backupTool).backupSettings()
        return Result.SUCCESS
    }

    companion object {
        private const val TAG = "BackupSettingsWorker"

        fun schedule() {
            val work = OneTimeWorkRequest.Builder(BackupSettingsWorker::class.java)
                    .addTag(BackupSettingsWorker.TAG)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }
}
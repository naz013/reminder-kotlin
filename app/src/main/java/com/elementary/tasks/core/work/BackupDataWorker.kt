package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.*
import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.utils.launchIo
import org.koin.core.KoinComponent

class BackupDataWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), KoinComponent {

    override fun doWork(): Result {
        launchIo {
            BulkDataFlow.fullBackup(applicationContext)
        }
        return Result.success()
    }

    companion object {
        private const val TAG = "BackupDataWorker"

        fun schedule(context: Context) {
            val work = OneTimeWorkRequest.Builder(BackupDataWorker::class.java)
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
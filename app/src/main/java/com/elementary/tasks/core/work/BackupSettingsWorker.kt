package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.*
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.converters.SettingsConverter
import com.elementary.tasks.core.cloud.repositories.SettingsRepository
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.launchIo

class BackupSettingsWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        launchIo {
            DataFlow(SettingsRepository(), SettingsConverter(),
                    CompositeStorage(DataFlow.availableStorageList(applicationContext)), null)
                    .backup("")
        }
        return Result.success()
    }

    companion object {
        private const val TAG = "BackupSettingsWorker"

        fun schedule() {
            val work = OneTimeWorkRequest.Builder(BackupSettingsWorker::class.java)
                    .addTag(TAG)
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.UNMETERED)
                            .setRequiresBatteryNotLow(true)
                            .build())
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }
}
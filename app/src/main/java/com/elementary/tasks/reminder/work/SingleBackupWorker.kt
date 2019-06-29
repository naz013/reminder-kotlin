package com.elementary.tasks.reminder.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.converters.ReminderConverter
import com.elementary.tasks.core.cloud.repositories.ReminderRepository
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault

class SingleBackupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val uuId = inputData.getString(Constants.INTENT_ID) ?: ""
        if (uuId.isNotEmpty()) {
            launchDefault {
                DataFlow(ReminderRepository(), ReminderConverter(),
                        CompositeStorage(DataFlow.availableStorageList(applicationContext)), null)
                        .backup(uuId)
            }
        }
        return Result.success()
    }
}
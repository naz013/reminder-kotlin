package com.elementary.tasks.navigation.settings.additional.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.converters.TemplateConverter
import com.elementary.tasks.core.cloud.repositories.TemplateRepository
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault

class DeleteBackupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val uuId = inputData.getString(Constants.INTENT_ID) ?: ""
        if (uuId.isNotEmpty()) {
            launchDefault {
                DataFlow(TemplateRepository(), TemplateConverter(),
                        CompositeStorage(DataFlow.availableStorageList(applicationContext)), null)
                        .delete(uuId, IndexTypes.TYPE_TEMPLATE, true)
            }
        }
        return Result.success()
    }
}
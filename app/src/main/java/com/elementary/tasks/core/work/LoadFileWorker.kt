package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.*
import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.utils.launchIo
import org.koin.core.KoinComponent

class LoadFileWorker(context: Context, val workerParams: WorkerParameters) : Worker(context, workerParams), KoinComponent {

    override fun doWork(): Result {
        val fileName = workerParams.inputData.getString(ARG_FILE_NAME) ?: ""
        if (fileName.isNotEmpty()) {
            launchIo {
                BulkDataFlow.fullBackup(applicationContext)
            }
        }
        return Result.success()
    }



    companion object {
        private const val TAG = "LoadFileWorker"
        private const val ARG_FILE_NAME = "arg_file_name"

        fun schedule(fileName: String) {
            val work = OneTimeWorkRequest.Builder(LoadFileWorker::class.java)
                    .addTag(TAG)
                    .setInputData(Data.Builder()
                            .putString(ARG_FILE_NAME, fileName)
                            .build())
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.UNMETERED)
                            .setRequiresBatteryNotLow(true)
                            .build())
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }
}
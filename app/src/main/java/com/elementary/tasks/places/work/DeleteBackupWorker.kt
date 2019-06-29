package com.elementary.tasks.places.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.storages.Dropbox
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.storages.GDrive
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.MemoryUtil
import java.io.File
import java.io.IOException

class DeleteBackupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val uuId = inputData.getString(Constants.INTENT_ID) ?: ""
        if (uuId.isNotEmpty()) {
            deleteSingleFile(uuId + FileConfig.FILE_NAME_PLACE)
        }
        return Result.success()
    }

    private fun deleteSingleFile(fileName: String) {
        deleteCacheFiles(fileName)
        Dropbox().deletePlace(fileName)
        try {
            GDrive.getInstance(applicationContext)?.deletePlaceFileByName(fileName)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun deleteCacheFiles(fileName: String) {
        var dir = MemoryUtil.placesDir
        if (dir != null) {
            val file = File(dir, fileName)
            if (file.exists()) file.delete()
        }
        dir = MemoryUtil.dropboxPlacesDir
        if (dir != null) {
            val file = File(dir, fileName)
            if (file.exists()) file.delete()
        }
        dir = MemoryUtil.googlePlacesDir
        if (dir != null) {
            val file = File(dir, fileName)
            if (file.exists()) file.delete()
        }
    }
}
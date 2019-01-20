package com.elementary.tasks.places.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.GDrive
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.SuperUtil
import java.io.File
import java.io.IOException

class DeleteBackupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val uuId = inputData.getString(Constants.INTENT_ID) ?: ""
        if (uuId.isNotEmpty()) {
            deleteSingleFile(uuId + FileConfig.FILE_NAME_PLACE)
        } else {
            val ids = inputData.getStringArray(Constants.INTENT_IDS) ?: return Result.SUCCESS
            ids.forEach { deleteSingleFile(it + FileConfig.FILE_NAME_PLACE) }
        }
        return Result.SUCCESS
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
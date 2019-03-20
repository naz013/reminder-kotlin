package com.elementary.tasks.reminder.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.GDrive
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.launchIo
import java.io.File
import java.io.IOException

class DeleteBackupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val uuId = inputData.getString(Constants.INTENT_ID) ?: ""
        if (uuId.isNotEmpty()) {
            launchIo {
                deleteSingleFile(uuId + FileConfig.FILE_NAME_REMINDER)
            }
        }
        return Result.success()
    }

    private fun deleteSingleFile(fileName: String) {
        deleteCacheFiles(fileName)
        Dropbox().deleteReminder(fileName)
        try {
            GDrive.getInstance(applicationContext)?.deleteReminderFileByName(fileName)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun deleteCacheFiles(fileName: String) {
        var dir = MemoryUtil.remindersDir
        if (dir != null) {
            val file = File(dir, fileName)
            if (file.exists()) file.delete()
        }
        dir = MemoryUtil.dropboxRemindersDir
        if (dir != null) {
            val file = File(dir, fileName)
            if (file.exists()) file.delete()
        }
        dir = MemoryUtil.googleRemindersDir
        if (dir != null) {
            val file = File(dir, fileName)
            if (file.exists()) file.delete()
        }
    }
}
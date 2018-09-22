package com.elementary.tasks.reminder.work

import androidx.work.Worker
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.SuperUtil
import java.io.File
import java.io.IOException

class DeleteBackupWorker : Worker() {

    override fun doWork(): Result {
        val uuId = inputData.getString(Constants.INTENT_ID) ?: ""
        if (uuId.isNotEmpty()) {
            deleteSingleFile(uuId + FileConfig.FILE_NAME_REMINDER)
        } else {
            val ids = inputData.getStringArray(Constants.INTENT_IDS) ?: return Result.SUCCESS
            ids.forEach { deleteSingleFile(it + FileConfig.FILE_NAME_REMINDER) }
        }
        return Result.SUCCESS
    }

    private fun deleteSingleFile(fileName: String) {
        deleteCacheFiles(fileName)
        val isConnected = SuperUtil.isConnected(applicationContext)
        if (isConnected) {
            Dropbox().deleteReminder(fileName)
            try {
                Google.getInstance()?.drive?.deleteReminderFileByName(fileName)
            } catch (e: IOException) {
                e.printStackTrace()
            }
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
package com.elementary.tasks.notes.work

import androidx.work.Worker
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.SuperUtil
import java.io.File
import java.io.IOException

class DeleteNoteBackupWorker : Worker() {

    override fun doWork(): Result {
        val uuId = inputData.getString(Constants.INTENT_ID) ?: ""
        if (uuId.isNotEmpty()) {
            deleteSingleFile(uuId + FileConfig.FILE_NAME_NOTE)
        } else {
            val ids = inputData.getStringArray(Constants.INTENT_IDS) ?: return Result.SUCCESS
            ids.forEach { deleteSingleFile(it + FileConfig.FILE_NAME_NOTE) }
        }
        return Result.SUCCESS
    }

    private fun deleteSingleFile(fileName: String) {
        deleteCacheFiles(fileName)
        val isConnected = SuperUtil.isConnected(applicationContext)
        if (isConnected) {
            Dropbox().deleteNote(fileName)
            try {
                Google.getInstance()?.drive?.deleteNoteFileByName(fileName)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun deleteCacheFiles(fileName: String) {
        var dir = MemoryUtil.notesDir
        if (dir != null) {
            val file = File(dir, fileName)
            if (file.exists()) file.delete()
        }
        dir = MemoryUtil.dropboxNotesDir
        if (dir != null) {
            val file = File(dir, fileName)
            if (file.exists()) file.delete()
        }
        dir = MemoryUtil.googleNotesDir
        if (dir != null) {
            val file = File(dir, fileName)
            if (file.exists()) file.delete()
        }
    }
}
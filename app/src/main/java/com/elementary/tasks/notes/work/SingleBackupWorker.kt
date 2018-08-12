package com.elementary.tasks.notes.work

import androidx.work.Worker
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.SuperUtil
import com.google.gson.Gson
import java.io.File
import java.io.IOException

class SingleBackupWorker : Worker() {

    override fun doWork(): Result {
        val uuId = inputData.getString(Constants.INTENT_ID) ?: ""
        if (uuId.isNotEmpty()) {
            val db = AppDb.getAppDatabase(applicationContext)
            val note = db.notesDao().getById(uuId)
            if (note != null) {
                cacheFiles(uuId + FileConfig.FILE_NAME_NOTE, Gson().toJson(note))
            }
        }
        return Result.SUCCESS
    }

    private fun cacheFiles(fileName: String, data: String) {
        val dir = MemoryUtil.notesDir
        if (dir != null) {
            try {
                MemoryUtil.writeFile(File(dir, fileName), data)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val isConnected = SuperUtil.isConnected(applicationContext)
        if (isConnected) {
            Dropbox().uploadNoteByFileName(fileName)
            try {
                Google.getInstance()?.drive?.saveNoteToDrive(File(dir, fileName).toString())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
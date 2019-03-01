package com.elementary.tasks.notes.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.GDrive
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.OldNote
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.MemoryUtil
import com.google.gson.Gson
import java.io.File
import java.io.IOException

class SingleBackupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val uuId = inputData.getString(Constants.INTENT_ID) ?: ""
        if (uuId.isNotEmpty()) {
            val db = AppDb.getAppDatabase(applicationContext)
            val note = db.notesDao().getById(uuId)
            if (note != null) {
                cacheFiles(uuId + FileConfig.FILE_NAME_NOTE, Gson().toJson(OldNote(note)))
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
        Dropbox().uploadNoteByFileName(fileName)
        try {
            GDrive.getInstance(applicationContext)?.saveNoteToDrive(File(dir, fileName).toString())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
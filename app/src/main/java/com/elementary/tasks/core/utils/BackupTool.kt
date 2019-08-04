package com.elementary.tasks.core.utils

import android.content.Context
import androidx.annotation.Keep
import com.elementary.tasks.core.arch.isValid
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.*
import com.elementary.tasks.core.utils.MemoryUtil.writeFileNoEncryption
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

class BackupTool(private val appDb: AppDb) {

    fun exportAll(context: Context): File? {
        val allData = AllData(
                reminders = appDb.reminderDao().all(),
                groups = appDb.reminderGroupDao().all(),
                notes = appDb.notesDao().all().map { OldNote(it) },
                places = appDb.placesDao().all(),
                templates = appDb.smsTemplatesDao().all(),
                birthdays = appDb.birthdaysDao().all()
        )
        return createAllDataFile(context, allData)
    }

    private fun createAllDataFile(context: Context, item: AllData): File? {
        val jsonData = WeakReference(Gson().toJson(item))
        val file: File
        val dir = context.externalCacheDir ?: context.cacheDir
        return if (dir != null) {
            val exportFileName = TimeUtil.gmtDateTime + FileConfig.FILE_NAME_FULL_BACKUP
            file = File(dir, exportFileName)
            try {
                writeFileNoEncryption(file, jsonData.get())
                jsonData.clear()
                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    fun reminderToFile(context: Context, item: Reminder): File? {
        return anyToFile(context, item, item.uuId + FileConfig.FILE_NAME_REMINDER)
    }

    fun noteToFile(context: Context, item: NoteWithImages?): File? {
        val note = item?.note ?: return null
        return anyToFile(context, item, note.key + FileConfig.FILE_NAME_NOTE)
    }

    fun placeToFile(context: Context, item: Place): File? {
        return anyToFile(context, item, item.id + FileConfig.FILE_NAME_PLACE)
    }

    fun templateToFile(context: Context, item: SmsTemplate): File? {
        return anyToFile(context, item, item.key + FileConfig.FILE_NAME_TEMPLATE)
    }

    fun birthdayToFile(context: Context, item: Birthday): File? {
        return anyToFile(context, item, item.uuId + FileConfig.FILE_NAME_BIRTHDAY)
    }

    fun groupToFile(context: Context, item: ReminderGroup): File? {
        return anyToFile(context, item, item.groupUuId + FileConfig.FILE_NAME_GROUP)
    }

    fun anyToFile(context: Context, any: Any, fileName: String): File? {
        val cacheDir = context.getExternalFilesDir("share") ?: context.filesDir
        val file = File(cacheDir, fileName)
        if (!file.createNewFile()) {
            try {
                file.delete()
                file.createNewFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return try {
            val outputStream = FileOutputStream(file)
            return if (MemoryUtil.toStream(any, outputStream)) {
                outputStream.flush()
                outputStream.close()
                file
            } else {
                outputStream.flush()
                outputStream.close()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        fun oldNoteToNew(oldNote: OldNote): NoteWithImages? {
            val noteWithImages = NoteWithImages()
            oldNote.images.forEach {
                it.noteId = oldNote.key
            }
            noteWithImages.note = Note(oldNote)
            noteWithImages.images = oldNote.images
            return if (noteWithImages.isValid()) {
                noteWithImages
            } else {
                null
            }
        }
    }

    @Keep
    data class AllData(val reminders: List<Reminder>,
                       val groups: List<ReminderGroup>,
                       val notes: List<OldNote>,
                       val places: List<Place>,
                       val templates: List<SmsTemplate>,
                       val birthdays: List<Birthday>)
}

package com.elementary.tasks.core.utils

import android.content.ContentResolver
import android.net.Uri
import androidx.annotation.Keep
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.converters.PlaceConverter
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.*
import com.elementary.tasks.core.utils.MemoryUtil.readFileToJson
import com.elementary.tasks.core.utils.MemoryUtil.writeFile
import com.elementary.tasks.core.utils.MemoryUtil.writeFileNoEncryption
import com.google.gson.Gson
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference

class BackupTool(private val appDb: AppDb) {

    fun exportAll(): File? {
        val allData = AllData(
                reminders = appDb.reminderDao().all(),
                groups = appDb.reminderGroupDao().all(),
                notes = appDb.notesDao().all().map { OldNote(it) },
                places = appDb.placesDao().all(),
                templates = appDb.smsTemplatesDao().all(),
                birthdays = appDb.birthdaysDao().all()
        )
        return createAllDataFile(allData)
    }

    private fun createAllDataFile(item: AllData): File? {
        val jsonData = WeakReference(Gson().toJson(item))
        val file: File
        val dir = MemoryUtil.mailDir
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

    fun getTemplate(cr: ContentResolver, name: Uri): SmsTemplate? {
        return try {
            val item = WeakReference(Gson().fromJson(readFileToJson(cr, name), SmsTemplate::class.java))
            item.get()
        } catch (e: java.lang.Exception) {
            null
        }
    }

    fun getTemplate(filePath: String?, json: String?): SmsTemplate? {
        return try {
            return if (filePath != null && MemoryUtil.isSdPresent) {
                val item = WeakReference(Gson().fromJson(readFileToJson(filePath), SmsTemplate::class.java))
                item.get()
            } else if (json != null) {
                val item = WeakReference(Gson().fromJson(json, SmsTemplate::class.java))
                item.get()
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            null
        }
    }

    fun getPlace(cr: ContentResolver, name: Uri): Place? {
        return try {
            val data = MemoryUtil.readFileContent(cr, name) ?: return null
            return PlaceConverter().convert(data)
        } catch (e: Exception) {
            null
        }
    }

    fun getPlace(filePath: String?, json: String?): Place? {
        return try {
            return if (filePath != null && MemoryUtil.isSdPresent) {
                val item = WeakReference(Gson().fromJson(readFileToJson(filePath), Place::class.java))
                item.get()
            } else if (json != null) {
                val item = WeakReference(Gson().fromJson(json, Place::class.java))
                item.get()
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            null
        }
    }

    fun getBirthday(cr: ContentResolver, name: Uri): Birthday? {
        return try {
            val item = WeakReference(Gson().fromJson(readFileToJson(cr, name), Birthday::class.java))
            return item.get()
        } catch (e: java.lang.Exception) {
            null
        }
    }

    fun getBirthday(filePath: String?, json: String?): Birthday? {
        return try {
            return if (filePath != null && MemoryUtil.isSdPresent) {
                val item = WeakReference(Gson().fromJson(readFileToJson(filePath), Birthday::class.java))
                item.get()
            } else if (json != null) {
                val item = WeakReference(Gson().fromJson(json, Birthday::class.java))
                item.get()
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            null
        }
    }

    fun getGroup(cr: ContentResolver, name: Uri): ReminderGroup? {
        return try {
            val item = WeakReference(Gson().fromJson(readFileToJson(cr, name), ReminderGroup::class.java))
            return item.get()
        } catch (e: java.lang.Exception) {
            null
        }
    }

    fun getGroup(filePath: String?, json: String?): ReminderGroup? {
        return try {
            return if (filePath != null && MemoryUtil.isSdPresent) {
                val item = WeakReference(Gson().fromJson(readFileToJson(filePath), ReminderGroup::class.java))
                item.get()
            } else if (json != null) {
                val item = WeakReference(Gson().fromJson(json, ReminderGroup::class.java))
                item.get()
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            null
        }
    }

    fun exportReminder(item: Reminder): String? {
        val jsonData = WeakReference(Gson().toJson(item))
        val dir = MemoryUtil.remindersDir
        if (dir != null) {
            val exportFileName = item.uuId + FileConfig.FILE_NAME_REMINDER
            try {
                return writeFile(File(dir, exportFileName), jsonData.get())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Timber.d("Couldn't find external storage!")
        }
        return null
    }

    fun getReminder(cr: ContentResolver, name: Uri): Reminder? {
        return try {
            return Gson().fromJson(readFileToJson(cr, name), Reminder::class.java)
        } catch (e: java.lang.Exception) {
            null
        }
    }

    fun getReminder(filePath: String?, json: String?): Reminder? {
        return try {
            return if (filePath != null && MemoryUtil.isSdPresent) {
                val item = WeakReference(Gson().fromJson(readFileToJson(filePath), Reminder::class.java))
                item.get()
            } else if (json != null) {
                val item = WeakReference(Gson().fromJson(json, Reminder::class.java))
                item.get()
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            null
        }
    }

    fun getNote(cr: ContentResolver, uri: Uri): NoteWithImages? {
        Timber.d("getNote: $uri")
        try {
            val weakNote = WeakReference(Gson().fromJson(readFileToJson(cr, uri), OldNote::class.java))
            val oldNote = weakNote.get() ?: return null
            val noteWithImages = NoteWithImages()
            oldNote.images.forEach {
                it.noteId = oldNote.key
            }
            noteWithImages.note = Note(oldNote)
            noteWithImages.images = oldNote.images
            return noteWithImages
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getNote(filePath: String?, json: String?): NoteWithImages? {
        Timber.d("getNote: $filePath, $json")
        return try {
            if (filePath != null && MemoryUtil.isSdPresent) {
                val oldNote = Gson().fromJson(readFileToJson(filePath), OldNote::class.java)
                        ?: return null
                val noteWithImages = NoteWithImages()
                oldNote.images.forEach {
                    it.noteId = oldNote.key
                }
                noteWithImages.note = Note(oldNote)
                noteWithImages.images = oldNote.images
                return noteWithImages
            } else if (json != null) {
                val weakNote = WeakReference(Gson().fromJson(json, OldNote::class.java))
                val oldNote = weakNote.get() ?: return null
                val noteWithImages = NoteWithImages()
                oldNote.images.forEach {
                    it.noteId = oldNote.key
                }
                noteWithImages.note = Note(oldNote)
                noteWithImages.images = oldNote.images
                return noteWithImages
            } else {
                return null
            }
        } catch (e: java.lang.Exception) {
            null
        }
    }

    fun createNote(item: NoteWithImages?): File? {
        val note = item?.note ?: return null
        val jsonData = WeakReference(Gson().toJson(OldNote(item)))
        val file: File
        val dir = MemoryUtil.mailDir
        return if (dir != null) {
            val exportFileName = note.key + FileConfig.FILE_NAME_NOTE
            file = File(dir, exportFileName)
            try {
                writeFile(file, jsonData.get())
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

    @Keep
    data class AllData(val reminders: List<Reminder>,
                       val groups: List<ReminderGroup>,
                       val notes: List<OldNote>,
                       val places: List<Place>,
                       val templates: List<SmsTemplate>,
                       val birthdays: List<Birthday>)
}

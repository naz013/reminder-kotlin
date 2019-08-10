package com.elementary.tasks.core.utils

import android.content.Context
import android.net.Uri
import androidx.annotation.Keep
import com.elementary.tasks.core.arch.isValid
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.completables.ReminderCompletable
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.*
import com.elementary.tasks.core.utils.MemoryUtil.writeFileNoEncryption
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.lang.ref.WeakReference

class BackupTool(private val appDb: AppDb) {

    fun importAll(context: Context, uri: Uri?, replace: Boolean = false, callback: (Boolean) -> Unit) {
        if (uri == null) {
            callback.invoke(false)
            return
        }
        val stream = context.contentResolver.openInputStream(uri)
        if (stream == null) {
            callback.invoke(false)
            return
        }
        launchIo {
            try {
                val allData = Gson().fromJson(InputStreamReader(stream), AllData::class.java)
                if (allData != null) {
                    var hasAnyData = false
                    val defGroup = if (allData.groups.isNotEmpty()) {
                        Timber.d("importAll: has groups ${allData.groups.size}")
                        hasAnyData = true
                        allData.groups.map { it.isDefaultGroup = false }
                        if (replace) {
                            appDb.reminderGroupDao().deleteAll()
                            allData.groups[0].isDefaultGroup = true
                        }
                        allData.groups.forEach { appDb.reminderGroupDao().insert(it) }
                        appDb.reminderGroupDao().defaultGroup()
                    } else {
                        appDb.reminderGroupDao().defaultGroup()
                    }

                    if (allData.reminders.isNotEmpty()) {
                        Timber.d("importAll: has reminders ${allData.reminders.size}")
                        hasAnyData = true
                        val allGroups = appDb.reminderGroupDao().all()
                        val completable = ReminderCompletable()
                        if (replace) {
                            appDb.reminderDao().deleteAll()
                        }
                        val dao = appDb.reminderDao()
                        allData.reminders.forEach {
                            if (!hasGroup(it.groupUuId, allGroups) && defGroup != null) {
                                it.groupUuId = defGroup.groupUuId
                                it.groupColor = defGroup.groupColor
                                it.groupTitle = defGroup.groupTitle
                            }
                            dao.insert(it)
                            completable.action(it)
                        }
                    }

                    if (allData.birthdays.isNotEmpty()) {
                        Timber.d("importAll: has birthdays ${allData.birthdays.size}")
                        hasAnyData = true
                        if (replace) {
                            appDb.birthdaysDao().deleteAll()
                        }
                        allData.birthdays.forEach { appDb.birthdaysDao().insert(it) }
                    }

                    if (allData.places.isNotEmpty()) {
                        Timber.d("importAll: has places ${allData.places.size}")
                        hasAnyData = true
                        if (replace) {
                            appDb.placesDao().deleteAll()
                        }
                        allData.places.forEach { appDb.placesDao().insert(it) }
                    }

                    if (allData.templates.isNotEmpty()) {
                        Timber.d("importAll: has templates ${allData.templates.size}")
                        hasAnyData = true
                        if (replace) {
                            appDb.smsTemplatesDao().deleteAll()
                        }
                        allData.templates.forEach { appDb.smsTemplatesDao().insert(it) }
                    }

                    if (allData.notes.isNotEmpty()) {
                        Timber.d("importAll: has notes ${allData.notes.size}")
                        hasAnyData = true
                        if (replace) {
                            appDb.notesDao().deleteAllImages()
                            appDb.notesDao().deleteAllNotes()
                        }
                        allData.notes.forEach {
                            it.images.forEach { image ->
                                image.noteId = it.key
                                appDb.notesDao().insert(image)
                            }
                            appDb.notesDao().insert(Note(it))
                        }
                    }
                    withUIContext { callback.invoke(hasAnyData) }
                } else {
                    withUIContext { callback.invoke(false) }
                }
            } catch (e: Exception) {
                withUIContext { callback.invoke(false) }
            }
        }
    }

    private fun hasGroup(uuId: String, list: List<ReminderGroup>): Boolean {
        for (g in list) {
            if (uuId == g.groupUuId) {
                return true
            }
        }
        return false
    }

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
    data class AllData(
            @SerializedName("reminders")
            var reminders: List<Reminder> = listOf(),
            @SerializedName("groups")
            var groups: List<ReminderGroup> = listOf(),
            @SerializedName("notes")
            var notes: List<OldNote> = listOf(),
            @SerializedName("places")
            var places: List<Place> = listOf(),
            @SerializedName("templates")
            var templates: List<SmsTemplate> = listOf(),
            @SerializedName("birthdays")
            var birthdays: List<Birthday> = listOf()
    )
}

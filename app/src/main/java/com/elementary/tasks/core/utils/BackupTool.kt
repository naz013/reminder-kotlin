package com.elementary.tasks.core.utils

import android.content.ContentResolver
import android.net.Uri
import android.text.TextUtils
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.*
import com.elementary.tasks.core.utils.MemoryUtil.readFileToJson
import com.elementary.tasks.core.utils.MemoryUtil.writeFile
import com.elementary.tasks.core.utils.MemoryUtil.writeFileNoEncryption
import com.elementary.tasks.groups.GroupsUtil
import com.google.gson.Gson
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Singleton
class BackupTool @Inject constructor(private val appDb: AppDb) {

    init {
        ReminderApp.appComponent.inject(this)
    }

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

    fun exportTemplates() {
        for (item in appDb.smsTemplatesDao().all()) {
            exportTemplate(item)
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun importTemplates() {
        val dir = MemoryUtil.templatesDir
        if (dir != null && dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_TEMPLATE)) {
                        val item = getTemplate(file.toString(), null)
                        if (item == null || TextUtils.isEmpty(item.title)
                                || TextUtils.isEmpty(item.key)) {
                            continue
                        }
                        appDb.smsTemplatesDao().insert(item)
                    }
                }
            }
        }
    }

    private fun exportTemplate(item: SmsTemplate) {
        val jsonData = WeakReference(Gson().toJson(item))
        val dir = MemoryUtil.templatesDir
        if (dir != null) {
            val exportFileName = item.key + FileConfig.FILE_NAME_TEMPLATE
            try {
                writeFile(File(dir, exportFileName), jsonData.get())
                jsonData.clear()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Timber.d("Couldn't find external storage!")
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

    fun exportPlaces() {
        for (item in appDb.placesDao().all()) {
            exportPlace(item)
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun importPlaces() {
        val dir = MemoryUtil.placesDir
        if (dir != null && dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_PLACE)) {
                        val item = getPlace(file.toString(), null)
                        if (item == null || TextUtils.isEmpty(item.name) ||
                                TextUtils.isEmpty(item.id)) {
                            continue
                        }
                        appDb.placesDao().insert(item)
                    }
                }
            }
        }
    }

    private fun exportPlace(item: Place) {
        val jsonData = WeakReference(Gson().toJson(item))
        val dir = MemoryUtil.placesDir
        if (dir != null) {
            val exportFileName = item.id + FileConfig.FILE_NAME_PLACE
            try {
                writeFile(File(dir, exportFileName), jsonData.get())
                jsonData.clear()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Timber.d("Couldn't find external storage!")
        }
    }

    fun getPlace(cr: ContentResolver, name: Uri): Place? {
        return try {
            val item = WeakReference(Gson().fromJson(readFileToJson(cr, name), Place::class.java))
            return item.get()
        } catch (e: java.lang.Exception) {
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

    fun exportBirthdays() {
        for (item in appDb.birthdaysDao().all()) {
            exportBirthday(item)
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun importBirthdays() {
        val dir = MemoryUtil.birthdaysDir
        if (dir != null && dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
                        val item = getBirthday(file.toString(), null)
                        if (item == null || TextUtils.isEmpty(item.name)
                                || TextUtils.isEmpty(item.uuId)) {
                            continue
                        }
                        appDb.birthdaysDao().insert(item)
                    }
                }
            }
        }
    }

    private fun exportBirthday(item: Birthday) {
        val jsonData = WeakReference(Gson().toJson(item))
        val dir = MemoryUtil.birthdaysDir
        if (dir != null) {
            val exportFileName = item.uuId + FileConfig.FILE_NAME_BIRTHDAY
            try {
                writeFile(File(dir, exportFileName), jsonData.get())
                jsonData.clear()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Timber.d("Couldn't find external storage!")
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

    fun exportGroups() {
        for (item in appDb.reminderGroupDao().all()) {
            exportGroup(item)
        }
    }

    private fun exportGroup(item: ReminderGroup) {
        val jsonData = WeakReference(Gson().toJson(item))
        val dir = MemoryUtil.groupsDir
        if (dir != null) {
            val exportFileName = item.groupUuId + FileConfig.FILE_NAME_GROUP
            val file = File(dir, exportFileName)
            try {
                writeFile(file, jsonData.get())
                jsonData.clear()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Timber.d("Couldn't find external storage!")
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun importGroups() {
        val dir = MemoryUtil.groupsDir
        if (dir != null && dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                val groups = appDb.reminderGroupDao().all().toMutableList()
                for (file in files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_GROUP)) {
                        val item = getGroup(file.toString(), null)
                        if (item == null || TextUtils.isEmpty(item.groupUuId)) continue
                        if (!TextUtils.isEmpty(item.groupTitle) && !hasGroup(groups, item.groupTitle)) {
                            appDb.reminderGroupDao().insert(item)
                            groups.add(item)
                        }
                    }
                }
            }
        }
    }

    private fun hasGroup(list: List<ReminderGroup>, comparable: String?): Boolean {
        if (comparable == null) return true
        for (item in list) {
            if (comparable == item.groupTitle) {
                return true
            }
        }
        return false
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

    fun exportReminders() {
        for (reminder in appDb.reminderDao().all()) {
            exportReminder(reminder)
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun importReminders() {
        val dir = MemoryUtil.remindersDir
        if (dir != null && dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                val groups = GroupsUtil.mapAll(appDb)
                val defGroup = appDb.reminderGroupDao().defaultGroup() ?: groups.values.first()
                for (file in files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_REMINDER)) {
                        val reminder = getReminder(file.toString(), null) ?: continue
                        if (TextUtils.isEmpty(reminder.uuId)) {
                            file.delete()
                            continue
                        }
                        if (!groups.containsKey(reminder.groupUuId)) {
                            reminder.apply {
                                this.groupTitle = defGroup.groupTitle
                                this.groupUuId = defGroup.groupUuId
                                this.groupColor = defGroup.groupColor
                            }
                        }
                        if (!Reminder.isGpsType(reminder.type) && !TimeCount.isCurrent(reminder.eventTime)) {
                            if (!Reminder.isSame(reminder.type, Reminder.BY_DATE_SHOP) || reminder.hasReminder) {
                                reminder.isRemoved = true
                                reminder.isActive = false
                            }
                        }
                        appDb.reminderDao().insert(reminder)
                        if (reminder.isActive && !reminder.isRemoved) {
                            val control = EventControlFactory.getController(reminder)
                            if (control.canSkip()) {
                                control.next()
                            } else {
                                control.start()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Export reminder object to file.
     *
     * @param item reminder object
     * @return Path to file
     */
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

    fun exportNotes() {
        for (item in appDb.notesDao().all()) {
            exportNote(item)
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun importNotes() {
        val dir = MemoryUtil.notesDir
        if (dir != null && dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_NOTE)) {
                        val item = getNote(file.toString(), null) ?: continue
                        val note = item.note ?: continue
                        if (TextUtils.isEmpty(note.key)) {
                            continue
                        }

                        appDb.notesDao().insertAll(item.images)
                        appDb.notesDao().insert(note)
                    }
                }
            }
        }
    }

    private fun exportNote(item: NoteWithImages) {
        val note = item.note ?: return
        val jsonData = WeakReference(Gson().toJson(OldNote(item)))
        val dir = MemoryUtil.notesDir
        if (dir != null) {
            val exportFileName = note.key + FileConfig.FILE_NAME_NOTE
            try {
                writeFile(File(dir, exportFileName), jsonData.get())
                jsonData.clear()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Timber.d("Couldn't find external storage!")
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

    data class AllData(val reminders: List<Reminder>,
                       val groups: List<ReminderGroup>,
                       val notes: List<OldNote>,
                       val places: List<Place>,
                       val templates: List<SmsTemplate>,
                       val birthdays: List<Birthday>)
}

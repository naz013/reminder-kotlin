package com.elementary.tasks.core.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.*
import com.google.gson.Gson
import java.io.*
import java.lang.ref.WeakReference
import javax.inject.Inject

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

class BackupTool private constructor() {
    @Inject
    lateinit var mContext: Context

    init {
        ReminderApp.appComponent.inject(this)
    }

    fun exportTemplates() {
        for (item in AppDb.getAppDatabase(mContext).smsTemplatesDao().all) {
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
                        AppDb.getAppDatabase(mContext).smsTemplatesDao().insert(item)
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
            LogUtil.i(TAG, "Couldn't find external storage!")
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun getTemplate(cr: ContentResolver, name: Uri): SmsTemplate? {
        val item = WeakReference(Gson().fromJson(readFileToJson(cr, name), SmsTemplate::class.java))
        return item.get()
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun getTemplate(filePath: String?, json: String?): SmsTemplate? {
        return if (filePath != null && MemoryUtil.isSdPresent) {
            val item = WeakReference(Gson().fromJson(readFileToJson(filePath), SmsTemplate::class.java))
            item.get()
        } else if (json != null) {
            val item = WeakReference(Gson().fromJson(json, SmsTemplate::class.java))
            item.get()
        } else {
            null
        }
    }

    fun exportPlaces() {
        for (item in AppDb.getAppDatabase(mContext).placesDao().all) {
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
                        AppDb.getAppDatabase(mContext).placesDao().insert(item)
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
            LogUtil.i(TAG, "Couldn't find external storage!")
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun getPlace(cr: ContentResolver, name: Uri): Place? {
        val item = WeakReference(Gson().fromJson(readFileToJson(cr, name), Place::class.java))
        return item.get()
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun getPlace(filePath: String?, json: String?): Place? {
        return if (filePath != null && MemoryUtil.isSdPresent) {
            val item = WeakReference(Gson().fromJson(readFileToJson(filePath), Place::class.java))
            item.get()
        } else if (json != null) {
            val item = WeakReference(Gson().fromJson(json, Place::class.java))
            item.get()
        } else {
            null
        }
    }

    fun exportBirthdays() {
        for (item in AppDb.getAppDatabase(mContext).birthdaysDao().all) {
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
                        AppDb.getAppDatabase(mContext).birthdaysDao().insert(item)
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
            LogUtil.i(TAG, "Couldn't find external storage!")
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun getBirthday(cr: ContentResolver, name: Uri): Birthday? {
        val item = WeakReference(Gson().fromJson(readFileToJson(cr, name), Birthday::class.java))
        return item.get()
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun getBirthday(filePath: String?, json: String?): Birthday? {
        return if (filePath != null && MemoryUtil.isSdPresent) {
            val item = WeakReference(Gson().fromJson(readFileToJson(filePath), Birthday::class.java))
            item.get()
        } else if (json != null) {
            val item = WeakReference(Gson().fromJson(json, Birthday::class.java))
            item.get()
        } else {
            null
        }
    }

    fun exportGroups() {
        for (item in AppDb.getAppDatabase(mContext).groupDao().all) {
            exportGroup(item)
        }
    }

    private fun exportGroup(item: Group) {
        val jsonData = WeakReference(Gson().toJson(item))
        val dir = MemoryUtil.groupsDir
        if (dir != null) {
            val exportFileName = item.uuId + FileConfig.FILE_NAME_GROUP
            val file = File(dir, exportFileName)
            LogUtil.d(TAG, "exportGroup: $file")
            try {
                writeFile(file, jsonData.get())
                jsonData.clear()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else {
            LogUtil.i(TAG, "Couldn't find external storage!")
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun importGroups() {
        val dir = MemoryUtil.groupsDir
        if (dir != null && dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                val groups = AppDb.getAppDatabase(mContext).groupDao().all.toMutableList()
                for (file in files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_GROUP)) {
                        val item = getGroup(file.toString(), null)
                        if (item == null || TextUtils.isEmpty(item.uuId)) continue
                        if (!TextUtils.isEmpty(item.title) && !hasGroup(groups, item.title)) {
                            AppDb.getAppDatabase(mContext).groupDao().insert(item)
                            groups.add(item)
                        }
                    }
                }
            }
        }
    }

    private fun hasGroup(list: List<Group>, comparable: String?): Boolean {
        if (comparable == null) return true
        for (item in list) {
            if (comparable == item.title) {
                return true
            }
        }
        return false
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun getGroup(cr: ContentResolver, name: Uri): Group? {
        val item = WeakReference(Gson().fromJson(readFileToJson(cr, name), Group::class.java))
        return item.get()
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun getGroup(filePath: String?, json: String?): Group? {
        return if (filePath != null && MemoryUtil.isSdPresent) {
            val item = WeakReference(Gson().fromJson(readFileToJson(filePath), Group::class.java))
            item.get()
        } else if (json != null) {
            val item = WeakReference(Gson().fromJson(json, Group::class.java))
            item.get()
        } else {
            null
        }
    }

    fun exportReminders() {
        for (reminder in AppDb.getAppDatabase(mContext).reminderDao().all) {
            exportReminder(reminder)
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun importReminders(mContext: Context) {
        val dir = MemoryUtil.remindersDir
        if (dir != null && dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                val defaultGroup = AppDb.getAppDatabase(mContext).groupDao().default
                for (file in files) {
                    if (file.toString().endsWith(FileConfig.FILE_NAME_REMINDER)) {
                        val reminder = getReminder(file.toString(), null) ?: continue
                        if (reminder.isRemoved || !reminder.isActive) {
                            continue
                        }
                        if (TextUtils.isEmpty(reminder.summary) ||
                                TextUtils.isEmpty(reminder.eventTime) ||
                                TextUtils.isEmpty(reminder.uuId)) {
                            continue
                        }
                        if (AppDb.getAppDatabase(mContext).groupDao().getById(reminder.groupUuId) == null && defaultGroup != null) {
                            reminder.groupUuId = defaultGroup.uuId
                        }
                        AppDb.getAppDatabase(mContext).reminderDao().insert(reminder)
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
            LogUtil.i(TAG, "Couldn't find external storage!")
        }
        return null
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun getReminder(cr: ContentResolver, name: Uri): Reminder? {
        var reminder: Reminder? = null
        try {
            reminder = Gson().fromJson(readFileToJson(cr, name), Reminder::class.java)
        } catch (ignored: IllegalStateException) {
        }

        return reminder
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun getReminder(filePath: String?, json: String?): Reminder? {
        return if (filePath != null && MemoryUtil.isSdPresent) {
            val item = WeakReference(Gson().fromJson(readFileToJson(filePath), Reminder::class.java))
            item.get()
        } else if (json != null) {
            val item = WeakReference(Gson().fromJson(json, Reminder::class.java))
            item.get()
        } else {
            null
        }
    }

    @Throws(IOException::class, IllegalStateException::class)
    fun getNote(cr: ContentResolver, name: Uri): Note? {
        return try {
            val note = WeakReference(Gson().fromJson(readFileToJson(cr, name), Note::class.java))
            note.get()
        } catch (e: Exception) {
            null
        }

    }

    @Throws(IOException::class, IllegalStateException::class)
    fun getNote(filePath: String?, json: String?): Note? {
        return if (filePath != null && MemoryUtil.isSdPresent) {
            val item = WeakReference(Gson().fromJson(readFileToJson(filePath), Note::class.java))
            item.get()
        } else if (json != null) {
            val item = WeakReference(Gson().fromJson(json, Note::class.java))
            item.get()
        } else {
            null
        }
    }

    fun exportNotes() {
        for (item in AppDb.getAppDatabase(mContext).notesDao().all) {
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
                        val item = getNote(file.toString(), null)
                        if (item == null || TextUtils.isEmpty(item.key)) {
                            continue
                        }
                        AppDb.getAppDatabase(mContext).notesDao().insert(item)
                    }
                }
            }
        }
    }

    private fun exportNote(item: Note) {
        val jsonData = WeakReference(Gson().toJson(item))
        val dir = MemoryUtil.notesDir
        if (dir != null) {
            val exportFileName = item.key + FileConfig.FILE_NAME_NOTE
            try {
                writeFile(File(dir, exportFileName), jsonData.get())
                jsonData.clear()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else {
            LogUtil.i(TAG, "Couldn't find external storage!")
        }
    }

    fun createNote(item: Note?, callback: CreateCallback?) {
        if (item == null) return
        val jsonData = WeakReference(Gson().toJson(item))
        var file: File? = null
        val dir = MemoryUtil.mailDir
        if (dir != null) {
            val exportFileName = item.key + FileConfig.FILE_NAME_NOTE
            file = File(dir, exportFileName)
            try {
                writeFile(file, jsonData.get())
                jsonData.clear()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else {
            LogUtil.i(TAG, "Couldn't find external storage!")
        }
        callback?.onReady(file)
    }

    @Throws(IOException::class)
    private fun readFileToJson(cr: ContentResolver, name: Uri): String? {
        var inputStream: InputStream? = null
        try {
            inputStream = cr.openInputStream(name)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        if (inputStream == null) {
            return null
        }
        val output64 = Base64InputStream(inputStream, Base64.DEFAULT)
        val r = BufferedReader(InputStreamReader(output64))
        val total = StringBuilder()
        var line: String?
        do {
            line = r.readLine()
            if (line != null) {
                total.append(line)
            }
        } while (line != null)
        output64.close()
        inputStream.close()
        val res = total.toString()
        return if (res.startsWith("{") && res.endsWith("}") || res.startsWith("[") && res.endsWith("]"))
            res
        else {
            throw IOException("Bad JSON")
        }
    }

    @Throws(IOException::class)
    private fun readFileToJson(path: String): String {
        val inputStream = FileInputStream(path)
        val output64 = Base64InputStream(inputStream, Base64.DEFAULT)
        val r = BufferedReader(InputStreamReader(output64))
        val total = StringBuilder()
        var line: String?
        do {
            line = r.readLine()
            if (line != null) {
                total.append(line)
            }
        } while (line != null)
        output64.close()
        inputStream.close()
        val res = total.toString()
        return if (res.startsWith("{") && res.endsWith("}") || res.startsWith("[") && res.endsWith("]"))
            res
        else {
            throw IOException("Bad JSON")
        }
    }

    /**
     * Write data to file.
     *
     * @param file target file.
     * @param data object data.
     * @return Path to file
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeFile(file: File, data: String?): String? {
        if (data == null) return null
        val inputStream = ByteArrayInputStream(data.toByteArray())
        val buffer = ByteArray(8192)
        var bytesRead: Int
        val output = ByteArrayOutputStream()
        val output64 = Base64OutputStream(output, Base64.DEFAULT)
        try {
            do {
                bytesRead = inputStream.read(buffer)
                if (bytesRead != -1) {
                    output64.write(buffer, 0, bytesRead)
                }
            } while (bytesRead != -1)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        output64.close()

        if (file.exists()) {
            file.delete()
        }
        val fw = FileWriter(file)
        fw.write(output.toString())
        fw.close()
        output.close()
        return file.toString()
    }

    interface CreateCallback {
        fun onReady(file: File?)
    }

    companion object {

        private val TAG = "BackupTool"
        private var instance: BackupTool? = null

        fun getInstance(): BackupTool {
            if (instance == null) {
                synchronized(BackupTool::class.java) {
                    if (instance == null) {
                        instance = BackupTool()
                    }
                }
            }
            return instance!!
        }
    }
}

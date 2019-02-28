package com.elementary.tasks.core.cloud

import android.content.Context
import android.os.Environment
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.http.OkHttp3Requestor
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.dropbox.core.v2.users.FullAccount
import com.dropbox.core.v2.users.SpaceUsage
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.groups.GroupsUtil
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.*
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
class Dropbox {

    private val dbxFolder = "/Reminders/"
    private val dbxNoteFolder = "/Notes/"
    private val dbxGroupFolder = "/Groups/"
    private val dbxBirthFolder = "/Birthdays/"
    private val dbxPlacesFolder = "/Places/"
    private val dbxTemplatesFolder = "/Templates/"
    private val dbxSettingsFolder = "/Settings/"

    private var mDBApi: DbxClientV2? = null
    @Inject lateinit var prefs: Prefs
    @Inject lateinit var backupTool: BackupTool
    @Inject lateinit var appDb: AppDb

    init {
        ReminderApp.appComponent.inject(this)
    }

    /**
     * Check if user has already connected to Dropbox from this application.
     *
     * @return Boolean
     */
    val isLinked: Boolean
        get() = mDBApi != null && prefs.dropboxToken != ""

    /**
     * Start connection to Dropbox.
     */
    fun startSession() {
        var token: String? = prefs.dropboxToken
        if (token == "") {
            token = Auth.getOAuth2Token()
        }
        if (token == null) {
            prefs.dropboxToken = ""
            return
        }
        prefs.dropboxToken = token
        val requestConfig = DbxRequestConfig.newBuilder("Just Reminder")
                .withHttpRequestor(OkHttp3Requestor(OkHttpClient()))
                .build()

        mDBApi = DbxClientV2(requestConfig, token)
    }

    /**
     * Holder Dropbox user name.
     *
     * @return String user name
     */
    fun userName(): String {
        val api = mDBApi ?: return ""
        var account: FullAccount? = null
        try {
            account = api.users().currentAccount
        } catch (e: DbxException) {
            e.printStackTrace()
        }

        return account?.name?.displayName ?: ""
    }

    /**
     * Holder user all apace on Dropbox.
     *
     * @return Long - user quota
     */
    fun userQuota(): Long {
        val api = mDBApi ?: return 0
        var account: SpaceUsage? = null
        try {
            account = api.users().spaceUsage
        } catch (e: DbxException) {
            Timber.d("userQuota: ${e.message}")
        }

        return account?.allocation?.individualValue?.allocated ?: 0
    }

    fun userQuotaNormal(): Long {
        val api = mDBApi ?: return 0
        var account: SpaceUsage? = null
        try {
            account = api.users().spaceUsage
        } catch (e: DbxException) {
            Timber.d("userQuotaNormal: ${e.message}")
        }

        return account?.used ?: 0
    }

    fun startLink(context: Context) {
        Auth.startOAuth2Authentication(context, APP_KEY)
    }

    fun unlink(): Boolean {
        var b = false
        if (logOut()) {
            b = true
        }
        return b
    }

    private fun logOut(): Boolean {
        clearKeys()
        return true
    }

    private fun clearKeys() {
        prefs.dropboxToken = ""
        prefs.dropboxUid = ""
    }

    /**
     * Upload to Dropbox folder backup files from selected folder on SD Card.
     *
     * @param path name of folder to upload.
     */
    private fun upload(path: String) {
        startSession()
        if (!isLinked) {
            return
        }
        val sdPath = Environment.getExternalStorageDirectory()
        val sdPathDr = File(sdPath.toString() + "/JustReminder/" + path)
        val files = sdPathDr.listFiles()
        val fileLoc = sdPathDr.toString()
        if (files == null) {
            return
        }
        val api = mDBApi ?: return
        for (file in files) {
            val fileLoopName = file.name
            val tmpFile = File(fileLoc, fileLoopName)
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(tmpFile)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            val folder: String = when {
                path.matches(MemoryUtil.DIR_NOTES_SD.toRegex()) -> dbxNoteFolder
                path.matches(MemoryUtil.DIR_GROUP_SD.toRegex()) -> dbxGroupFolder
                path.matches(MemoryUtil.DIR_BIRTHDAY_SD.toRegex()) -> dbxBirthFolder
                path.matches(MemoryUtil.DIR_PLACES_SD.toRegex()) -> dbxPlacesFolder
                path.matches(MemoryUtil.DIR_TEMPLATES_SD.toRegex()) -> dbxTemplatesFolder
                else -> dbxFolder
            }
            if (fis == null) return
            try {
                val filePath = folder + fileLoopName
                api.files().uploadBuilder(filePath)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(fis)
            } catch (e: DbxException) {
                Timber.d("upload: ${e.message}")
            } catch (e: IOException) {
                Timber.d("upload: ${e.message}")
            }

        }
    }

    fun uploadReminderByFileName(fileName: String?) {
        val dir = MemoryUtil.remindersDir ?: return
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        if (fileName != null) {
            val tmpFile = File(dir.toString(), fileName)
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(tmpFile)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            if (fis == null) return
            try {
                api.files().uploadBuilder(dbxFolder + fileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(fis)
            } catch (e: DbxException) {
                Timber.d("uploadReminderByFileName: ${e.message}")
            } catch (e: IOException) {
                Timber.d("uploadReminderByFileName: ${e.message}")
            } catch (e: NullPointerException) {
                Timber.d("uploadReminderByFileName: ${e.message}")
            }
        } else {
            upload(MemoryUtil.DIR_SD)
        }
    }

    fun uploadBirthdayByFileName(fileName: String?) {
        val dir = MemoryUtil.birthdaysDir ?: return
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        if (fileName != null) {
            val tmpFile = File(dir, fileName)
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(tmpFile)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            if (fis == null) return
            try {
                api.files().uploadBuilder(dbxBirthFolder + fileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(fis)
            } catch (e: DbxException) {
                Timber.d("uploadBirthdayByFileName: ${e.message}")
            } catch (e: IOException) {
                Timber.d("uploadBirthdayByFileName: ${e.message}")
            } catch (e: NullPointerException) {
                Timber.d("uploadBirthdayByFileName: ${e.message}")
            }
        } else {
            upload(MemoryUtil.DIR_BIRTHDAY_SD)
        }
    }

    fun uploadGroupByFileName(fileName: String?) {
        val dir = MemoryUtil.groupsDir ?: return
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        if (fileName != null) {
            val tmpFile = File(dir, fileName)
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(tmpFile)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            if (fis == null) return
            try {
                api.files().uploadBuilder(dbxGroupFolder + fileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(fis)
            } catch (e: DbxException) {
                Timber.d("uploadGroupByFileName: ${e.message}")
            } catch (e: IOException) {
                Timber.d("uploadGroupByFileName: ${e.message}")
            } catch (e: NullPointerException) {
                Timber.d("uploadGroupByFileName: ${e.message}")
            }
        } else {
            upload(MemoryUtil.DIR_GROUP_SD)
        }
    }

    fun uploadPlaceByFileName(fileName: String?) {
        val dir = MemoryUtil.placesDir ?: return
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        if (fileName != null) {
            val tmpFile = File(dir, fileName)
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(tmpFile)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            if (fis == null) return
            try {
                api.files().uploadBuilder(dbxPlacesFolder + fileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(fis)
            } catch (e: DbxException) {
                Timber.d("uploadPlaceByFileName: ${e.message}")
            } catch (e: IOException) {
                Timber.d("uploadPlaceByFileName: ${e.message}")
            } catch (e: NullPointerException) {
                Timber.d("uploadPlaceByFileName: ${e.message}")
            }
        } else {
            upload(MemoryUtil.DIR_PLACES_SD)
        }
    }

    fun uploadNoteByFileName(fileName: String?) {
        val dir = MemoryUtil.notesDir ?: return
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        if (fileName != null) {
            val tmpFile = File(dir, fileName)
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(tmpFile)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            if (fis == null) return
            try {
                api.files().uploadBuilder(dbxNoteFolder + fileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(fis)
            } catch (e: DbxException) {
                Timber.d("uploadNoteByFileName: ${e.message}")
            } catch (e: IOException) {
                Timber.d("uploadNoteByFileName: ${e.message}")
            } catch (e: NullPointerException) {
                Timber.d("uploadNoteByFileName: ${e.message}")
            }
        } else {
            upload(MemoryUtil.DIR_NOTES_SD)
        }
    }

    fun uploadTemplateByFileName(fileName: String?) {
        val dir = MemoryUtil.templatesDir ?: return
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        if (fileName != null) {
            val tmpFile = File(dir, fileName)
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(tmpFile)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            if (fis == null) return
            try {
                api.files().uploadBuilder(dbxTemplatesFolder + fileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(fis)
            } catch (e: DbxException) {
                Timber.d("uploadTemplateByFileName: ${e.message}")
            } catch (e: IOException) {
                Timber.d("uploadTemplateByFileName: ${e.message}")
            } catch (e: NullPointerException) {
                Timber.d("uploadTemplateByFileName: ${e.message}")
            }
        } else {
            upload(MemoryUtil.DIR_TEMPLATES_SD)
        }
    }

    /**
     * Upload all note backup files to Dropbox folder.
     */
    fun uploadNotes() {
        upload(MemoryUtil.DIR_NOTES_SD)
    }

    /**
     * Upload all reminderGroup backup files to Dropbox folder.
     */
    fun uploadGroups() {
        upload(MemoryUtil.DIR_GROUP_SD)
    }

    /**
     * Upload all birthday backup files to Dropbox folder.
     */
    fun uploadBirthdays() {
        upload(MemoryUtil.DIR_BIRTHDAY_SD)
    }

    /**
     * Upload all places backup files to Dropbox folder.
     */
    fun uploadPlaces() {
        upload(MemoryUtil.DIR_PLACES_SD)
    }

    /**
     * Upload all templates backup files to Dropbox folder.
     */
    fun uploadTemplates() {
        upload(MemoryUtil.DIR_TEMPLATES_SD)
    }

    /**
     * Delete reminder backup file from Dropbox folder.
     *
     * @param name file name.
     */
    fun deleteReminder(name: String) {
        Timber.d("deleteReminder: $name")
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        try {
            api.files().deleteV2(dbxFolder + name)
        } catch (e: DbxException) {
            Timber.d("deleteReminder: ${e.message}")
        }
    }

    /**
     * Delete note backup file from Dropbox folder.
     *
     * @param name file name.
     */
    fun deleteNote(name: String) {
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        try {
            api.files().deleteV2(dbxNoteFolder + name)
        } catch (e: DbxException) {
            Timber.d("deleteNote: ${e.message}")
        }
    }

    /**
     * Delete reminderGroup backup file from Dropbox folder.
     *
     * @param name file name.
     */
    fun deleteGroup(name: String) {
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        try {
            api.files().deleteV2(dbxGroupFolder + name)
        } catch (e: DbxException) {
            Timber.d("deleteGroup: ${e.message}")
        }
    }

    /**
     * Delete birthday backup file from Dropbox folder.
     *
     * @param name file name
     */
    fun deleteBirthday(name: String) {
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        try {
            api.files().deleteV2(dbxBirthFolder + name)
        } catch (e: DbxException) {
            Timber.d("deleteBirthday: ${e.message}")
        }
    }

    /**
     * Delete place backup file from Dropbox folder.
     *
     * @param name file name
     */
    fun deletePlace(name: String) {
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        try {
            api.files().deleteV2(dbxPlacesFolder + name)
        } catch (e: DbxException) {
            Timber.d("deletePlace: ${e.message}")
        }
    }

    /**
     * Delete place backup file from Dropbox folder.
     *
     * @param name file name
     */
    fun deleteTemplate(name: String) {
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        try {
            api.files().deleteV2(dbxTemplatesFolder + name)
        } catch (e: DbxException) {
            Timber.d("deleteTemplate: ${e.message}")
        }
    }

    /**
     * Delete all folders inside application folder on Dropbox.
     */
    fun cleanFolder() {
        startSession()
        if (!isLinked) {
            return
        }
        deleteFolder(dbxNoteFolder)
        deleteFolder(dbxGroupFolder)
        deleteFolder(dbxBirthFolder)
        deleteFolder(dbxPlacesFolder)
        deleteFolder(dbxTemplatesFolder)
        deleteFolder(dbxSettingsFolder)
        deleteFolder(dbxFolder)
    }

    private fun deleteFolder(folder: String) {
        val api = mDBApi ?: return
        try {
            api.files().deleteV2(folder)
        } catch (e: DbxException) {
            Timber.d("deleteFolder: ${e.message}")
        }
    }

    /**
     * Download on SD Card all template backup files found on Dropbox.
     */
    fun downloadTemplates(deleteFile: Boolean) {
        val dir = MemoryUtil.dropboxTemplatesDir ?: return
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        try {
            val result = api.files().listFolder(dbxTemplatesFolder) ?: return
            val dao = appDb.smsTemplatesDao()
            for (e in result.entries) {
                val fileName = e.name
                val localFile = File("$dir/$fileName")
                val cloudFile = dbxTemplatesFolder + fileName
                downloadFile(localFile, cloudFile)
                val template = backupTool.getTemplate(localFile.toString(), null)
                if (template != null) dao.insert(template)
                if (deleteFile) {
                    if (localFile.exists()) {
                        localFile.delete()
                    }
                    api.files().deleteV2(e.pathLower)
                }
            }
        } catch (e: DbxException) {
            Timber.d("downloadTemplates: ${e.message}")
        } catch (e: IOException) {
            Timber.d("downloadTemplates: ${e.message}")
        } catch (e: IllegalStateException) {
            Timber.d("downloadTemplates: ${e.message}")
        }
    }

    /**
     * Download on SD Card all reminder backup files found on Dropbox.
     */
    fun downloadReminders(deleteFile: Boolean) {
        val dir = MemoryUtil.dropboxRemindersDir ?: return
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        try {
            val result = api.files().listFolder(dbxFolder) ?: return
            val dao = appDb.reminderDao()
            val groups = GroupsUtil.mapAll(appDb)
            val defGroup = appDb.reminderGroupDao().defaultGroup() ?: groups.values.first()
            for (e in result.entries) {
                val fileName = e.name
                val localFile = File("$dir/$fileName")
                val cloudFile = dbxFolder + fileName
                downloadFile(localFile, cloudFile)
                val reminder = backupTool.getReminder(localFile.toString(), null)
                if (reminder == null) {
                    deleteReminder(fileName)
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
                dao.insert(reminder)
                if (reminder.isActive && !reminder.isRemoved) {
                    val control = EventControlFactory.getController(reminder)
                    if (control.canSkip()) {
                        control.next()
                    } else {
                        control.start()
                    }
                }
                if (deleteFile) {
                    if (localFile.exists()) {
                        localFile.delete()
                    }
                    api.files().deleteV2(e.pathLower)
                }
            }
        } catch (e: DbxException) {
            Timber.d("downloadReminders: ${e.message}")
        } catch (e: IOException) {
            Timber.d("downloadReminders: ${e.message}")
        } catch (e: IllegalStateException) {
            Timber.d("downloadReminders: ${e.message}")
        }
    }

    private fun downloadFile(localFile: File, cloudFile: String) {
        val api = mDBApi ?: return
        try {
            if (!localFile.exists()) {
                localFile.createNewFile()
            }
            val outputStream = FileOutputStream(localFile)
            api.files().download(cloudFile).download(outputStream)
        } catch (e: DbxException) {
            Timber.d("downloadFile: ${e.message}")
        } catch (e: IOException) {
            Timber.d("downloadFile: ${e.message}")
        }
    }

    /**
     * Download on SD Card all note backup files found on Dropbox.
     */
    fun downloadNotes(deleteFile: Boolean) {
        val dir = MemoryUtil.dropboxNotesDir ?: return
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        try {
            val result = api.files().listFolder(dbxNoteFolder) ?: return
            val dao = appDb.notesDao()
            for (e in result.entries) {
                val fileName = e.name
                val localFile = File("$dir/$fileName")
                val cloudFile = dbxNoteFolder + fileName
                downloadFile(localFile, cloudFile)
                val item = backupTool.getNote(localFile.toString(), null)
                val note = item?.note
                if (item != null && note != null) {
                    dao.insert(note)
                    dao.insertAll(item.images)
                }
                if (deleteFile) {
                    if (localFile.exists()) {
                        localFile.delete()
                    }
                    api.files().deleteV2(e.pathLower)
                }
            }
        } catch (e: DbxException) {
            Timber.d("downloadNotes: ${e.message}")
        } catch (e: IOException) {
            Timber.d("downloadNotes: ${e.message}")
        } catch (e: IllegalStateException) {
            Timber.d("downloadNotes: ${e.message}")
        }
    }

    /**
     * Download on SD Card all reminderGroup backup files found on Dropbox.
     */
    fun downloadGroups(deleteFile: Boolean) {
        val dir = MemoryUtil.dropboxGroupsDir ?: return
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        try {
            val result = api.files().listFolder(dbxGroupFolder) ?: return
            val dao = appDb.reminderGroupDao()
            for (e in result.entries) {
                val fileName = e.name
                val localFile = File("$dir/$fileName")
                val cloudFile = dbxGroupFolder + fileName
                downloadFile(localFile, cloudFile)
                val group = backupTool.getGroup(localFile.toString(), null)
                if (group != null) {
                    dao.insert(group)
                }
                if (deleteFile) {
                    if (localFile.exists()) {
                        localFile.delete()
                    }
                    api.files().deleteV2(e.pathLower)
                }
            }
        } catch (e: DbxException) {
            Timber.d("downloadGroups: ${e.message}")
        } catch (e: IOException) {
            Timber.d("downloadGroups: ${e.message}")
        } catch (e: IllegalStateException) {
            Timber.d("downloadGroups: ${e.message}")
        }
    }

    /**
     * Download on SD Card all birthday backup files found on Dropbox.
     */
    fun downloadBirthdays(deleteFile: Boolean) {
        val dir = MemoryUtil.dropboxBirthdaysDir ?: return
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        try {
            val result = api.files().listFolder(dbxBirthFolder) ?: return
            val dao = appDb.birthdaysDao()
            for (e in result.entries) {
                val fileName = e.name
                val localFile = File("$dir/$fileName")
                val cloudFile = dbxBirthFolder + fileName
                downloadFile(localFile, cloudFile)
                val birthday = backupTool.getBirthday(localFile.toString(), null)
                if (birthday != null) {
                    dao.insert(birthday)
                }
                if (deleteFile) {
                    if (localFile.exists()) {
                        localFile.delete()
                    }
                    api.files().deleteV2(e.pathLower)
                }
            }
        } catch (e: DbxException) {
            Timber.d("downloadBirthdays: ${e.message}")
        } catch (e: IOException) {
            Timber.d("downloadBirthdays: ${e.message}")
        } catch (e: IllegalStateException) {
            Timber.d("downloadBirthdays: ${e.message}")
        }
    }

    /**
     * Download on SD Card all places backup files found on Dropbox.
     */
    fun downloadPlaces(deleteFile: Boolean) {
        val dir = MemoryUtil.dropboxPlacesDir ?: return
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        try {
            val result = api.files().listFolder(dbxPlacesFolder) ?: return
            val dao = appDb.placesDao()
            for (e in result.entries) {
                val fileName = e.name
                val localFile = File("$dir/$fileName")
                val cloudFile = dbxPlacesFolder + fileName
                downloadFile(localFile, cloudFile)
                val place = backupTool.getPlace(localFile.toString(), null)
                if (place != null) {
                    dao.insert(place)
                }
                if (deleteFile) {
                    if (localFile.exists()) {
                        localFile.delete()
                    }
                    api.files().deleteV2(e.pathLower)
                }
            }
        } catch (e: DbxException) {
            Timber.d("downloadPlaces: ${e.message}")
        } catch (e: IOException) {
            Timber.d("downloadPlaces: ${e.message}")
        } catch (e: IllegalStateException) {
            Timber.d("downloadPlaces: ${e.message}")
        }
    }

    fun uploadSettings() {
        val dir = MemoryUtil.prefsDir ?: return
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (!file.toString().endsWith(FileConfig.FILE_NAME_SETTINGS)) {
                continue
            }
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(file)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            if (fis == null) return
            try {
                api.files().uploadBuilder(dbxSettingsFolder + file.name)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(fis)
            } catch (e: DbxException) {
                Timber.d("uploadSettings: ${e.message}")
            } catch (e: IOException) {
                Timber.d("uploadSettings: ${e.message}")
            }
            break
        }
    }

    fun downloadSettings() {
        val dir = MemoryUtil.prefsDir ?: return
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        try {
            val result = api.files().listFolder(dbxSettingsFolder) ?: return
            for (e in result.entries) {
                val fileName = e.name
                if (fileName.contains(FileConfig.FILE_NAME_SETTINGS)) {
                    val localFile = File("$dir/$fileName")
                    val cloudFile = dbxPlacesFolder + fileName
                    downloadFile(localFile, cloudFile)
                    prefs.loadPrefsFromFile()
                    break
                }
            }
        } catch (e: DbxException) {
            Timber.d("downloadSettings: ${e.message}")
        }

    }

    /**
     * Count all reminder backup files in Dropbox folder.
     *
     * @return number of found backup files.
     */
    fun countFiles(): Int {
        var count = 0
        startSession()
        if (!isLinked) {
            return 0
        }
        val api = mDBApi ?: return 0
        try {
            val result = api.files().listFolder("/") ?: return 0
            count = result.entries.size
        } catch (e: DbxException) {
            Timber.d("countFiles: ${e.message}")
        }

        return count
    }

    companion object {
        private const val APP_KEY = "4zi1d414h0v8sxe"
    }
}

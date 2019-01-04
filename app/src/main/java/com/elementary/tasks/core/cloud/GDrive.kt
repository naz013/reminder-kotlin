package com.elementary.tasks.core.cloud

import android.content.Context
import android.text.TextUtils
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.navigation.settings.export.backups.UserItem
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.tasks.TasksScopes
import timber.log.Timber
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
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

class GDrive private constructor(context: Context) {

    private var driveService: Drive? = null

    @Inject
    lateinit var appDb: AppDb
    @Inject
    lateinit var prefs: Prefs
    @Inject
    lateinit var backupTool: BackupTool

    var isLogged: Boolean = false
        private set

    init {
        ReminderApp.appComponent.inject(this)

        val user = prefs.driveUser
        if (user.matches(".*@.*".toRegex())) {
            val credential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(DriveScopes.DRIVE, TasksScopes.TASKS))
            credential.selectedAccountName = user
            val mJsonFactory = GsonFactory.getDefaultInstance()
            val mTransport = AndroidHttp.newCompatibleTransport()
            driveService = Drive.Builder(mTransport, mJsonFactory, credential).setApplicationName(APPLICATION_NAME).build()
            isLogged = true
        }
    }

    fun logOut() {
        prefs.driveUser = Prefs.DRIVE_USER_NONE
        driveService = null
        isLogged = false
    }

    /**
     * Holder information about user.
     *
     * @return user info object
     */
    val data: UserItem?
        get() {
            val service = driveService ?: return null
            if (!isLogged) return null
            try {
                val about = service.about().get().setFields("user, storageQuota").execute() ?: return null
                val quota = about.storageQuota ?: return null
                return UserItem(name = about.user.displayName, quota = quota.limit,
                        used = quota.usage, count = countFiles(), photo = about.user.photoLink)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

    /**
     * Holder application folder identifier on GTasks Drive.
     *
     * @return Drive folder identifier.
     */
    private val folderId: String?
        @Throws(IOException::class, IllegalArgumentException::class)
        get() {
            val service = driveService ?: return null
            if (!isLogged) return null
            val request = service.files().list()
                    .setQ("mimeType = 'application/vnd.google-apps.folder' and name contains '$FOLDER_NAME'")
                    ?: return null
            do {
                val files = request.execute() ?: return null
                val fileList = files.files as ArrayList<File>
                for (f in fileList) {
                    val fileMIME = f.mimeType
                    if (fileMIME.trim { it <= ' ' }.contains("application/vnd.google-apps.folder") && f.name.contains(FOLDER_NAME)) {
                        Timber.d("getFolderId: ${f.name}, ${f.mimeType}")
                        return f.id
                    }
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
            val file = createFolder()
            return file?.id
        }

    /**
     * Count all backup files stored on GTasks Drive.
     *
     * @return number of files in local folder.
     */
    @Throws(IOException::class)
    private fun countFiles(): Int {
        val service = driveService ?: return 0
        if (!isLogged) return 0
        var count = 0
        val request = service.files().list().setQ("mimeType = 'text/plain'").setFields("nextPageToken, files") ?: return 0
        do {
            val files = request.execute()
            val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
            for (f in fileList) {
                val title = f.name
                when {
                    title.contains(FileConfig.FILE_NAME_SETTINGS) -> count++
                    title.endsWith(FileConfig.FILE_NAME_TEMPLATE) -> count++
                    title.endsWith(FileConfig.FILE_NAME_PLACE) -> count++
                    title.endsWith(FileConfig.FILE_NAME_BIRTHDAY) -> count++
                    title.endsWith(FileConfig.FILE_NAME_GROUP) -> count++
                    title.endsWith(FileConfig.FILE_NAME_NOTE) -> count++
                    title.endsWith(FileConfig.FILE_NAME_REMINDER) -> count++
                }
            }
            request.pageToken = files.nextPageToken
        } while (request.pageToken != null)
        return count
    }

    @Throws(IOException::class)
    fun saveSettingsToDrive() {
        val service = driveService ?: return
        if (!isLogged) return
        var foId: String? = null
        try {
            foId = folderId
        } catch (ignored: IllegalArgumentException) {
        }

        if (foId == null) {
            return
        }
        val folder = MemoryUtil.prefsDir ?: return
        val files = folder.listFiles() ?: return
        for (file in files) {
            if (!file.toString().endsWith(FileConfig.FILE_NAME_SETTINGS)) continue
            removeAllCopies(file.name)
            val fileMetadata = File()
            fileMetadata.name = file.name
            fileMetadata.description = "Settings Backup"
            fileMetadata.parents = listOf(foId)
            val mediaContent = FileContent("text/plain", file)
            val req = service.files().create(fileMetadata, mediaContent)
            req.fields = "id"
            req.execute()
            break
        }
    }

    @Throws(IOException::class)
    private fun removeAllCopies(fileName: String) {
        val service = driveService ?: return
        if (!isLogged) return
        if (TextUtils.isEmpty(fileName)) return
        val request = service.files().list()
                .setQ("mimeType = 'text/plain' and name contains '$fileName'")
                .setFields("nextPageToken, files")
        do {
            val files = request.execute()
            val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
            for (f in fileList) {
                service.files().delete(f.id).execute()
            }
            request.pageToken = files.nextPageToken
        } while (request.pageToken != null)
    }

    @Throws(IOException::class)
    fun downloadSettings(context: Context, deleteFile: Boolean) {
        val service = driveService ?: return
        if (!isLogged) return
        val folder = MemoryUtil.prefsDir
        if (folder == null || !folder.exists() && !folder.mkdirs()) {
            return
        }
        val request = service.files().list()
                .setQ("mimeType = 'text/plain' and name contains '" + FileConfig.FILE_NAME_SETTINGS + "'")
                .setFields("nextPageToken, files")
        do {
            val files = request.execute()
            val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
            for (f in fileList) {
                val title = f.name
                if (title.contains(FileConfig.FILE_NAME_SETTINGS)) {
                    val file = java.io.File(folder, title)
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    val out = FileOutputStream(file)
                    service.files().get(f.id).executeMediaAndDownloadTo(out)
                    if (deleteFile) {
                        service.files().delete(f.id).execute()
                    }
                    prefs.loadPrefsFromFile()
                    break
                }
            }
            request.pageToken = files.nextPageToken
        } while (request.pageToken != null)
    }

    /**
     * Upload all template backup files stored on SD Card.
     */
    fun saveTemplatesToDrive() {
        try {
            saveToDrive(Metadata(FileConfig.FILE_NAME_TEMPLATE, MemoryUtil.templatesDir, "Template Backup", null))
        } catch (e: IOException) {
            Timber.d("saveTemplatesToDrive: ${e.message}")
        }
    }

    /**
     * Upload all reminder backup files stored on SD Card.
     */
    fun saveRemindersToDrive() {
        try {
            saveToDrive(Metadata(FileConfig.FILE_NAME_REMINDER, MemoryUtil.remindersDir, "Reminder Backup", null))
        } catch (e: IOException) {
            Timber.d("saveRemindersToDrive: ${e.message}")
        }
    }

    fun saveReminderToDrive(pathToFile: String) {
        try {
            val metadata = Metadata(FileConfig.FILE_NAME_REMINDER, MemoryUtil.remindersDir, "Reminder Backup", null)
            saveFileToDrive(pathToFile, metadata)
        } catch (e: IOException) {
            Timber.d("saveReminderToDrive: ${e.message}")
        }
    }

    fun saveBirthdayToDrive(pathToFile: String) {
        try {
            val metadata = Metadata(FileConfig.FILE_NAME_BIRTHDAY, MemoryUtil.birthdaysDir, "Birthday Backup", null)
            saveFileToDrive(pathToFile, metadata)
        } catch (e: IOException) {
            Timber.d("saveBirthdayToDrive: ${e.message}")
        }
    }

    fun saveGroupToDrive(pathToFile: String) {
        try {
            val metadata = Metadata(FileConfig.FILE_NAME_GROUP, MemoryUtil.groupsDir, "ReminderGroup Backup", null)
            saveFileToDrive(pathToFile, metadata)
        } catch (e: IOException) {
            Timber.d("saveGroupToDrive: ${e.message}")
        }
    }

    fun savePlaceToDrive(pathToFile: String) {
        try {
            val metadata = Metadata(FileConfig.FILE_NAME_PLACE, MemoryUtil.placesDir, "Place Backup", null)
            saveFileToDrive(pathToFile, metadata)
        } catch (e: IOException) {
            Timber.d("savePlaceToDrive: ${e.message}")
        }
    }

    fun saveNoteToDrive(pathToFile: String) {
        try {
            val metadata = Metadata(FileConfig.FILE_NAME_NOTE, MemoryUtil.notesDir, "Note Backup", null)
            saveFileToDrive(pathToFile, metadata)
        } catch (e: IOException) {
            Timber.d("saveNoteToDrive: ${e.message}")
        }
    }

    fun saveTemplateToDrive(pathToFile: String) {
        try {
            val metadata = Metadata(FileConfig.FILE_NAME_TEMPLATE, MemoryUtil.templatesDir, "Template Backup", null)
            saveFileToDrive(pathToFile, metadata)
        } catch (e: IOException) {
            Timber.d("saveTemplateToDrive: ${e.message}")
        }
    }

    fun saveNotesToDrive() {
        try {
            saveToDrive(Metadata(FileConfig.FILE_NAME_NOTE, MemoryUtil.notesDir, "Note Backup", null))
        } catch (e: IOException) {
            Timber.d("saveNotesToDrive: ${e.message}")
        }
    }

    /**
     * Upload all reminderGroup backup files stored on SD Card.
     */
    fun saveGroupsToDrive() {
        try {
            saveToDrive(Metadata(FileConfig.FILE_NAME_GROUP, MemoryUtil.groupsDir, "ReminderGroup Backup", null))
        } catch (e: IOException) {
            Timber.d("saveGroupsToDrive: ${e.message}")
        }
    }

    /**
     * Upload all birthday backup files stored on SD Card.
     */
    fun saveBirthdaysToDrive() {
        try {
            saveToDrive(Metadata(FileConfig.FILE_NAME_BIRTHDAY, MemoryUtil.birthdaysDir, "Birthday Backup", null))
        } catch (e: IOException) {
            Timber.d("saveBirthdaysToDrive: ${e.message}")
        }
    }

    /**
     * Upload all place backup files stored on SD Card.
     */
    fun savePlacesToDrive() {
        try {
            saveToDrive(Metadata(FileConfig.FILE_NAME_PLACE, MemoryUtil.placesDir, "Place Backup", null))
        } catch (e: IOException) {
            Timber.d("savePlacesToDrive: ${e.message}")
        }
    }

    /**
     * Upload files from folder to GTasks Drive.
     *
     * @param metadata metadata.
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun saveToDrive(metadata: Metadata) {
        if (metadata.folder == null) return
        val service = driveService ?: return
        if (!isLogged) return
        val files = metadata.folder.listFiles() ?: return
        var foId: String? = null
        try {
            foId = folderId
        } catch (ignored: IllegalArgumentException) {
        }

        if (foId == null) {
            return
        }
        for (file in files) {
            if (!file.name.endsWith(metadata.fileExt)) continue
            removeAllCopies(file.name)
            val fileMetadata = File()
            fileMetadata.name = file.name
            fileMetadata.description = metadata.meta
            fileMetadata.parents = listOf(foId)
            val mediaContent = FileContent("text/plain", file)
            service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
        }
    }

    /**
     * Upload file from folder to GTasks Drive.
     *
     * @param metadata metadata.
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun saveFileToDrive(pathToFile: String, metadata: Metadata) {
        if (metadata.folder == null) return
        val service = driveService ?: return
        if (!isLogged) return
        var fId: String? = null
        try {
            fId = folderId
        } catch (ignored: IllegalArgumentException) {
        }

        if (fId == null) {
            return
        }
        val f = java.io.File(pathToFile)
        if (!f.exists()) {
            return
        }
        if (!f.name.endsWith(metadata.fileExt)) return
        removeAllCopies(f.name)
        val fileMetadata = File()
        fileMetadata.name = f.name
        fileMetadata.description = metadata.meta
        fileMetadata.parents = listOf(fId)
        val mediaContent = FileContent("text/plain", f)
        service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
    }

    @Throws(IOException::class)
    fun download(deleteBackup: Boolean, metadata: Metadata) {
        val service = driveService ?: return
        if (!isLogged) return
        val folder = metadata.folder
        if (folder == null || !folder.exists() && !folder.mkdirs()) {
            return
        }
        val request = service.files().list()
                .setQ("mimeType = 'text/plain' and name contains '" + metadata.fileExt + "'")
                .setFields("nextPageToken, files")
        do {
            val files = request.execute()
            val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
            for (f in fileList) {
                val title = f.name
                if (title.endsWith(metadata.fileExt)) {
                    val file = java.io.File(folder, title)
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    val out = FileOutputStream(file)
                    service.files().get(f.id).executeMediaAndDownloadTo(out)
                    if (metadata.action != null) {
                        metadata.action.onSave(file)
                    }
                    if (deleteBackup) {
                        if (file.exists()) {
                            file.delete()
                        }
                        service.files().delete(f.id).execute()
                    }
                }
            }
            request.pageToken = files.nextPageToken
        } while (request.pageToken != null)
    }

    /**
     * Download on SD Card all reminder backup files stored on GTasks Drive.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun downloadTemplates(deleteBackup: Boolean) {
        download(deleteBackup, Metadata(FileConfig.FILE_NAME_TEMPLATE, MemoryUtil.googleRemindersDir, null, object : Action {
            override fun onSave(file: java.io.File) {
                try {
                    val item = backupTool.getTemplate(file.toString(), null)
                    if (item != null) appDb.smsTemplatesDao().insert(item)
                } catch (e: IOException) {
                    Timber.d("onSave: ${e.message}")
                } catch (e: IllegalStateException) {
                    Timber.d("onSave: ${e.message}")
                }
            }
        }))
    }

    /**
     * Download on SD Card all reminder backup files stored on GTasks Drive.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun downloadReminders(context: Context, deleteBackup: Boolean) {
        download(deleteBackup, Metadata(FileConfig.FILE_NAME_REMINDER, MemoryUtil.googleRemindersDir, null, object : Action {
            override fun onSave(file: java.io.File) {
                try {
                    val reminder = backupTool.getReminder(file.toString(), null)
                    if (reminder == null || reminder.isRemoved || !reminder.isActive) return
                    appDb.reminderDao().insert(reminder)
                    val control = EventControlFactory.getController(reminder)
                    if (control.canSkip()) {
                        control.next()
                    } else {
                        control.start()
                    }
                } catch (e: IOException) {
                    Timber.d("onSave: ${e.message}")
                } catch (e: IllegalStateException) {
                    Timber.d("onSave: ${e.message}")
                }
            }
        }))
    }

    /**
     * Download on SD Card all place backup files stored on GTasks Drive.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun downloadPlaces(deleteBackup: Boolean) {
        download(deleteBackup, Metadata(FileConfig.FILE_NAME_PLACE, MemoryUtil.googlePlacesDir, null, object : Action {
            override fun onSave(file: java.io.File) {
                try {
                    val item = backupTool.getPlace(file.toString(), null)
                    if (item != null) appDb.placesDao().insert(item)
                } catch (e: IOException) {
                    Timber.d("onSave: ${e.message}")
                } catch (e: IllegalStateException) {
                    Timber.d("onSave: ${e.message}")
                }
            }
        }))
    }

    /**
     * Download on SD Card all note backup files stored on GTasks Drive.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun downloadNotes(deleteBackup: Boolean) {
        download(deleteBackup, Metadata(FileConfig.FILE_NAME_NOTE, MemoryUtil.googleNotesDir, null, object : Action {
            override fun onSave(file: java.io.File) {
                try {
                    val item = backupTool.getNote(file.toString(), null)
                    val note = item?.note
                    if (item != null && note != null) {
                        appDb.notesDao().insert(note)
                        appDb.notesDao().insertAll(item.images)
                    }
                } catch (e: IOException) {
                    Timber.d("onSave: ${e.message}")
                } catch (e: IllegalStateException) {
                    Timber.d("onSave: ${e.message}")
                }
            }
        }))
    }

    /**
     * Download on SD Card all reminderGroup backup files stored on GTasks Drive.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun downloadGroups(deleteBackup: Boolean) {
        download(deleteBackup, Metadata(FileConfig.FILE_NAME_GROUP, MemoryUtil.googleGroupsDir, null, object : Action {
            override fun onSave(file: java.io.File) {
                try {
                    val item = backupTool.getGroup(file.toString(), null)
                    if (item != null) appDb.reminderGroupDao().insert(item)
                } catch (e: IOException) {
                    Timber.d("onSave: ${e.message}")
                } catch (e: IllegalStateException) {
                    Timber.d("onSave: ${e.message}")
                }
            }
        }))
    }

    /**
     * Download on SD Card all birthday backup files stored on GTasks Drive.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun downloadBirthdays(deleteBackup: Boolean) {
        download(deleteBackup, Metadata(FileConfig.FILE_NAME_BIRTHDAY, MemoryUtil.googleBirthdaysDir, null, object : Action {
            override fun onSave(file: java.io.File) {
                try {
                    val item = backupTool.getBirthday(file.toString(), null)
                    if (item != null) appDb.birthdaysDao().insert(item)
                } catch (e: IOException) {
                    Timber.d("onSave: ${e.message}")
                } catch (e: IllegalStateException) {
                    Timber.d("onSave: ${e.message}")
                }
            }
        }))
    }

    /**
     * Delete reminder backup file from GTasks Drive by file name.
     *
     * @param title file name.
     */
    @Throws(IOException::class)
    fun deleteReminderFileByName(title: String?) {
        val service = driveService ?: return
        if (!isLogged) return
        var titleStr = title ?: return
        Timber.d("deleteReminderFileByName: $titleStr")
        if (titleStr == "") {
            return
        }
        val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (strs.isNotEmpty()) {
            titleStr = strs[0]
        }
        val request = service.files().list()
                .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
        do {
            val files = request.execute()
            val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
            for (f in fileList) {
                val fileTitle = f.name
                if (fileTitle.endsWith(FileConfig.FILE_NAME_REMINDER)) {
                    service.files().delete(f.id).execute()
                }
            }
            request.pageToken = files.nextPageToken
        } while (request.pageToken != null && request.pageToken.length >= 0)
    }

    /**
     * Delete note backup file from GTasks Drive by file name.
     *
     * @param title file name.
     */
    @Throws(IOException::class)
    fun deleteNoteFileByName(title: String?) {
        val service = driveService ?: return
        if (!isLogged) return
        var titleStr = title ?: return
        val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (strs.isNotEmpty()) {
            titleStr = strs[0]
        }
        val request = service.files().list()
                .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
        do {
            val files = request.execute()
            val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
            for (f in fileList) {
                val fileTitle = f.name
                if (fileTitle.endsWith(FileConfig.FILE_NAME_NOTE)) {
                    service.files().delete(f.id).execute()
                }
            }
            request.pageToken = files.nextPageToken
        } while (request.pageToken != null && request.pageToken.length >= 0)
    }

    /**
     * Delete reminderGroup backup file from GTasks Drive by file name.
     *
     * @param title file name.
     */
    @Throws(IOException::class)
    fun deleteGroupFileByName(title: String?) {
        val service = driveService ?: return
        if (!isLogged) return
        var titleStr = title ?: return
        val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (strs.isNotEmpty()) {
            titleStr = strs[0]
        }
        val request = service.files().list()
                .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
        do {
            val files = request.execute()
            val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
            for (f in fileList) {
                val fileTitle = f.name
                if (fileTitle.endsWith(FileConfig.FILE_NAME_GROUP)) {
                    service.files().delete(f.id).execute()
                }
            }
            request.pageToken = files.nextPageToken
        } while (request.pageToken != null && request.pageToken.length >= 0)
    }

    /**
     * Delete birthday backup file from GTasks Drive by file name.
     *
     * @param title file name.
     */
    @Throws(IOException::class)
    fun deleteBirthdayFileByName(title: String?) {
        val service = driveService ?: return
        if (!isLogged) return
        var titleStr = title ?: return
        val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (strs.isNotEmpty()) {
            titleStr = strs[0]
        }
        val request = service.files().list()
                .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
        do {
            val files = request.execute()
            val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
            for (f in fileList) {
                val fileTitle = f.name
                if (fileTitle.endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
                    service.files().delete(f.id).execute()
                }
            }
            request.pageToken = files.nextPageToken
        } while (request.pageToken != null && request.pageToken.length >= 0)
    }

    /**
     * Delete place backup file from GTasks Drive by file name.
     *
     * @param title file name.
     */
    @Throws(IOException::class)
    fun deletePlaceFileByName(title: String?) {
        val service = driveService ?: return
        if (!isLogged) return
        var titleStr = title ?: return
        val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (strs.isNotEmpty()) {
            titleStr = strs[0]
        }
        val request = service.files().list()
                .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
        do {
            val files = request.execute()
            val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
            for (f in fileList) {
                val fileTitle = f.name
                if (fileTitle.endsWith(FileConfig.FILE_NAME_PLACE)) {
                    service.files().delete(f.id).execute()
                }
            }
            request.pageToken = files.nextPageToken
        } while (request.pageToken != null && request.pageToken.length >= 0)
    }

    @Throws(IOException::class)
    fun deleteTemplateFileByName(title: String?) {
        val service = driveService ?: return
        if (!isLogged) return
        var titleStr = title ?: return
        val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (strs.isNotEmpty()) {
            titleStr = strs[0]
        }
        val request = service.files().list()
                .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
        do {
            val files = request.execute()
            val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
            for (f in fileList) {
                val fileTitle = f.name
                if (fileTitle.endsWith(FileConfig.FILE_NAME_TEMPLATE)) {
                    service.files().delete(f.id).execute()
                }
            }
            request.pageToken = files.nextPageToken
        } while (request.pageToken != null && request.pageToken.length >= 0)
    }

    /**
     * Delete application folder from GTasks Drive.
     */
    @Throws(IOException::class)
    fun clean() {
        val service = driveService ?: return
        if (!isLogged) return
        val request = service.files().list()
                .setQ("mimeType = 'application/vnd.google-apps.folder' and name contains '$FOLDER_NAME'")
                ?: return
        do {
            val files = request.execute()
            val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
            for (f in fileList) {
                val fileMIME = f.mimeType
                if (fileMIME.contains("application/vnd.google-apps.folder") && f.name.contains(FOLDER_NAME)) {
                    service.files().delete(f.id).execute()
                    break
                }
            }
            request.pageToken = files.nextPageToken
        } while (request.pageToken != null && request.pageToken.length >= 0)
    }

    /**
     * Remove all backup files from app folder.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun cleanFolder() {
        val service = driveService ?: return
        if (!isLogged) return
        val request = service.files().list()
                .setQ("mimeType = 'text/plain' and (name contains '" + FileConfig.FILE_NAME_SETTINGS + "' " +
                        "or name contains '" + FileConfig.FILE_NAME_TEMPLATE + "' " +
                        "or name contains '" + FileConfig.FILE_NAME_PLACE + "' " +
                        "or name contains '" + FileConfig.FILE_NAME_BIRTHDAY + "' " +
                        "or name contains '" + FileConfig.FILE_NAME_NOTE + "' " +
                        "or name contains '" + FileConfig.FILE_NAME_GROUP + "' " +
                        "or name contains '" + FileConfig.FILE_NAME_REMINDER + "' " +
                        ")") ?: return
        do {
            val files = request.execute()
            val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
            for (f in fileList) {
                service.files().delete(f.id).execute()
            }
            request.pageToken = files.nextPageToken
        } while (request.pageToken != null && request.pageToken.length >= 0)
    }

    /**
     * Create application folder on GTasks Drive.
     *
     * @return Drive folder
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun createFolder(): File? {
        val service = driveService ?: return null
        if (!isLogged) return null
        val folder = File()
        folder.name = FOLDER_NAME
        folder.mimeType = "application/vnd.google-apps.folder"
        val folderInsert = service.files().create(folder)
        return folderInsert?.execute()
    }

    data class Metadata (val fileExt: String,
                         val folder: java.io.File?,
                         val meta: String?,
                         val action: Action?)

    interface Action {
        fun onSave(file: java.io.File)
    }

    companion object {
        private const val APPLICATION_NAME = "Reminder/6.0"
        private const val FOLDER_NAME = "Reminder"

        private var instance: GDrive? = null

        fun getInstance(context: Context): GDrive? {
            if (instance == null) {
                instance = GDrive(context)
            }
            return instance
        }
    }
}

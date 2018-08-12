package com.elementary.tasks.core.cloud

import android.content.Context
import android.text.TextUtils
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.backups.UserItem
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.LogUtil
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.Prefs
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.Data
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.tasks.Tasks
import com.google.api.services.tasks.TasksScopes
import com.google.api.services.tasks.model.Task
import com.google.api.services.tasks.model.TaskList
import com.google.api.services.tasks.model.TaskLists
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

class Google @Throws(IllegalStateException::class)
private constructor() {

    private var driveService: Drive? = null
    private var tasksService: Tasks? = null

    var tasks: GTasks? = null
        private set
    var drive: Drives? = null
        private set
    @Inject lateinit var mContext: Context
    @Inject lateinit var prefs: Prefs
    @Inject lateinit var backupTool: BackupTool

    init {
        ReminderApp.appComponent.inject(this)

        val user = prefs.driveUser
        if (user.matches(".*@.*".toRegex())) {
            val credential = GoogleAccountCredential.usingOAuth2(mContext, Arrays.asList(DriveScopes.DRIVE, TasksScopes.TASKS))
            credential.selectedAccountName = user
            val mJsonFactory = GsonFactory.getDefaultInstance()
            val mTransport = AndroidHttp.newCompatibleTransport()
            driveService = Drive.Builder(mTransport, mJsonFactory, credential).setApplicationName(APPLICATION_NAME).build()
            tasksService = com.google.api.services.tasks.Tasks.Builder(mTransport, mJsonFactory, credential).setApplicationName(APPLICATION_NAME).build()
            drive = Drives()
            tasks = GTasks()
        } else {
            logOut()
            throw IllegalArgumentException("Not logged to Google")
        }
    }

    internal fun logOut() {
        prefs.driveUser = Prefs.DRIVE_USER_NONE
        instance = null
    }

    inner class GTasks {

        val taskLists: TaskLists?
            @Throws(IOException::class)
            get() = if (tasksService == null) null else tasksService!!.tasklists().list().execute()

        @Throws(IOException::class)
        fun insertTask(item: GoogleTask): Boolean {
            if (TextUtils.isEmpty(item.title) || tasksService == null) {
                return false
            }
            try {
                val task = Task()
                task.title = item.title
                if (item.notes != "") {
                    task.notes = item.notes
                }
                if (item.dueDate != 0L) {
                    task.due = DateTime(item.dueDate)
                }
                val result: Task?
                val listId = item.listId
                if (!TextUtils.isEmpty(listId)) {
                    result = tasksService!!.tasks().insert(listId, task).execute()
                } else {
                    val googleTaskList = AppDb.getAppDatabase(mContext).googleTaskListsDao().defaultGoogleTaskList()
                    if (googleTaskList != null) {
                        item.listId = googleTaskList.listId
                        result = tasksService!!.tasks().insert(googleTaskList.listId, task).execute()
                    } else {
                        result = tasksService!!.tasks().insert("@default", task).execute()
                        val list = tasksService!!.tasklists().get("@default").execute()
                        if (list != null) {
                            item.listId = list.id
                        }
                    }
                }
                if (result != null) {
                    item.update(result)
                    AppDb.getAppDatabase(mContext).googleTasksDao().insert(item)
                    return true
                }
            } catch (e: IllegalArgumentException) {
                return false
            }

            return false
        }

        @Throws(IOException::class)
        fun updateTaskStatus(status: String, listId: String, taskId: String) {
            if (tasksService == null) return
            val task = tasksService!!.tasks().get(listId, taskId).execute()
            task.status = status
            if (status.matches(TASKS_NEED_ACTION.toRegex())) {
                task.completed = Data.NULL_DATE_TIME
            }
            task.updated = DateTime(System.currentTimeMillis())
            tasksService!!.tasks().update(listId, task.id, task).execute()
        }

        @Throws(IOException::class)
        fun deleteTask(item: GoogleTask) {
            if (item.listId == "" || tasksService == null) return
            tasksService!!.tasks().delete(item.listId, item.taskId).execute()
        }

        @Throws(IOException::class)
        fun updateTask(item: GoogleTask) {
            if (tasksService == null) return
            val task = tasksService!!.tasks().get(item.listId, item.taskId).execute()
            task.status = TASKS_NEED_ACTION
            task.title = item.title
            task.completed = Data.NULL_DATE_TIME
            if (item.dueDate != 0L) task.due = DateTime(item.dueDate)
            if (item.notes != "") task.notes = item.notes
            task.updated = DateTime(System.currentTimeMillis())
            tasksService!!.tasks().update(item.listId, task.id, task).execute()
        }

        fun getTasks(listId: String): List<Task> {
            var taskLists: List<Task> = ArrayList()
            if (tasksService == null) return taskLists
            try {
                taskLists = tasksService!!.tasks().list(listId).execute().items
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return taskLists ?: ArrayList()
        }

        fun insertTasksList(listTitle: String, color: Int) {
            if (tasksService == null) return
            val taskList = TaskList()
            taskList.title = listTitle
            try {
                val result = tasksService!!.tasklists().insert(taskList).execute()
                val item = GoogleTaskList(result, color)
                AppDb.getAppDatabase(mContext).googleTaskListsDao().insert(item)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        @Throws(IOException::class)
        fun updateTasksList(listTitle: String, listId: String?) {
            if (listId == null || tasksService == null) {
                return
            }
            val taskList = tasksService!!.tasklists().get(listId).execute()
            taskList.title = listTitle
            tasksService!!.tasklists().update(listId, taskList).execute()
            val item = AppDb.getAppDatabase(mContext).googleTaskListsDao().getById(listId)
            if (item != null) {
                item.update(taskList)
                AppDb.getAppDatabase(mContext).googleTaskListsDao().insert(item)
            }
        }

        fun deleteTaskList(listId: String?) {
            if (listId == null || tasksService == null) {
                return
            }
            try {
                tasksService!!.tasklists().delete(listId).execute()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        fun clearTaskList(listId: String?) {
            if (listId == null || tasksService == null) {
                return
            }
            try {
                tasksService!!.tasks().clear(listId).execute()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        fun moveTask(item: GoogleTask, oldList: String): Boolean {
            if (tasksService == null) {
                return false
            }
            try {
                val task = tasksService!!.tasks().get(oldList, item.taskId).execute()
                if (task != null) {
                    val clone = GoogleTask(item)
                    clone.listId = oldList
                    deleteTask(clone)
                    return insertTask(item)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return false
        }
    }

    inner class Drives {
        /**
         * Holder information about user.
         *
         * @return user info object
         */
        val data: UserItem?
            get() {
                if (driveService == null) return null
                try {
                    val about = driveService!!.about().get().setFields("user, storageQuota").execute()
                    val quota = about.storageQuota
                    return UserItem(about.user.displayName, quota.limit!!,
                            quota.usage!!, countFiles(), about.user.photoLink)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                return null
            }

        /**
         * Holder application folder identifier on Google Drive.
         *
         * @return Drive folder identifier.
         */
        private val folderId: String?
            @Throws(IOException::class, IllegalArgumentException::class)
            get() {
                if (driveService == null) return null
                val request = driveService!!.files().list()
                        .setQ("mimeType = 'application/vnd.google-apps.folder' and name contains '$FOLDER_NAME'")
                        ?: return null
                do {
                    val files = request.execute() ?: return null
                    val fileList = files.files as ArrayList<File>
                    for (f in fileList) {
                        val fileMIME = f.mimeType
                        if (fileMIME.trim { it <= ' ' }.contains("application/vnd.google-apps.folder") && f.name.contains(FOLDER_NAME)) {
                            LogUtil.d(TAG, "getFolderId: " + f.name + ", " + f.mimeType)
                            return f.id
                        }
                    }
                    request.pageToken = files.nextPageToken
                } while (request.pageToken != null && request.pageToken.length >= 0)
                val file = createFolder()
                return file?.id
            }

        /**
         * Count all backup files stored on Google Drive.
         *
         * @return number of files in local folder.
         */
        @Throws(IOException::class)
        private fun countFiles(): Int {
            var count = 0
            if (driveService == null) return 0
            val request = driveService!!.files().list().setQ("mimeType = 'text/plain'").setFields("nextPageToken, files")
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
            var foId: String? = null
            try {
                foId = folderId
            } catch (ignored: IllegalArgumentException) {
            }

            if (foId == null || driveService == null) {
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
                val req = driveService!!.files().create(fileMetadata, mediaContent)
                req.fields = "id"
                req.execute()
                break
            }
        }

        @Throws(IOException::class)
        private fun removeAllCopies(fileName: String) {
            if (driveService == null || TextUtils.isEmpty(fileName)) return
            val request = driveService!!.files().list()
                    .setQ("mimeType = 'text/plain' and name contains '$fileName'")
                    .setFields("nextPageToken, files")
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
                for (f in fileList) {
                    driveService!!.files().delete(f.id).execute()
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null)
        }

        @Throws(IOException::class)
        fun downloadSettings(context: Context, deleteFile: Boolean) {
            if (driveService == null) return
            val folder = MemoryUtil.prefsDir
            if (folder == null || !folder.exists() && !folder.mkdirs()) {
                return
            }
            val request = driveService!!.files().list()
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
                        driveService!!.files().get(f.id).executeMediaAndDownloadTo(out)
                        if (deleteFile) {
                            driveService!!.files().delete(f.id).execute()
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
                LogUtil.d(TAG, "saveTemplatesToDrive: " + e.localizedMessage)
            }
        }

        /**
         * Upload all reminder backup files stored on SD Card.
         */
        fun saveRemindersToDrive() {
            try {
                saveToDrive(Metadata(FileConfig.FILE_NAME_REMINDER, MemoryUtil.remindersDir, "Reminder Backup", null))
            } catch (e: IOException) {
                LogUtil.d(TAG, "saveRemindersToDrive: " + e.localizedMessage)
            }
        }

        fun saveReminderToDrive(pathToFile: String) {
            try {
                val metadata = Metadata(FileConfig.FILE_NAME_REMINDER, MemoryUtil.remindersDir, "Reminder Backup", null)
                saveFileToDrive(pathToFile, metadata)
            } catch (e: IOException) {
                LogUtil.d(TAG, "saveRemindersToDrive: " + e.localizedMessage)
            }
        }

        fun saveBirthdayToDrive(pathToFile: String) {
            try {
                val metadata = Metadata(FileConfig.FILE_NAME_BIRTHDAY, MemoryUtil.birthdaysDir, "Birthday Backup", null)
                saveFileToDrive(pathToFile, metadata)
            } catch (e: IOException) {
                LogUtil.d(TAG, "saveRemindersToDrive: " + e.localizedMessage)
            }
        }

        fun saveGroupToDrive(pathToFile: String) {
            try {
                val metadata = Metadata(FileConfig.FILE_NAME_GROUP, MemoryUtil.groupsDir, "Group Backup", null)
                saveFileToDrive(pathToFile, metadata)
            } catch (e: IOException) {
                LogUtil.d(TAG, "saveRemindersToDrive: " + e.localizedMessage)
            }
        }

        fun savePlaceToDrive(pathToFile: String) {
            try {
                val metadata = Metadata(FileConfig.FILE_NAME_PLACE, MemoryUtil.placesDir, "Place Backup", null)
                saveFileToDrive(pathToFile, metadata)
            } catch (e: IOException) {
                LogUtil.d(TAG, "saveRemindersToDrive: " + e.localizedMessage)
            }
        }

        fun saveNoteToDrive(pathToFile: String) {
            try {
                val metadata = Metadata(FileConfig.FILE_NAME_NOTE, MemoryUtil.notesDir, "Note Backup", null)
                saveFileToDrive(pathToFile, metadata)
            } catch (e: IOException) {
                LogUtil.d(TAG, "saveRemindersToDrive: " + e.localizedMessage)
            }
        }

        fun saveTemplateToDrive(pathToFile: String) {
            try {
                val metadata = Metadata(FileConfig.FILE_NAME_TEMPLATE, MemoryUtil.templatesDir, "Template Backup", null)
                saveFileToDrive(pathToFile, metadata)
            } catch (e: IOException) {
                LogUtil.d(TAG, "saveRemindersToDrive: " + e.localizedMessage)
            }
        }

        fun saveNotesToDrive() {
            try {
                saveToDrive(Metadata(FileConfig.FILE_NAME_NOTE, MemoryUtil.notesDir, "Note Backup", null))
            } catch (e: IOException) {
                LogUtil.d(TAG, "saveNotesToDrive: " + e.localizedMessage)
            }
        }

        /**
         * Upload all group backup files stored on SD Card.
         */
        fun saveGroupsToDrive() {
            try {
                saveToDrive(Metadata(FileConfig.FILE_NAME_GROUP, MemoryUtil.groupsDir, "Group Backup", null))
            } catch (e: IOException) {
                LogUtil.d(TAG, "saveGroupsToDrive: " + e.localizedMessage)
            }
        }

        /**
         * Upload all birthday backup files stored on SD Card.
         */
        fun saveBirthdaysToDrive() {
            try {
                saveToDrive(Metadata(FileConfig.FILE_NAME_BIRTHDAY, MemoryUtil.birthdaysDir, "Birthday Backup", null))
            } catch (e: IOException) {
                LogUtil.d(TAG, "saveBirthdaysToDrive: " + e.localizedMessage)
            }
        }

        /**
         * Upload all place backup files stored on SD Card.
         */
        fun savePlacesToDrive() {
            try {
                saveToDrive(Metadata(FileConfig.FILE_NAME_PLACE, MemoryUtil.placesDir, "Place Backup", null))
            } catch (e: IOException) {
                LogUtil.d(TAG, "savePlacesToDrive: " + e.localizedMessage)
            }
        }

        /**
         * Upload files from folder to Google Drive.
         *
         * @param metadata metadata.
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun saveToDrive(metadata: Metadata) {
            if (metadata.folder == null) return
            if (driveService == null) return
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
                driveService!!.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute()
            }
        }

        /**
         * Upload file from folder to Google Drive.
         *
         * @param metadata metadata.
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun saveFileToDrive(pathToFile: String, metadata: Metadata) {
            if (metadata.folder == null) return
            val driveService = driveService ?: return
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
            driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
        }

        @Throws(IOException::class)
        fun download(deleteBackup: Boolean, metadata: Metadata) {
            val driveService = driveService ?: return
            val folder = metadata.folder
            if (folder == null || !folder.exists() && !folder.mkdirs()) {
                return
            }
            val request = driveService.files().list()
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
                        driveService.files().get(f.id).executeMediaAndDownloadTo(out)
                        if (metadata.action != null) {
                            metadata.action.onSave(file)
                        }
                        if (deleteBackup) {
                            if (file.exists()) {
                                file.delete()
                            }
                            driveService.files().delete(f.id).execute()
                        }
                    }
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null)
        }

        /**
         * Download on SD Card all reminder backup files stored on Google Drive.
         *
         * @throws IOException
         */
        @Throws(IOException::class)
        fun downloadTemplates(deleteBackup: Boolean) {
            download(deleteBackup, Metadata(FileConfig.FILE_NAME_TEMPLATE, MemoryUtil.googleRemindersDir, null, object : Action {
                override fun onSave(file: java.io.File) {
                    try {
                        val item = backupTool.getTemplate(file.toString(), null)
                        if (item != null) AppDb.getAppDatabase(mContext).smsTemplatesDao().insert(item)
                    } catch (e: IOException) {
                        LogUtil.d(TAG, "downloadTemplates: " + e.localizedMessage)
                    } catch (e: IllegalStateException) {
                        LogUtil.d(TAG, "downloadTemplates: " + e.localizedMessage)
                    }
                }
            }))
        }

        /**
         * Download on SD Card all reminder backup files stored on Google Drive.
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
                        AppDb.getAppDatabase(mContext).reminderDao().insert(reminder)
                        val control = EventControlFactory.getController(reminder)
                        if (control.canSkip()) {
                            control.next()
                        } else {
                            control.start()
                        }
                    } catch (e: IOException) {
                        LogUtil.d(TAG, "downloadReminders: " + e.localizedMessage)
                    } catch (e: IllegalStateException) {
                        LogUtil.d(TAG, "downloadReminders: " + e.localizedMessage)
                    }
                }
            }))
        }

        /**
         * Download on SD Card all place backup files stored on Google Drive.
         *
         * @throws IOException
         */
        @Throws(IOException::class)
        fun downloadPlaces(deleteBackup: Boolean) {
            download(deleteBackup, Metadata(FileConfig.FILE_NAME_PLACE, MemoryUtil.googlePlacesDir, null, object : Action {
                override fun onSave(file: java.io.File) {
                    try {
                        val item = backupTool.getPlace(file.toString(), null)
                        if (item != null) AppDb.getAppDatabase(mContext).placesDao().insert(item)
                    } catch (e: IOException) {
                        LogUtil.d(TAG, "downloadPlaces: " + e.localizedMessage)
                    } catch (e: IllegalStateException) {
                        LogUtil.d(TAG, "downloadPlaces: " + e.localizedMessage)
                    }
                }
            }))
        }

        /**
         * Download on SD Card all note backup files stored on Google Drive.
         *
         * @throws IOException
         */
        @Throws(IOException::class)
        fun downloadNotes(deleteBackup: Boolean) {
            download(deleteBackup, Metadata(FileConfig.FILE_NAME_NOTE, MemoryUtil.googleNotesDir, null, object : Action {
                override fun onSave(file: java.io.File) {
                    try {
                        val item = backupTool.getNote(file.toString(), null)
                        if (item != null) AppDb.getAppDatabase(mContext).notesDao().insert(item)
                    } catch (e: IOException) {
                        LogUtil.d(TAG, "downloadNotes: " + e.localizedMessage)
                    } catch (e: IllegalStateException) {
                        LogUtil.d(TAG, "downloadNotes: " + e.localizedMessage)
                    }
                }
            }))
        }

        /**
         * Download on SD Card all group backup files stored on Google Drive.
         *
         * @throws IOException
         */
        @Throws(IOException::class)
        fun downloadGroups(deleteBackup: Boolean) {
            download(deleteBackup, Metadata(FileConfig.FILE_NAME_GROUP, MemoryUtil.googleGroupsDir, null, object : Action {
                override fun onSave(file: java.io.File) {
                    try {
                        val item = backupTool.getGroup(file.toString(), null)
                        if (item != null) AppDb.getAppDatabase(mContext).groupDao().insert(item)
                    } catch (e: IOException) {
                        LogUtil.d(TAG, "downloadGroups: " + e.localizedMessage)
                    } catch (e: IllegalStateException) {
                        LogUtil.d(TAG, "downloadGroups: " + e.localizedMessage)
                    }
                }
            }))
        }

        /**
         * Download on SD Card all birthday backup files stored on Google Drive.
         *
         * @throws IOException
         */
        @Throws(IOException::class)
        fun downloadBirthdays(deleteBackup: Boolean) {
            download(deleteBackup, Metadata(FileConfig.FILE_NAME_BIRTHDAY, MemoryUtil.googleBirthdaysDir, null, object : Action {
                override fun onSave(file: java.io.File) {
                    try {
                        val item = backupTool.getBirthday(file.toString(), null)
                        if (item != null) AppDb.getAppDatabase(mContext).birthdaysDao().insert(item)
                    } catch (e: IOException) {
                        LogUtil.d(TAG, "downloadBirthdays: " + e.localizedMessage)
                    } catch (e: IllegalStateException) {
                        LogUtil.d(TAG, "downloadBirthdays: " + e.localizedMessage)
                    }
                }
            }))
        }

        /**
         * Delete reminder backup file from Google Drive by file name.
         *
         * @param title file name.
         */
        @Throws(IOException::class)
        fun deleteReminderFileByName(title: String?) {
            var titleStr = title
            LogUtil.d(TAG, "deleteReminderFileByName: " + titleStr!!)
            if (titleStr == "" || driveService == null) {
                return
            }
            val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (strs.isNotEmpty()) {
                titleStr = strs[0]
            }
            val request = driveService!!.files().list()
                    .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
                for (f in fileList) {
                    val fileTitle = f.name
                    if (fileTitle.endsWith(FileConfig.FILE_NAME_REMINDER)) {
                        driveService!!.files().delete(f.id).execute()
                    }
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
        }

        /**
         * Delete note backup file from Google Drive by file name.
         *
         * @param title file name.
         */
        @Throws(IOException::class)
        fun deleteNoteFileByName(title: String?) {
            var titleStr = title
            if (titleStr == null || driveService == null) {
                return
            }
            val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (strs.isNotEmpty()) {
                titleStr = strs[0]
            }
            val request = driveService!!.files().list()
                    .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
                for (f in fileList) {
                    val fileTitle = f.name
                    if (fileTitle.endsWith(FileConfig.FILE_NAME_NOTE)) {
                        driveService!!.files().delete(f.id).execute()
                    }
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
        }

        /**
         * Delete group backup file from Google Drive by file name.
         *
         * @param title file name.
         */
        @Throws(IOException::class)
        fun deleteGroupFileByName(title: String?) {
            var titleStr = title
            if (titleStr == null || driveService == null) {
                return
            }
            val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (strs.isNotEmpty()) {
                titleStr = strs[0]
            }
            val request = driveService!!.files().list()
                    .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
                for (f in fileList) {
                    val fileTitle = f.name
                    if (fileTitle.endsWith(FileConfig.FILE_NAME_GROUP)) {
                        driveService!!.files().delete(f.id).execute()
                    }
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
        }

        /**
         * Delete birthday backup file from Google Drive by file name.
         *
         * @param title file name.
         */
        @Throws(IOException::class)
        fun deleteBirthdayFileByName(title: String?) {
            var titleStr = title
            if (titleStr == null || driveService == null) {
                return
            }
            val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (strs.isNotEmpty()) {
                titleStr = strs[0]
            }
            val request = driveService!!.files().list()
                    .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
                for (f in fileList) {
                    val fileTitle = f.name
                    if (fileTitle.endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
                        driveService!!.files().delete(f.id).execute()
                    }
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
        }

        /**
         * Delete place backup file from Google Drive by file name.
         *
         * @param title file name.
         */
        @Throws(IOException::class)
        fun deletePlaceFileByName(title: String?) {
            var titleStr = title
            if (titleStr == null || driveService == null) {
                return
            }
            val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (strs.isNotEmpty()) {
                titleStr = strs[0]
            }
            val request = driveService!!.files().list()
                    .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
                for (f in fileList) {
                    val fileTitle = f.name
                    if (fileTitle.endsWith(FileConfig.FILE_NAME_PLACE)) {
                        driveService!!.files().delete(f.id).execute()
                    }
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
        }

        @Throws(IOException::class)
        fun deleteTemplateFileByName(title: String?) {
            var titleStr = title
            if (titleStr == null || driveService == null) {
                return
            }
            val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (strs.isNotEmpty()) {
                titleStr = strs[0]
            }
            val request = driveService!!.files().list()
                    .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
                for (f in fileList) {
                    val fileTitle = f.name
                    if (fileTitle.endsWith(FileConfig.FILE_NAME_TEMPLATE)) {
                        driveService!!.files().delete(f.id).execute()
                    }
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
        }

        /**
         * Delete application folder from Google Drive.
         */
        @Throws(IOException::class)
        fun clean() {
            if (driveService == null) return
            val request = driveService!!.files().list()
                    .setQ("mimeType = 'application/vnd.google-apps.folder' and name contains '$FOLDER_NAME'")
                    ?: return
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<com.google.api.services.drive.model.File>
                for (f in fileList) {
                    val fileMIME = f.mimeType
                    if (fileMIME.contains("application/vnd.google-apps.folder") && f.name.contains(FOLDER_NAME)) {
                        driveService!!.files().delete(f.id).execute()
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
            if (driveService == null) return
            val request = driveService!!.files().list()
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
                    driveService!!.files().delete(f.id).execute()
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
        }

        /**
         * Create application folder on Google Drive.
         *
         * @return Drive folder
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun createFolder(): File? {
            if (driveService == null) return null
            val folder = File()
            folder.name = FOLDER_NAME
            folder.mimeType = "application/vnd.google-apps.folder"
            val folderInsert = driveService!!.files().create(folder)
            return folderInsert?.execute()
        }

        inner class Metadata internal constructor(internal val fileExt: String,
                                                  internal val folder: java.io.File?,
                                                  internal val meta: String?,
                                                  val action: Action?)
    }

    interface GoogleTaksFunc {
        fun apply(googleTask: GoogleTask?)
    }

    interface GoogleTaksListFunc {
        fun apply(googleTaskList: GoogleTaskList?)
    }

    internal interface Action {
        fun onSave(file: java.io.File)
    }

    companion object {

        const val TASKS_NEED_ACTION = "needsAction"
        const val TASKS_COMPLETE = "completed"
        private const val TAG = "Google"
        private const val APPLICATION_NAME = "Reminder/6.0"
        private const val FOLDER_NAME = "Reminder"

        private var instance: Google? = null

        fun getInstance(): Google? {
            try {
                instance = Google()
            } catch (e: IllegalArgumentException) {
                LogUtil.d(TAG, "getInstance: " + e.localizedMessage)
            } catch (e: NullPointerException) {
                LogUtil.d(TAG, "getInstance: " + e.localizedMessage)
            }
            return instance
        }
    }
}

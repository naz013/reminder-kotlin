package com.elementary.tasks.core.cloud.storages

import android.content.Context
import android.text.TextUtils
import com.crashlytics.android.Crashlytics
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.groups.GroupsUtil
import com.elementary.tasks.navigation.settings.export.backups.UserItem
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class GDrive private constructor(context: Context) : Storage(), KoinComponent {

    private var driveService: Drive? = null

    private val appDb: AppDb by inject()
    private val prefs: Prefs by inject()
    private val backupTool: BackupTool by inject()
    private val tokenDataFile = TokenDataFile()
    private val indexDataFile = IndexDataFile()

    var statusObserver: ((Boolean) -> Unit)? = null
    var isLogged: Boolean = false
        private set

    init {
        val user = prefs.driveUser
        if (SuperUtil.isGooglePlayServicesAvailable(context) && user.matches(".*@.*".toRegex())) {
            Timber.d("GDrive: user -> $user")
            val credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_APPDATA))
            credential.selectedAccountName = user
            driveService = Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory(), credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build()
            isLogged = true
            statusObserver?.invoke(true)
            if (!indexDataFile.isLoaded) {
                loadIndexFile()
            }
            if (!tokenDataFile.isLoaded) {
                loadTokenFile()
            }
        } else {
            logOut()
        }
    }

    override suspend fun backup(json: String, metadata: com.elementary.tasks.core.cloud.converters.Metadata) {
        val service = driveService ?: return
        if (!isLogged) return
        if (TextUtils.isEmpty(metadata.fileName)) return
        try {
            removeAllCopies(metadata.fileName)
            val fileMetadata = File()
            fileMetadata.name = metadata.fileName
            fileMetadata.description = metadata.meta
            fileMetadata.parents = PARENTS
            val mediaContent = InputStreamContent("text/plain", ByteArrayInputStream(json.toByteArray()))
            val driveFile = service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
            Timber.d("saveFileToDrive: ${driveFile.id}")
        } catch (e: java.lang.Exception) {
        }
    }

    override suspend fun restore(fileName: String): String? {
        val service = driveService ?: return null
        if (!isLogged) return null
        try {
            val request = service.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("mimeType = 'text/plain' and name contains '$fileName'")
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<File>
                for (f in fileList) {
                    Timber.d("download: ${f.name}, ${f.id}")
                    val title = f.name
                    if (title == fileName) {
                        val out = ByteArrayOutputStream()
                        service.files().get(f.id).executeMediaAndDownloadTo(out)
                        return out.toString()
                    }
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null)
        } catch (e: Exception) {
            return null
        }
        return null
    }

    override suspend fun delete(fileName: String) {
        removeAllCopies(fileName)
    }

    override fun hasIndex(id: String): Boolean {
        return indexDataFile.hasIndex(id)
    }

    override fun saveIndex(fileIndex: FileIndex) {
        indexDataFile.addIndex(fileIndex)
        saveIndexFile()
        tokenDataFile.notifyDevices()
    }

    override fun removeIndex(id: String) {
        indexDataFile.removeIndex(id)
        saveIndexFile()
    }

    override fun needBackup(id: String, updatedAt: String): Boolean {
        return indexDataFile.isFileChanged(id, updatedAt)
    }

    private fun loadTokenFile() {

    }

    private fun saveTokenFile() {

    }

    private fun loadIndexFile() {

    }

    private fun saveIndexFile() {

    }

    fun updateToken(token: String?) {
        if (token == null) return
        tokenDataFile.addDevice(token)
        saveTokenFile()
    }

    fun logOut() {
        prefs.driveUser = Prefs.DRIVE_USER_NONE
        driveService = null
        isLogged = false
        statusObserver?.invoke(false)
        instance = null
    }

    val data: UserItem?
        get() {
            val service = driveService ?: return null
            if (!isLogged) return null
            try {
                val about = service.about().get().setFields("user, storageQuota").execute() ?: return null
                val quota = about.storageQuota ?: return null
                return UserItem(name = about.user.displayName ?: "", quota = quota.limit,
                        used = quota.usage, count = countFiles(), photo = about.user.photoLink ?: "")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

    private fun countFiles(): Int {
        val service = driveService ?: return 0
        if (!isLogged) return 0
        var count = 0
        val request = service.files().list()
                .setSpaces("appDataFolder")
                .setFields("nextPageToken, files(id, name)")
                .setQ("mimeType = 'text/plain'") ?: return 0
        try {
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<File>
                for (f in fileList) {
                    val title = f.name
                    Timber.d("countFiles: $title")
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
        } catch (e: Exception) {
        }
        return count
    }

    fun saveSettingsToDrive() {
        val service = driveService ?: return
        if (!isLogged) return
        val folder = MemoryUtil.prefsDir ?: return
        val files = folder.listFiles() ?: return
        try {
            for (file in files) {
                if (!file.toString().endsWith(FileConfig.FILE_NAME_SETTINGS)) continue
                removeAllCopies(file.name)
                val fileMetadata = File()
                fileMetadata.name = file.name
                fileMetadata.description = "Settings Backup"
                fileMetadata.parents = PARENTS
                val mediaContent = FileContent("text/plain", file)
                val req = service.files().create(fileMetadata, mediaContent)
                req.fields = "id"
                req.execute()
                break
            }
        } catch (e: Exception) {
        }
    }

    private fun removeAllCopies(fileName: String) {
        val service = driveService ?: return
        if (!isLogged) return
        if (TextUtils.isEmpty(fileName)) return
        try {
            val request = service.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("mimeType = 'text/plain' and name contains '$fileName'")
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<File>
                for (f in fileList) {
                    service.files().delete(f.id).execute()
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun downloadSettings(deleteFile: Boolean) {
        val service = driveService ?: return
        if (!isLogged) return
        val folder = MemoryUtil.prefsDir
        if (folder == null || !folder.exists() && !folder.mkdirs()) {
            return
        }
        try {
            val request = service.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("mimeType = 'text/plain' and name contains '" + FileConfig.FILE_NAME_SETTINGS + "'")
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<File>
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
        } catch (e: Exception) {
        }
    }

    fun saveTemplatesToDrive() {
        try {
            saveToDrive(Metadata(FileConfig.FILE_NAME_TEMPLATE, MemoryUtil.templatesDir, "Template Backup", null))
        } catch (e: IOException) {
            Timber.d("saveTemplatesToDrive: ${e.message}")
        }
    }

    fun saveRemindersToDrive() {
        try {
            saveToDrive(Metadata(FileConfig.FILE_NAME_REMINDER, MemoryUtil.remindersDir, "Reminder Backup", null))
        } catch (e: IOException) {
            Timber.d("saveRemindersToDrive: ${e.message}")
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

    fun saveGroupsToDrive() {
        try {
            saveToDrive(Metadata(FileConfig.FILE_NAME_GROUP, MemoryUtil.groupsDir, "ReminderGroup Backup", null))
        } catch (e: IOException) {
            Timber.d("saveGroupsToDrive: ${e.message}")
        }
    }

    fun saveBirthdaysToDrive() {
        try {
            saveToDrive(Metadata(FileConfig.FILE_NAME_BIRTHDAY, MemoryUtil.birthdaysDir, "Birthday Backup", null))
        } catch (e: IOException) {
            Timber.d("saveBirthdaysToDrive: ${e.message}")
        }
    }

    fun savePlacesToDrive() {
        try {
            saveToDrive(Metadata(FileConfig.FILE_NAME_PLACE, MemoryUtil.placesDir, "Place Backup", null))
        } catch (e: IOException) {
            Timber.d("savePlacesToDrive: ${e.message}")
        }
    }

    private fun saveToDrive(metadata: Metadata) {
        if (metadata.folder == null) return
        val service = driveService ?: return
        if (!isLogged) return
        try {
            val files = metadata.folder.listFiles() ?: return
            for (file in files) {
                if (!file.name.endsWith(metadata.fileExt)) continue
                removeAllCopies(file.name)
                val fileMetadata = File()
                fileMetadata.name = file.name
                fileMetadata.description = metadata.meta
                fileMetadata.parents = PARENTS
                val mediaContent = FileContent("text/plain", file)
                val driveFile = service.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute()
                Timber.d("saveToDrive: ${driveFile.id}")
            }
        } catch (e: java.lang.Exception) {
            Crashlytics.logException(e)
        }
    }

    private fun saveFileToDrive(pathToFile: String, metadata: Metadata) {
        if (metadata.folder == null) return
        val service = driveService ?: return
        if (!isLogged) return
        val f = java.io.File(pathToFile)
        if (!f.exists()) {
            return
        }
        val name = f.name
        if (TextUtils.isEmpty(name)) return
        if (!name.endsWith(metadata.fileExt)) return
        try {
            removeAllCopies(name)
            val fileMetadata = File()
            fileMetadata.name = name
            fileMetadata.description = metadata.meta
            fileMetadata.parents = PARENTS
            val mediaContent = FileContent("text/plain", f)
            val driveFile = service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
            Timber.d("saveFileToDrive: ${driveFile.id}")
        } catch (e: java.lang.Exception) {
        }
    }

    fun download(deleteBackup: Boolean, metadata: Metadata) {
        val service = driveService ?: return
        if (!isLogged) return
        val folder = metadata.folder
        if (folder == null || !folder.exists() && !folder.mkdirs()) {
            return
        }
        try {
            val request = service.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("mimeType = 'text/plain' and name contains '" + metadata.fileExt + "'")
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<File>
                for (f in fileList) {
                    Timber.d("download: ${f.name}, ${f.id}")
                    val title = f.name
                    if (title.endsWith(metadata.fileExt)) {
                        val file = java.io.File(folder, title)
                        if (!file.exists()) {
                            file.createNewFile()
                        }
                        val out = FileOutputStream(file)
                        service.files().get(f.id).executeMediaAndDownloadTo(out)
                        if (metadata.action != null) {
                            metadata.action.onSave(file, f.id, f.name)
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
        } catch (e: Exception) {
        }
    }

    fun downloadTemplates(deleteBackup: Boolean) {
        download(deleteBackup, Metadata(FileConfig.FILE_NAME_TEMPLATE, MemoryUtil.googleRemindersDir, null, object : Action {
            override fun onSave(file: java.io.File, id: String, name: String) {
                try {
                    val item = backupTool.getTemplate(file.toString(), null)
                    if (item != null) {
                        appDb.smsTemplatesDao().insert(item)
                    } else {
                        if (name.endsWith(FileConfig.FILE_NAME_TEMPLATE)) {
                            deleteTemplateFileByName(name)
                        }
                    }
                } catch (e: IOException) {
                    Timber.d("onSave: ${e.message}")
                } catch (e: IllegalStateException) {
                    Timber.d("onSave: ${e.message}")
                }
            }
        }))
    }

    fun downloadReminders(deleteBackup: Boolean) {
        val dao = appDb.reminderDao()
        val groups = GroupsUtil.mapAll(appDb)
        val defGroup = appDb.reminderGroupDao().defaultGroup() ?: groups.values.first()
        download(deleteBackup, Metadata(FileConfig.FILE_NAME_REMINDER, MemoryUtil.googleRemindersDir, null, object : Action {
            override fun onSave(file: java.io.File, id: String, name: String) {
                try {
                    val reminder = backupTool.getReminder(file.toString(), null)
                    if (reminder == null) {
                        if (name.endsWith(FileConfig.FILE_NAME_REMINDER)) {
                            deleteReminderFileByName(name)
                        }
                        return
                    }
                    if (!groups.containsKey(reminder.groupUuId)) {
                        reminder.apply {
                            this.groupTitle = defGroup.groupTitle
                            this.groupUuId = defGroup.groupUuId
                            this.groupColor = defGroup.groupColor
                        }
                    }
                    if (!reminder.isActive || reminder.isRemoved) {
                        reminder.isActive = false
                    }
                    if (!Reminder.isGpsType(reminder.type) && !TimeCount.isCurrent(reminder.eventTime)) {
                        if (!Reminder.isSame(reminder.type, Reminder.BY_DATE_SHOP) || reminder.hasReminder) {
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
                } catch (e: IOException) {
                    Timber.d("onSave: ${e.message}")
                } catch (e: IllegalStateException) {
                    Timber.d("onSave: ${e.message}")
                }
            }
        }))
        Timber.d("downloadReminders: ${dao.all().size}")
    }

    fun downloadPlaces(deleteBackup: Boolean) {
        download(deleteBackup, Metadata(FileConfig.FILE_NAME_PLACE, MemoryUtil.googlePlacesDir, null, object : Action {
            override fun onSave(file: java.io.File, id: String, name: String) {
                try {
                    val item = backupTool.getPlace(file.toString(), null)
                    if (item != null) {
                        appDb.placesDao().insert(item)
                    } else {
                        if (name.endsWith(FileConfig.FILE_NAME_PLACE)) {
                            deletePlaceFileByName(name)
                        }
                    }
                } catch (e: IOException) {
                    Timber.d("onSave: ${e.message}")
                } catch (e: IllegalStateException) {
                    Timber.d("onSave: ${e.message}")
                }
            }
        }))
    }

    fun downloadNotes(deleteBackup: Boolean) {
        download(deleteBackup, Metadata(FileConfig.FILE_NAME_NOTE, MemoryUtil.googleNotesDir, null, object : Action {
            override fun onSave(file: java.io.File, id: String, name: String) {
                try {
                    val item = backupTool.getNote(file.toString(), null)
                    val note = item?.note
                    if (item != null && note != null) {
                        appDb.notesDao().insert(note)
                        appDb.notesDao().insertAll(item.images)
                    } else {
                        if (name.endsWith(FileConfig.FILE_NAME_NOTE)) {
                            deleteNoteFileByName(name)
                        }
                    }
                } catch (e: IOException) {
                    Timber.d("onSave: ${e.message}")
                } catch (e: IllegalStateException) {
                    Timber.d("onSave: ${e.message}")
                }
            }
        }))
    }

    fun downloadGroups(deleteBackup: Boolean) {
        val dao = appDb.reminderGroupDao()
        download(deleteBackup, Metadata(FileConfig.FILE_NAME_GROUP, MemoryUtil.googleGroupsDir, null, object : Action {
            override fun onSave(file: java.io.File, id: String, name: String) {
                try {
                    val item = backupTool.getGroup(file.toString(), null)
                    if (item != null) {
                        dao.insert(item)
                    } else {
                        if (name.endsWith(FileConfig.FILE_NAME_GROUP)) {
                            deleteGroupFileByName(name)
                        }
                    }
                } catch (e: IOException) {
                    Timber.d("onSave: ${e.message}")
                } catch (e: IllegalStateException) {
                    Timber.d("onSave: ${e.message}")
                }
            }
        }))
        Timber.d("downloadGroups: ${dao.all().size}")
    }

    fun downloadBirthdays(deleteBackup: Boolean) {
        download(deleteBackup, Metadata(FileConfig.FILE_NAME_BIRTHDAY, MemoryUtil.googleBirthdaysDir, null, object : Action {
            override fun onSave(file: java.io.File, id: String, name: String) {
                try {
                    val item = backupTool.getBirthday(file.toString(), null)
                    if (item != null) {
                        launchDefault { appDb.birthdaysDao().insert(item) }
                    } else {
                        if (name.endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
                            deleteBirthdayFileByName(name)
                        }
                    }
                } catch (e: IOException) {
                    Timber.d("onSave: ${e.message}")
                } catch (e: IllegalStateException) {
                    Timber.d("onSave: ${e.message}")
                }
            }
        }))
    }

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
        try {
            val request = service.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<File>
                for (f in fileList) {
                    val fileTitle = f.name
                    if (fileTitle.endsWith(FileConfig.FILE_NAME_REMINDER)) {
                        service.files().delete(f.id).execute()
                    }
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
        } catch (e: Exception) {
        }
    }

    fun deleteNoteFileByName(title: String?) {
        val service = driveService ?: return
        if (!isLogged) return
        var titleStr = title ?: return
        val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (strs.isNotEmpty()) {
            titleStr = strs[0]
        }
        try {
            val request = service.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<File>
                for (f in fileList) {
                    val fileTitle = f.name
                    if (fileTitle.endsWith(FileConfig.FILE_NAME_NOTE)) {
                        service.files().delete(f.id).execute()
                    }
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
        } catch (e: Exception) {
        }
    }

    fun deleteGroupFileByName(title: String?) {
        val service = driveService ?: return
        if (!isLogged) return
        var titleStr = title ?: return
        val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (strs.isNotEmpty()) {
            titleStr = strs[0]
        }
        try {
            val request = service.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<File>
                for (f in fileList) {
                    val fileTitle = f.name
                    if (fileTitle.endsWith(FileConfig.FILE_NAME_GROUP)) {
                        service.files().delete(f.id).execute()
                    }
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
        } catch (e: Exception) {
        }
    }

    fun deleteBirthdayFileByName(title: String?) {
        val service = driveService ?: return
        if (!isLogged) return
        var titleStr = title ?: return
        val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (strs.isNotEmpty()) {
            titleStr = strs[0]
        }
        try {
            val request = service.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<File>
                for (f in fileList) {
                    val fileTitle = f.name
                    if (fileTitle.endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
                        service.files().delete(f.id).execute()
                    }
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
        } catch (e: Exception) {
        }
    }

    fun deletePlaceFileByName(title: String?) {
        val service = driveService ?: return
        if (!isLogged) return
        var titleStr = title ?: return
        val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (strs.isNotEmpty()) {
            titleStr = strs[0]
        }
        try {
            val request = service.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<File>
                for (f in fileList) {
                    val fileTitle = f.name
                    if (fileTitle.endsWith(FileConfig.FILE_NAME_PLACE)) {
                        service.files().delete(f.id).execute()
                    }
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
        } catch (e: Exception) {
        }
    }

    fun deleteTemplateFileByName(title: String?) {
        val service = driveService ?: return
        if (!isLogged) return
        var titleStr = title ?: return
        val strs = titleStr.split(".".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (strs.isNotEmpty()) {
            titleStr = strs[0]
        }
        try {
            val request = service.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)")
                    .setQ("mimeType = 'text/plain' and name contains '$titleStr'") ?: return
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<File>
                for (f in fileList) {
                    val fileTitle = f.name
                    if (fileTitle.endsWith(FileConfig.FILE_NAME_TEMPLATE)) {
                        service.files().delete(f.id).execute()
                    }
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
        } catch (e: Exception) {
        }
    }

    fun clean() {
        val service = driveService ?: return
        if (!isLogged) return
        try {
            val request = service.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name)") ?: return
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<File>
                for (f in fileList) {
                    service.files().delete(f.id).execute()
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
        } catch (e: java.lang.Exception) {
        }
    }

    fun cleanFolder() {
        val service = driveService ?: return
        if (!isLogged) return
        val request = service.files().list()
                .setSpaces("appDataFolder")
                .setFields("nextPageToken, files(id, name)") ?: return
        try {
            do {
                val files = request.execute()
                val fileList = files.files as ArrayList<File>
                for (f in fileList) {
                    service.files().delete(f.id).execute()
                }
                request.pageToken = files.nextPageToken
            } while (request.pageToken != null && request.pageToken.length >= 0)
        } catch (e: java.lang.Exception) {
        }
    }

    companion object {
        const val APPLICATION_NAME = "Reminder/7.0"
        private val PARENTS = Collections.singletonList("appDataFolder")

        private var instance: GDrive? = null

        fun getInstance(context: Context): GDrive? {
            if (instance == null) {
                instance = GDrive(context)
            }
            return instance
        }
    }
}

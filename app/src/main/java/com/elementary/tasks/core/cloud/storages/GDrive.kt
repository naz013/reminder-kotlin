package com.elementary.tasks.core.cloud.storages

import android.content.Context
import android.text.TextUtils
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.navigation.settings.export.backups.UserItem
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.channels.Channel
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

class GDrive private constructor(context: Context) : Storage(), KoinComponent {

    private var driveService: Drive? = null

    private val prefs: Prefs by inject()
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
                val filesResult = request.execute()
                val fileList = filesResult.files as ArrayList<File>
                for (f in fileList) {
                    Timber.d("restore: ${f.name}, ${f.id}, $fileName")
                    val title = f.name
                    if (title == fileName) {
                        val out = ByteArrayOutputStream()
                        service.files().get(f.id).executeMediaAndDownloadTo(out)
                        return out.toString()
                    }
                }
                request.pageToken = filesResult.nextPageToken
            } while (request.pageToken != null)
        } catch (e: Exception) {
            return null
        }
        return null
    }

    override fun restoreAll(ext: String, deleteFile: Boolean): Channel<String> {
        val channel = Channel<String>()
        val service = driveService
        if (service == null) {
            channel.cancel()
            return channel
        }
        if (!isLogged) {
            channel.cancel()
            return channel
        }
        launchIo {
            try {
                val request = service.files().list()
                        .setSpaces("appDataFolder")
                        .setFields("nextPageToken, files(id, name)")
                        .setQ("mimeType = 'text/plain' and name contains '$ext'")
                do {
                    val filesResult = request.execute()
                    val fileList = filesResult.files as ArrayList<File>
                    for (f in fileList) {
                        Timber.d("restoreAll: ${f.name}, ${f.id}, $ext")
                        val title = f.name
                        if (title.endsWith(ext)) {
                            val out = ByteArrayOutputStream()
                            service.files().get(f.id).executeMediaAndDownloadTo(out)
                            channel.send(out.toString())
                            if (deleteFile) {
                                service.files().delete(f.id).execute()
                            }
                        }
                    }
                    request.pageToken = filesResult.nextPageToken
                } while (request.pageToken != null)
            } catch (e: Exception) {
            }
            channel.close()
        }
        return channel
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
        launchDefault {
            val json = restore(TokenDataFile.FILE_NAME)
            tokenDataFile.parse(json)
        }
    }

    private fun saveTokenFile() {
        launchDefault {
            val json = tokenDataFile.toJson() ?: return@launchDefault
            backup(json, com.elementary.tasks.core.cloud.converters.Metadata(
                    "",
                    TokenDataFile.FILE_NAME,
                    FileConfig.FILE_NAME_JSON,
                    TimeUtil.gmtDateTime,
                    "Token file"
            ))
        }
    }

    private fun loadIndexFile() {
        launchDefault {
            val json = restore(IndexDataFile.FILE_NAME)
            indexDataFile.parse(json)
        }
    }

    private fun saveIndexFile() {
        launchDefault {
            val json = indexDataFile.toJson() ?: return@launchDefault
            backup(json, com.elementary.tasks.core.cloud.converters.Metadata(
                    "",
                    IndexDataFile.FILE_NAME,
                    FileConfig.FILE_NAME_JSON,
                    TimeUtil.gmtDateTime,
                    "Index file"
            ))
        }
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

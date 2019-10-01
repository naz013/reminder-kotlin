package com.elementary.tasks.core.cloud.storages

import android.content.Context
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.http.OkHttp3Requestor
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.dropbox.core.v2.users.FullAccount
import com.dropbox.core.v2.users.SpaceUsage
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.converters.Metadata
import com.elementary.tasks.core.utils.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.channels.Channel
import okhttp3.OkHttpClient
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.InputStream

class Dropbox : Storage(), KoinComponent {

    private val rootFolder = "/"
    private val reminderFolder = "/Reminders/"
    private val noteFolder = "/Notes/"
    private val groupFolder = "/Groups/"
    private val birthFolder = "/Birthdays/"
    private val placeFolder = "/Places/"
    private val templateFolder = "/Templates/"
    private val settingsFolder = "/Settings/"

    private var mDBApi: DbxClientV2? = null
    private val prefs: Prefs by inject()

    private val tokenDataFile = TokenDataFile()
    private val indexDataFile = IndexDataFile()

    val isLinked: Boolean
        get() = mDBApi != null && prefs.dropboxToken != ""

    init {
        startSession()
    }

    override suspend fun backup(fileIndex: FileIndex, metadata: Metadata) {
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        val stream = fileIndex.stream
        if (stream == null) {
            return
        } else {
            val folder = folderFromExt(metadata.fileExt)
            Timber.d("backup: ${metadata.fileName}, $folder")
            val fis = ByteArrayInputStream(stream.toByteArray())
            try {
                api.files().uploadBuilder(folder + metadata.fileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(fis)
                fis.close()
                stream.close()
            } catch (e: Exception) {
            } catch (e: OutOfMemoryError) {
            }
        }
    }

    private fun backup(json: String, metadata: Metadata) {
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        val folder = folderFromExt(metadata.fileExt)
        Timber.d("backup: ${metadata.fileName}, $folder")
        val fis = ByteArrayInputStream(json.toByteArray())
        try {
            api.files().uploadBuilder(folder + metadata.fileName)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(fis)
            fis.close()
        } catch (e: Exception) {
        } catch (e: OutOfMemoryError) {
        }
    }

    override suspend fun restore(fileName: String): InputStream? {
        if (!isLinked) {
            return null
        }
        val api = mDBApi ?: return null
        val folder = folderFromFileName(fileName)
        Timber.d("restore: $fileName, $folder")
        return try {
            api.files().download(folder + fileName).inputStream
        } catch (e: Exception) {
            Timber.d("restore: ${e.message}")
            null
        }
    }

    override fun restoreAll(ext: String, deleteFile: Boolean): Channel<InputStream> {
        val channel = Channel<InputStream>()
        if (!isLinked) {
            channel.cancel()
            return channel
        }
        val api = mDBApi
        if (api == null) {
            channel.cancel()
            return channel
        }
        val folder = folderFromExt(ext)
        Timber.d("restoreAll: $ext, $folder")
        launchIo {
            try {
                val result = api.files().listFolder(templateFolder)
                if (result != null) {
                    for (e in result.entries) {
                        val fileName = e.name
                        channel.send(api.files().download(folder + fileName).inputStream)
                        if (deleteFile) {
                            api.files().deleteV2(e.pathLower)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.d("restoreAll: ${e.message}")
            }
            channel.close()
        }
        return channel
    }

    override suspend fun delete(fileName: String) {
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        Timber.d("delete: $fileName")
        val folder = folderFromFileName(fileName)
        try {
            api.files().deleteV2(folder + fileName)
        } catch (e: DbxException) {
            Timber.d("delete: ${e.message}")
        }
    }

    override fun hasIndex(id: String): Boolean {
        return indexDataFile.hasIndex(id)
    }

    override fun needBackup(id: String, updatedAt: String): Boolean {
        return indexDataFile.isFileChanged(id, updatedAt)
    }

    override fun removeIndex(id: String) {
        indexDataFile.removeIndex(id)
        saveIndexFile()
    }

    override fun saveIndex(fileIndex: FileIndex) {
        indexDataFile.addIndex(fileIndex)
        saveIndexFile()
        sendNotification("file", fileIndex.id + fileIndex.ext)
    }

    override suspend fun loadIndex() {
        loadIndexFile()
        loadTokenFile()
    }

    override suspend fun saveIndex() {
        saveIndexFile()
    }

    override fun sendNotification(type: String, details: String) {
        tokenDataFile.notifyDevices(type, details)
    }

    private suspend fun loadTokenFile() {
        if (tokenDataFile.isLoading) return
        if (!tokenDataFile.isEmpty() && !tokenDataFile.isOld()) {
            return
        }
        tokenDataFile.isLoading = true
        val inputStream = restore(TokenDataFile.FILE_NAME) ?: return
        tokenDataFile.parse(inputStream)
        withUIContext {
            if (prefs.multiDeviceModeEnabled) {
                FirebaseInstanceId.getInstance().instanceId
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                return@OnCompleteListener
                            }
                            val token = task.result?.token
                            updateToken(token)
                        })
            }
        }
    }

    private fun saveTokenFile() {
        launchDefault {
            val json = tokenDataFile.toJson() ?: return@launchDefault
            backup(json, Metadata(
                    "",
                    TokenDataFile.FILE_NAME,
                    FileConfig.FILE_NAME_JSON,
                    TimeUtil.gmtDateTime,
                    "Token file"
            ))
        }
    }

    private suspend fun loadIndexFile() {
        val inputStream = restore(IndexDataFile.FILE_NAME)
        indexDataFile.parse(inputStream)
    }

    private fun saveIndexFile() {
        launchDefault {
            val json = indexDataFile.toJson() ?: return@launchDefault
            backup(json, Metadata(
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
        if (tokenDataFile.addDevice(token)) {
            if (!tokenDataFile.isLoading) saveTokenFile()
        }
    }

    private fun folderFromFileName(fileName: String): String {
        if (fileName.isEmpty()) return reminderFolder
        val parts = fileName.split(".".toRegex())
        if (parts.size < 2) {
            return reminderFolder
        }
        return folderFromExt(".${parts[1]}")
    }

    private fun folderFromExt(ext: String): String {
        return when(ext) {
            FileConfig.FILE_NAME_NOTE -> noteFolder
            FileConfig.FILE_NAME_GROUP -> groupFolder
            FileConfig.FILE_NAME_BIRTHDAY -> birthFolder
            FileConfig.FILE_NAME_PLACE -> placeFolder
            FileConfig.FILE_NAME_TEMPLATE -> templateFolder
            FileConfig.FILE_NAME_SETTINGS_EXT -> settingsFolder
            FileConfig.FILE_NAME_JSON -> rootFolder
            else -> reminderFolder
        }
    }

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

        if (!indexDataFile.isLoaded) {
            launchDefault { loadIndexFile() }
        }
        if (!tokenDataFile.isLoaded) {
            launchDefault { loadTokenFile() }
        }
    }

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

    fun deleteReminder(name: String) {
        Timber.d("deleteReminder: $name")
        startSession()
        if (!isLinked) {
            return
        }
        val api = mDBApi ?: return
        try {
            api.files().deleteV2(reminderFolder + name)
        } catch (e: DbxException) {
            Timber.d("deleteReminder: ${e.message}")
        }
    }

    fun cleanFolder() {
        startSession()
        if (!isLinked) {
            return
        }
        deleteFolder(noteFolder)
        deleteFolder(groupFolder)
        deleteFolder(birthFolder)
        deleteFolder(placeFolder)
        deleteFolder(templateFolder)
        deleteFolder(settingsFolder)
        deleteFolder(reminderFolder)
    }

    private fun deleteFolder(folder: String) {
        val api = mDBApi ?: return
        try {
            api.files().deleteV2(folder)
        } catch (e: DbxException) {
            Timber.d("deleteFolder: ${e.message}")
        }
    }

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

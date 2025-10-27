package com.github.naz013.cloudapi.dropbox

import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.http.OkHttp3Requestor
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.dropbox.core.v2.users.FullAccount
import com.dropbox.core.v2.users.SpaceUsage
import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFiles
import com.github.naz013.cloudapi.FileConfig
import com.github.naz013.cloudapi.Source
import com.github.naz013.cloudapi.legacy.Convertible
import com.github.naz013.cloudapi.legacy.DataChannel
import com.github.naz013.cloudapi.legacy.Metadata
import com.github.naz013.cloudapi.stream.CopyByteArrayStream
import com.github.naz013.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.ByteArrayInputStream
import java.io.InputStream

internal class DropboxApiImpl(
  private val dropboxAuthManager: DropboxAuthManager
) : DropboxApi {

  private var dbxClientV2: DbxClientV2? = null
  private var isInitialized: Boolean = false

  init {
    initialize()
  }

  override fun initialize(): Boolean {
    isInitialized = false
    if (!dropboxAuthManager.isAuthorized()) {
      Logger.i(TAG, "Failed to initialize Dropbox, not authorized")
      return false
    }
    val token = dropboxAuthManager.getOAuth2Token()
    if (token.isEmpty()) {
      Logger.i(TAG, "Failed to initialize Dropbox, token is empty")
      return false
    }
    val requestConfig = DbxRequestConfig.newBuilder("Just Reminder")
      .withHttpRequestor(OkHttp3Requestor(OkHttpClient()))
      .build()

    dbxClientV2 = DbxClientV2(requestConfig, token)
    isInitialized = true
    Logger.i(TAG, "Dropbox initialized")
    return true
  }

  override fun disconnect() {
    dbxClientV2 = null
    isInitialized = false
    Logger.i(TAG, "Dropbox disconnected")
  }

  override suspend fun saveFile(stream: CopyByteArrayStream, metadata: Metadata) {
    if (!isInitialized) {
      return
    }
    val folder = folderFromExt(metadata.fileExt)
    Logger.d(TAG, "Saving file: ${metadata.fileName}, folder = $folder")
    val fis = ByteArrayInputStream(stream.toByteArray())
    try {
      val result = dbxClientV2?.files()?.uploadBuilder(folder + metadata.fileName)
        ?.withMode(WriteMode.OVERWRITE)
        ?.uploadAndFinish(fis) ?: return

      withContext(Dispatchers.IO) {
        fis.close()
        stream.close()
      }

      dbxClientV2?.files().listRevisions(result.pathLower)
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to save file: ${e.message}")
    }
  }

  override suspend fun getFiles(folder: String, predicate: (CloudFile) -> Boolean): CloudFiles? {
    if (!isInitialized) {
      return null
    }
    if (folder.isEmpty()) return null
    Logger.i(TAG, "Going to get files, folder = $folder")
    try {
      val result = dbxClientV2?.files()?.listFolder(folder) ?: return null
      val files = mutableListOf<CloudFile>()
      for (f in result.entries) {
        val cloudFile = CloudFile(
          id = f.previewUrl ?: "",
          name = f.name,
          folder = folder
        )
        if (predicate(cloudFile)) {
          files.add(cloudFile)
        }
      }
      return CloudFiles(files)
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to get files: ${e.message}")
      return null
    }
  }

  override suspend fun getFile(fileName: String): InputStream? {
    if (!isInitialized) {
      return null
    }
    val folder = folderFromFileName(fileName)
    Logger.i(TAG, "Going to download file: $fileName, folder = $folder")
    return try {
      dbxClientV2?.files()?.download(folder + fileName)?.inputStream
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to download file: ${e.message}")
      null
    }
  }

  override suspend fun getFile(cloudFile: CloudFile): InputStream? {
    if (!isInitialized) {
      return null
    }
    if (cloudFile.folder.isEmpty()) {
      return null
    }
    Logger.i(TAG, "Going to download file: ${cloudFile.name}, folder = ${cloudFile.folder}")
    return try {
      dbxClientV2?.files()?.download(cloudFile.folder + cloudFile.name)?.inputStream
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to download file: ${e.message}")
      null
    }
  }

  override suspend fun deleteFile(fileName: String): Boolean {
    if (!isInitialized) {
      return false
    }
    if (fileName.isEmpty()) {
      return false
    }
    val folder = folderFromFileName(fileName)
    try {
      dbxClientV2?.files()?.deleteV2(folder + fileName)
      Logger.i(TAG, "File $fileName deleted")
      return true
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to delete file: $fileName, ${e.message}")
      return false
    }
  }

  override suspend fun removeAllData(): Boolean {
    if (!isInitialized) {
      return false
    }
    return dbxClientV2?.run {
      deleteFolder(this, NOTE_FOLDER)
      deleteFolder(this, GROUP_FOLDER)
      deleteFolder(this, BIRTH_FOLDER)
      deleteFolder(this, PLACE_FOLDER)
      deleteFolder(this, TEMPLATE_FOLDER)
      deleteFolder(this, SETTINGS_FOLDER)
      deleteFolder(this, REMINDER_FOLDER)
      Logger.i(TAG, "All data removed")
      true
    } ?: false
  }

  @Deprecated("Use saveFile() instead")
  override suspend fun backup(stream: CopyByteArrayStream, metadata: Metadata) {
    if (!isInitialized) {
      return
    }
    val api = dbxClientV2 ?: return
    val folder = folderFromExt(metadata.fileExt)
    val fis = ByteArrayInputStream(stream.toByteArray())
    try {
      api.files().uploadBuilder(folder + metadata.fileName)
        .withMode(WriteMode.OVERWRITE)
        .uploadAndFinish(fis)
      withContext(Dispatchers.IO) {
        fis.close()
      }
      withContext(Dispatchers.IO) {
        stream.close()
      }
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to backup: ${e.message}")
    }
  }

  @Deprecated("Use getFile() instead")
  override suspend fun restore(fileName: String): InputStream? {
    if (!isInitialized) {
      return null
    }
    val api = dbxClientV2 ?: return null
    val folder = folderFromFileName(fileName)
    return try {
      api.files().download(folder + fileName).inputStream
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to restore: ${e.message}")
      null
    }
  }

  @Deprecated("Use getFiles() and getFile() instead")
  override suspend fun <T> restoreAll(
    ext: String,
    deleteFile: Boolean,
    convertible: Convertible<T>,
    outputChannel: DataChannel<T>
  ) {
    if (!isInitialized) {
      return
    }
    val api = dbxClientV2 ?: return
    val folder = folderFromExt(ext)
    try {
      val result = api.files().listFolder(folder)
      if (result != null) {
        for (e in result.entries) {
          val fileName = e.name
          val obj = convertible.convert(api.files().download(folder + fileName).inputStream)
          if (obj != null) {
            outputChannel.onNewData(obj)
          }
          if (deleteFile) {
            api.files().deleteV2(e.pathLower)
          }
        }
      }
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to restoreAll: ${e.message}")
    }
  }

  @Deprecated("Use deleteFile() instead")
  override suspend fun delete(fileName: String) {
    if (!isInitialized) {
      return
    }
    val api = dbxClientV2 ?: return
    val folder = folderFromFileName(fileName)
    try {
      api.files().deleteV2(folder + fileName)
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to delete: ${e.message}")
    }
  }

  override val source: Source = Source.Dropbox

  private fun folderFromFileName(fileName: String): String {
    if (fileName.isEmpty()) return REMINDER_FOLDER
    val parts = fileName.split(".".toRegex())
    if (parts.size < 2) {
      return REMINDER_FOLDER
    }
    return folderFromExt(".${parts[1]}")
  }

  private fun folderFromExt(ext: String): String {
    return when (ext) {
      FileConfig.FILE_NAME_NOTE -> NOTE_FOLDER
      FileConfig.FILE_NAME_GROUP -> GROUP_FOLDER
      FileConfig.FILE_NAME_BIRTHDAY -> BIRTH_FOLDER
      FileConfig.FILE_NAME_PLACE -> PLACE_FOLDER
      FileConfig.FILE_NAME_SETTINGS_EXT -> SETTINGS_FOLDER
      FileConfig.FILE_NAME_JSON -> ROOT_FOLDER
      else -> REMINDER_FOLDER
    }
  }

  fun userName(): String {
    val api = dbxClientV2 ?: return ""
    var account: FullAccount? = null
    try {
      account = api.users().currentAccount
    } catch (e: DbxException) {
      e.printStackTrace()
    }

    return account?.name?.displayName ?: ""
  }

  fun userQuota(): Long {
    val api = dbxClientV2 ?: return 0
    var account: SpaceUsage? = null
    try {
      account = api.users().spaceUsage
    } catch (e: DbxException) {
      Logger.e("Dropbox: userQuota: ${e.message}")
    }

    return account?.allocation?.individualValue?.allocated ?: 0
  }

  fun userQuotaNormal(): Long {
    val api = dbxClientV2 ?: return 0
    var account: SpaceUsage? = null
    try {
      account = api.users().spaceUsage
    } catch (e: DbxException) {
      Logger.e("Dropbox: userQuota: ${e.message}")
    }

    return account?.used ?: 0
  }

  private fun deleteFolder(dbxClientV2: DbxClientV2, folder: String) {
    try {
      dbxClientV2.files().deleteV2(folder)
      Logger.i(TAG, "Folder $folder deleted")
    } catch (e: DbxException) {
      Logger.e(TAG, "Failed to delete folder: $folder, ${e.message}")
    }
  }

  fun countFiles(): Int {
    var count = 0
    if (!isInitialized) {
      return 0
    }
    val api = dbxClientV2 ?: return 0
    try {
      val result = api.files().listFolder("/") ?: return 0
      count = result.entries.size
    } catch (e: DbxException) {
      Logger.e("Dropbox: countFiles: ${e.message}")
    }

    return count
  }

  override fun toString(): String {
    return TAG
  }

  companion object {
    private const val TAG = "DropboxApi"
    private const val ROOT_FOLDER = "/"
    private const val REMINDER_FOLDER = "/Reminders/"
    private const val NOTE_FOLDER = "/Notes/"
    private const val GROUP_FOLDER = "/Groups/"
    private const val BIRTH_FOLDER = "/Birthdays/"
    private const val PLACE_FOLDER = "/Places/"

    @Deprecated("After R")
    private const val TEMPLATE_FOLDER = "/Templates/"
    private const val SETTINGS_FOLDER = "/Settings/"
  }
}

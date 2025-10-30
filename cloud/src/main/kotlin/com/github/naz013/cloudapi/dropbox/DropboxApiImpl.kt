package com.github.naz013.cloudapi.dropbox

import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.http.OkHttp3Requestor
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.WriteMode
import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileSearchParams
import com.github.naz013.cloudapi.FileConfig
import com.github.naz013.cloudapi.Source
import com.github.naz013.logging.Logger
import okhttp3.OkHttpClient
import java.io.InputStream

/**
 * Dropbox API implementation for file operations.
 *
 * Manages file uploads, downloads, searches, and deletions in Dropbox.
 * Automatically initializes when the user has a valid OAuth2 token.
 */
internal class DropboxApiImpl(
  private val dropboxAuthManager: DropboxAuthManager
) : DropboxApi {

  private var dbxClientV2: DbxClientV2? = null
  private var isInitialized: Boolean = false

  override val source: Source = Source.Dropbox

  init {
    initialize()
  }

  /**
   * Uploads a file to Dropbox.
   *
   * Deletes any existing file with the same name before uploading.
   * The input stream is automatically closed after upload.
   *
   * @param stream The input stream containing the file data
   * @param cloudFile Metadata for the file to upload
   * @return Updated CloudFile with server-generated metadata (id, size, lastModified, rev)
   * @throws IllegalStateException if Dropbox is not initialized or upload fails
   * @throws IllegalArgumentException if file name or extension is blank
   */
  override suspend fun uploadFile(stream: InputStream, cloudFile: CloudFile): CloudFile {
    require(cloudFile.name.isNotBlank()) { "File name cannot be blank" }
    require(cloudFile.fileExtension.isNotBlank()) { "File extension cannot be blank" }
    if (!isInitialized) {
      throw IllegalStateException("DropboxApi is not initialized")
    }
    val folder = folderFromExt(cloudFile.fileExtension)
    Logger.d(TAG, "Saving file: ${cloudFile.name}, folder = $folder")
    return try {
      deleteFile(cloudFile.name)
      stream.use {
        val result = dbxClientV2?.files()?.uploadBuilder(folder + cloudFile.name)
          ?.withMode(WriteMode.OVERWRITE)
          ?.uploadAndFinish(it) ?: throw IllegalStateException("Upload failed")
        CloudFile(
          id = result.id,
          name = result.name,
          size = result.size.toInt(),
          lastModified = result.serverModified.time,
          fileExtension = cloudFile.fileExtension,
          rev = result.rev
        )
      }
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to save file: ${e.message}", e)
      throw e
    }
  }

  /**
   * Searches for a specific file in Dropbox.
   *
   * @param searchParams Search parameters including file name and extension
   * @return CloudFile if found, null otherwise
   */
  override suspend fun findFile(searchParams: CloudFileSearchParams): CloudFile? {
    if (searchParams.name.isBlank() || searchParams.fileExtension.isBlank()) {
      return null
    }
    if (!isInitialized) {
      return null
    }
    val folder = folderFromExt(searchParams.fileExtension)
    Logger.i(TAG, "Going to find file: ${searchParams.name}, folder = $folder")
    if (folder.isEmpty()) return null
    try {
      val result = dbxClientV2?.files()?.listFolder(folder) ?: return null
      for (f in result.entries) {
        if (f.name == searchParams.name && f is FileMetadata) {
          return CloudFile(
            id = f.id,
            name = f.name,
            fileExtension = searchParams.fileExtension,
            lastModified = f.serverModified.time,
            size = f.size.toInt(),
            rev = f.rev,
          )
        }
      }
      return null
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to get files: ${e.message}")
      return null
    }
  }

  /**
   * Searches for all files with a specific extension in Dropbox.
   *
   * @param fileExtension The file extension to search for
   * @return List of CloudFiles matching the extension
   */
  override suspend fun findFiles(fileExtension: String): List<CloudFile> {
    if (fileExtension.isBlank() || !isInitialized) {
      return emptyList()
    }
    val folder = folderFromExt(fileExtension)
    Logger.i(TAG, "Going to find files, folder = $folder")
    try {
      val result = dbxClientV2?.files()?.listFolder(folder) ?: return emptyList()
      val files = mutableListOf<CloudFile>()
      for (f in result.entries) {
        if (f is FileMetadata) {
          val cloudFile = CloudFile(
            id = f.id,
            name = f.name,
            fileExtension = fileExtension,
            lastModified = f.serverModified.time,
            size = f.size.toInt(),
            rev = f.rev,
          )
          files.add(cloudFile)
        }
      }
      return files
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to get files: ${e.message}")
      return emptyList()
    }
  }

  /**
   * Downloads a file from Dropbox.
   *
   * @param cloudFile The file metadata including name and extension
   * @return InputStream containing file data, or null if download fails
   */
  override suspend fun downloadFile(cloudFile: CloudFile): InputStream? {
    if (cloudFile.name.isBlank() || cloudFile.fileExtension.isBlank() || !isInitialized) {
      return null
    }
    val folder = folderFromExt(cloudFile.fileExtension)
    Logger.i(TAG, "Going to download file: ${cloudFile.name}, folder = $folder")
    return try {
      dbxClientV2?.files()?.download(folder + cloudFile.name)?.inputStream
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to download file: ${e.message}")
      null
    }
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

  /**
   * Deletes a file from Dropbox by filename.
   *
   * @param fileName The name of the file to delete
   * @return true if deletion succeeded, false otherwise
   */
  override suspend fun deleteFile(fileName: String): Boolean {
    if (!isInitialized || fileName.isBlank()) {
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

  /**
   * Removes all data from Dropbox by deleting all application folders.
   *
   * @return true if all folders were deleted successfully, false otherwise
   */
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
      else -> throw IllegalArgumentException("Unknown file extension: $ext")
    }
  }

  private fun deleteFolder(dbxClientV2: DbxClientV2, folder: String) {
    try {
      dbxClientV2.files().deleteV2(folder)
      Logger.i(TAG, "Folder $folder deleted")
    } catch (e: DbxException) {
      Logger.e(TAG, "Failed to delete folder: $folder, ${e.message}")
    }
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

package com.github.naz013.cloudapi.googledrive

import android.content.Context
import android.text.TextUtils
import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileSearchParams
import com.github.naz013.cloudapi.Source
import com.github.naz013.cloudapi.legacy.Convertible
import com.github.naz013.cloudapi.legacy.DataChannel
import com.github.naz013.cloudapi.legacy.Metadata
import com.github.naz013.cloudapi.stream.CopyByteArrayStream
import com.github.naz013.logging.Logger
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.Collections

internal class GoogleDriveApiImpl(
  private val context: Context,
  private val googleDriveAuthManager: GoogleDriveAuthManager
) : GoogleDriveApi {

  private var drive: Drive? = null
  private var isInitialized: Boolean = false

  init {
    initialize()
  }

  override fun initialize(): Boolean {
    if (googleDriveAuthManager.hasGooglePlayServices() && googleDriveAuthManager.isAuthorized()) {
      val credential = GoogleAccountCredential.usingOAuth2(
        context,
        Collections.singleton(DriveScopes.DRIVE_APPDATA)
      )
      credential.selectedAccountName = googleDriveAuthManager.getUserName()
      drive = Drive.Builder(NetHttpTransport(), GsonFactory(), credential)
        .setApplicationName(APPLICATION_NAME)
        .build()
      isInitialized = true
      Logger.i(TAG, "Google Drive initialized")
    } else {
      googleDriveAuthManager.removeUserName()
      drive = null
      isInitialized = false
      Logger.i(TAG, "Google Drive is not authorized")
    }
    return isInitialized
  }

  override fun disconnect() {
    drive = null
    isInitialized = false
    Logger.i(TAG, "Google Drive disconnected")
  }

  override suspend fun saveFile(stream: CopyByteArrayStream, metadata: Metadata) {
    if (!isInitialized) return
    if (metadata.fileName.isEmpty()) return
    try {
      deleteFile(metadata.fileName)
      val fileMetadata = File().apply {
        name = metadata.fileName
        description = metadata.meta
        parents = PARENTS
      }
      val mediaContent = InputStreamContent("text/plain", stream.toInputStream())
      stream.use {
        drive?.files()?.create(fileMetadata, mediaContent)
          ?.setFields("id")
          ?.execute()
      }
      Logger.d(TAG, "File saved, file name ${metadata.fileName}")
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to save file: ${e.message}")
    }
  }

  override suspend fun getFile(cloudFile: CloudFile): InputStream? {
    if (!isInitialized) return null
    if (cloudFile.name.isEmpty()) return null
    if (cloudFile.id.isEmpty()) return null
    Logger.i(TAG, "Going to get file: ${cloudFile.name}")
    try {
      return drive?.files()?.get(cloudFile.id)?.executeMediaAsInputStream()
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to get file: ${e.message}")
      return null
    }
  }

  override suspend fun getFile(fileName: String): InputStream? {
    if (!isInitialized) return null
    if (fileName.isEmpty()) return null
    Logger.i(TAG, "Going to get file: $fileName")
    try {
      val request = drive?.files()?.list()
        ?.setSpaces("appDataFolder")
        ?.setFields("nextPageToken, files(id, name)")
        ?.setQ("mimeType = 'text/plain' and name contains '$fileName'")
        ?: return null
      do {
        val filesResult = request.execute() ?: return null
        val fileList = filesResult.files as ArrayList<File>
        val file = fileList.firstOrNull { it.name == fileName }
        if (file != null) {
          return drive?.files()?.get(file.id)?.executeMediaAsInputStream()
        }
        request.pageToken = filesResult.nextPageToken
      } while (request.pageToken != null)
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to get file: ${e.message}")
      return null
    }
    return null
  }

  override suspend fun deleteFile(fileName: String): Boolean {
    if (!isInitialized) return false
    if (fileName.isEmpty()) return false
    try {
      val request = drive?.files()?.list()
        ?.setSpaces("appDataFolder")
        ?.setFields("nextPageToken, files(id, name)")
        ?.setPageSize(10)
        ?.setQ("mimeType = 'text/plain' and name contains '$fileName'")
      val files = request?.execute() ?: return false
      val fileList = files.files as ArrayList<File>
      for (f in fileList) {
        drive?.files()?.delete(f.id)?.execute()
      }
      Logger.d(TAG, "File deleted, file name $fileName")
      return true
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to delete file: ${e.message}")
      return false
    }
  }

  override suspend fun removeAllData(): Boolean {
    if (!isInitialized) return false
    try {
      val request = drive?.files()?.list()
        ?.setSpaces("appDataFolder")
        ?.setFields("nextPageToken, files(id, name)") ?: return false
      do {
        val files = request.execute()
        val fileList = files.files as ArrayList<File>
        for (f in fileList) {
          drive?.files()?.delete(f.id)?.execute()
        }
        request.pageToken = files.nextPageToken
      } while (request.pageToken != null && request.pageToken.isNotEmpty())
      Logger.d(TAG, "All files deleted")
      return true
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to delete all files: ${e.message}")
      return false
    }
  }

  @Deprecated("Use saveFile() instead")
  override suspend fun backup(stream: CopyByteArrayStream, metadata: Metadata) {
    val service = drive ?: return
    if (!isInitialized) return
    if (TextUtils.isEmpty(metadata.fileName)) return
    try {
      deleteFile(metadata.fileName)
      val fileMetadata = File()
      fileMetadata.name = metadata.fileName
      fileMetadata.description = metadata.meta
      fileMetadata.parents = PARENTS
      val mediaContent = InputStreamContent("text/plain", stream.toInputStream())
      service.files().create(fileMetadata, mediaContent)
        .setFields("id")
        .execute()
      withContext(Dispatchers.IO) {
        stream.close()
      }
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to save file: ${e.message}")
    }
  }

  @Deprecated("Use getFile() instead")
  override suspend fun restore(fileName: String): InputStream? {
    val service = drive ?: return null
    if (!isInitialized) return null
    try {
      val request = service.files().list()
        .setSpaces("appDataFolder")
        .setFields("nextPageToken, files(id, name)")
        .setQ("mimeType = 'text/plain' and name contains '$fileName'")
      do {
        val filesResult = request.execute()
        val fileList = filesResult.files as ArrayList<File>
        for (f in fileList) {
          val title = f.name
          if (title == fileName) {
            return service.files().get(f.id).executeMediaAsInputStream()
          }
        }
        request.pageToken = filesResult.nextPageToken
      } while (request.pageToken != null)
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to restore file: ${e.message}")
      return null
    }
    return null
  }

  @Deprecated("Use getFiles() and getFile() instead")
  override suspend fun <T> restoreAll(
    ext: String,
    deleteFile: Boolean,
    convertible: Convertible<T>,
    outputChannel: DataChannel<T>
  ) {
    val service = drive ?: return
    if (!isInitialized) {
      return
    }
    try {
      val request = service.files().list()
        .setSpaces("appDataFolder")
        .setFields("nextPageToken, files(id, name)")
        .setQ("mimeType = 'text/plain' and name contains '$ext'")
      do {
        val filesResult = request.execute()
        val fileList = filesResult.files as ArrayList<File>
        for (f in fileList) {
          val shouldDownload = f.name.endsWith(ext)
          if (shouldDownload) {
            val obj = convertible.convert(service.files().get(f.id).executeMediaAsInputStream())
            if (obj != null) {
              outputChannel.onNewData(obj)
            }
            if (deleteFile) {
              service.files().delete(f.id).execute()
            }
          }
        }
        request.pageToken = filesResult.nextPageToken
      } while (request.pageToken != null)
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to restore all files: ${e.message}")
    }
  }

  @Deprecated("Use deleteFile() instead", ReplaceWith("deleteFile(fileName)"))
  override suspend fun delete(fileName: String) {
    deleteFile(fileName)
  }

  override val source: Source = Source.GoogleDrive

  override suspend fun uploadFile(stream: InputStream, cloudFile: CloudFile): CloudFile {
    if (!isInitialized) {
      throw IllegalStateException("Google Drive is not initialized")
    }
    if (cloudFile.name.isEmpty()) {
      throw IllegalArgumentException("File name is empty")
    }
    Logger.i(TAG, "Going to upload file: ${cloudFile.name}")
    try {
      deleteFile(cloudFile.name)
      val fileMetadata = File().apply {
        name = cloudFile.name
        description = cloudFile.fileDescription
        parents = PARENTS
        version = cloudFile.version
      }
      val mediaContent = InputStreamContent("text/plain", stream)
      var resultFile: File? = null
      stream.use {
        resultFile = drive?.files()?.create(fileMetadata, mediaContent)
          ?.setFields("id")
          ?.execute()
      }
      if (resultFile == null) {
        throw IllegalStateException("File upload failed")
      }
      Logger.d(TAG, "File saved, file name ${cloudFile.name}")
      return cloudFile.copy(
        id = resultFile.id ?: throw IllegalStateException("Failed to get file ID after upload"),
        lastModified = resultFile.modifiedTime?.value ?: 0L,
        size = resultFile.size
      )
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to save file: ${e.message}")
      throw e
    }
  }

  override suspend fun findFile(searchParams: CloudFileSearchParams): CloudFile? {
    if (!isInitialized) {
      throw IllegalStateException("Google Drive is not initialized")
    }
    Logger.i(TAG, "Going to find file: ${searchParams.name}")
    try {
      val request = drive?.files()?.list()
        ?.setSpaces("appDataFolder")
        ?.setFields("nextPageToken, files(id, name)")
        ?.setQ("mimeType = 'text/plain' and name contains '${searchParams.name}'")
        ?: return null
      do {
        val filesResult = request.execute() ?: return null
        val fileList = filesResult.files as ArrayList<File>
        for (f in fileList) {
          if (f.name == searchParams.name) {
            return CloudFile(
              id = f.id,
              name = f.name,
              fileDescription = f.description,
              fileExtension = f.fileExtension,
              lastModified = f.modifiedTime.value,
              size = f.size,
            )
          }
        }
        request.pageToken = filesResult.nextPageToken
      } while (request.pageToken != null)
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to find file: ${e.message}")

    }
    return null
  }

  override suspend fun downloadFile(cloudFile: CloudFile): InputStream? {
    if (!isInitialized) {
      throw IllegalStateException("Google Drive is not initialized")
    }
    if (cloudFile.name.isEmpty()) {
      throw IllegalArgumentException("File name is empty")
    }
    if (cloudFile.id.isEmpty()) {
      throw IllegalArgumentException("File ID is empty")
    }
    Logger.i(TAG, "Going to download file: ${cloudFile.name}")
    try {
      return drive?.files()?.get(cloudFile.id)?.executeMediaAsInputStream()
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to download file: ${e.message}")
      throw e
    }
  }

  override fun toString(): String {
    return TAG
  }

  companion object {
    private const val TAG = "GoogleDriveApi"
    private const val APPLICATION_NAME = "Reminder/7.0"
    private val PARENTS = Collections.singletonList("appDataFolder")
  }
}

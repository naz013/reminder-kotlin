package com.github.naz013.cloudapi.googledrive

import android.content.Context
import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileSearchParams
import com.github.naz013.cloudapi.FileConfig
import com.github.naz013.cloudapi.Source
import com.github.naz013.logging.Logger
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import java.io.InputStream
import java.util.Collections

internal class GoogleDriveApiImpl(
  private val context: Context,
  private val googleDriveAuthManager: GoogleDriveAuthManager
) : GoogleDriveApi {

  private var drive: Drive? = null
  private var isInitialized: Boolean = false

  override val source: Source = Source.GoogleDrive

  init {
    initialize()
  }

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
      }
      val mediaContent = InputStreamContent("text/plain", stream)
      var resultFile: File? = null
      stream.use {
        resultFile = drive?.files()?.create(fileMetadata, mediaContent)
          ?.setFields(FILES_FIELDS)
          ?.execute()
      }
      val uploadedFile = resultFile ?: throw IllegalStateException("File upload failed")
      Logger.d(TAG, "File saved, file name ${cloudFile.name}")
      return cloudFile.copy(
        id = uploadedFile.id ?: throw IllegalStateException("Failed to get file ID after upload"),
        lastModified = uploadedFile.modifiedTime?.value ?: 0L,
        size = uploadedFile.size,
        version = uploadedFile.version ?: 0L
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
        ?.setFields("nextPageToken, files($FILES_FIELDS)")
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
              fileExtension = f.fileExtension ?: "",
              lastModified = f.modifiedTime?.value ?: 0L,
              size = f.size,
              version = f.version ?: 0L
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

  override suspend fun findFiles(fileExtension: String): List<CloudFile> {
    val result = mutableListOf<CloudFile>()
    if (!isInitialized) {
      throw IllegalStateException("Google Drive is not initialized")
    }
    Logger.i(TAG, "Going to find files with extension: $fileExtension")
    try {
      val request = drive?.files()?.list()
        ?.setSpaces("appDataFolder")
        ?.setFields("nextPageToken, files($FILES_FIELDS)")
        ?.setQ("mimeType = 'text/plain' and name contains '$fileExtension'")
        ?: return emptyList()
      do {
        val filesResult = request.execute() ?: return emptyList()
        val fileList = filesResult.files as ArrayList<File>
        for (f in fileList) {
          if (f.name.endsWith(fileExtension)) {
            result.add(
              CloudFile(
                id = f.id,
                name = f.name,
                fileDescription = f.description,
                fileExtension = f.fileExtension ?: "",
                lastModified = f.modifiedTime?.value ?: 0L,
                size = f.size,
                version = f.version ?: 0L
              )
            )
          }
        }
        request.pageToken = filesResult.nextPageToken
      } while (request.pageToken != null)
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to find files: ${e.message}")
    }
    return result
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
    val id = if (cloudFile.fileExtension == FileConfig.FILE_NAME_NOTE_IMAGE) {
      val foundFile = findFile(
        CloudFileSearchParams(
          name = cloudFile.name,
          fileExtension = cloudFile.fileExtension
        )
      ) ?: throw IllegalStateException("File not found: ${cloudFile.name}")
      foundFile.id
    } else {
      cloudFile.id
    }
    Logger.i(TAG, "Going to download file: ${cloudFile.name}")
    try {
      return drive?.files()?.get(id)?.executeMediaAsInputStream()
    } catch (e: Throwable) {
      Logger.e(TAG, "Failed to download file: ${e.message}")
      throw e
    }
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
    if (!isInitialized) {
      Logger.e(TAG, "Remove all data: not initialized")
      return false
    }
    try {
      val request = drive?.files()?.list()
        ?.setSpaces("appDataFolder")
        ?.setFields("nextPageToken, files(id, name)") ?: run {
        Logger.i(TAG, "Remove all data: request is null")
        return false
      }
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

  companion object {
    private const val TAG = "GoogleDriveApi"
    private const val APPLICATION_NAME = "Reminder/7.0"
    private val PARENTS = Collections.singletonList("appDataFolder")
    private const val FILES_FIELDS = "id, name, description, fileExtension, modifiedTime, size, version"
  }
}

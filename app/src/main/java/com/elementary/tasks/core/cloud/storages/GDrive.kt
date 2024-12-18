package com.elementary.tasks.core.cloud.storages

import android.content.Context
import android.text.TextUtils
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.converters.Convertible
import com.elementary.tasks.core.cloud.converters.Metadata
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.io.CopyByteArrayStream
import com.elementary.tasks.core.utils.params.Prefs
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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Collections

class GDrive(
  private val context: Context,
  private val prefs: Prefs
) : Storage() {

  private var driveService: Drive? = null

  var statusCallback: StatusCallback? = null
  var isLogged: Boolean = false
    private set

  init {
    val user = prefs.driveUser
    login(user)
  }

  fun login(user: String) {
    if (SuperUtil.isGooglePlayServicesAvailable(context) && user.matches(".*@.*".toRegex())) {
      Logger.d("GDrive: user -> $user")
      val credential = GoogleAccountCredential.usingOAuth2(
        context,
        Collections.singleton(DriveScopes.DRIVE_APPDATA)
      )
      credential.selectedAccountName = user
      driveService = Drive.Builder(NetHttpTransport(), GsonFactory(), credential)
        .setApplicationName(APPLICATION_NAME)
        .build()
      isLogged = true
      statusCallback?.onStatusChanged(true)
    } else {
      logOut()
    }
  }

  fun logOut() {
    prefs.driveUser = Prefs.DRIVE_USER_NONE
    driveService = null
    isLogged = false
    statusCallback?.onStatusChanged(false)
  }

  override suspend fun backup(stream: CopyByteArrayStream, metadata: Metadata) {
    val service = driveService ?: return
    if (!isLogged) return
    if (TextUtils.isEmpty(metadata.fileName)) return
    try {
      removeAllCopies(metadata.fileName)
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
      Logger.e("Gdrive: backup: ${e.message}")
    }
  }

  override suspend fun restore(fileName: String): InputStream? {
    Logger.d("restore: $fileName")
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
          Logger.d("restore: ${f.name}, ${f.id}, $fileName")
          val title = f.name
          if (title == fileName) {
            return service.files().get(f.id).executeMediaAsInputStream()
          }
        }
        request.pageToken = filesResult.nextPageToken
      } while (request.pageToken != null)
    } catch (e: Throwable) {
      Logger.e("Gdrive: restore: ${e.message}")
      return null
    }
    return null
  }

  override suspend fun <T> restoreAll(
    ext: String,
    deleteFile: Boolean,
    convertible: Convertible<T>,
    outputChannel: DataChannel<T>
  ) {
    Logger.d("restoreAll: start, isLogged=$isLogged, service=$driveService")
    val service = driveService ?: return
    if (!isLogged) {
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
          Logger.d("restoreAll: ${f.name}, ${f.id}, $ext")
          val shouldDownload = f.name.endsWith(ext)
          Logger.d("restoreAll: should download = $shouldDownload")
          if (shouldDownload) {
            val obj = convertible.convert(service.files().get(f.id).executeMediaAsInputStream())
            Logger.d("restoreAll: converted = $obj")
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
      Logger.e("Gdrive: restoreAll: ${e.message}")
    }
  }

  override suspend fun delete(fileName: String) {
    removeAllCopies(fileName)
  }

  fun clean() {
    if (!isLogged) {
      return
    }
    catchError("Failed to clean") {
      val request = it.files().list()
        .setSpaces("appDataFolder")
        .setFields("nextPageToken, files(id, name)")
      do {
        val files = request.execute()
        val fileList = files.files as ArrayList<File>
        for (f in fileList) {
          it.files().delete(f.id).execute()
        }
        request.pageToken = files.nextPageToken
      } while (request.pageToken != null && request.pageToken.length >= 0)
    }
  }

  fun cleanFolder() {
    catchError("Failed to clean folder") {
      val request = it.files().list()
        .setSpaces("appDataFolder")
        .setFields("nextPageToken, files(id, name)")

      do {
        val files = request.execute()
        val fileList = files.files as ArrayList<File>
        for (f in fileList) {
          it.files().delete(f.id).execute()
        }
        request.pageToken = files.nextPageToken
      } while (request.pageToken != null && request.pageToken.length >= 0)
    }
  }

  private fun countFiles(): Int {
    return catchError("Failed to count files") {
      var count = 0
      val request = it.files().list()
        .setSpaces("appDataFolder")
        .setFields("nextPageToken, files(id, name)")
        .setQ("mimeType = 'text/plain'")
      do {
        val files = request.execute()
        val fileList = files.files as ArrayList<File>
        for (f in fileList) {
          val title = f.name
          Logger.d("countFiles: $title")
          when {
            title.contains(FileConfig.FILE_NAME_SETTINGS) -> count++
            title.endsWith(FileConfig.FILE_NAME_PLACE) -> count++
            title.endsWith(FileConfig.FILE_NAME_BIRTHDAY) -> count++
            title.endsWith(FileConfig.FILE_NAME_GROUP) -> count++
            title.endsWith(FileConfig.FILE_NAME_NOTE) -> count++
            title.endsWith(FileConfig.FILE_NAME_REMINDER) -> count++
          }
        }
        request.pageToken = files.nextPageToken
      } while (request.pageToken != null)
      count
    } ?: 0
  }

  private fun removeAllCopies(fileName: String) {
    if (fileName.isEmpty()) return
    catchError("Failed to removeAllCopies") {
      val request = it.files().list()
        .setSpaces("appDataFolder")
        .setFields("nextPageToken, files(id, name)")
        .setPageSize(10)
        .setQ("mimeType = 'text/plain' and name contains '$fileName'")
      val files = request.execute()
      val fileList = files.files as ArrayList<File>
      for (f in fileList) {
        it.files().delete(f.id).execute()
      }
    }
  }

  private fun showContent(id: String) {
    if (BuildConfig.DEBUG) {
      catchError("Failed to show file content") {
        val out = ByteArrayOutputStream()
        it.files().get(id).executeMediaAndDownloadTo(out)
        val data = out.toString()
        Logger.d("showContent: $id, $data")
      }
    }
  }

  private fun backup(json: String, metadata: Metadata) {
    if (metadata.fileName.isEmpty()) return
    removeAllCopies(metadata.fileName)
    catchError("Failed to backup file") {
      val fileMetadata = File()
      fileMetadata.name = metadata.fileName
      fileMetadata.description = metadata.meta
      fileMetadata.parents = PARENTS
      val mediaContent = InputStreamContent("text/plain", ByteArrayInputStream(json.toByteArray()))
      val driveFile = it.files().create(fileMetadata, mediaContent)
        .setFields("id")
        .execute()
      Logger.d("backup: ${driveFile.id}, ${metadata.fileName}")
    }
  }

  private fun <T> catchError(errorMessage: String? = null, call: (Drive) -> T): T? {
    return try {
      withService(call)
    } catch (e: Throwable) {
      Logger.e("Failed to $errorMessage: ${e.message}")
      null
    }
  }

  private fun <T> withService(call: (Drive) -> T): T? {
    val service = driveService?.takeIf { isLogged } ?: return null
    return call.invoke(service)
  }

  interface StatusCallback {
    fun onStatusChanged(isLogged: Boolean)
  }

  companion object {
    const val APPLICATION_NAME = "Reminder/7.0"
    private val PARENTS = Collections.singletonList("appDataFolder")
  }
}

package com.elementary.tasks.core.cloud.storages

import android.content.Context
import android.text.TextUtils
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.converters.Convertible
import com.elementary.tasks.core.cloud.converters.Metadata
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.settings.export.backups.UserItem
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Collections

class GDrive(
  private val context: Context,
  private val prefs: Prefs,
  private val dispatcherProvider: DispatcherProvider,
  private val dateTimeManager: DateTimeManager
) : Storage() {

  private var driveService: Drive? = null

  private val indexDataFile = IndexDataFile()

  var statusCallback: StatusCallback? = null
  var isLogged: Boolean = false
    private set

  val data: UserItem?
    get() {
      val service = driveService ?: return null
      if (!isLogged) return null
      try {
        val about = service.about().get().setFields("user, storageQuota").execute()
          ?: return null
        val quota = about.storageQuota ?: return null
        return UserItem(
          name = about.user.displayName ?: "", quota = quota.limit,
          used = quota.usage, count = countFiles(), photo = about.user.photoLink
            ?: ""
        )
      } catch (e: Throwable) {
        Timber.d(e, "Failed to get user data")
      }
      return null
    }

  init {
    val user = prefs.driveUser
    login(user)
  }

  fun login(user: String) {
    if (SuperUtil.isGooglePlayServicesAvailable(context) && user.matches(".*@.*".toRegex())) {
      Timber.d("GDrive: user -> $user")
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
      if (!indexDataFile.isLoaded) {
        launchDefault { loadIndexFile() }
      }
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

  override suspend fun backup(fileIndex: FileIndex, metadata: Metadata) {
    val service = driveService ?: return
    if (!isLogged) return
    if (TextUtils.isEmpty(metadata.fileName)) return
    val stream = fileIndex.stream
    if (stream == null) {
      return
    } else {
      try {
        removeAllCopies(metadata.fileName)
        val fileMetadata = File()
        fileMetadata.name = metadata.fileName
        fileMetadata.description = metadata.meta
        fileMetadata.parents = PARENTS
        val mediaContent = InputStreamContent("text/plain", stream.toInputStream())
        val driveFile = service.files().create(fileMetadata, mediaContent)
          .setFields("id")
          .execute()
        stream.close()
        Timber.d("backup: STREAM ${driveFile.id}, ${metadata.fileName}")
        if (BuildConfig.DEBUG) showContent(driveFile.id)
      } catch (e: Throwable) {
        Timber.d(e)
      }
    }
  }

  override suspend fun restore(fileName: String): InputStream? {
    Timber.d("restore: $fileName")
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
            return service.files().get(f.id).executeMediaAsInputStream()
          }
        }
        request.pageToken = filesResult.nextPageToken
      } while (request.pageToken != null)
    } catch (e: Throwable) {
      Timber.d(e)
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
    Timber.d("restoreAll: start, isLogged=$isLogged, service=$driveService")
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
          Timber.d("restoreAll: ${f.name}, ${f.id}, $ext")
          val shouldDownload = f.name.endsWith(ext)
          Timber.d("restoreAll: should download = $shouldDownload")
          if (shouldDownload) {
            val obj = convertible.convert(service.files().get(f.id).executeMediaAsInputStream())
            Timber.d("restoreAll: converted = $obj")
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
      Timber.d(e)
    }
  }

  override suspend fun delete(fileName: String) {
    removeAllCopies(fileName)
  }

  override suspend fun hasIndex(id: String): Boolean {
    return indexDataFile.hasIndex(id)
  }

  override suspend fun saveIndex(fileIndex: FileIndex) {
    indexDataFile.addIndex(fileIndex)
    saveIndexFile()
  }

  override suspend fun removeIndex(id: String) {
    indexDataFile.removeIndex(id)
    saveIndexFile()
  }

  override suspend fun saveIndex() {
    saveIndexFile()
  }

  override fun needBackup(id: String, updatedAt: String): Boolean {
    return indexDataFile.isFileChanged(id, updatedAt)
  }

  override suspend fun loadIndex() {
    loadIndexFile()
  }

  fun clean() {
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

  private suspend fun loadIndexFile() {
    val inputStream = restore(IndexDataFile.FILE_NAME)
    indexDataFile.parse(inputStream)
  }

  private suspend fun saveIndexFile() {
    withContext(dispatcherProvider.default()) {
      val json = indexDataFile.toJson() ?: return@withContext
      backup(
        json, Metadata(
          "",
          IndexDataFile.FILE_NAME,
          FileConfig.FILE_NAME_JSON,
          dateTimeManager.getNowGmtDateTime(),
          "Index file"
        )
      )
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
          Timber.d("countFiles: $title")
          when {
            title.contains(FileConfig.FILE_NAME_SETTINGS) -> count++
            title.endsWith(FileConfig.FILE_NAME_TEMPLATE) -> count++
            title.endsWith(FileConfig.FILE_NAME_PLACE) -> count++
            title.endsWith(FileConfig.FILE_NAME_BIRTHDAY) -> count++
            title.endsWith(FileConfig.FILE_NAME_GROUP) -> count++
            title.endsWith(FileConfig.FILE_NAME_NOTE) -> count++
            title.endsWith(FileConfig.FILE_NAME_REMINDER) -> count++
            title.contains(IndexDataFile.FILE_NAME) -> count++
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
        Timber.d("showContent: $id, $data")
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
      Timber.d("backup: ${driveFile.id}, ${metadata.fileName}")
    }
  }

  private fun <T> catchError(errorMessage: String? = null, call: (Drive) -> T): T? {
    return try {
      withService(call)
    } catch (e: Throwable) {
      Timber.d(e, errorMessage ?: "Failed to call Drive service")
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

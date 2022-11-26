package com.elementary.tasks.core.cloud.storages

import android.content.Context
import android.text.TextUtils
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.converters.Metadata
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.settings.export.backups.UserItem
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.channels.Channel
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Collections

class GDrive(
  context: Context,
  private val prefs: Prefs
) : Storage() {

  private var driveService: Drive? = null

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
      driveService = Drive.Builder(NetHttpTransport(), GsonFactory(), credential)
        .setApplicationName(APPLICATION_NAME)
        .build()
      isLogged = true
      statusObserver?.invoke(true)
      if (!indexDataFile.isLoaded) {
        launchDefault { loadIndexFile() }
      }
    } else {
      logOut()
    }
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
      } catch (e: Exception) {
      } catch (e: OutOfMemoryError) {
      }
    }
  }

  private fun showContent(id: String) {
    val service = driveService ?: return
    if (!isLogged) return
    try {
      val out = ByteArrayOutputStream()
      service.files().get(id).executeMediaAndDownloadTo(out)
      val data = out.toString()
      if (BuildConfig.DEBUG) Timber.d("showContent: $id, $data")
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun backup(json: String, metadata: Metadata) {
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
      Timber.d("backup: ${driveFile.id}, ${metadata.fileName}")
    } catch (e: Exception) {
    } catch (e: OutOfMemoryError) {
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
    } catch (e: Exception) {
      return null
    }
    return null
  }

  override fun restoreAll(ext: String, deleteFile: Boolean): Channel<InputStream> {
    val channel = Channel<InputStream>()
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
              channel.send(service.files().get(f.id).executeMediaAsInputStream())
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
  }

  override fun removeIndex(id: String) {
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

  fun logOut() {
    prefs.driveUser = Prefs.DRIVE_USER_NONE
    driveService = null
    isLogged = false
    statusObserver?.invoke(false)
  }

  val data: UserItem?
    get() {
      val service = driveService ?: return null
      if (!isLogged) return null
      try {
        val about = service.about().get().setFields("user, storageQuota").execute()
          ?: return null
        val quota = about.storageQuota ?: return null
        return UserItem(name = about.user.displayName ?: "", quota = quota.limit,
          used = quota.usage, count = countFiles(), photo = about.user.photoLink
          ?: "")
      } catch (e: Exception) {
      } catch (e: OutOfMemoryError) {
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
            title.contains(IndexDataFile.FILE_NAME) -> count++
          }
        }
        request.pageToken = files.nextPageToken
      } while (request.pageToken != null)
    } catch (e: Exception) {
    } catch (e: OutOfMemoryError) {
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
        .setPageSize(10)
        .setQ("mimeType = 'text/plain' and name contains '$fileName'")
      val files = request.execute()
      val fileList = files.files as ArrayList<File>
      for (f in fileList) {
        service.files().delete(f.id).execute()
      }
    } catch (e: java.lang.Exception) {
    } catch (e: OutOfMemoryError) {
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
    } catch (e: OutOfMemoryError) {
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
    } catch (e: OutOfMemoryError) {
    }
  }

  companion object {
    const val APPLICATION_NAME = "Reminder/7.0"
    private val PARENTS = Collections.singletonList("appDataFolder")
  }
}

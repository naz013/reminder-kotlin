package com.elementary.tasks.core.cloud.storages

import android.content.Context
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.converters.Convertible
import com.elementary.tasks.core.cloud.converters.Metadata
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.io.MemoryUtil
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

class LocalStorage(context: Context) : Storage() {

  private val hasSdPermission =
    Permissions.checkPermission(context, Permissions.WRITE_EXTERNAL, Permissions.READ_EXTERNAL)

  override suspend fun backup(fileIndex: FileIndex, metadata: Metadata) {
    if (!Module.is10 && hasSdPermission) {
      val stream = fileIndex.stream
      if (stream == null) {
        return
      } else {
        val dir = folderFromExt(metadata.fileExt)
        if (dir != null) {
          try {
            val fos = FileOutputStream(File(dir, metadata.fileName))
            fos.write(stream.toByteArray())
            fos.close()
            stream.close()
          } catch (e: Throwable) {
            Timber.d(e)
          }
        }
      }
    }
  }

  override suspend fun restore(fileName: String): InputStream? {
    if (!Module.is10 && hasSdPermission) {
      val dir = folderFromFileName(fileName)
      if (dir != null) {
        val file = File(dir, fileName)
        return if (file.exists()) {
          try {
            FileInputStream(file)
          } catch (e: Throwable) {
            Timber.d(e)
            null
          }
        } else {
          null
        }
      }
    }
    return null
  }

  override suspend fun <T> restoreAll(
    ext: String,
    deleteFile: Boolean,
    convertible: Convertible<T>,
    outputChannel: DataChannel<T>
  ) {
    if (Module.is10 || !hasSdPermission) {
      return
    }
    val dir = folderFromExt(ext)
    if (dir == null || !dir.exists()) {
      return
    }
    val files = dir.listFiles()
    if (files.isNullOrEmpty()) {
      return
    }
    for (f in files) {
      try {
        try {
          val obj = convertible.convert(FileInputStream(f))
          Timber.d("restoreAll: obj=$obj")
          if (obj != null) {
            outputChannel.onNewData(obj)
          }
        } catch (e: Throwable) {
          Timber.d(e)
        }
        if (deleteFile && f.exists()) {
          f.delete()
        }
      } catch (e: Throwable) {
        Timber.d(e)
      }
    }
  }

  override suspend fun delete(fileName: String) {
    if (!Module.is10 && hasSdPermission) {
      val dir = folderFromFileName(fileName)
      if (dir != null) {
        val file = File(dir, fileName)
        if (file.exists()) {
          file.delete()
        }
      }
    }
  }

  override suspend fun removeIndex(id: String) {
  }

  override suspend fun saveIndex(fileIndex: FileIndex) {
  }

  override suspend fun saveIndex() {
  }

  override suspend fun hasIndex(id: String): Boolean {
    return true
  }

  override fun needBackup(id: String, updatedAt: String): Boolean {
    return true
  }

  override suspend fun loadIndex() {
  }

  private fun folderFromFileName(fileName: String): File? {
    if (fileName.isEmpty()) return MemoryUtil.remindersDir
    val parts = fileName.split(".".toRegex())
    if (parts.size < 2) {
      return MemoryUtil.remindersDir
    }
    return folderFromExt(".${parts[1]}")
  }

  private fun folderFromExt(ext: String): File? {
    return when (ext) {
      FileConfig.FILE_NAME_NOTE -> MemoryUtil.notesDir
      FileConfig.FILE_NAME_GROUP -> MemoryUtil.groupsDir
      FileConfig.FILE_NAME_BIRTHDAY -> MemoryUtil.birthdaysDir
      FileConfig.FILE_NAME_PLACE -> MemoryUtil.placesDir
      FileConfig.FILE_NAME_SETTINGS_EXT -> MemoryUtil.prefsDir
      else -> MemoryUtil.remindersDir
    }
  }
}

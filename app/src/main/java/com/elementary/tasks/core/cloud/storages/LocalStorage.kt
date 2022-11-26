package com.elementary.tasks.core.cloud.storages

import android.content.Context
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.converters.Metadata
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.launchIo
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class LocalStorage(context: Context) : Storage() {

  private val hasSdPermission = Permissions.checkPermission(context, Permissions.WRITE_EXTERNAL, Permissions.READ_EXTERNAL)

  override suspend fun backup(fileIndex: FileIndex, metadata: Metadata) {
    if (!Module.isQ && hasSdPermission) {
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
          } catch (e: IOException) {
            e.printStackTrace()
          }
        }
      }
    }
  }

  override suspend fun restore(fileName: String): InputStream? {
    if (!Module.isQ && hasSdPermission) {
      val dir = folderFromFileName(fileName)
      if (dir != null) {
        val file = File(dir, fileName)
        return if (file.exists()) {
          try {
            FileInputStream(file)
          } catch (e: Exception) {
            null
          }
        } else {
          null
        }
      }
    }
    return null
  }

  override fun restoreAll(ext: String, deleteFile: Boolean): Channel<InputStream> {
    val channel = Channel<InputStream>()
    if (Module.isQ || !hasSdPermission) {
      channel.cancel()
      return channel
    }
    val dir = folderFromExt(ext)
    if (dir == null || !dir.exists()) {
      channel.cancel()
      return channel
    }
    val files = dir.listFiles()
    if (files.isNullOrEmpty()) {
      channel.cancel()
      return channel
    }
    launchIo {
      for (f in files) {
        try {
          try {
            channel.send(FileInputStream(f))
          } catch (e: Exception) {
          }
          if (deleteFile && f.exists()) {
            f.delete()
          }
        } catch (e: Exception) {
        }
      }
      channel.close()
    }
    return channel
  }

  override suspend fun delete(fileName: String) {
    if (!Module.isQ && hasSdPermission) {
      val dir = folderFromFileName(fileName)
      if (dir != null) {
        val file = File(dir, fileName)
        if (file.exists()) {
          file.delete()
        }
      }
    }
  }

  override fun removeIndex(id: String) {
  }

  override fun saveIndex(fileIndex: FileIndex) {
  }

  override suspend fun saveIndex() {
  }

  override fun hasIndex(id: String): Boolean {
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
      FileConfig.FILE_NAME_TEMPLATE -> MemoryUtil.templatesDir
      FileConfig.FILE_NAME_SETTINGS_EXT -> MemoryUtil.prefsDir
      else -> MemoryUtil.remindersDir
    }
  }
}
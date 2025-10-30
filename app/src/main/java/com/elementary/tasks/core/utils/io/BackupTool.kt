package com.elementary.tasks.core.utils.io

import android.content.Context
import com.github.naz013.cloudapi.FileConfig
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.note.NoteWithImages
import java.io.File
import java.io.FileOutputStream

class BackupTool(
  private val context: Context,
  private val memoryUtil: MemoryUtil,
) {

  fun reminderToFile(item: Reminder): File? {
    return anyToFile(item, item.uuId + FileConfig.FILE_NAME_REMINDER)
  }

  fun noteToFile(item: NoteWithImages?): File? {
    val note = item?.note ?: return null
    return anyToFile(item, note.key + FileConfig.FILE_NAME_NOTE)
  }

  fun placeToFile(item: Place): File? {
    return anyToFile(item, item.id + FileConfig.FILE_NAME_PLACE)
  }

  private fun anyToFile(any: Any, fileName: String): File? {
    val cacheDir = context.externalCacheDir ?: context.cacheDir
    val file = File(cacheDir, fileName)
    if (!file.createNewFile()) {
      try {
        file.delete()
        file.createNewFile()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    return try {
      val outputStream = FileOutputStream(file)
      return if (memoryUtil.toStream(any, outputStream)) {
        outputStream.flush()
        outputStream.close()
        file
      } else {
        outputStream.flush()
        outputStream.close()
        null
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }
}

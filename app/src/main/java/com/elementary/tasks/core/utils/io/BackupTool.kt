package com.elementary.tasks.core.utils.io

import android.content.Context
import com.github.naz013.domain.Place
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.files.DataConverter
import com.github.naz013.files.FileConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class BackupTool(
  private val context: Context,
  private val dataConverter: DataConverter,
) {
  suspend fun reminderToFile(item: ReminderV2): File? = anyToFile(item, item.uuId + FileConfig.FILE_NAME_REMINDER_V2)

  suspend fun placeToFile(item: Place): File? = anyToFile(item, item.id + FileConfig.FILE_NAME_PLACE)

  private suspend fun anyToFile(
    any: Any,
    fileName: String,
  ): File? {
    val cacheDir = context.externalCacheDir ?: context.cacheDir
    val file = File(cacheDir, fileName)
    if (!withContext(Dispatchers.IO) {
        file.createNewFile()
      }) {
      try {
        file.delete()
        withContext(Dispatchers.IO) {
          file.createNewFile()
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    return try {
      val outputStream = withContext(Dispatchers.IO) {
        FileOutputStream(file)
      }
      dataConverter.toOutputStream(any, outputStream)
      withContext(Dispatchers.IO) {
        outputStream.flush()
      }
      withContext(Dispatchers.IO) {
        outputStream.close()
      }
      file
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }
}

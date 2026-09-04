package com.elementary.tasks.core.utils.io

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class CacheUtil(
  val context: Context,
) {

  fun cacheFile(f: File): File? {
    val cacheDir = context.externalCacheDir ?: context.cacheDir
    val file = File(cacheDir, f.name)
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
      return if (toStream(FileInputStream(f), outputStream)) {
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

  private fun toStream(
    inputStream: InputStream,
    outputStream: OutputStream,
  ): Boolean {
    try {
      inputStream.copyTo(outputStream)
      return true
    } catch (e: Exception) {
      e.printStackTrace()
      return false
    } finally {
      inputStream.close()
    }
  }
}

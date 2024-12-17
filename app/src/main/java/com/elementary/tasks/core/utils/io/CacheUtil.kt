package com.elementary.tasks.core.utils.io

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import com.elementary.tasks.core.utils.copyInputStreamToFile
import com.github.naz013.logging.Logger
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

class CacheUtil(
  val context: Context,
  private val memoryUtil: MemoryUtil
) {

  private val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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
      return if (memoryUtil.toStream(FileInputStream(f), outputStream)) {
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

  fun removeFromCache(path: String) {
    val file = File(path)
    if (file.exists()) {
      file.delete()
      sp.edit().remove(path).apply()
    }
  }

  fun cacheFile(intent: Intent?): String? {
    val uri = intent?.data ?: return null
    return cacheFile(uri)
  }

  @SuppressLint("Range")
  fun cacheFile(uri: Uri): String? {
    val cacheDir = context.externalCacheDir ?: context.cacheDir
    val inputStream = try {
      context.contentResolver.openInputStream(uri)
    } catch (e: FileNotFoundException) {
      null
    } catch (e: Exception) {
      null
    } ?: return null
    val fileId = try {
      DocumentsContract.getDocumentId(uri)
    } catch (e: Exception) {
      ""
    }

    val cursor: Cursor? = context.contentResolver.query(
      /* uri = */ uri,
      /* projection = */ null,
      /* selection = */ null,
      /* selectionArgs = */ null,
      /* sortOrder = */ null,
      /* cancellationSignal = */ null
    )
    val name = cursor?.use {
      if (it.moveToFirst()) {
        try {
          it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME)) ?: ""
        } catch (e: Exception) {
          ""
        }
      } else {
        ""
      }
    } ?: ""
    if (name.isEmpty() && fileId.isEmpty()) {
      return null
    }

    val fileName = name.ifEmpty { fileId }
    val file = File(cacheDir, fileName)
    val fId = fileId.ifEmpty { name }

    Logger.d("cacheFile: $fId, ${file.absolutePath}, $fileName")

    if (hasCache(fId) && file.exists()) {
      Logger.d("cacheFile: FROM CACHE")
      return file.absolutePath
    }

    return try {
      if (!file.createNewFile()) {
        runCatching {
          file.delete()
          file.createNewFile()
        }
      }
      file.copyInputStreamToFile(inputStream)
      saveCache(fId)
      file.absolutePath
    } catch (e: Exception) {
      null
    }
  }

  private fun hasCache(path: String): Boolean {
    return sp.getBoolean(path, false)
  }

  private fun saveCache(path: String) {
    sp.edit().putBoolean(path, true).apply()
  }

  companion object {
    private const val PREFS_NAME = "cache_prefs"
  }
}

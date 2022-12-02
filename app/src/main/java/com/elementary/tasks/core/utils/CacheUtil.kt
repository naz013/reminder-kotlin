package com.elementary.tasks.core.utils

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.annotation.RequiresPermission
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

class CacheUtil(val context: Context) {

  private val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  fun removeFromCache(path: String) {
    val file = File(path)
    if (file.exists()) {
      file.delete()
      sp.edit().remove(path).apply()
    }
  }

  @RequiresPermission(Permissions.READ_EXTERNAL)
  fun cacheFile(intent: Intent?): String? {
    val uri = intent?.data ?: return null
    return cacheFile(uri)
  }

  @RequiresPermission(Permissions.READ_EXTERNAL)
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

    val cursor: Cursor? = context.contentResolver.query(uri, null, null,
      null, null, null)
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

    val fileName = if (name.isEmpty()) fileId else name
    val file = File(cacheDir, fileName)
    val fId = if (fileId.isEmpty()) name else fileId

    Timber.d("cacheFile: $fId, ${file.absolutePath}, $fileName")

    if (hasCache(fId) && file.exists()) {
      Timber.d("cacheFile: FROM CACHE")
      return file.absolutePath
    }

    return try {
      if (!file.createNewFile()) {
        try {
          file.delete()
          file.createNewFile()
        } catch (e: Exception) {
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
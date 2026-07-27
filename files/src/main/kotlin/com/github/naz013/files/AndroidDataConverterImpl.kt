package com.github.naz013.files

import android.content.Context
import android.net.Uri
import com.github.naz013.logging.Logger

class AndroidDataConverterImpl(
  private val context: Context,
  private val dataConverter: DataConverter,
) : AndroidDataConverter {

  override suspend fun toData(uri: Uri): Any? {
    val inputStream = try {
      context.contentResolver.openInputStream(uri) ?: return null
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to read Intent URI: ${e.message}")
      return null
    }

    return try {
      dataConverter.toData(inputStream)
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to parse data from URI: ${e.message}")
      null
    }
  }

  companion object {
    private const val TAG = "AndroidDataConverter"
  }
}

package com.github.naz013.feature.reminder.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.github.naz013.feature.common.readString

internal class UriHelper(
  private val context: Context,
) {

  fun getMimeType(uri: Uri): String? = context.contentResolver.getType(uri)

  fun getFileName(uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.moveToFirst()
    val fileName = cursor?.readString(OpenableColumns.DISPLAY_NAME)
    cursor?.close()
    return fileName
  }
}

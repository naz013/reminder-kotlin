package com.elementary.tasks.core.utils.io

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns

class UriHelper(private val context: Context) {

  fun grantPermission(uri: Uri) {
    runCatching {
      context.contentResolver.takePersistableUriPermission(
        uri,
        (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
      )
    }
  }

  fun getMimeType(uri: Uri): String? {
    return context.contentResolver.getType(uri)
  }

  fun getFileName(uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.moveToFirst()
    val fileName = cursor?.readString(OpenableColumns.DISPLAY_NAME)
    cursor?.close()
    return fileName
  }
}

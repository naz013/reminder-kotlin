package com.github.naz013.common.uri

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.github.naz013.logging.Logger
import java.io.File

object UriUtil {

  private const val TAG = "UriUtil"
  const val ANY_MIME = "any"

  fun getUri(context: Context, filePath: String, applicationId: String): Uri? {
    Logger.d(TAG, "getUri: $applicationId, $filePath")
    return try {
      FileProvider.getUriForFile(context, "$applicationId.provider", File(filePath))
    } catch (_: Throwable) {
      null
    }
  }

  fun getUri(context: Context, file: File, applicationId: String): Uri? {
    Logger.d(TAG, "getUri: $applicationId, $file")
    return try {
      FileProvider.getUriForFile(context, "$applicationId.provider", file)
    } catch (_: Throwable) {
      null
    }
  }
}

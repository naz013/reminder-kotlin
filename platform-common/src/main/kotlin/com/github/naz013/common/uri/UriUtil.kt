package com.github.naz013.common.uri

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.github.naz013.logging.Logger
import java.io.File

object UriUtil {

  const val URI_MIME = "application/x-arc-uri-list"
  const val ANY_MIME = "any"
  const val IMAGE_MIME = "image/*"

  fun getUri(context: Context, filePath: String, applicationId: String): Uri? {
    Logger.d("getUri: $applicationId, $filePath")
    return try {
      FileProvider.getUriForFile(context, "$applicationId.provider", File(filePath))
    } catch (e: Throwable) {
      null
    }
  }

  fun getUri(context: Context, file: File, applicationId: String): Uri? {
    Logger.d("getUri: $applicationId, $file")
    return try {
      FileProvider.getUriForFile(context, "$applicationId.provider", file)
    } catch (e: Throwable) {
      null
    }
  }
}

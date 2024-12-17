package com.elementary.tasks.core.utils

import android.content.Context
import android.net.Uri
import androidx.annotation.RequiresPermission
import androidx.core.content.FileProvider
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.io.CacheUtil
import com.github.naz013.logging.Logger
import java.io.File

object UriUtil {

  const val URI_MIME = "application/x-arc-uri-list"
  const val ANY_MIME = "any"
  const val IMAGE_MIME = "image/*"

  @RequiresPermission(Permissions.READ_EXTERNAL)
  fun obtainPath(cacheUtil: CacheUtil, uri: Uri, onReady: (Boolean, String?) -> Unit) {
    launchDefault {
      try {
        val path = cacheUtil.cacheFile(uri)
        if (path == null) {
          withUIContext { onReady.invoke(false, null) }
        } else {
          withUIContext { onReady.invoke(true, path) }
        }
      } catch (e: Throwable) {
        withUIContext { onReady.invoke(false, null) }
      }
    }
  }

  fun getUri(context: Context, filePath: String): Uri? {
    Logger.d("getUri: ${BuildConfig.APPLICATION_ID}, $filePath")
    return try {
      FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", File(filePath))
    } catch (e: Throwable) {
      null
    }
  }

  fun getUri(context: Context, file: File): Uri? {
    Logger.d("getUri: ${BuildConfig.APPLICATION_ID}, $file")
    return try {
      FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
    } catch (e: Throwable) {
      null
    }
  }
}

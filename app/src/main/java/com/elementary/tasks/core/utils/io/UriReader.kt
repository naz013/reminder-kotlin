package com.elementary.tasks.core.utils.io

import android.content.ContentResolver
import android.net.Uri
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.os.ContextProvider

class UriReader(
  private val contextProvider: ContextProvider
) {

  fun readBirthdayObject(uri: Uri): Birthday? {
    return if (ContentResolver.SCHEME_CONTENT != uri.scheme) {
      val any = MemoryUtil.readFromUri(contextProvider.context, uri, FileConfig.FILE_NAME_BIRTHDAY)
      if (any != null && any is Birthday) {
        any
      } else {
        null
      }
    } else {
      null
    }
  }
}

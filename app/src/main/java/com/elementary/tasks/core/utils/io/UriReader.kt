package com.elementary.tasks.core.utils.io

import android.content.ContentResolver
import android.net.Uri
import com.elementary.tasks.core.cloud.FileConfig
import com.github.naz013.domain.Birthday
import com.github.naz013.feature.common.android.ContextProvider

class UriReader(
  private val contextProvider: ContextProvider
) {

  fun readObject(uri: Uri, source: String = ""): Any? {
    return MemoryUtil.readFromUri(contextProvider.context, uri, source)
  }

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

package com.github.naz013.common.intent

import android.content.Context
import android.content.Intent
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.common.uri.UriUtil
import java.io.File

class IntentFactory(
  private val context: Context,
  private val buildInfo: BuildInfo,
) {

  fun createFileUriIntent(file: File, fileType: String = "*/*"): Intent {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = fileType
    val uri = UriUtil.getUri(context, file, buildInfo.applicationId)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    return intent
  }
}

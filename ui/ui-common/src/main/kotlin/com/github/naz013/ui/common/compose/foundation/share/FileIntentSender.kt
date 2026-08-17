package com.github.naz013.ui.common.compose.foundation.share

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.github.naz013.ui.common.R
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.common.uri.UriUtil
import com.github.naz013.logging.Logger
import org.koin.compose.koinInject
import java.io.File

interface FileIntentSender {
  fun send(text: String?, file: File)
}

class FileIntentSenderImpl(
  private val context: Context,
  private val buildInfo: BuildInfo,
) : FileIntentSender {
  override fun send(text: String?, file: File) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "*/*"
    intent.putExtra(Intent.EXTRA_SUBJECT, text)
    val uri = UriUtil.getUri(context, file, buildInfo.applicationId)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    try {
      context.startActivity(
        Intent.createChooser(
          intent,
          context.getString(R.string.share_send_email)
        )
      )
    } catch (e: Exception) {
      Logger.w("FileIntentSender", "App not found, exception: ${e.message}")
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }
}

@Composable
fun rememberFileIntentSender(): FileIntentSender {
  val context = LocalContext.current
  val buildInfo = koinInject<BuildInfo>()
  return FileIntentSenderImpl(context, buildInfo)
}

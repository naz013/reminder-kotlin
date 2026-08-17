package com.github.naz013.feature.settings

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.github.naz013.common.uri.UriUtil
import com.github.naz013.ui.common.R
import java.io.File

interface SendEmailResolver {
  fun send(
    email: String,
    subject: String,
    message: String,
    file: File?,
  )
}

private class SendEmailResolverImpl(
  private val context: Context,
  private val applicationId: String,
) : SendEmailResolver {
  override fun send(email: String, subject: String, message: String, file: File?) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, message)
    if (file != null) {
      val uri = UriUtil.getUri(context, file, applicationId)
      intent.putExtra(Intent.EXTRA_STREAM, uri)
      intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    try {
      context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_send_email)))
    } catch (_: Exception) {
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }
}

@Composable
fun rememberSendEmailResolver(applicationId: String): SendEmailResolver {
  val context = LocalContext.current
  return SendEmailResolverImpl(context, applicationId)
}

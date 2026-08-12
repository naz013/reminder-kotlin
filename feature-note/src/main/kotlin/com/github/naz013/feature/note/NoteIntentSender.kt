package com.github.naz013.feature.note

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.github.naz013.common.uri.UriUtil
import com.github.naz013.logging.Logger
import java.io.File

internal interface NoteIntentSender {
  fun send(text: String?, file: File)
}

private class NoteIntentSenderImpl(
  private val context: Context,
  private val applicationId: String,
) : NoteIntentSender {
  override fun send(text: String?, file: File) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    var title = "Note"
    var note = ""
    if (text != null) {
      if (text.length > 100) {
        title = text.take(48)
        title = "$title..."
      }
      if (text.length > 150) {
        note = text.take(135)
        note = "$note..."
      }
    }
    intent.putExtra(Intent.EXTRA_SUBJECT, title)
    intent.putExtra(Intent.EXTRA_TEXT, note)
    val uri = UriUtil.getUri(context, file, applicationId)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    try {
      context.startActivity(
        Intent.createChooser(
          intent,
          context.getString(R.string.share_send_note)
        )
      )
    } catch (e: Exception) {
      Logger.w("NoteIntentSender", "App not found, exception: ${e.message}")
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }
}

@Composable
internal fun rememberNoteIntentSender(applicationId: String): NoteIntentSender {
  val context = LocalContext.current
  return NoteIntentSenderImpl(context, applicationId)
}

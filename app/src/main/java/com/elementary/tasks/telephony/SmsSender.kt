package com.elementary.tasks.telephony

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.elementary.tasks.R

interface SmsSender {
  fun send(number: String, message: String?)
}

class SmsSenderImpl(
  private val context: Context,
) : SmsSender {
  override fun send(number: String, message: String?) {
    if (TextUtils.isEmpty(number)) {
      return
    }
    val smsIntent = Intent(Intent.ACTION_VIEW)
    smsIntent.data = "sms:$number".toUri()
    smsIntent.putExtra("sms_body", message)
    smsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    try {
      context.startActivity(smsIntent)
    } catch (e: Exception) {
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }
}

@Composable
fun rememberSmsSender(): SmsSender {
  val context = LocalContext.current
  return SmsSenderImpl(context)
}

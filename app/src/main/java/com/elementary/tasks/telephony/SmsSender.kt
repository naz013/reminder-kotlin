package com.elementary.tasks.telephony

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

interface SmsSender {
  fun send(number: String, message: String?)
}

class SmsSenderImpl(
  private val context: Context,
) : SmsSender {
  override fun send(number: String, message: String?) {
    TODO("Not yet implemented")
  }
}

@Composable
fun rememberSmsSender(): SmsSender {
  val context = LocalContext.current
  return SmsSenderImpl(context)
}

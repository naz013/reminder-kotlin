package com.github.naz013.ui.common.compose.foundation.snackbar

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.github.naz013.logging.Logger

enum class ToastDuration(val value: Int) {
  Long(Toast.LENGTH_LONG),
  Short(Toast.LENGTH_SHORT),
}

interface ToastDispatcher {
  fun showToast(
    messageRes: Int? = null,
    message: String? = null,
    duration: ToastDuration = ToastDuration.Short,
  )
}

@Composable
fun rememberToastDispatcher(): ToastDispatcher {
  val toastData = remember { mutableStateOf(ToastData()) }
  val showToast = remember { mutableStateOf(false) }

  val context = LocalContext.current
  if (showToast.value) {
    toastData.value.message?.let {
      Toast.makeText(context, it, toastData.value.duration.value).show()
    } ?: toastData.value.messageRes?.let {
      Toast.makeText(context, it, toastData.value.duration.value).show()
    }
    showToast.value = false
  }

  return object : ToastDispatcher {
    override fun showToast(messageRes: Int?, message: String?, duration: ToastDuration) {
      Logger.i("ToastDispatcher", "Showing toast with message: ${Logger.private(message)}")
      toastData.value = ToastData(messageRes, message, duration)
      showToast.value = true
    }
  }
}

private data class ToastData(
  val messageRes: Int? = null,
  val message: String? = null,
  val duration: ToastDuration = ToastDuration.Short,
)



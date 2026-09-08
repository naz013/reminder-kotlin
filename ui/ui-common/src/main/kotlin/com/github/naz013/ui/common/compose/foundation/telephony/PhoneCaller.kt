package com.github.naz013.ui.common.compose.foundation.telephony

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.R

interface PhoneCaller {
  fun call(number: String)
}

class PhoneCallerImpl(
  private val context: Context,
) : PhoneCaller {
  override fun call(number: String) {
    if (TextUtils.isEmpty(number)) {
      return
    }
    val callIntent = Intent(Intent.ACTION_CALL)
    callIntent.data = "tel:$number".toUri()
    callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    try {
      context.startActivity(callIntent)
    } catch (e: ActivityNotFoundException) {
      Logger.w("PhoneCaller", "App not found, exception: ${e.message}")
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }
}

@Composable
fun rememberPhoneCaller(): PhoneCaller {
  val context = LocalContext.current
  return PhoneCallerImpl(context)
}

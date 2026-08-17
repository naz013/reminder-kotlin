package com.github.naz013.ui.common.compose.foundation.telephony

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.github.naz013.ui.common.R
import androidx.core.net.toUri

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
    } catch (e: Exception) {
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }
}

@Composable
fun rememberPhoneCaller(): PhoneCaller {
  val context = LocalContext.current
  return PhoneCallerImpl(context)
}

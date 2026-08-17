package com.github.naz013.ui.common.compose.foundation.telephony

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.github.naz013.ui.common.R

interface UrlLauncher {
  fun launch(url: String)
}

class UrlLauncherImpl(
  private val context: Context,
) : UrlLauncher {
  override fun launch(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
    try {
      context.startActivity(browserIntent)
    } catch (_: Exception) {
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }
}

@Composable
fun rememberUrlLauncher(): UrlLauncher {
  val context = LocalContext.current
  return UrlLauncherImpl(context)
}

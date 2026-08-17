package com.github.naz013.ui.common.compose.foundation.telephony

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.github.naz013.ui.common.R

interface ApplicationLauncher {
  fun launch(packageName: String)
}

class ApplicationLauncherImpl(
  private val context: Context,
) : ApplicationLauncher {
  override fun launch(packageName: String) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    try {
      context.startActivity(launchIntent)
    } catch (_: Exception) {
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }
}

@Composable
fun rememberApplicationLauncher(): ApplicationLauncher {
  val context = LocalContext.current
  return ApplicationLauncherImpl(context)
}

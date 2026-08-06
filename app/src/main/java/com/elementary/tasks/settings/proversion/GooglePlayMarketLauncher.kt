package com.elementary.tasks.settings.proversion

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.elementary.tasks.R
import androidx.core.net.toUri

interface GooglePlayMarketLauncher {
  fun launch(packageName: String)
  fun launchSelf()
}

private class GooglePlayMarketLauncherImpl(
  private val context: Context,
) : GooglePlayMarketLauncher {

  override fun launchSelf() {
    launch(context.packageName)
  }

  override fun launch(packageName: String) {
    val uri = ("market://details?id=$packageName").toUri()
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    try {
      context.startActivity(goToMarket)
    } catch (_: ActivityNotFoundException) {
      Toast
        .makeText(
          context,
          context.getString(R.string.could_not_launch_market),
          Toast.LENGTH_SHORT,
        ).show()
    }
  }
}

@Composable
fun rememberGooglePlayMarketLauncher(): GooglePlayMarketLauncher {
  val context = LocalContext.current
  return GooglePlayMarketLauncherImpl(context)
}

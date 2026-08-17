package com.github.naz013.feature.settings.proversion

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.github.naz013.ui.common.R

interface GooglePlayMarketLauncher {
  /**
   * [referrer] is passed through to the Play Store as the standard `referrer` install-referrer
   * query param, so the target app can read it back via the Play Install Referrer Library and
   * attribute the resulting install to whoever launched this deep link - the only attribution
   * available when the two apps are separate paid/free listings rather than one app with an
   * in-app purchase.
   */
  fun launch(packageName: String, referrer: String? = null)
  fun launchSelf()
}

private class GooglePlayMarketLauncherImpl(
  private val context: Context,
) : GooglePlayMarketLauncher {

  override fun launchSelf() {
    launch(context.packageName)
  }

  override fun launch(packageName: String, referrer: String?) {
    val referrerSuffix = referrer?.let { "&referrer=${Uri.encode(it)}" }.orEmpty()
    val uri = ("market://details?id=$packageName$referrerSuffix").toUri()
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

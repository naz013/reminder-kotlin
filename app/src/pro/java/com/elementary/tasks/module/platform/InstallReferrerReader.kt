package com.elementary.tasks.module.platform

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.ProInstallAttributedEvent
import com.github.naz013.logging.Logger

/**
 * Reads the Play Store install-referrer value once, at first launch, and logs it as
 * [ProInstallAttributedEvent] - the only way to attribute a PRO install back to the free app's
 * "Buy PRO" deep link (see GooglePlayMarketLauncher), since PRO is a separate paid listing
 * rather than an in-app purchase inside the free app.
 */
class InstallReferrerReader(
  private val context: Context,
  private val prefs: Prefs,
  private val analyticsEventSender: AnalyticsEventSender,
) {
  fun readOnce() {
    if (prefs.installReferrerLogged) return

    val client = InstallReferrerClient.newBuilder(context).build()
    client.startConnection(
      object : InstallReferrerStateListener {
        override fun onInstallReferrerSetupFinished(responseCode: Int) {
          if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
            val referrer = runCatching { client.installReferrer.installReferrer }.getOrNull()
            if (!referrer.isNullOrBlank()) {
              analyticsEventSender.send(ProInstallAttributedEvent(source = referrer))
            }
            prefs.installReferrerLogged = true
          } else {
            Logger.i(TAG, "Install referrer unavailable, response code: $responseCode")
          }
          runCatching { client.endConnection() }
        }

        override fun onInstallReferrerServiceDisconnected() = Unit
      },
    )
  }

  companion object {
    private const val TAG = "InstallReferrerReader"
  }
}

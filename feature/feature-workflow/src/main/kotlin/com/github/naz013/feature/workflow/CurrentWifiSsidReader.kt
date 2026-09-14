package com.github.naz013.feature.workflow

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager

/**
 * Reads the SSID of the currently-connected WiFi network, if any - shared by `app`'s
 * `WifiConnectionMonitor` (real-time, via [ConnectivityManager.registerNetworkCallback]) and
 * [RunWorkflowWifiPollTask] (periodic fallback poll) so the quoted-SSID/unknown-SSID handling
 * lives in exactly one place. Null whenever the SSID can't be read - not connected to WiFi, or
 * `ACCESS_FINE_LOCATION` isn't granted (required by [WifiInfo.getSSID] pre-API 33), in which case
 * it returns [WifiManager.UNKNOWN_SSID] rather than throwing.
 */
class CurrentWifiSsidReader(
  private val context: Context
) {
  private val connectivityManager: ConnectivityManager? =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

  /** The active network's SSID, if it's WiFi. */
  fun read(): String? = connectivityManager?.activeNetwork?.let { read(it) }

  /** [network]'s SSID - for [ConnectivityManager.NetworkCallback.onAvailable], where the newly
   * available network isn't necessarily [ConnectivityManager.getActiveNetwork] yet. Not usable
   * from `onLost` - a lost network's capabilities are no longer queryable by that point, which is
   * why `WifiConnectionMonitor` tracks the SSID itself instead of re-reading it there. */
  fun read(network: Network): String? {
    val wifiInfo = connectivityManager?.getNetworkCapabilities(network)?.transportInfo as? WifiInfo ?: return null
    val ssid = wifiInfo.ssid
    if (ssid.isNullOrEmpty() || ssid == WifiManager.UNKNOWN_SSID) return null
    return ssid.removeSurrounding("\"")
  }
}

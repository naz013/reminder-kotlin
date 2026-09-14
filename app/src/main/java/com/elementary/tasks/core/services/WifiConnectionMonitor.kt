package com.elementary.tasks.core.services

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.logic.workflow.WorkflowConfig
import com.github.naz013.logic.workflow.WorkflowTriggerRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Fires `WorkflowTrigger.WifiConnected`/`WifiDisconnected` rules via
 * [ConnectivityManager.registerNetworkCallback]. Manifest-registered receivers stopped getting
 * `CONNECTIVITY_ACTION`/`NETWORK_STATE_CHANGED_ACTION` starting API 24, so a callback registered
 * once at process start (see [start], called from `ReminderApp.onCreate`) is the closest
 * equivalent to "always listening" available on modern Android. Unlike
 * [BluetoothConnectionReceiver]'s manifest registration, this doesn't survive process death -
 * WiFi triggers are best-effort while the app process is alive, a known v1 limitation (same class
 * of caveat as `TableChangeNotifier`'s `LocalBroadcastManager` use - see
 * docs/workflow-engine-research.md).
 */
class WifiConnectionMonitor(
  private val context: Context,
  private val workflowConfig: WorkflowConfig,
  private val workflowTriggerRunner: WorkflowTriggerRunner,
  private val dispatcherProvider: DispatcherProvider,
) {
  private val connectivityManager: ConnectivityManager? =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
  private var registered = false

  private val callback =
    object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) = onWifiChanged(network, connected = true)

      override fun onLost(network: Network) = onWifiChanged(network, connected = false)
    }

  /** No-op if [WorkflowConfig.isEnabled] is off or the callback is already registered - safe to
   * call more than once (e.g. if the workflow feature flag toggle ever calls this reactively). */
  fun start() {
    if (!workflowConfig.isEnabled || registered) return
    val request =
      NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()
    runCatching { connectivityManager?.registerNetworkCallback(request, callback) }
      .onSuccess { registered = true }
      .onFailure { Logger.e(TAG, "Failed to register WiFi network callback", it) }
  }

  private fun onWifiChanged(network: Network, connected: Boolean) {
    val ssid = currentSsid(network) ?: return
    CoroutineScope(dispatcherProvider.io()).launch {
      if (connected) {
        workflowTriggerRunner.onWifiConnected(ssid)
      } else {
        workflowTriggerRunner.onWifiDisconnected(ssid)
      }
    }
  }

  /** Null when the SSID can't be read - e.g. `ACCESS_FINE_LOCATION` isn't granted, in which case
   * [WifiInfo.getSSID] returns [WifiManager.UNKNOWN_SSID] rather than throwing. Strips the
   * surrounding quotes [WifiInfo.getSSID] wraps non-hex SSIDs in, matching how
   * `WorkflowTrigger.WifiConnected.ssid` is stored/compared. */
  private fun currentSsid(network: Network): String? {
    val wifiInfo = connectivityManager?.getNetworkCapabilities(network)?.transportInfo as? WifiInfo ?: return null
    val ssid = wifiInfo.ssid
    if (ssid.isNullOrEmpty() || ssid == WifiManager.UNKNOWN_SSID) return null
    return ssid.removeSurrounding("\"")
  }

  companion object {
    private const val TAG = "WifiConnectionMonitor"
  }
}

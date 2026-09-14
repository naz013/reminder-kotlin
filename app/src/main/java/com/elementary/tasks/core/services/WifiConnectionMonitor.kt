package com.elementary.tasks.core.services

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.workflow.CurrentWifiSsidReader
import com.github.naz013.feature.workflow.WorkflowWifiPollState
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
 * [BluetoothConnectionReceiver]'s manifest registration, this doesn't survive process death, so
 * `RunWorkflowWifiPollTask` runs periodically as a fallback to catch transitions missed while the
 * app process was dead - see that class's doc for the full picture.
 *
 * [WorkflowWifiPollState.lastKnownSsid] is this monitor's own memory of "what SSID is currently
 * connected", kept up to date here on every fired transition - not just bookkeeping for the poll
 * task's benefit: `onLost`'s [Network] argument is already torn down by the time the callback
 * fires, so [CurrentWifiSsidReader] can't re-read its SSID at that point either. Tracking the
 * last-connected SSID ourselves (rather than trying to read it from the lost network) is what
 * makes the disconnected trigger fire at all.
 */
class WifiConnectionMonitor(
  private val context: Context,
  private val workflowConfig: WorkflowConfig,
  private val workflowTriggerRunner: WorkflowTriggerRunner,
  private val dispatcherProvider: DispatcherProvider,
  private val currentWifiSsidReader: CurrentWifiSsidReader,
  private val workflowWifiPollState: WorkflowWifiPollState,
) {
  private val connectivityManager: ConnectivityManager? =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
  private var registered = false

  private val callback =
    object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        val ssid = currentWifiSsidReader.read(network) ?: return
        fire(connected = true, ssid = ssid)
      }

      override fun onLost(network: Network) {
        val ssid = workflowWifiPollState.lastKnownSsid ?: return
        fire(connected = false, ssid = ssid)
      }
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

  private fun fire(connected: Boolean, ssid: String) {
    workflowWifiPollState.lastKnownSsid = if (connected) ssid else null
    CoroutineScope(dispatcherProvider.io()).launch {
      if (connected) {
        workflowTriggerRunner.onWifiConnected(ssid)
      } else {
        workflowTriggerRunner.onWifiDisconnected(ssid)
      }
    }
  }

  companion object {
    private const val TAG = "WifiConnectionMonitor"
  }
}

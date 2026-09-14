package com.github.naz013.feature.workflow

import android.content.Context

/**
 * Persists the last WiFi SSID a `WorkflowTrigger.WifiConnected`/`WifiDisconnected` rule was fired
 * for - the shared source of truth between `app`'s real-time `WifiConnectionMonitor` (updates it
 * as connect/disconnect events happen) and [RunWorkflowWifiPollTask] (the periodic fallback,
 * which diffs the current SSID against this to catch transitions missed while the app process was
 * dead - see that class's doc for why a fallback is needed at all). A plain `SharedPreferences`
 * value, not Room-backed: this is transient connectivity bookkeeping, not user data, and needs to
 * survive process death but nothing more.
 */
class WorkflowWifiPollState(
  context: Context
) {
  private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  var lastKnownSsid: String?
    get() = prefs.getString(KEY_LAST_KNOWN_SSID, null)
    set(value) {
      prefs.edit().putString(KEY_LAST_KNOWN_SSID, value).apply()
    }

  private companion object {
    const val PREFS_NAME = "workflow_wifi_poll_state"
    const val KEY_LAST_KNOWN_SSID = "last_known_ssid"
  }
}

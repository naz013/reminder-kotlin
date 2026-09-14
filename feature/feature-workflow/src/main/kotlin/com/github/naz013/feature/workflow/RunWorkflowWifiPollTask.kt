package com.github.naz013.feature.workflow

import com.github.naz013.logging.Logger
import com.github.naz013.logic.workflow.WorkflowTriggerRunner
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

/**
 * Durability fallback for `WorkflowTrigger.WifiConnected`/`WifiDisconnected`: `app`'s
 * `WifiConnectionMonitor` fires those in real time via [android.net.ConnectivityManager
 * .registerNetworkCallback], but that registration doesn't survive process death, so a
 * connect/disconnect that happens while the app isn't running would otherwise never fire (same
 * class of gap `TableChangeNotifier`'s `LocalBroadcastManager` use has, per
 * docs/workflow-engine-research.md). Run periodically (see `JobScheduler
 * .scheduleWorkflowWifiPollCheck`), this diffs the currently-connected SSID against
 * [WorkflowWifiPollState.lastKnownSsid] and fires whatever [WorkflowTriggerRunner] would have
 * fired for the transition(s) missed in between - at the poll interval's granularity, not
 * real-time, consistent with this engine's other periodic-poll triggers.
 */
class RunWorkflowWifiPollTask(
  private val currentWifiSsidReader: CurrentWifiSsidReader,
  private val workflowWifiPollState: WorkflowWifiPollState,
  private val workflowTriggerRunner: WorkflowTriggerRunner
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter
  ): TaskResult {
    val previousSsid = workflowWifiPollState.lastKnownSsid
    val currentSsid = currentWifiSsidReader.read()
    if (previousSsid != currentSsid) {
      previousSsid?.let { workflowTriggerRunner.onWifiDisconnected(it) }
      currentSsid?.let { workflowTriggerRunner.onWifiConnected(it) }
      workflowWifiPollState.lastKnownSsid = currentSsid
    }
    Logger.i(TASK_KEY, "Ran WiFi connectivity poll for workflow rules.")
    return TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "run_workflow_wifi_poll"
  }
}

package com.github.naz013.feature.settings.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.feature.settings.export.work.ObservableBackupTask
import com.github.naz013.feature.settings.export.work.ObservableEraseDataTask
import com.github.naz013.feature.settings.export.work.ObservableSyncTask
import com.github.naz013.logging.Logger
import com.github.naz013.logic.schedule.WorkerNetworkType
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.ui.common.R
import com.github.naz013.workapi.ExistingWorkPolicy
import com.github.naz013.workapi.NetworkRequirement
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler
import com.github.naz013.workapi.WorkState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class CloudBackupSettingsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val cloudApiProvider: CloudApiProvider,
  private val jobScheduler: JobSchedulerApi,
  private val prefs: CloudBackupSettingsPreferences,
  private val textProvider: TextProvider,
  private val workScheduler: WorkScheduler,
) : ViewModel() {

  private val _state = MutableStateFlow(CloudBackupSettingsState())
  val state = _state.stateInWhileSubscribed(CloudBackupSettingsState())
    .onStart { loadCloudApis() }

  fun onAutoBackupIntervalClick() {
    val options = syncStates()
    _state.update {
      it.copy(
        dialog =
          CloudBackupDialog.AutoBackupInterval(
            options = options,
            selectedIndex = positionFromState(prefs.autoBackupState),
          ),
      )
    }
  }

  fun onAutoBackupIntervalSelected(position: Int) {
    prefs.autoBackupState = stateFromPosition(position)
    refreshState()
    Logger.i(TAG, "Auto backup interval changed, rescheduling auto backup job.")
    jobScheduler.scheduleAutoBackup()
  }

  fun onNetworkTypeClick() {
    val options = networkTypeNames()
    _state.update {
      it.copy(
        dialog =
          CloudBackupDialog.NetworkType(
            options = options,
            selectedIndex = prefs.workerNetworkType.ordinal,
          ),
      )
    }
  }

  fun onNetworkTypeSelected(position: Int) {
    prefs.workerNetworkType = WorkerNetworkType.entries[position]
    refreshState()
  }

  fun onEraseClick() {
    _state.update { it.copy(dialog = CloudBackupDialog.EraseConfirm) }
  }

  fun onEraseConfirmed() {
    dismissDialog()
    runObservableWork(
      taskKey = ObservableEraseDataTask.TASK_KEY,
      progressKey = ObservableEraseDataTask.KEY_IS_IN_PROGRESS,
    )
  }

  fun onBackupNowClick() {
    runObservableWork(
      taskKey = ObservableBackupTask.TASK_KEY,
      progressKey = ObservableBackupTask.KEY_IS_IN_PROGRESS,
    )
  }

  fun onSyncNowClick() {
    runObservableWork(
      taskKey = ObservableSyncTask.TASK_KEY,
      progressKey = ObservableSyncTask.KEY_IS_IN_PROGRESS,
    )
  }

  fun onDialogDismiss() {
    dismissDialog()
  }

  private fun runObservableWork(
    taskKey: String,
    progressKey: String,
  ) {
    _state.update { it.copy(isInProgress = true) }
    workScheduler.enqueueUnique(
      uniqueName = taskKey,
      policy = ExistingWorkPolicy.REPLACE,
      request = WorkRequest(taskKey = taskKey, networkRequirement = NetworkRequirement.CONNECTED),
    )
    viewModelScope.launch(dispatcherProvider.default()) {
      workScheduler.observeUniqueWork(taskKey).collect { workState ->
        val inProgress =
          when (workState) {
            is WorkState.Enqueued -> true
            is WorkState.Running -> workState.progress.getBoolean(progressKey, true)
            is WorkState.Succeeded, is WorkState.Failed, is WorkState.Cancelled -> false
            is WorkState.Blocked -> return@collect
          }
        _state.update { it.copy(isInProgress = inProgress) }
      }
    }
  }

  private fun dismissDialog() {
    _state.update { it.copy(dialog = null) }
  }

  private fun refreshState() {
    val refreshed = buildState()
    _state.update {
      it.copy(
        autoBackupStateName = refreshed.autoBackupStateName,
        networkTypeName = refreshed.networkTypeName,
        dialog = null,
      )
    }
  }

  private fun loadCloudApis() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val apis = cloudApiProvider.getAllowedCloudApis()
      Logger.i(TAG, "Loaded cloud APIs: ${apis.size}")
      withContext(dispatcherProvider.main()) {
        _state.update { it.copy(hasAnyCloudApi = apis.isNotEmpty()) }
      }
    }
  }

  private fun buildState(): CloudBackupSettingsState =
    CloudBackupSettingsState(
      autoBackupStateName = syncStates()[positionFromState(prefs.autoBackupState)],
      networkTypeName =
        networkTypeNames().getOrElse(prefs.workerNetworkType.ordinal) {
          textProvider.getString(R.string.network_type_any_network)
        },
    )

  private fun positionFromState(state: Int): Int =
    when (state) {
      1 -> 1
      6 -> 2
      12 -> 3
      24 -> 4
      48 -> 5
      else -> 0
    }

  private fun stateFromPosition(position: Int): Int =
    when (position) {
      1 -> 1
      2 -> 6
      3 -> 12
      4 -> 24
      5 -> 48
      else -> 0
    }

  private fun syncStates(): List<String> {
    val prefix = textProvider.getString(R.string.auto_backup_every) + " "
    return listOf(
      textProvider.getString(R.string.disabled),
      prefix + textProvider.getString(R.string.one_hour),
      prefix + textProvider.getString(R.string.six_hours),
      prefix + textProvider.getString(R.string.twelve_hours),
      prefix + textProvider.getString(R.string.one_day),
      prefix + textProvider.getString(R.string.two_days),
    )
  }

  private fun networkTypeNames(): List<String> =
    listOf(
      textProvider.getString(R.string.network_type_any_network),
      textProvider.getString(R.string.network_type_wifi_only),
      textProvider.getString(R.string.network_type_cellular),
    )

  companion object {
    private const val TAG = "CloudBackupViewModel"
  }
}

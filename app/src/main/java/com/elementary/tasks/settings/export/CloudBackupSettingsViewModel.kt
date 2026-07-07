package com.elementary.tasks.settings.export

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.worker.WorkerNetworkType
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.settings.export.work.ObservableBackupTask
import com.elementary.tasks.settings.export.work.ObservableEraseDataTask
import com.elementary.tasks.settings.export.work.ObservableSyncTask
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.workapi.ExistingWorkPolicy
import com.github.naz013.workapi.NetworkRequirement
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler
import com.github.naz013.workapi.WorkState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CloudBackupSettingsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val cloudApiProvider: CloudApiProvider,
  private val jobScheduler: JobScheduler,
  private val prefs: Prefs,
  private val textProvider: TextProvider,
  private val workScheduler: WorkScheduler,
) : ViewModel(),
  DefaultLifecycleObserver {
  private val _hasAnyCloudApi = mutableLiveDataOf<Boolean>()
  val hasAnyCloudApi = _hasAnyCloudApi.toLiveData()

  val state: StateFlow<CloudBackupSettingsState> field = MutableStateFlow(buildState())

  val isInProgress: StateFlow<Boolean> field = MutableStateFlow(false)

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    loadCloudApis()
  }

  fun onAutoBackupIntervalClick() {
    val options = syncStates()
    state.update {
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
    dismissDialog()
    Logger.i(TAG, "Auto backup interval changed, rescheduling auto backup job.")
    jobScheduler.scheduleAutoBackup()
  }

  fun onNetworkTypeClick() {
    val options = networkTypeNames()
    state.update {
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
    dismissDialog()
  }

  fun onEraseClick() {
    state.update { it.copy(dialog = CloudBackupDialog.EraseConfirm) }
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
    isInProgress.update { true }
    workScheduler.enqueueUnique(
      uniqueName = taskKey,
      policy = ExistingWorkPolicy.REPLACE,
      request = WorkRequest(taskKey = taskKey, networkRequirement = NetworkRequirement.CONNECTED),
    )
    viewModelScope.launch(dispatcherProvider.default()) {
      workScheduler.observeUniqueWork(taskKey).collect { workState ->
        when (workState) {
          is WorkState.Enqueued -> isInProgress.update { true }
          is WorkState.Running -> isInProgress.update { workState.progress.getBoolean(progressKey, true) }
          is WorkState.Succeeded, is WorkState.Failed, is WorkState.Cancelled -> isInProgress.update { false }
          is WorkState.Blocked -> Unit
        }
      }
    }
  }

  private fun dismissDialog() {
    state.update { buildState().copy(dialog = null) }
  }

  private fun loadCloudApis() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val apis = cloudApiProvider.getAllowedCloudApis()
      Logger.i(TAG, "Loaded cloud APIs: ${apis.size}")
      withContext(dispatcherProvider.main()) {
        _hasAnyCloudApi.value = apis.isNotEmpty()
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

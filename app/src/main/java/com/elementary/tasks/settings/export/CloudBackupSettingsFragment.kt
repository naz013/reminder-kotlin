package com.elementary.tasks.settings.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.elementary.tasks.R
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.settings.export.work.ObservableBackupWorker
import com.elementary.tasks.settings.export.work.ObservableEraseDataWorker
import com.elementary.tasks.settings.export.work.ObservableSyncWorker
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CloudBackupSettingsFragment : BaseComposeToolbarFragment() {

  private val viewModel by viewModel<CloudBackupSettingsViewModel>()
  private val observableWorkerManager by inject<ObservableWorkerManager>()
  private var setInProgress: ((Boolean) -> Unit)? = null

  override fun onDestroy() {
    super.onDestroy()
    observableWorkerManager.unsubscribe()
  }

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    val hasAnyCloudApi by viewModel.hasAnyCloudApi.observeAsState(false)
    var isInProgress by remember { mutableStateOf(false) }
    setInProgress = { isInProgress = it }
    viewModel.navigationEvent.ObserveEvent { handleEvent(it) }

    LaunchedEffect(viewModel) { lifecycle.addObserver(viewModel) }

    CloudBackupSettingsScreen(
      state = state,
      hasAnyCloudApi = hasAnyCloudApi,
      isInProgress = isInProgress,
      onCloudServicesClick = {
        safeNavigation { CloudBackupSettingsFragmentDirections.actionExportSettingsFragmentToFragmentCloudDrives() }
      },
      onAutoBackupIntervalClick = viewModel::onAutoBackupIntervalClick,
      onAutoBackupIntervalSelected = viewModel::onAutoBackupIntervalSelected,
      onNetworkTypeClick = viewModel::onNetworkTypeClick,
      onNetworkTypeSelected = viewModel::onNetworkTypeSelected,
      onEraseClick = viewModel::onEraseClick,
      onEraseConfirmed = viewModel::onEraseConfirmed,
      onBackupNowClick = ::runBackup,
      onSyncNowClick = ::runSync,
      onDialogDismiss = viewModel::onDialogDismiss,
    )
  }

  private fun handleEvent(event: CloudBackupSettingsEvent) {
    when (event) {
      CloudBackupSettingsEvent.RunErase -> runErase()
    }
  }

  private fun runBackup() {
    beginObservedWork()
    ObservableBackupWorker.schedule(requireContext())
    observableWorkerManager.observeWork(
      viewLifecycleOwner,
      ObservableBackupWorker.getWorkTag(),
      ObservableBackupWorker.KEY_IS_IN_PROGRESS,
    )
  }

  private fun runSync() {
    beginObservedWork()
    ObservableSyncWorker.schedule(requireContext())
    observableWorkerManager.observeWork(
      viewLifecycleOwner,
      ObservableSyncWorker.getWorkTag(),
      ObservableSyncWorker.KEY_IS_IN_PROGRESS,
    )
  }

  private fun runErase() {
    beginObservedWork()
    ObservableEraseDataWorker.schedule(requireContext())
    observableWorkerManager.observeWork(
      viewLifecycleOwner,
      ObservableEraseDataWorker.getWorkTag(),
      ObservableEraseDataWorker.KEY_IS_IN_PROGRESS,
    )
  }

  private fun beginObservedWork() {
    setInProgress?.invoke(true)
    observableWorkerManager.listener = { setInProgress?.invoke(it) }
    observableWorkerManager.onEnd = { setInProgress?.invoke(false) }
  }

  override fun getTitle(): String = getString(R.string.cloud_backup)
}

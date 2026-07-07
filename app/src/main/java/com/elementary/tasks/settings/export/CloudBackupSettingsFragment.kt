package com.elementary.tasks.settings.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.elementary.tasks.R
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class CloudBackupSettingsFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<CloudBackupSettingsViewModel>()

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(viewModel) { lifecycle.addObserver(viewModel) }
    val hasAnyCloudApi by viewModel.hasAnyCloudApi.observeAsState(false)
    val isInProgress by viewModel.isInProgress.collectAsState()

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
      onBackupNowClick = viewModel::onBackupNowClick,
      onSyncNowClick = viewModel::onSyncNowClick,
      onDialogDismiss = viewModel::onDialogDismiss,
    )
  }

  override fun getTitle(): String = getString(R.string.cloud_backup)
}

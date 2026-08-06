package com.elementary.tasks.settings.export

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.compose.rememberDropboxLogin
import com.elementary.tasks.core.cloud.compose.rememberGoogleDriveLogin
import com.elementary.tasks.core.cloud.compose.rememberGoogleTasksLogin
import com.elementary.tasks.core.os.compose.rememberPermissionRequesterRationale
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.settings.SettingsScaffold
import com.elementary.tasks.settings.export.services.CloudServicesScreen
import com.elementary.tasks.settings.export.services.CloudServicesState
import com.elementary.tasks.settings.export.services.CloudServicesViewModel
import com.github.naz013.common.Permissions
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.exportEntries(backStack: MutableList<NavKey>) {
  entry<ExportNavKey.CloudBackup> { CloudBackupEntry(backStack) }
  entry<ExportNavKey.CloudServices> { CloudServicesEntry(backStack) }
}

@Composable
private fun CloudBackupEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<CloudBackupSettingsViewModel>()
  val state by viewModel.state.collectAsState(CloudBackupSettingsState())

  SettingsScaffold(
    title = stringResource(R.string.cloud_backup),
    onBackClick = { backStack.removeLastOrNull() },
  ) { padding ->
    CloudBackupSettingsScreen(
      state = state,
      onCloudServicesClick = { backStack.add(ExportNavKey.CloudServices) },
      onAutoBackupIntervalClick = viewModel::onAutoBackupIntervalClick,
      onAutoBackupIntervalSelected = viewModel::onAutoBackupIntervalSelected,
      onNetworkTypeClick = viewModel::onNetworkTypeClick,
      onNetworkTypeSelected = viewModel::onNetworkTypeSelected,
      onEraseClick = viewModel::onEraseClick,
      onEraseConfirmed = viewModel::onEraseConfirmed,
      onBackupNowClick = viewModel::onBackupNowClick,
      onSyncNowClick = viewModel::onSyncNowClick,
      onDialogDismiss = viewModel::onDialogDismiss,
      modifier = Modifier.padding(padding),
    )
  }
}

@Composable
private fun CloudServicesEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<CloudServicesViewModel>()

  val dialogDispatcher = rememberDialogDispatcher()
  val permissionRequester = rememberPermissionRequesterRationale()
  val toastDispatcher = rememberToastDispatcher()
  val state by viewModel.state.collectAsState(CloudServicesState())

  val googleDriveLogin = rememberGoogleDriveLogin(
    onResult = { viewModel.onGoogleDriveLoginStateChanged(it) },
    onFail = { viewModel.onGoogleDriveAuthFailed() },
  )
  val googleTasksLogin = rememberGoogleTasksLogin(
    onResult = { viewModel.onGoogleTasksLoginStateChanged(it) },
    onFail = { viewModel.onGoogleTasksAuthFailed() },
  )
  val dropboxLogin = rememberDropboxLogin()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      CloudServicesViewModel.ViewModelEvent.RequestAccountsPermission -> {
        permissionRequester.request(
          permission = Permissions.GET_ACCOUNTS,
          onGranted = { viewModel.onAccountsPermissionGranted() },
        )
      }

      is CloudServicesViewModel.ViewModelEvent.ShowToast -> {
        toastDispatcher.showToast(messageRes = event.messageRes)
      }

      CloudServicesViewModel.ViewModelEvent.LogInGoogleDrive -> {
        googleDriveLogin.login()
      }

      CloudServicesViewModel.ViewModelEvent.LogOutGoogleDrive -> {
        googleDriveLogin.logOut { viewModel.onGoogleDriveLoginStateChanged(it) }
      }

      CloudServicesViewModel.ViewModelEvent.LogInGoogleTasks -> {
        googleTasksLogin.login()
      }

      CloudServicesViewModel.ViewModelEvent.LogInDropbox -> {
        dropboxLogin.login(
          onAuthResult = { viewModel.onDropboxLoginStateChanged(it) },
          onDuplicateFound = {
            dialogDispatcher.showDialog(
              textRes = R.string.other_version_detected,
              positiveButtonRes = R.string.open,
              neutralButtonRes = R.string.cancel,
              negativeButtonRes = R.string.delete,
              onPositive = { dropboxLogin.openApp() },
              onNeutral = {},
              onNegative = { dropboxLogin.deleteApp() }
            )
          }
        )
      }

      CloudServicesViewModel.ViewModelEvent.ShowLoginError -> {
        dialogDispatcher.showDialog(
          textRes = R.string.failed_to_login,
          positiveButtonRes = R.string.ok,
        )
      }
    }
  }

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(dropboxLogin, lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        dropboxLogin.checkAuthOnResume()
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  CloudServicesScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() },
    onDropboxClick = { viewModel.onDropboxClicked() },
    onGoogleDriveClick = { viewModel.onGoogleDriveClicked() },
    onGoogleTasksClick = { viewModel.onGoogleTasksClicked() },
  )
}

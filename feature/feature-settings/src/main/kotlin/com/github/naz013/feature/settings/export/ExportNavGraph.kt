package com.github.naz013.feature.settings.export

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
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
import com.github.naz013.common.Permissions
import com.github.naz013.feature.settings.SettingsDetailPane
import com.github.naz013.feature.settings.SettingsScaffold
import com.github.naz013.feature.settings.export.services.CloudServicesScreen
import com.github.naz013.feature.settings.export.services.CloudServicesState
import com.github.naz013.feature.settings.export.services.CloudServicesViewModel
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.dialog.rememberDialogDispatcher
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import com.github.naz013.ui.googletask.rememberGoogleTasksLogin
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.exportEntries(backStack: MutableList<NavKey>) {
  entry<ExportNavKey.CloudBackup>(metadata = SettingsDetailPane) { CloudBackupEntry(backStack) }
  entry<ExportNavKey.CloudServices>(metadata = SettingsDetailPane) { CloudServicesEntry(backStack) }
}

@Composable
private fun CloudBackupEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<CloudBackupSettingsViewModel>()
  val state by viewModel.state.collectAsState(CloudBackupSettingsState())

  // This entry isn't necessarily recreated after a trip to the Cloud Services screen (reached
  // via onCloudServicesClick below without popping this entry off the backstack), so re-check
  // cloud login state on every ON_RESUME - otherwise hasAnyCloudApi stays stale after logging
  // out/in there.
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(viewModel, lifecycleOwner) {
    val observer =
      LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          viewModel.refreshCloudApiState()
        }
      }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  SettingsScaffold(
    title = stringResource(R.string.cloud_backup),
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
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
    onBackClick = { if (backStack.size > 1) backStack.removeLastOrNull() },
    onDropboxClick = { viewModel.onDropboxClicked() },
    onGoogleDriveClick = { viewModel.onGoogleDriveClicked() },
    onGoogleTasksClick = { viewModel.onGoogleTasksClicked() },
  )
}

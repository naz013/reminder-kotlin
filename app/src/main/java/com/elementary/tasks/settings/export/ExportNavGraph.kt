package com.elementary.tasks.settings.export

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.cloud.compose.rememberGoogleDriveLogin
import com.elementary.tasks.core.cloud.compose.rememberGoogleTasksLogin
import com.elementary.tasks.core.os.compose.rememberPermissionRequesterRationale
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.settings.SettingsScaffold
import com.elementary.tasks.settings.export.services.CloudServicesScreen
import com.elementary.tasks.settings.export.services.CloudServicesViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManager
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.common.Permissions
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.activity.toast
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Contributes the Cloud Backup/Cloud Services Settings sub-tree's screens (Nav3 entries) into the
 * app's single, shared [androidx.navigation3.ui.NavDisplay] (see
 * [com.elementary.tasks.navigation.nav3.AppNavGraph]). Cloud Services' Google sign-in used to
 * require a Fragment host (`GoogleLogin` registers an `ActivityResultLauncher` eagerly in
 * `onCreate()`, see that class's kdoc) - replaced here by
 * [com.elementary.tasks.core.cloud.compose.rememberGoogleDriveLogin]/[rememberGoogleTasksLogin],
 * the same `rememberLauncherForActivityResult`-based pattern already proven for Google Tasks'
 * own island in Phase 2.
 */
fun EntryProviderScope<NavKey>.exportEntries(backStack: MutableList<NavKey>) {
  entry<ExportNavKey.CloudBackup> { CloudBackupEntry(backStack) }
  entry<ExportNavKey.CloudServices> { CloudServicesEntry(backStack) }
}

@Composable
private fun CloudBackupEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<CloudBackupSettingsViewModel>()
  bindLifecycle(viewModel)
  val state by viewModel.state.collectAsState()

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
  val activity = LocalActivity.current as FragmentActivity
  val featureManager = koinInject<FeatureManager>()
  val dialogues = koinInject<Dialogues>()
  val analyticsEventSender = koinInject<AnalyticsEventSender>()
  val googleDriveAuthManager = koinInject<GoogleDriveAuthManager>()
  val googleTasksAuthManager = koinInject<GoogleTasksAuthManager>()
  val permissionRequester = rememberPermissionRequesterRationale()
  val state by viewModel.state.collectAsState()

  var isGoogleDriveLoggedIn by remember { mutableStateOf(googleDriveAuthManager.isAuthorized()) }
  var isGoogleTasksLoggedIn by remember { mutableStateOf(googleTasksAuthManager.isAuthorized()) }
  var isDropboxLoggedIn by remember { mutableStateOf(false) }

  fun showErrorDialog() {
    val builder = dialogues.getMaterialDialog(activity)
    builder.setMessage(activity.getString(R.string.failed_to_login))
    builder.setPositiveButton(R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
    builder.create().show()
  }

  val googleDriveLogin =
    rememberGoogleDriveLogin(
      onResult = { isLogged ->
        isGoogleDriveLoggedIn = isLogged
        if (isLogged) analyticsEventSender.send(FeatureUsedEvent(Feature.GOOGLE_DRIVE))
      },
      onFail = { showErrorDialog() },
    )
  val googleTasksLogin =
    rememberGoogleTasksLogin(
      onResult = { isLogged ->
        if (isLogged) viewModel.loadGoogleTasks()
        isGoogleTasksLoggedIn = isLogged
        if (isLogged) analyticsEventSender.send(FeatureUsedEvent(Feature.GOOGLE_TASK))
      },
      onFail = { showErrorDialog() },
    )
  val dropboxCallback =
    remember {
      object : DropboxLogin.LoginCallback {
        override fun onResult(isSuccess: Boolean) {
          if (isSuccess) analyticsEventSender.send(FeatureUsedEvent(Feature.DROPBOX))
          isDropboxLoggedIn = isSuccess
        }
      }
    }
  val dropboxLogin = koinInject<DropboxLogin> { parametersOf(activity, dropboxCallback) }

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(dropboxLogin, googleDriveAuthManager, googleTasksAuthManager, lifecycleOwner) {
    analyticsEventSender.send(ScreenUsedEvent(Screen.CLOUD_DRIVES))
    val observer =
      LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          dropboxLogin.checkAuthOnResume()
          isGoogleDriveLoggedIn = googleDriveAuthManager.isAuthorized()
          isGoogleTasksLoggedIn = googleTasksAuthManager.isAuthorized()
        }
      }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  val isDropboxVisible = remember { featureManager.isFeatureEnabled(FeatureManager.Feature.DROPBOX) }
  val isGoogleDriveVisible =
    remember {
      SuperUtil.isGooglePlayServicesAvailable(activity) &&
        featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_DRIVE)
    }
  val isGoogleTasksVisible =
    remember {
      SuperUtil.isGooglePlayServicesAvailable(activity) &&
        featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_TASKS)
    }

  fun googleDriveButtonClick() {
    permissionRequester.request(
      Permissions.GET_ACCOUNTS,
      onGranted = {
        if (!SuperUtil.checkGooglePlayServicesAvailability(activity)) {
          activity.toast(R.string.google_play_services_not_installed)
        } else if (googleDriveAuthManager.isAuthorized()) {
          googleDriveLogin.logOut { isGoogleDriveLoggedIn = it }
        } else {
          googleDriveLogin.login()
        }
      },
    )
  }

  fun googleTasksButtonClick() {
    permissionRequester.request(
      Permissions.GET_ACCOUNTS,
      onGranted = {
        if (!SuperUtil.checkGooglePlayServicesAvailability(activity)) {
          activity.toast(R.string.google_play_services_not_installed)
        } else if (googleTasksAuthManager.isAuthorized()) {
          isGoogleTasksLoggedIn = false
          viewModel.clearGoogleTasks()
        } else {
          googleTasksLogin.login()
        }
      },
    )
  }

  CloudServicesScreen(
    isLoading = state.isLoading,
    isDropboxVisible = isDropboxVisible,
    isDropboxLoggedIn = isDropboxLoggedIn,
    isGoogleDriveVisible = isGoogleDriveVisible,
    isGoogleDriveLoggedIn = isGoogleDriveLoggedIn,
    isGoogleTasksVisible = isGoogleTasksVisible,
    isGoogleTasksLoggedIn = isGoogleTasksLoggedIn,
    onBackClick = { backStack.removeLastOrNull() },
    onDropboxClick = { dropboxLogin.login() },
    onGoogleDriveClick = { googleDriveButtonClick() },
    onGoogleTasksClick = { googleTasksButtonClick() },
  )
}

@Composable
private fun bindLifecycle(observer: DefaultLifecycleObserver) {
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(observer, lifecycleOwner) {
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}

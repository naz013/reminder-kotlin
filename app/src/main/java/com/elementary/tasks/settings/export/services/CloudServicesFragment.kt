package com.elementary.tasks.settings.export.services

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.SuperUtil
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.Permissions
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.compose.ComposeFragment
import com.github.naz013.ui.common.fragment.toast
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CloudServicesFragment : ComposeFragment() {
  private val viewModel by viewModel<CloudServicesViewModel>()
  private val featureManager by inject<FeatureManager>()
  private val dialogues by inject<Dialogues>()
  private val analyticsEventSender by inject<AnalyticsEventSender>()
  private lateinit var permissionFlow: PermissionFlow

  private val dropboxLogin: DropboxLogin by inject {
    parametersOf(requireActivity(), dropboxCallback)
  }
  private val googleLogin: GoogleLogin by inject {
    parametersOf(this@CloudServicesFragment, googleCallback)
  }

  private var isDropboxLoggedIn by mutableStateOf(false)
  private var isGoogleDriveLoggedIn by mutableStateOf(false)
  private var isGoogleTasksLoggedIn by mutableStateOf(false)

  private val googleCallback =
    object : GoogleLogin.LoginCallback {
      override fun onProgress(
        isLoading: Boolean,
        mode: GoogleLogin.Mode,
      ) {
      }

      override fun onResult(
        isLogged: Boolean,
        mode: GoogleLogin.Mode,
      ) {
        Logger.i(TAG, "Google login result: isLogged=$isLogged, mode=$mode")
        if (mode == GoogleLogin.Mode.TASKS) {
          if (isLogged) {
            viewModel.loadGoogleTasks()
          }
          isGoogleTasksLoggedIn = isLogged
          if (isLogged) {
            analyticsEventSender.send(FeatureUsedEvent(Feature.GOOGLE_TASK))
          }
        } else {
          isGoogleDriveLoggedIn = isLogged
          if (isLogged) {
            analyticsEventSender.send(FeatureUsedEvent(Feature.GOOGLE_DRIVE))
          }
        }
      }

      override fun onFail(mode: GoogleLogin.Mode) {
        showErrorDialog()
      }
    }

  private val dropboxCallback =
    object : DropboxLogin.LoginCallback {
      override fun onResult(isSuccess: Boolean) {
        if (isSuccess) {
          analyticsEventSender.send(FeatureUsedEvent(Feature.DROPBOX))
        }
        isDropboxLoggedIn = isSuccess
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    permissionFlow = PermissionFlow(this, dialogues)
  }

  @Composable
  override fun FragmentContent() {
    val state by viewModel.state.collectAsState()
    val isDropboxVisible = remember { featureManager.isFeatureEnabled(FeatureManager.Feature.DROPBOX) }
    val isGoogleDriveVisible = remember {
      SuperUtil.isGooglePlayServicesAvailable(requireContext()) &&
        featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_DRIVE)
    }
    val isGoogleTasksVisible = remember {
      SuperUtil.isGooglePlayServicesAvailable(requireContext()) &&
        featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_TASKS)
    }

    CloudServicesScreen(
      isLoading = state.isLoading,
      isDropboxVisible = isDropboxVisible,
      isDropboxLoggedIn = isDropboxLoggedIn,
      isGoogleDriveVisible = isGoogleDriveVisible,
      isGoogleDriveLoggedIn = isGoogleDriveLoggedIn,
      isGoogleTasksVisible = isGoogleTasksVisible,
      isGoogleTasksLoggedIn = isGoogleTasksLoggedIn,
      onBackClick = { moveBack() },
      onDropboxClick = { dropboxLogin.login() },
      onGoogleDriveClick = { googleDriveButtonClick() },
      onGoogleTasksClick = { googleTasksButtonClick() },
    )
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    isGoogleDriveLoggedIn = googleLogin.isGoogleDriveLogged
    isGoogleTasksLoggedIn = googleLogin.isGoogleTasksLogged
    analyticsEventSender.send(ScreenUsedEvent(Screen.CLOUD_DRIVES))
  }

  override fun onResume() {
    super.onResume()
    dropboxLogin.checkAuthOnResume()
    isGoogleDriveLoggedIn = googleLogin.isGoogleDriveLogged
    isGoogleTasksLoggedIn = googleLogin.isGoogleTasksLogged
  }

  private fun moveBack() {
    activity?.onBackPressedDispatcher?.onBackPressed()
  }

  private fun googleTasksButtonClick() {
    permissionFlow.askPermission(Permissions.GET_ACCOUNTS) { switchGoogleTasksStatus() }
  }

  private fun googleDriveButtonClick() {
    permissionFlow.askPermission(Permissions.GET_ACCOUNTS) { switchGoogleDriveStatus() }
  }

  private fun switchGoogleTasksStatus() {
    val activity = requireActivity()
    if (!SuperUtil.checkGooglePlayServicesAvailability(activity)) {
      toast(R.string.google_play_services_not_installed)
      Logger.e(TAG, "Google Play Services not available.")
      return
    }
    if (googleLogin.isGoogleTasksLogged) {
      disconnectFromGoogleTasks()
    } else {
      googleLogin.loginTasks()
    }
  }

  private fun switchGoogleDriveStatus() {
    val activity = requireActivity()
    if (!SuperUtil.checkGooglePlayServicesAvailability(activity)) {
      toast(R.string.google_play_services_not_installed)
      Logger.e(TAG, "Google Play Services not available.")
      return
    }
    if (googleLogin.isGoogleDriveLogged) {
      disconnectFromGoogleDrive()
    } else {
      googleLogin.loginDrive()
    }
  }

  private fun disconnectFromGoogleTasks() {
    googleLogin.logOutTasks()
    viewModel.clearGoogleTasks()
  }

  private fun disconnectFromGoogleDrive() {
    googleLogin.logOutDrive()
  }

  private fun showErrorDialog() {
    val context = context ?: return
    val builder = dialogues.getMaterialDialog(context)
    builder.setMessage(getString(R.string.failed_to_login))
    builder.setPositiveButton(R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
    builder.create().show()
  }

  companion object {
    private const val TAG = "CloudServicesFragment"
  }
}

package com.elementary.tasks.settings.export.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.databinding.FragmentSettingsCloudDrivesBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.Permissions
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.fragment.toast
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.visible
import com.github.naz013.ui.common.view.visibleGone
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CloudServicesFragment : BaseSettingsFragment<FragmentSettingsCloudDrivesBinding>() {

  private val featureManager by inject<FeatureManager>()

  private val viewModel by viewModel<CloudServicesFragmentViewModel>()
  private val dropboxLogin: DropboxLogin by inject {
    parametersOf(requireActivity(), dropboxCallback)
  }
  private val googleLogin: GoogleLogin by inject {
    parametersOf(this@CloudServicesFragment, googleCallback)
  }

  private val googleCallback = object : GoogleLogin.LoginCallback {
    override fun onProgress(isLoading: Boolean, mode: GoogleLogin.Mode) {
      updateProgress(isLoading)
    }

    override fun onResult(isLogged: Boolean, mode: GoogleLogin.Mode) {
      Logger.i(TAG, "Google login result: isLogged=$isLogged, mode=$mode")
      if (mode == GoogleLogin.Mode.TASKS) {
        if (isLogged) {
          viewModel.loadGoogleTasks()
        }
        updateGoogleTasksStatus(isLogged)
        if (isLogged) {
          analyticsEventSender.send(FeatureUsedEvent(Feature.GOOGLE_TASK))
        }
      } else {
        updateGoogleDriveStatus(isLogged)
        if (isLogged) {
          analyticsEventSender.send(FeatureUsedEvent(Feature.GOOGLE_DRIVE))
        }
      }
    }

    override fun onFail(mode: GoogleLogin.Mode) {
      showErrorDialog()
    }
  }

  private val dropboxCallback = object : DropboxLogin.LoginCallback {
    override fun onResult(isSuccess: Boolean) {
      if (isSuccess) {
        analyticsEventSender.send(FeatureUsedEvent(Feature.DROPBOX))
        binding.linkDropbox.text = getString(R.string.logout)
      } else {
        binding.linkDropbox.text = getString(R.string.log_in)
      }
    }
  }

  private fun showErrorDialog() {
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      builder.setMessage(getString(R.string.failed_to_login))
      builder.setPositiveButton(R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
      builder.create().show()
    }
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsCloudDrivesBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    updateProgress(false)
    binding.progressMessageView.text = getString(R.string.please_wait)

    initDropboxButton()
    initGoogleDriveButton()
    initGoogleTasksButton()

    updateGoogleTasksStatus(googleLogin.isGoogleTasksLogged)
    updateGoogleDriveStatus(googleLogin.isGoogleDriveLogged)

    analyticsEventSender.send(ScreenUsedEvent(Screen.CLOUD_DRIVES))
  }

  override fun onStart() {
    super.onStart()
    viewModel.isLoading.nonNullObserve(viewLifecycleOwner) { updateProgress(it) }
  }

  private fun initGoogleTasksButton() {
    withContext {
      if (SuperUtil.isGooglePlayServicesAvailable(it) &&
        featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_TASKS)
      ) {
        binding.linkGTasks.visible()
        binding.linkGTasks.setOnClickListener { googleTasksButtonClick() }
        binding.logoutGTasks.setOnClickListener { googleTasksButtonClick() }
      } else {
        binding.linkGTasks.gone()
        binding.logoutGTasks.gone()
      }
    }
  }

  private fun initGoogleDriveButton() {
    withContext {
      if (SuperUtil.isGooglePlayServicesAvailable(it) &&
        featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_DRIVE)
      ) {
        binding.linkGDrive.visible()
        binding.linkGDrive.setOnClickListener { googleDriveButtonClick() }
        binding.logoutGDrive.setOnClickListener { googleDriveButtonClick() }
      } else {
        binding.linkGDrive.gone()
        binding.logoutGDrive.gone()
      }
    }
  }

  private fun initDropboxButton() {
    binding.linkDropbox.visibleGone(featureManager.isFeatureEnabled(FeatureManager.Feature.DROPBOX))
    binding.linkDropbox.setOnClickListener { dropboxLogin.login() }
  }

  private fun googleTasksButtonClick() {
    permissionFlow.askPermission(Permissions.GET_ACCOUNTS) { switchGoogleTasksStatus() }
  }

  private fun switchGoogleTasksStatus() {
    withActivity {
      if (!SuperUtil.checkGooglePlayServicesAvailability(it)) {
        toast(R.string.google_play_services_not_installed)
        Logger.e(TAG, "Google Play Services not available.")
        return@withActivity
      }
      if (googleLogin.isGoogleTasksLogged) {
        disconnectFromGoogleTasks()
      } else {
        googleLogin.loginTasks()
      }
    }
  }

  private fun updateProgress(loading: Boolean) {
    binding.progressView.visibleGone(loading)
    binding.linkDropbox.isEnabled = !loading
    binding.linkGDrive.isEnabled = !loading
    binding.linkGTasks.isEnabled = !loading
  }

  private fun googleDriveButtonClick() {
    permissionFlow.askPermission(Permissions.GET_ACCOUNTS) { switchGoogleDriveStatus() }
  }

  private fun switchGoogleDriveStatus() {
    withActivity {
      if (!SuperUtil.checkGooglePlayServicesAvailability(it)) {
        toast(R.string.google_play_services_not_installed)
        Logger.e(TAG, "Google Play Services not available.")
        return@withActivity
      }
      if (googleLogin.isGoogleDriveLogged) {
        disconnectFromGoogleDrive()
      } else {
        googleLogin.loginDrive()
      }
    }
  }

  private fun disconnectFromGoogleTasks() {
    googleLogin.logOutTasks()
    viewModel.clearGoogleTasks()
  }

  private fun disconnectFromGoogleDrive() {
    googleLogin.logOutDrive()
  }

  private fun updateGoogleTasksStatus(isLogged: Boolean) {
    runCatching {
      binding.linkGTasks.visibleGone(!isLogged)
      binding.logoutGTasks.visibleGone(isLogged)
    }
  }

  private fun updateGoogleDriveStatus(isLogged: Boolean) {
    runCatching {
      binding.linkGDrive.visibleGone(!isLogged)
      binding.logoutGDrive.visibleGone(isLogged)
    }
  }

  override fun onBackStackResume() {
    super.onBackStackResume()
    dropboxLogin.checkAuthOnResume()
    updateGoogleDriveStatus(googleLogin.isGoogleDriveLogged)
    updateGoogleTasksStatus(googleLogin.isGoogleTasksLogged)
  }

  override fun getTitle(): String = getString(R.string.cloud_services)

  companion object {
    private const val TAG = "CloudServicesFragment"
  }
}

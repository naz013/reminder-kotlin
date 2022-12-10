package com.elementary.tasks.settings.export

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.FragmentSettingsCloudDrivesBinding
import com.elementary.tasks.settings.BaseSettingsFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class FragmentCloudDrives : BaseSettingsFragment<FragmentSettingsCloudDrivesBinding>() {

  private val featureManager by inject<FeatureManager>()

  private val viewModel by viewModel<CloudViewModel>()
  private val dropboxLogin: DropboxLogin by inject {
    parametersOf(requireActivity(), dropboxCallback)
  }
  private val googleLogin: GoogleLogin by inject { parametersOf(requireActivity(), googleCallback) }

  private val googleCallback = object : GoogleLogin.LoginCallback {
    override fun onProgress(isLoading: Boolean, mode: GoogleLogin.Mode) {
      updateProgress(isLoading)
    }

    override fun onResult(isLogged: Boolean, mode: GoogleLogin.Mode) {
      Timber.d("onResult: $isLogged, mode=$mode")
      if (mode == GoogleLogin.Mode.TASKS) {
        if (isLogged) {
          viewModel.loadGoogleTasks()
        }
        updateGoogleTasksStatus(isLogged)
      } else {
        updateGoogleDriveStatus(isLogged)
      }
    }

    override fun onFail(mode: GoogleLogin.Mode) {
      showErrorDialog()
    }
  }

  private val dropboxCallback = object : DropboxLogin.LoginCallback {
    override fun onSuccess(b: Boolean) {
      if (b) {
        binding.linkDropbox.text = getString(R.string.disconnect)
      } else {
        binding.linkDropbox.text = getString(R.string.connect)
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
        binding.tasksView.visibility = View.VISIBLE
        binding.linkGTasks.setOnClickListener { googleTasksButtonClick() }
      } else {
        binding.tasksView.visibility = View.GONE
      }
    }
  }

  private fun initGoogleDriveButton() {
    withContext {
      if (SuperUtil.isGooglePlayServicesAvailable(it) &&
        featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_DRIVE)
      ) {
        binding.driveView.visibility = View.VISIBLE
        binding.linkGDrive.setOnClickListener { googleDriveButtonClick() }
      } else {
        binding.driveView.visibility = View.GONE
      }
    }
  }

  private fun googleTasksButtonClick() {
    withActivity {
      if (Permissions.checkPermission(
          it, 104,
          Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
          Permissions.WRITE_EXTERNAL
        )
      ) {
        switchGoogleTasksStatus()
      }
    }
  }

  private fun googleDriveButtonClick() {
    withActivity {
      if (Permissions.checkPermission(
          it, 103,
          Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
          Permissions.WRITE_EXTERNAL
        )
      ) {
        switchGoogleDriveStatus()
      }
    }
  }

  private fun initDropboxButton() {
    binding.dropboxView.visibleGone(featureManager.isFeatureEnabled(FeatureManager.Feature.DROPBOX))
    binding.linkDropbox.setOnClickListener { dropboxLogin.login() }
  }

  private fun switchGoogleTasksStatus() {
    withActivity {
      if (!SuperUtil.checkGooglePlayServicesAvailability(it)) {
        Toast.makeText(it, R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show()
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
    if (loading) {
      binding.progressView.visibility = View.VISIBLE
    } else {
      binding.progressView.visibility = View.GONE
    }
    binding.linkDropbox.isEnabled = !loading
    binding.linkGDrive.isEnabled = !loading
    binding.linkGTasks.isEnabled = !loading
  }

  private fun switchGoogleDriveStatus() {
    withActivity {
      if (!SuperUtil.checkGooglePlayServicesAvailability(it)) {
        Toast.makeText(it, R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show()
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

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (Permissions.checkPermission(grantResults)) {
      when (requestCode) {
        103 -> switchGoogleDriveStatus()
        104 -> switchGoogleTasksStatus()
      }
    }
  }

  private fun updateGoogleTasksStatus(isLogged: Boolean) {
    if (isLogged) {
      binding.linkGTasks.text = getString(R.string.disconnect)
    } else {
      binding.linkGTasks.text = getString(R.string.connect)
    }
  }

  private fun updateGoogleDriveStatus(isLogged: Boolean) {
    if (isLogged) {
      binding.linkGDrive.text = getString(R.string.disconnect)
    } else {
      binding.linkGDrive.text = getString(R.string.connect)
    }
  }

  override fun onBackStackResume() {
    super.onBackStackResume()
    dropboxLogin.checkDropboxStatus()
    updateGoogleDriveStatus(googleLogin.isGoogleDriveLogged)
    updateGoogleTasksStatus(googleLogin.isGoogleTasksLogged)
  }

  override fun getTitle(): String = getString(R.string.cloud_services)

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    googleLogin.onActivityResult(requestCode, resultCode, data)
  }
}

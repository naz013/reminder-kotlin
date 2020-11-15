package com.elementary.tasks.navigation.settings.export

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.cloud.storages.Dropbox
import com.elementary.tasks.core.cloud.storages.GDrive
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.databinding.FragmentSettingsCloudDrivesBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class FragmentCloudDrives : BaseSettingsFragment<FragmentSettingsCloudDrivesBinding>() {

  private val dropbox by inject<Dropbox>()
  private val gTasks by inject<GTasks>()
  private val gDrive by inject<GDrive>()

  private val viewModel by viewModel<CloudViewModel>()
  private val mDropbox: DropboxLogin by lazy {
    DropboxLogin(requireActivity(), dropbox, mDropboxCallback)
  }
  private val mGoogleLogin: GoogleLogin by lazy {
    GoogleLogin(requireActivity(), prefs, gDrive, gTasks)
  }

  private val mDropboxCallback = object : DropboxLogin.LoginCallback {
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

  override fun layoutRes(): Int = R.layout.fragment_settings_cloud_drives

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    updateProgress(false)
    binding.progressMessageView.text = getString(R.string.please_wait)
    mGoogleLogin.googleStatus = {
      checkGoogleStatus()
    }
    initDropboxButton()
    initGoogleDriveButton()
    initGoogleTasksButton()

    checkGoogleStatus()
  }

  override fun onStart() {
    super.onStart()

    viewModel.isLoading.observe(viewLifecycleOwner, {
      if (it != null) {
        updateProgress(it)
      }
    })
    viewModel.isReady.observe(viewLifecycleOwner, { b ->
      if (b != null && b) {
        withContext { UpdatesHelper.updateTasksWidget(it) }
      }
    })
  }

  private fun initGoogleTasksButton() {
    withContext {
      if (SuperUtil.isGooglePlayServicesAvailable(it)) {
        binding.tasksView.visibility = View.VISIBLE
        binding.linkGTasks.setOnClickListener { googleTasksButtonClick() }
      } else {
        binding.tasksView.visibility = View.GONE
      }
    }
  }

  private fun initGoogleDriveButton() {
    withContext {
      if (SuperUtil.isGooglePlayServicesAvailable(it)) {
        binding.driveView.visibility = View.VISIBLE
        binding.linkGDrive.setOnClickListener { googleDriveButtonClick() }
      } else {
        binding.driveView.visibility = View.GONE
      }
    }
  }

  private fun googleTasksButtonClick() {
    withActivity {
      if (Permissions.checkPermission(it, 104,
          Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
          Permissions.WRITE_EXTERNAL)) {
        switchGoogleTasksStatus()
      }
    }
  }

  private fun googleDriveButtonClick() {
    withActivity {
      if (Permissions.checkPermission(it, 103,
          Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
          Permissions.WRITE_EXTERNAL)) {
        switchGoogleDriveStatus()
      }
    }
  }

  private fun initDropboxButton() {
    binding.linkDropbox.setOnClickListener { mDropbox.login() }
  }

  private fun switchGoogleTasksStatus() {
    withActivity {
      if (!SuperUtil.checkGooglePlayServicesAvailability(it)) {
        Toast.makeText(it, R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show()
        return@withActivity
      }
      if (mGoogleLogin.isGoogleTasksLogged) {
        disconnectFromGoogleTasks()
      } else {
        mGoogleLogin.loginTasks(object : GoogleLogin.TasksCallback {
          override fun onProgress(isLoading: Boolean) {
            updateProgress(isLoading)
          }

          override fun onResult(v: GTasks?, isLogged: Boolean) {
            Timber.d("onResult: $isLogged")
            if (isLogged) {
              viewModel.loadGoogleTasks()
            }
            checkGoogleStatus()
          }

          override fun onFail() {
            showErrorDialog()
          }
        })
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
      if (mGoogleLogin.isGoogleDriveLogged) {
        disconnectFromGoogleDrive()
      } else {
        mGoogleLogin.loginDrive(object : GoogleLogin.DriveCallback {
          override fun onProgress(isLoading: Boolean) {
            updateProgress(isLoading)
          }

          override fun onResult(v: GDrive?, isLogged: Boolean) {
            if (isLogged) {
              checkGoogleStatus()
            }
          }

          override fun onFail() {
            showErrorDialog()
          }
        })
      }
    }
  }

  private fun disconnectFromGoogleTasks() {
    val ctx = context ?: return

    mGoogleLogin.logOutTasks()
    updateProgress(true)
    launchDefault {
      AppDb.getAppDatabase(ctx).googleTasksDao().deleteAll()
      AppDb.getAppDatabase(ctx).googleTaskListsDao().deleteAll()
      withUIContext {
        UpdatesHelper.updateTasksWidget(ctx)
        updateProgress(false)
        checkGoogleStatus()
      }
    }
  }

  private fun disconnectFromGoogleDrive() {
    mGoogleLogin.logOutDrive()
    checkGoogleStatus()
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (Permissions.checkPermission(grantResults)) {
      when (requestCode) {
        103 -> switchGoogleDriveStatus()
        104 -> switchGoogleTasksStatus()
      }
    }
  }

  private fun checkGoogleStatus() {
    if (mGoogleLogin.isGoogleDriveLogged) {
      binding.linkGDrive.text = getString(R.string.disconnect)
    } else {
      binding.linkGDrive.text = getString(R.string.connect)
    }
    if (mGoogleLogin.isGoogleTasksLogged) {
      binding.linkGTasks.text = getString(R.string.disconnect)
    } else {
      binding.linkGTasks.text = getString(R.string.connect)
    }
  }

  override fun onBackStackResume() {
    super.onBackStackResume()
    mDropbox.checkDropboxStatus()
    checkGoogleStatus()
  }

  override fun onDestroy() {
    super.onDestroy()
    mGoogleLogin.googleStatus = null
  }

  override fun getTitle(): String = getString(R.string.cloud_services)

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    mGoogleLogin.onActivityResult(requestCode, resultCode, data)
  }
}

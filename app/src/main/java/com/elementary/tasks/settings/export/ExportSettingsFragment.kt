package com.elementary.tasks.settings.export

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.elementary.tasks.R
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.work.BackupWorker
import com.elementary.tasks.core.work.SyncWorker
import com.elementary.tasks.databinding.FragmentSettingsExportBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import com.github.naz013.cloudapi.dropbox.DropboxApi
import com.github.naz013.cloudapi.googledrive.GoogleDriveApi
import com.github.naz013.common.Permissions
import com.github.naz013.ui.common.view.transparent
import com.github.naz013.ui.common.view.visible
import org.koin.android.ext.android.inject

class ExportSettingsFragment : BaseSettingsFragment<FragmentSettingsExportBinding>() {

  private val syncWorker by inject<SyncWorker>()
  private val backupWorker by inject<BackupWorker>()
  private val dropboxApi by inject<DropboxApi>()
  private val googleDriveApi by inject<GoogleDriveApi>()
  private val jobScheduler by inject<JobScheduler>()

  private var mItemSelect: Int = 0

  private val onSyncEnd: () -> Unit = {
    binding.progressView.transparent()
    binding.syncButton.isEnabled = true
    binding.backupButton.isEnabled = true
  }
  private val onProgress: (Boolean) -> Unit = {
    if (it) {
      binding.syncButton.isEnabled = false
      binding.backupButton.isEnabled = false
      binding.progressView.visible()
    } else {
      onSyncEnd.invoke()
    }
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsExportBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    onSyncEnd.invoke()

    binding.cloudsPrefs.setOnClickListener {
      safeNavigation {
        ExportSettingsFragmentDirections.actionExportSettingsFragmentToFragmentCloudDrives()
      }
    }

    initAutoBackupPrefs()
    initClearDataPrefs()
  }

  override fun onResume() {
    super.onResume()
    initSyncButton()
    initBackupButton()
  }

  override fun onDestroy() {
    super.onDestroy()
    syncWorker.unsubscribe()
    backupWorker.unsubscribe()
  }

  private fun initSyncButton() {
    binding.syncButton.isEnabled = true
    binding.syncButton.visibility = View.VISIBLE
    binding.syncButton.setOnClickListener { syncClick() }
    syncWorker.listener = onProgress
    syncWorker.onEnd = onSyncEnd
  }

  private fun syncClick() {
    permissionFlow.askPermissions(listOf(Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
      onProgress.invoke(true)
      syncWorker.sync(lifecycleScope)
    }
  }

  private fun initBackupButton() {
    binding.backupButton.isEnabled = true
    binding.backupButton.visibility = View.VISIBLE
    binding.backupButton.setOnClickListener { backupClick() }
    backupWorker.listener = onProgress
    backupWorker.onEnd = onSyncEnd
  }

  private fun backupClick() {
    permissionFlow.askPermissions(listOf(Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
      onProgress.invoke(true)
      backupWorker.backup(lifecycleScope)
    }
  }

  private fun initClearDataPrefs() {
    binding.cleanPrefs.setOnClickListener { showCleanDialog() }
  }

  private fun showCleanDialog() {
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setCancelable(true)
    builder.setTitle(getString(R.string.erase_cloud_data))
    builder.setMessage(getString(R.string.erase_cloud_data_message))
    builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
    builder.setPositiveButton(R.string.erase) { _, _ -> removeAllData() }
    builder.create().show()
  }

  private fun removeAllData() {
    launchDefault {
      googleDriveApi.removeAllData()
      dropboxApi.removeAllData()
    }
  }


  private fun initAutoBackupPrefs() {
    binding.autoBackupPrefs.setOnClickListener {
      showIntervalDialog(getString(R.string.automatically_backup), prefs.autoBackupState) { state ->
        prefs.autoBackupState = stateFromPosition(state)
        showBackupState()
        jobScheduler.scheduleAutoBackup()
      }
    }
    showBackupState()
  }

  private fun showBackupState() {
    binding.autoBackupPrefs.setDetailText(syncStates()[positionFromState(prefs.autoBackupState)])
  }

  private fun positionFromState(state: Int): Int {
    val position = when (state) {
      1 -> 1
      6 -> 2
      12 -> 3
      24 -> 4
      48 -> 5
      else -> 0
    }
    mItemSelect = position
    return position
  }

  private fun showIntervalDialog(title: String, current: Int, onSelect: (Int) -> Unit) {
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setTitle(title)
    builder.setSingleChoiceItems(
      syncStates(),
      positionFromState(current)
    ) { _, item -> mItemSelect = item }
    builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
      dialog.dismiss()
      onSelect.invoke(mItemSelect)
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ ->
      dialog.dismiss()
    }
    builder.create().show()
  }

  private fun stateFromPosition(position: Int): Int {
    return when (position) {
      1 -> 1
      2 -> 6
      3 -> 12
      4 -> 24
      5 -> 48
      else -> 0
    }
  }

  override fun getTitle(): String = getString(R.string.cloud_backup)

  private fun syncStates(): Array<String> {
    val prefix = getString(R.string.auto_backup_every) + " "
    return arrayOf(
      getString(R.string.disabled),
      prefix + getString(R.string.one_hour),
      prefix + getString(R.string.six_hours),
      prefix + getString(R.string.twelve_hours),
      prefix + getString(R.string.one_day),
      prefix + getString(R.string.two_days)
    )
  }
}

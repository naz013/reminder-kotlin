package com.elementary.tasks.settings.export

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.worker.WorkerNetworkType
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.databinding.FragmentSettingsExportBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import com.github.naz013.ui.common.view.transparent
import com.github.naz013.ui.common.view.visible
import org.koin.android.ext.android.inject

class ExportSettingsFragment : BaseSettingsFragment<FragmentSettingsExportBinding>() {

  private val observableWorkerManager by inject<ObservableWorkerManager>()
  private val jobScheduler by inject<JobScheduler>()

  private var mItemSelect: Int = 0

  private val onSyncEnd: () -> Unit = {
    binding.progressView.transparent()
    binding.syncButton.isEnabled = true
    binding.backupButton.isEnabled = true
    binding.cleanPrefs.isEnabled = true
  }
  private val onProgress: (Boolean) -> Unit = {
    if (it) {
      binding.syncButton.isEnabled = false
      binding.backupButton.isEnabled = false
      binding.cleanPrefs.isEnabled = false
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
    initNetworkTypePrefs()
  }

  override fun onResume() {
    super.onResume()
    initSyncButton()
    initBackupButton()
  }

  override fun onDestroy() {
    super.onDestroy()
    observableWorkerManager.unsubscribe()
  }

  private fun initNetworkTypePrefs() {
    binding.connectionPrefs.setOnClickListener {
      showNetworkTypeDialog(prefs.workerNetworkType.ordinal) { type ->
        prefs.workerNetworkType = WorkerNetworkType.entries[type]
        showNetworkTypeState()
      }
    }
    showNetworkTypeState()
  }

  private fun initSyncButton() {
    binding.syncButton.isEnabled = true
    binding.syncButton.visibility = View.VISIBLE
    binding.syncButton.setOnClickListener { observableSyncClick() }
  }

  private fun observableSyncClick() {
    onProgress.invoke(true)
    observableWorkerManager.listener = onProgress
    observableWorkerManager.onEnd = onSyncEnd
    ObservableSyncWorker.schedule(requireContext())
    observableWorkerManager.observeWork(
      viewLifecycleOwner,
      ObservableSyncWorker.getWorkTag(),
      ObservableSyncWorker.KEY_IS_IN_PROGRESS
    )
  }

  private fun initBackupButton() {
    binding.backupButton.isEnabled = true
    binding.backupButton.visibility = View.VISIBLE
    binding.backupButton.setOnClickListener { observableBackupClick() }
  }

  private fun observableBackupClick() {
    onProgress.invoke(true)
    observableWorkerManager.listener = onProgress
    observableWorkerManager.onEnd = onSyncEnd
    ObservableBackupWorker.schedule(requireContext())
    observableWorkerManager.observeWork(
      viewLifecycleOwner,
      ObservableBackupWorker.getWorkTag(),
      ObservableBackupWorker.KEY_IS_IN_PROGRESS
    )
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
    onProgress.invoke(true)
    observableWorkerManager.listener = onProgress
    observableWorkerManager.onEnd = onSyncEnd
    ObservableEraseDataWorker.schedule(requireContext())
    observableWorkerManager.observeWork(
      viewLifecycleOwner,
      ObservableEraseDataWorker.getWorkTag(),
      ObservableEraseDataWorker.KEY_IS_IN_PROGRESS
    )
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

  private fun showNetworkTypeDialog(
    currentType: Int,
    onSelect: (Int) -> Unit
  ) {
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setTitle(getString(R.string.select_network_type))
    var selectedItem = currentType
    builder.setSingleChoiceItems(
      getNetworkTypeNames(),
      currentType
    ) { _, item -> selectedItem = item }
    builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
      dialog.dismiss()
      onSelect.invoke(selectedItem)
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ ->
      dialog.dismiss()
    }
    builder.create().show()
  }

  private fun showNetworkTypeState() {
    binding.connectionPrefs.setDetailText(getNetworkTypeName(prefs.workerNetworkType.ordinal))
  }

  private fun getNetworkTypeName(position: Int): String {
    return getNetworkTypeNames().getOrElse(position) { getString(R.string.network_type_any_network) }
  }

  private fun getNetworkTypeNames(): Array<String> {
    return arrayOf(
      getString(R.string.network_type_any_network),
      getString(R.string.network_type_wifi_only),
      getString(R.string.network_type_cellular)
    )
  }
}

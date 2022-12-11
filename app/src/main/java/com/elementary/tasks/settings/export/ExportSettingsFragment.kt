package com.elementary.tasks.settings.export

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.storages.Dropbox
import com.elementary.tasks.core.cloud.storages.GDrive
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.IntentUtil
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.hide
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.show
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.work.BackupWorker
import com.elementary.tasks.core.work.ExportAllDataWorker
import com.elementary.tasks.core.work.SyncDataWorker
import com.elementary.tasks.core.work.SyncWorker
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsExportBinding
import com.elementary.tasks.settings.BaseCalendarFragment
import org.koin.android.ext.android.inject
import java.io.File
import java.util.Locale

class ExportSettingsFragment : BaseCalendarFragment<FragmentSettingsExportBinding>() {

  private val backupTool by inject<BackupTool>()
  private val syncWorker by inject<SyncWorker>()
  private val backupWorker by inject<BackupWorker>()
  private val exportAllDataWorker by inject<ExportAllDataWorker>()
  private val dropbox by inject<Dropbox>()
  private val gDrive by inject<GDrive>()
  private val jobScheduler by inject<JobScheduler>()

  private var mDataList: MutableList<CalendarUtils.CalendarItem> = mutableListOf()
  private var mItemSelect: Int = 0
  private var keepOldData: Boolean = true

  private val currentPosition: Int
    get() {
      return findPosition(mDataList)
    }
  private val onSyncEnd: () -> Unit = {
    binding.progressView.hide()
    binding.syncButton.isEnabled = true
    binding.backupButton.isEnabled = true
    binding.exportButton.isEnabled = true
    binding.importButton.isEnabled = true
  }
  private val onMessage: (String) -> Unit = {
    binding.progressMessageView.text = it
  }
  private val onProgress: (Boolean) -> Unit = {
    if (it) {
      binding.syncButton.isEnabled = false
      binding.backupButton.isEnabled = false
      binding.exportButton.isEnabled = false
      binding.importButton.isEnabled = false
      binding.progressView.show()
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
    ViewUtils.listenScrollableView(binding.scrollView) {
      setToolbarAlpha(toAlpha(it.toFloat(), NESTED_SCROLL_MAX))
    }

    onSyncEnd.invoke()

    binding.cloudsPrefs.setOnClickListener {
      safeNavigation(ExportSettingsFragmentDirections.actionExportSettingsFragmentToFragmentCloudDrives())
    }

    initBackupPrefs()
    initExportToCalendarPrefs()
    initEventDurationPrefs()
    initSelectCalendarPrefs()
    initExportToStockPrefs()
    initSettingsBackupPrefs()
    initClearDataPrefs()

    initAutoBackupPrefs()
    initAutoSyncPrefs()
    initBackupFilesPrefs()
    initLocalBackupPrefs()

    binding.backupsPrefs.setOnClickListener {
      safeNavigation(ExportSettingsFragmentDirections.actionExportSettingsFragmentToBackupsFragment())
    }
    binding.backupsPrefs.setDependentView(binding.backupDataPrefs)
  }

  override fun onResume() {
    super.onResume()
    initSyncButton()
    initBackupButton()
    initExportButton()
    initImportButton()
  }

  override fun onDestroy() {
    super.onDestroy()
    syncWorker.unsubscribe()
    backupWorker.unsubscribe()
    exportAllDataWorker.unsubscribe()
  }

  private fun initImportButton() {
    if (prefs.isBackupEnabled) {
      binding.importButton.isEnabled = true
      binding.importButton.show()
      binding.importButton.setOnClickListener { showImportDialog() }
    } else {
      binding.importButton.hide()
    }
  }

  private fun showImportDialog() {
    dialogues.getMaterialDialog(requireContext())
      .setMessage(R.string.what_to_do_with_current_data)
      .setPositiveButton(R.string.keep) { dialogInterface, _ ->
        dialogInterface.dismiss()
        keepOldData = true
        pickFile()
      }
      .setNegativeButton(R.string.replace) { dialogInterface, _ ->
        dialogInterface.dismiss()
        keepOldData = false
        pickFile()
      }
      .setNeutralButton(R.string.cancel) { dialogInterface, _ ->
        dialogInterface.dismiss()
      }
      .create()
      .show()
  }

  private fun pickFile() {
    permissionFlow.askPermission(Permissions.READ_EXTERNAL) {
      IntentUtil.pickFile(requireActivity(), REQ_PICK_RBAK)
    }
  }

  private fun initLocalBackupPrefs() {
    if (Module.is10) {
      binding.localPrefs.hide()
    } else {
      binding.localPrefs.show()
      binding.localPrefs.isChecked = prefs.localBackup
      binding.localPrefs.setOnClickListener { changeLocalBackupPrefs() }
      binding.localPrefs.setDependentView(binding.backupDataPrefs)
    }
  }

  private fun changeLocalBackupPrefs() {
    val isChecked = binding.localPrefs.isChecked
    if (!isChecked) {
      permissionFlow.askPermissions(
        listOf(Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL),
        {
          binding.localPrefs.isChecked = true
          prefs.localBackup = true
        },
        {
          binding.localPrefs.isChecked = false
          prefs.localBackup = false
        }
      )
    } else {
      binding.localPrefs.isChecked = false
      prefs.localBackup = false
    }
  }

  private fun initBackupFilesPrefs() {
    binding.backupFilesPrefs.hide()
//        binding.backupFilesPrefs.isChecked = prefs.backupAttachedFiles
//        binding.backupFilesPrefs.setOnClickListener { changeBackupFilesPrefs() }
//        binding.backupFilesPrefs.setDependentView(binding.backupDataPrefs)
  }

  private fun changeBackupFilesPrefs() {
    val isChecked = binding.backupFilesPrefs.isChecked
    binding.backupFilesPrefs.isChecked = !isChecked
    prefs.backupAttachedFiles = !isChecked
  }

  private fun initAutoSyncPrefs() {
    binding.autoSyncPrefs.setOnClickListener {
      showIntervalDialog(getString(R.string.automatically_sync), prefs.autoSyncState) { state ->
        prefs.autoSyncState = stateFromPosition(state)
        showSyncState()
        jobScheduler.scheduleAutoSync()
      }
    }
    binding.autoSyncPrefs.setDependentView(binding.backupDataPrefs)
    showSyncState()
  }

  private fun showSyncState() {
    binding.autoSyncPrefs.setDetailText(syncStates()[positionFromState(prefs.autoSyncState)])
    initAutoSyncFlagsPrefs()
  }

  private fun initAutoSyncFlagsPrefs() {
    binding.autoSyncFlagsPrefs.setOnClickListener {
      showFlagsDialog(getString(R.string.sync_flags), prefs.autoSyncFlags) {
        prefs.autoSyncFlags = it
      }
    }
    binding.autoSyncFlagsPrefs.setDependentView(binding.backupDataPrefs)
    binding.autoSyncFlagsPrefs.setDependentValue(prefs.autoSyncState > 0)
  }

  private fun findPosition(list: List<CalendarUtils.CalendarItem>): Int {
    if (list.isEmpty()) return -1
    val id = prefs.defaultCalendarId
    for (i in list.indices) {
      val item = list[i]
      if (item.id == id) {
        return i
      }
    }
    return -1
  }

  private fun initBackupPrefs() {
    binding.backupDataPrefs.isChecked = prefs.isBackupEnabled
    binding.backupDataPrefs.setOnClickListener { changeBackupPrefs() }
    initSyncButton()
    initBackupButton()
    initExportButton()
  }

  private fun initSyncButton() {
    if (prefs.isBackupEnabled) {
      binding.syncButton.isEnabled = true
      binding.syncButton.visibility = View.VISIBLE
      binding.syncButton.setOnClickListener { syncClick() }
      syncWorker.listener = onProgress
      syncWorker.onEnd = onSyncEnd
      syncWorker.progress = onMessage
    } else {
      binding.syncButton.visibility = View.GONE
    }
  }

  private fun syncClick() {
    permissionFlow.askPermissions(listOf(Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
      onProgress.invoke(true)
      syncWorker.sync()
    }
  }

  private fun initExportButton() {
    if (prefs.isBackupEnabled) {
      binding.exportButton.isEnabled = true
      binding.exportButton.visibility = View.VISIBLE
      binding.exportButton.setOnClickListener { exportClick() }
      exportAllDataWorker.onEnd = { file ->
        if (file != null) {
          TelephonyUtil.sendFile(file, requireContext())
        }
      }
      backupWorker.listener = onProgress
    } else {
      binding.exportButton.visibility = View.GONE
    }
  }

  private fun exportClick() {
    permissionFlow.askPermissions(listOf(Permissions.WRITE_EXTERNAL, Permissions.READ_EXTERNAL)) {
      onProgress.invoke(true)
      exportAllDataWorker.export()
    }
  }

  private fun initBackupButton() {
    if (prefs.isBackupEnabled) {
      binding.backupButton.isEnabled = true
      binding.backupButton.visibility = View.VISIBLE
      binding.backupButton.setOnClickListener { backupClick() }
      backupWorker.listener = onProgress
      backupWorker.onEnd = onSyncEnd
      backupWorker.progress = onMessage
    } else {
      binding.backupButton.visibility = View.GONE
    }
  }

  private fun backupClick() {
    permissionFlow.askPermissions(listOf(Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
      onProgress.invoke(true)
      backupWorker.backup()
    }
  }

  private fun changeBackupPrefs() {
    val isChecked = binding.backupDataPrefs.isChecked
    binding.backupDataPrefs.isChecked = !isChecked
    prefs.isBackupEnabled = !isChecked
    initSyncButton()
    initBackupButton()
    initExportButton()
    initImportButton()
  }

  private fun initClearDataPrefs() {
    binding.cleanPrefs.setOnClickListener { showCleanDialog() }
    binding.cleanPrefs.setDependentView(binding.backupDataPrefs)
  }

  private fun showCleanDialog() {
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setCancelable(true)
    builder.setTitle(getString(R.string.clean))
    builder.setNeutralButton(R.string.local) { _, _ ->
      MemoryUtil.parent?.let { dir ->
        deleteRecursive(dir)
      }
    }
    builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
    builder.setPositiveButton(R.string.all) { _, _ -> removeAllData() }
    builder.create().show()
  }

  private fun removeAllData() {
    MemoryUtil.parent?.let {
      deleteRecursive(it)
    }
    launchDefault {
      gDrive.clean()
      dropbox.cleanFolder()
    }
  }

  private fun deleteRecursive(fileOrDirectory: File) {
    if (fileOrDirectory.isDirectory) {
      val list = fileOrDirectory.listFiles()
      if (list != null) {
        for (child in list) {
          deleteRecursive(child)
        }
      }
    }
    fileOrDirectory.delete()
  }

  private fun initAutoBackupPrefs() {
    binding.autoBackupPrefs.setOnClickListener {
      showIntervalDialog(getString(R.string.automatically_backup), prefs.autoBackupState) { state ->
        prefs.autoBackupState = stateFromPosition(state)
        showBackupState()
        jobScheduler.scheduleAutoBackup()
      }
    }
    binding.autoBackupPrefs.setDependentView(binding.backupDataPrefs)
    showBackupState()
  }

  private fun showBackupState() {
    binding.autoBackupPrefs.setDetailText(syncStates()[positionFromState(prefs.autoBackupState)])
    initAutoBackupFlagsPrefs()
  }

  private fun initAutoBackupFlagsPrefs() {
    binding.autoBackupFlagsPrefs.setOnClickListener {
      showFlagsDialog(getString(R.string.backup_flags), prefs.autoBackupFlags) {
        prefs.autoBackupFlags = it
      }
    }
    binding.autoBackupFlagsPrefs.setDependentView(binding.backupDataPrefs)
    binding.autoBackupFlagsPrefs.setDependentValue(prefs.autoBackupState > 0)
  }

  private fun showFlagsDialog(title: String, current: Array<String>, onSelect: (Array<String>) -> Unit) {
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setTitle(title)
    val syncFlags = syncFlags(current)
    builder.setMultiChoiceItems(syncFlags.map { it.title }.toTypedArray(), checkStates(syncFlags)) { _, which, isChecked ->
      syncFlags[which].isChecked = isChecked
    }
    builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
      dialog.dismiss()
      onSelect.invoke(syncFlags.filter { it.isChecked }.map { it.key }.toTypedArray())
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ ->
      dialog.dismiss()
    }
    builder.create().show()
  }

  private fun checkStates(syncFlags: Array<SyncFlag>): BooleanArray {
    return syncFlags.map { it.isChecked }.toBooleanArray()
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
    builder.setSingleChoiceItems(syncStates(), positionFromState(current)) { _, item -> mItemSelect = item }
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

  private fun initSettingsBackupPrefs() {
    binding.syncSettingsPrefs.isChecked = prefs.isSettingsBackupEnabled
    binding.syncSettingsPrefs.setOnClickListener { changeSettingsBackupPrefs() }
    binding.syncSettingsPrefs.setDependentView(binding.backupDataPrefs)
  }

  private fun changeSettingsBackupPrefs() {
    val isChecked = binding.syncSettingsPrefs.isChecked
    binding.syncSettingsPrefs.isChecked = !isChecked
    prefs.isSettingsBackupEnabled = !isChecked
  }

  private fun initExportToStockPrefs() {
    binding.exportToStockPrefs.isChecked = prefs.isStockCalendarEnabled
    binding.exportToStockPrefs.setOnClickListener { changeExportToStockPrefs() }
  }

  private fun changeExportToStockPrefs() {
    val isChecked = binding.exportToStockPrefs.isChecked
    binding.exportToStockPrefs.isChecked = !isChecked
    prefs.isStockCalendarEnabled = !isChecked
  }

  private fun initSelectCalendarPrefs() {
    binding.selectCalendarPrefs.setOnClickListener { tryToShowSelectCalendarDialog() }
    binding.selectCalendarPrefs.setDependentView(binding.exportToCalendarPrefs)
    showCurrentCalendar()
  }

  private fun initEventDurationPrefs() {
    binding.eventDurationPrefs.setOnClickListener { showEventDurationDialog() }
    binding.eventDurationPrefs.setDependentView(binding.exportToCalendarPrefs)
    showEventDuration()
  }

  private fun showEventDuration() {
    binding.eventDurationPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
      prefs.calendarEventDuration.toString()))
  }

  private fun showEventDurationDialog() {
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setTitle(R.string.event_duration)
    val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)
    b.seekBar.max = 120
    b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes), progress.toString())
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {

      }

      override fun onStopTrackingTouch(seekBar: SeekBar) {

      }
    })
    val duration = prefs.calendarEventDuration
    b.seekBar.progress = duration
    b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes), duration.toString())
    builder.setView(b.root)
    builder.setPositiveButton(R.string.ok) { _, _ ->
      prefs.calendarEventDuration = b.seekBar.progress
      showEventDuration()
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
    val dialog = builder.create()
    dialog.show()
    Dialogues.setFullWidthDialog(dialog, requireActivity())
  }

  private fun changeExportToCalendarPrefs() {
    permissionFlow.askPermissions(listOf(Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
      val isChecked = binding.exportToCalendarPrefs.isChecked
      binding.exportToCalendarPrefs.isChecked = !isChecked
      prefs.isCalendarEnabled = !isChecked
      if (binding.exportToCalendarPrefs.isChecked && !showSelectCalendarDialog()) {
        prefs.isCalendarEnabled = false
        binding.exportToCalendarPrefs.isChecked = false
      }
    }
  }

  private fun tryToShowSelectCalendarDialog() {
    permissionFlow.askPermissions(listOf(Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
      showSelectCalendarDialog()
    }
  }

  private fun showSelectCalendarDialog(): Boolean {
    mDataList.clear()
    mDataList.addAll(calendarUtils.getCalendarsList())
    if (mDataList.isEmpty()) {
      return false
    }
    val names = mDataList.map { it.name }.toTypedArray()
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setTitle(R.string.choose_calendar)
    mItemSelect = currentPosition
    builder.setSingleChoiceItems(names, mItemSelect) { _, i ->
      mItemSelect = i
    }
    builder.setPositiveButton(R.string.save) { dialog, _ ->
      if (mItemSelect != -1 && mItemSelect < mDataList.size) {
        prefs.defaultCalendarId = mDataList[mItemSelect].id
      }
      dialog.dismiss()
      showCurrentCalendar()
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ ->
      dialog.dismiss()
    }
    builder.create().show()
    return true
  }

  private fun showCurrentCalendar() {
    val calendars = calendarUtils.getCalendarsList()
    val pos = findPosition(calendars)
    if (calendars.isNotEmpty() && pos != -1 && pos < calendars.size) {
      val name = calendars[pos].name
      binding.selectCalendarPrefs.setDetailText(name)
    } else {
      binding.selectCalendarPrefs.setDetailText(null)
    }
  }

  private fun initExportToCalendarPrefs() {
    binding.exportToCalendarPrefs.setOnClickListener { changeExportToCalendarPrefs() }
    binding.exportToCalendarPrefs.isChecked = prefs.isCalendarEnabled
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQ_PICK_RBAK && resultCode == Activity.RESULT_OK) {
      onProgress.invoke(true)
      backupTool.importAll(data?.data, keepOldData) {
        onSyncEnd.invoke()
        binding.importButton.post {
          if (it) {
            toast(getString(R.string.backup_file_imported_successfully))
          } else {
            toast(getString(R.string.failed_to_import_backup))
          }
        }
      }
    }
  }

  override fun getTitle(): String = getString(R.string.export_and_sync)

  private fun syncStates(): Array<String> {
    return arrayOf(getString(R.string.disabled), getString(R.string.one_hour), getString(R.string.six_hours),
      getString(R.string.twelve_hours), getString(R.string.one_day), getString(R.string.two_days))
  }

  private fun syncFlags(current: Array<String>): Array<SyncFlag> {
    return arrayOf(
      SyncFlag(getString(R.string.reminders_), SyncDataWorker.FLAG_REMINDER, current.contains(SyncDataWorker.FLAG_REMINDER)),
      SyncFlag(getString(R.string.birthdays), SyncDataWorker.FLAG_BIRTHDAY, current.contains(SyncDataWorker.FLAG_BIRTHDAY)),
      SyncFlag(getString(R.string.notes), SyncDataWorker.FLAG_NOTE, current.contains(SyncDataWorker.FLAG_NOTE)),
      SyncFlag(getString(R.string.places), SyncDataWorker.FLAG_PLACE, current.contains(SyncDataWorker.FLAG_PLACE)),
      SyncFlag(getString(R.string.messages), SyncDataWorker.FLAG_TEMPLATE, current.contains(SyncDataWorker.FLAG_TEMPLATE)),
      SyncFlag(getString(R.string.action_settings), SyncDataWorker.FLAG_SETTINGS, current.contains(SyncDataWorker.FLAG_SETTINGS))
    )
  }

  data class SyncFlag(val title: String, val key: String, var isChecked: Boolean)

  companion object {
    private const val REQ_PICK_RBAK = 600
  }
}

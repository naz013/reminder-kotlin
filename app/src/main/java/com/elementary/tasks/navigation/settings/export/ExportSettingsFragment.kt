package com.elementary.tasks.navigation.settings.export

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.GDrive
import com.elementary.tasks.core.services.AlarmReceiver
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.work.BackupWorker
import com.elementary.tasks.core.work.ExportAllDataWorker
import com.elementary.tasks.core.work.SyncWorker
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsExportBinding
import com.elementary.tasks.navigation.settings.BaseCalendarFragment
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class ExportSettingsFragment : BaseCalendarFragment<FragmentSettingsExportBinding>() {

    @Inject
    lateinit var backupTool: BackupTool

    private var mDataList: MutableList<CalendarUtils.CalendarItem> = mutableListOf()
    private var mItemSelect: Int = 0
    private val intervalPosition: Int
        get() {
            val position: Int
            val interval = prefs.autoBackupInterval
            position = when (interval) {
                1 -> 0
                6 -> 1
                12 -> 2
                24 -> 3
                48 -> 4
                else -> 0
            }
            mItemSelect = position
            return position
        }

    private val currentPosition: Int
        get() {
            return findPosition(mDataList)
        }
    private val onSyncEnd: () -> Unit = {
        binding.progressView.visibility = View.INVISIBLE
        binding.syncButton.isEnabled = true
        binding.backupButton.isEnabled = true
        binding.exportButton.isEnabled = true
    }
    private val onMessage: (String) -> Unit = {
        binding.progressMessageView.text = it
    }
    private val onProgress: (Boolean) -> Unit = {
        if (it) {
            binding.syncButton.isEnabled = false
            binding.backupButton.isEnabled = false
            binding.exportButton.isEnabled = false
            binding.progressView.visibility = View.VISIBLE
        } else {
            onSyncEnd.invoke()
        }
    }

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun layoutRes(): Int = R.layout.fragment_settings_export

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setScroll(it)
        }

        binding.cloudsPrefs.setOnClickListener { callback?.openFragment(FragmentCloudDrives(), getString(R.string.cloud_services)) }

        initBackupPrefs()
        initExportToCalendarPrefs()
        initEventDurationPrefs()
        initSelectCalendarPrefs()
        initExportToStockPrefs()
        initSettingsBackupPrefs()
        initAutoBackupPrefs()
        initAutoBackupIntervalPrefs()
        initClearDataPrefs()

        binding.backupsPrefs.setOnClickListener {
            callback?.openFragment(BackupsFragment.newInstance(), getString(R.string.backup_files))
        }
        binding.backupsPrefs.setDependentView(binding.backupDataPrefs)
    }

    override fun onResume() {
        super.onResume()
        initSyncButton()
        initBackupButton()
        initExportButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        SyncWorker.unsubscribe()
        BackupWorker.unsubscribe()
        ExportAllDataWorker.unsubscribe()
    }

    private fun findPosition(list: List<CalendarUtils.CalendarItem>): Int {
        if (list.isEmpty()) return -1
        val id = prefs.calendarId
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
            SyncWorker.listener = onProgress
            SyncWorker.onEnd = onSyncEnd
            SyncWorker.progress = onMessage
        } else {
            binding.syncButton.visibility = View.GONE
        }
    }

    private fun syncClick() {
        if (Permissions.ensurePermissions(activity!!, PERM_SYNC, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            onProgress.invoke(true)
            SyncWorker.sync(context!!, IoHelper(context!!, prefs, backupTool))
        }
    }

    private fun initExportButton() {
        if (prefs.isBackupEnabled) {
            binding.exportButton.isEnabled = true
            binding.exportButton.visibility = View.VISIBLE
            binding.exportButton.setOnClickListener { exportClick() }
            ExportAllDataWorker.onEnd = {
                if (it != null) {
                    TelephonyUtil.sendFile(it, context!!)
                }
            }
            BackupWorker.listener = onProgress
        } else {
            binding.exportButton.visibility = View.GONE
        }
    }

    private fun exportClick() {
        if (Permissions.ensurePermissions(activity!!, PERM_EXPORT, Permissions.WRITE_EXTERNAL, Permissions.READ_EXTERNAL)) {
            onProgress.invoke(true)
            ExportAllDataWorker.export(IoHelper(context!!, prefs, backupTool))
        }
    }

    private fun initBackupButton() {
        if (prefs.isBackupEnabled) {
            binding.backupButton.isEnabled = true
            binding.backupButton.visibility = View.VISIBLE
            binding.backupButton.setOnClickListener { backupClick() }
            BackupWorker.listener = onProgress
            BackupWorker.onEnd = onSyncEnd
            BackupWorker.progress = onMessage
        } else {
            binding.backupButton.visibility = View.GONE
        }
    }

    private fun backupClick() {
        if (Permissions.ensurePermissions(activity!!, PERM_BACKUP, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            onProgress.invoke(true)
            BackupWorker.backup(context!!, IoHelper(context!!, prefs, backupTool))
        }
    }

    private fun changeBackupPrefs() {
        val isChecked = binding.backupDataPrefs.isChecked
        binding.backupDataPrefs.isChecked = !isChecked
        prefs.isBackupEnabled = !isChecked
        initSyncButton()
        initBackupButton()
        initExportButton()
    }

    private fun initClearDataPrefs() {
        binding.cleanPrefs.setOnClickListener { showCleanDialog() }
        binding.cleanPrefs.setDependentView(binding.backupDataPrefs)
    }

    private fun showCleanDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.clean))
        builder.setNeutralButton(R.string.local) { _, _ ->
            val dir = MemoryUtil.parent
            deleteRecursive(dir!!)
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
        builder.setPositiveButton(R.string.all) { _, _ ->
            val dir = MemoryUtil.parent
            deleteRecursive(dir!!)
            launchDefault {
                val gdx = GDrive.getInstance(context!!)
                val dbx = Dropbox()
                if (gdx != null) {
                    try {
                        gdx.clean()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                dbx.cleanFolder()
            }
        }
        val dialog = builder.create()
        dialog.show()
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

    private fun initAutoBackupIntervalPrefs() {
        binding.syncIntervalPrefs.setOnClickListener { showIntervalDialog() }
        binding.syncIntervalPrefs.setDependentView(binding.autoBackupPrefs)
        binding.syncIntervalPrefs.setDependentView(binding.backupDataPrefs)
        showBackupInterval()
    }

    private fun showBackupInterval() {
        val items = arrayOf<CharSequence>(getString(R.string.one_hour), getString(R.string.six_hours), getString(R.string.twelve_hours), getString(R.string.one_day), getString(R.string.two_days))
        binding.syncIntervalPrefs.setDetailText(items[intervalPosition].toString())
    }

    private fun showIntervalDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.interval))
        val items = arrayOf<CharSequence>(getString(R.string.one_hour), getString(R.string.six_hours), getString(R.string.twelve_hours), getString(R.string.one_day), getString(R.string.two_days))
        builder.setSingleChoiceItems(items, intervalPosition) { _, item -> mItemSelect = item }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            saveIntervalPrefs()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun saveIntervalPrefs() {
        when (mItemSelect) {
            0 -> prefs.autoBackupInterval = 1
            1 -> prefs.autoBackupInterval = 6
            2 -> prefs.autoBackupInterval = 12
            3 -> prefs.autoBackupInterval = 24
            4 -> prefs.autoBackupInterval = 48
        }
        AlarmReceiver().enableAutoSync(context!!)
        showBackupInterval()
    }

    private fun initAutoBackupPrefs() {
        binding.autoBackupPrefs.isChecked = prefs.isAutoBackupEnabled
        binding.autoBackupPrefs.setOnClickListener { changeAutoBackupPrefs() }
        binding.autoBackupPrefs.setDependentView(binding.backupDataPrefs)
    }

    private fun changeAutoBackupPrefs() {
        val isChecked = binding.autoBackupPrefs.isChecked
        binding.autoBackupPrefs.isChecked = !isChecked
        prefs.isAutoBackupEnabled = !isChecked
        if (binding.autoBackupPrefs.isChecked) {
            AlarmReceiver().enableAutoSync(context!!)
        } else {
            AlarmReceiver().cancelAutoSync(context!!)
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
        binding.selectCalendarPrefs.setOnClickListener { showSelectCalendarDialog() }
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
        val builder = dialogues.getDialog(context!!)
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
        Dialogues.setFullWidthDialog(dialog, activity!!)
    }

    private fun changeExportToCalendarPrefs() {
        if (!Permissions.ensurePermissions(activity!!, CALENDAR_CODE, Permissions.READ_CALENDAR)) {
            return
        }
        val isChecked = binding.exportToCalendarPrefs.isChecked
        binding.exportToCalendarPrefs.isChecked = !isChecked
        prefs.isCalendarEnabled = !isChecked
        if (binding.exportToCalendarPrefs.isChecked && !showSelectCalendarDialog()) {
            prefs.isCalendarEnabled = false
            binding.exportToCalendarPrefs.isChecked = false
        }
    }

    private fun checkCalendarPerm(): Boolean {
        return Permissions.ensurePermissions(activity!!, CALENDAR_PERM, Permissions.READ_CALENDAR)
    }

    private fun showSelectCalendarDialog(): Boolean {
        if (!checkCalendarPerm()) {
            return false
        }
        mDataList.clear()
        mDataList.addAll(calendarUtils.getCalendarsList())
        if (mDataList.isEmpty()) {
            return false
        }
        val names = mDataList.map { it.name }.toTypedArray()
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(R.string.choose_calendar)
        mItemSelect = currentPosition
        builder.setSingleChoiceItems(names, mItemSelect) { _, i ->
            mItemSelect = i
        }
        builder.setPositiveButton(R.string.save) { dialog, _ ->
            prefs.calendarId = mDataList[mItemSelect].id
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
        if (calendars.isNotEmpty() && pos != -1) {
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Permissions.isAllGranted(grantResults)) {
            when (requestCode) {
                CALENDAR_CODE -> changeExportToCalendarPrefs()
                CALENDAR_PERM -> showSelectCalendarDialog()
                PERM_BACKUP -> backupClick()
                PERM_EXPORT -> exportClick()
                PERM_SYNC -> syncClick()
            }
        }
    }

    override fun getTitle(): String = getString(R.string.export_and_sync)

    companion object {
        private const val CALENDAR_CODE = 124
        private const val CALENDAR_PERM = 500
        private const val PERM_SYNC = 501
        private const val PERM_BACKUP = 502
        private const val PERM_EXPORT = 503
    }
}

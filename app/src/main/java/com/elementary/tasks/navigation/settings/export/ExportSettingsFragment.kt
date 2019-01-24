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
import com.elementary.tasks.navigation.settings.BaseCalendarFragment
import kotlinx.android.synthetic.main.dialog_with_seek_and_title.view.*
import kotlinx.android.synthetic.main.fragment_settings_export.*
import kotlinx.android.synthetic.main.view_progress.*
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
class ExportSettingsFragment : BaseCalendarFragment() {

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
        progressView.visibility = View.INVISIBLE
        syncButton.isEnabled = true
        backupButton.isEnabled = true
        exportButton.isEnabled = true
    }
    private val onMessage: (String) -> Unit = {
        progressMessageView.text = it
    }
    private val onProgress: (Boolean) -> Unit = {
        if (it) {
            syncButton.isEnabled = false
            backupButton.isEnabled = false
            exportButton.isEnabled = false
            progressView.visibility = View.VISIBLE
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
        ViewUtils.listenScrollableView(scrollView) {
            setScroll(it)
        }

        cloudsPrefs.setOnClickListener { callback?.openFragment(FragmentCloudDrives(), getString(R.string.cloud_services)) }

        initBackupPrefs()
        initExportToCalendarPrefs()
        initEventDurationPrefs()
        initSelectCalendarPrefs()
        initExportToStockPrefs()
        initSettingsBackupPrefs()
        initAutoBackupPrefs()
        initAutoBackupIntervalPrefs()
        initClearDataPrefs()

        backupsPrefs.setOnClickListener {
            callback?.openFragment(BackupsFragment.newInstance(), getString(R.string.backup_files))
        }
        backupsPrefs.setDependentView(backupDataPrefs)
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
        backupDataPrefs.isChecked = prefs.isBackupEnabled
        backupDataPrefs.setOnClickListener { changeBackupPrefs() }
        initSyncButton()
        initBackupButton()
        initExportButton()
    }

    private fun initSyncButton() {
        if (prefs.isBackupEnabled) {
            syncButton.isEnabled = true
            syncButton.visibility = View.VISIBLE
            syncButton.setOnClickListener { syncClick() }
            SyncWorker.listener = onProgress
            SyncWorker.onEnd = onSyncEnd
            SyncWorker.progress = onMessage
        } else {
            syncButton.visibility = View.GONE
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
            exportButton.isEnabled = true
            exportButton.visibility = View.VISIBLE
            exportButton.setOnClickListener { exportClick() }
            ExportAllDataWorker.onEnd = {
                if (it != null) {
                    TelephonyUtil.sendFile(it, context!!)
                }
            }
            BackupWorker.listener = onProgress
        } else {
            exportButton.visibility = View.GONE
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
            backupButton.isEnabled = true
            backupButton.visibility = View.VISIBLE
            backupButton.setOnClickListener { backupClick() }
            BackupWorker.listener = onProgress
            BackupWorker.onEnd = onSyncEnd
            BackupWorker.progress = onMessage
        } else {
            backupButton.visibility = View.GONE
        }
    }

    private fun backupClick() {
        if (Permissions.ensurePermissions(activity!!, PERM_BACKUP, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            onProgress.invoke(true)
            BackupWorker.backup(context!!, IoHelper(context!!, prefs, backupTool))
        }
    }

    private fun changeBackupPrefs() {
        val isChecked = backupDataPrefs.isChecked
        backupDataPrefs.isChecked = !isChecked
        prefs.isBackupEnabled = !isChecked
        initSyncButton()
        initBackupButton()
        initExportButton()
    }

    private fun initClearDataPrefs() {
        cleanPrefs.setOnClickListener { showCleanDialog() }
        cleanPrefs.setDependentView(backupDataPrefs)
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
        syncIntervalPrefs.setOnClickListener { showIntervalDialog() }
        syncIntervalPrefs.setDependentView(autoBackupPrefs)
        syncIntervalPrefs.setDependentView(backupDataPrefs)
        showBackupInterval()
    }

    private fun showBackupInterval() {
        val items = arrayOf<CharSequence>(getString(R.string.one_hour), getString(R.string.six_hours), getString(R.string.twelve_hours), getString(R.string.one_day), getString(R.string.two_days))
        syncIntervalPrefs.setDetailText(items[intervalPosition].toString())
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
        autoBackupPrefs.isChecked = prefs.isAutoBackupEnabled
        autoBackupPrefs.setOnClickListener { changeAutoBackupPrefs() }
        autoBackupPrefs.setDependentView(backupDataPrefs)
    }

    private fun changeAutoBackupPrefs() {
        val isChecked = autoBackupPrefs.isChecked
        autoBackupPrefs.isChecked = !isChecked
        prefs.isAutoBackupEnabled = !isChecked
        if (autoBackupPrefs.isChecked) {
            AlarmReceiver().enableAutoSync(context!!)
        } else {
            AlarmReceiver().cancelAutoSync(context!!)
        }
    }

    private fun initSettingsBackupPrefs() {
        syncSettingsPrefs.isChecked = prefs.isSettingsBackupEnabled
        syncSettingsPrefs.setOnClickListener { changeSettingsBackupPrefs() }
        syncSettingsPrefs.setDependentView(backupDataPrefs)
    }

    private fun changeSettingsBackupPrefs() {
        val isChecked = syncSettingsPrefs.isChecked
        syncSettingsPrefs.isChecked = !isChecked
        prefs.isSettingsBackupEnabled = !isChecked
    }

    private fun initExportToStockPrefs() {
        exportToStockPrefs.isChecked = prefs.isStockCalendarEnabled
        exportToStockPrefs.setOnClickListener { changeExportToStockPrefs() }
    }

    private fun changeExportToStockPrefs() {
        val isChecked = exportToStockPrefs.isChecked
        exportToStockPrefs.isChecked = !isChecked
        prefs.isStockCalendarEnabled = !isChecked
    }

    private fun initSelectCalendarPrefs() {
        selectCalendarPrefs.setOnClickListener { showSelectCalendarDialog() }
        selectCalendarPrefs.setDependentView(exportToCalendarPrefs)
        showCurrentCalendar()
    }

    private fun initEventDurationPrefs() {
        eventDurationPrefs.setOnClickListener { showEventDurationDialog() }
        eventDurationPrefs.setDependentView(exportToCalendarPrefs)
        showEventDuration()
    }

    private fun showEventDuration() {
        eventDurationPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                prefs.calendarEventDuration.toString()))
    }

    private fun showEventDurationDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(R.string.event_duration)
        val b = layoutInflater.inflate(R.layout.dialog_with_seek_and_title, null)
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
        builder.setView(b)
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
        val isChecked = exportToCalendarPrefs.isChecked
        exportToCalendarPrefs.isChecked = !isChecked
        prefs.isCalendarEnabled = !isChecked
        if (exportToCalendarPrefs.isChecked && !showSelectCalendarDialog()) {
            prefs.isCalendarEnabled = false
            exportToCalendarPrefs.isChecked = false
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
            selectCalendarPrefs.setDetailText(name)
        } else {
            selectCalendarPrefs.setDetailText(null)
        }
    }

    private fun initExportToCalendarPrefs() {
        exportToCalendarPrefs.setOnClickListener { changeExportToCalendarPrefs() }
        exportToCalendarPrefs.isChecked = prefs.isCalendarEnabled
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

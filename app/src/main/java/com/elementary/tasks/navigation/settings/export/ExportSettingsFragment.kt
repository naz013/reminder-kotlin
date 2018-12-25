package com.elementary.tasks.navigation.settings.export

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.SeekBar
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.GDrive
import com.elementary.tasks.core.services.AlarmReceiver
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.navigation.settings.BaseCalendarFragment
import kotlinx.android.synthetic.main.dialog_with_seek_and_title.view.*
import kotlinx.android.synthetic.main.fragment_settings_export.*
import java.io.File
import java.io.IOException
import java.util.*

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
            var position = 0
            val id = prefs.calendarId
            for (i in mDataList.indices) {
                val item = mDataList[i]
                if (item.id == id) {
                    position = i
                    break
                }
            }
            return position
        }

    override fun layoutRes(): Int = R.layout.fragment_settings_export

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(scrollView) {
            callback?.onScrollUpdate(it)
        }

        backupsPrefs.setOnClickListener {
            callback?.openFragment(BackupsFragment.newInstance(), getString(R.string.backup_files))
        }

        initExportToCalendarPrefs()
        initEventDurationPrefs()
        initSelectCalendarPrefs()
        initExportToStockPrefs()
        initSettingsBackupPrefs()
        initAutoBackupPrefs()
        initAutoBackupIntervalPrefs()
        initClearDataPrefs()
        initCloudDrivesPrefs()
    }

    private fun initCloudDrivesPrefs() {
        cloudsPrefs.setOnClickListener { callback?.openFragment(FragmentCloudDrives(), getString(R.string.cloud_services)) }
    }

    private fun initClearDataPrefs() {
        cleanPrefs.setOnClickListener { showCleanDialog() }
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
                if (SuperUtil.isConnected(context!!)) {
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
        if (!Permissions.checkPermission(activity!!, Permissions.READ_CALENDAR)) {
            Permissions.requestPermission(activity!!, CALENDAR_CODE, Permissions.READ_CALENDAR)
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
        return if (Permissions.checkPermission(activity!!, Permissions.READ_CALENDAR)) {
            true
        } else {
            Permissions.requestPermission(activity!!, CALENDAR_PERM, Permissions.READ_CALENDAR)
            false
        }
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
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(R.string.choose_calendar)
        builder.setSingleChoiceItems(object : ArrayAdapter<CalendarUtils.CalendarItem>(context!!,
                android.R.layout.simple_list_item_single_choice) {
            override fun getCount(): Int {
                return mDataList.size
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var cView = convertView
                if (cView == null) {
                    cView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_single_choice, parent, false)
                }
                val tvName = cView!!.findViewById<CheckedTextView>(android.R.id.text1)
                tvName.text = mDataList[position].name
                return cView
            }
        }, currentPosition) { dialogInterface, i ->
            dialogInterface.dismiss()
            prefs.calendarId = mDataList[i].id
        }
        builder.create().show()
        return true
    }

    private fun initExportToCalendarPrefs() {
        exportToCalendarPrefs.setOnClickListener { changeExportToCalendarPrefs() }
        exportToCalendarPrefs.isChecked = prefs.isCalendarEnabled
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            CALENDAR_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                changeExportToCalendarPrefs()
            }
            CALENDAR_PERM -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSelectCalendarDialog()
            }
        }
    }

    override fun getTitle(): String = getString(R.string.export_and_sync)

    companion object {

        private const val CALENDAR_CODE = 124
        private const val CALENDAR_PERM = 500
    }
}

package com.elementary.tasks.navigation.settings

import android.app.AlertDialog
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
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.services.AlarmReceiver
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsExportBinding
import com.elementary.tasks.navigation.settings.export.FragmentCloudDrives

import java.io.File
import java.io.IOException
import java.util.Locale

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
class ExportSettingsFragment : BaseSettingsFragment() {

    private var mDataList: List<CalendarUtils.CalendarItem>? = null
    private var mItemSelect: Int = 0

    private var binding: FragmentSettingsExportBinding? = null
    private val mCalendarClick = { view -> changeExportToCalendarPrefs() }

    private val intervalPosition: Int
        get() {
            val position: Int
            val interval = prefs!!.autoBackupInterval
            when (interval) {
                1 -> position = 0
                6 -> position = 1
                12 -> position = 2
                24 -> position = 3
                48 -> position = 4
                else -> position = 0
            }
            mItemSelect = position
            return position
        }

    private val currentPosition: Int
        get() {
            var position = 0
            val id = prefs!!.calendarId
            for (i in mDataList!!.indices) {
                val item = mDataList!![i]
                if (item.id == id) {
                    position = i
                    break
                }
            }
            return position
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsExportBinding.inflate(inflater, container, false)
        initExportToCalendarPrefs()
        initEventDurationPrefs()
        initSelectCalendarPrefs()
        initExportToStockPrefs()
        initSettingsBackupPrefs()
        initAutoBackupPrefs()
        initAutoBackupIntervalPrefs()
        initClearDataPrefs()
        initCloudDrivesPrefs()
        return binding!!.root
    }

    private fun initCloudDrivesPrefs() {
        binding!!.cloudsPrefs.setOnClickListener { view -> replaceFragment(FragmentCloudDrives(), getString(R.string.cloud_services)) }
    }

    private fun initClearDataPrefs() {
        binding!!.cleanPrefs.setOnClickListener { view -> showCleanDialog() }
    }

    private fun showCleanDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.clean))
        builder.setNeutralButton(R.string.local) { dialog, which ->
            val dir = MemoryUtil.parent
            deleteRecursive(dir!!)
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
        builder.setPositiveButton(R.string.all) { dialog, which ->
            val dir = MemoryUtil.parent
            deleteRecursive(dir!!)
            Thread {
                val gdx = Google.getInstance(context!!)
                val dbx = Dropbox(context)
                if (SuperUtil.isConnected(context!!)) {
                    if (gdx != null && gdx.drive != null) {
                        try {
                            gdx.drive!!.clean()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }
                    dbx.cleanFolder()
                }
            }.start()

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
        binding!!.syncIntervalPrefs.setOnClickListener { view -> showIntervalDialog() }
        binding!!.syncIntervalPrefs.setDependentView(binding!!.autoBackupPrefs)
        showBackupInterval()
    }

    private fun showBackupInterval() {
        val items = arrayOf<CharSequence>(getString(R.string.one_hour), getString(R.string.six_hours), getString(R.string.twelve_hours), getString(R.string.one_day), getString(R.string.two_days))
        binding!!.syncIntervalPrefs.setDetailText(items[intervalPosition].toString())
    }

    private fun showIntervalDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.interval))
        val items = arrayOf<CharSequence>(getString(R.string.one_hour), getString(R.string.six_hours), getString(R.string.twelve_hours), getString(R.string.one_day), getString(R.string.two_days))
        builder.setSingleChoiceItems(items, intervalPosition) { dialog, item -> mItemSelect = item }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            saveIntervalPrefs()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { dialogInterface -> mItemSelect = 0 }
        dialog.setOnDismissListener { dialogInterface -> mItemSelect = 0 }
        dialog.show()
    }

    private fun saveIntervalPrefs() {
        if (mItemSelect == 0) {
            prefs!!.autoBackupInterval = 1
        } else if (mItemSelect == 1) {
            prefs!!.autoBackupInterval = 6
        } else if (mItemSelect == 2) {
            prefs!!.autoBackupInterval = 12
        } else if (mItemSelect == 3) {
            prefs!!.autoBackupInterval = 24
        } else if (mItemSelect == 4) {
            prefs!!.autoBackupInterval = 48
        }
        AlarmReceiver().enableAutoSync(context)
        showBackupInterval()
    }

    private fun initAutoBackupPrefs() {
        binding!!.autoBackupPrefs.isChecked = prefs!!.isAutoBackupEnabled
        binding!!.autoBackupPrefs.setOnClickListener { view -> changeAutoBackupPrefs() }
    }

    private fun changeAutoBackupPrefs() {
        val isChecked = binding!!.autoBackupPrefs.isChecked
        binding!!.autoBackupPrefs.isChecked = !isChecked
        prefs!!.isAutoBackupEnabled = !isChecked
        if (binding!!.autoBackupPrefs.isChecked) {
            AlarmReceiver().enableAutoSync(context)
        } else {
            AlarmReceiver().cancelAutoSync(context)
        }
    }

    private fun initSettingsBackupPrefs() {
        binding!!.syncSettingsPrefs.isChecked = prefs!!.isSettingsBackupEnabled
        binding!!.syncSettingsPrefs.setOnClickListener { view -> changeSettingsBackupPrefs() }
    }

    private fun changeSettingsBackupPrefs() {
        val isChecked = binding!!.syncSettingsPrefs.isChecked
        binding!!.syncSettingsPrefs.isChecked = !isChecked
        prefs!!.isSettingsBackupEnabled = !isChecked
    }

    private fun initExportToStockPrefs() {
        binding!!.exportToStockPrefs.isChecked = prefs!!.isStockCalendarEnabled
        binding!!.exportToStockPrefs.setOnClickListener { view -> changeExportToStockPrefs() }
    }

    private fun changeExportToStockPrefs() {
        val isChecked = binding!!.exportToStockPrefs.isChecked
        binding!!.exportToStockPrefs.isChecked = !isChecked
        prefs!!.isStockCalendarEnabled = !isChecked
    }

    private fun initSelectCalendarPrefs() {
        binding!!.selectCalendarPrefs.setOnClickListener { view -> showSelectCalendarDialog() }
        binding!!.selectCalendarPrefs.setDependentView(binding!!.exportToCalendarPrefs)
    }

    private fun initEventDurationPrefs() {
        binding!!.eventDurationPrefs.setOnClickListener { view -> showEventDurationDialog() }
        binding!!.eventDurationPrefs.setDependentView(binding!!.exportToCalendarPrefs)
        showEventDuration()
    }

    private fun showEventDuration() {
        binding!!.eventDurationPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                prefs!!.calendarEventDuration.toString()))
    }

    private fun showEventDurationDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.event_duration)
        val b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(context))
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
        val duration = prefs!!.calendarEventDuration
        b.seekBar.progress = duration
        b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes), duration.toString())
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
            prefs!!.calendarEventDuration = b.seekBar.progress
            showEventDuration()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        builder.create().show()
    }

    private fun changeExportToCalendarPrefs() {
        if (!Permissions.checkPermission(activity, Permissions.READ_CALENDAR)) {
            Permissions.requestPermission(activity, CALENDAR_CODE, Permissions.READ_CALENDAR)
            return
        }
        val isChecked = binding!!.exportToCalendarPrefs.isChecked
        binding!!.exportToCalendarPrefs.isChecked = !isChecked
        prefs!!.isCalendarEnabled = !isChecked
        if (binding!!.exportToCalendarPrefs.isChecked && !showSelectCalendarDialog()) {
            prefs!!.isCalendarEnabled = false
            binding!!.exportToCalendarPrefs.isChecked = false
        }
    }

    private fun checkCalendarPerm(): Boolean {
        if (Permissions.checkPermission(activity, Permissions.READ_CALENDAR)) {
            return true
        } else {
            Permissions.requestPermission(activity, CALENDAR_PERM, Permissions.READ_CALENDAR)
            return false
        }
    }

    private fun showSelectCalendarDialog(): Boolean {
        if (!checkCalendarPerm()) {
            return false
        }
        mDataList = CalendarUtils.getCalendarsList(context)
        if (mDataList == null || mDataList!!.isEmpty()) {
            return false
        }
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.choose_calendar)
        builder.setSingleChoiceItems(object : ArrayAdapter<CalendarUtils.CalendarItem>(context!!, android.R.layout.simple_list_item_single_choice) {
            override fun getCount(): Int {
                return mDataList!!.size
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var convertView = convertView
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_single_choice, parent, false)
                }
                val tvName = convertView!!.findViewById<CheckedTextView>(android.R.id.text1)
                tvName.text = mDataList!![position].name
                return convertView
            }
        }, currentPosition) { dialogInterface, i ->
            dialogInterface.dismiss()
            prefs!!.calendarId = mDataList!![i].id
        }
        builder.create().show()
        return true
    }

    private fun initExportToCalendarPrefs() {
        binding!!.exportToCalendarPrefs.setOnClickListener(mCalendarClick)
        binding!!.exportToCalendarPrefs.isChecked = prefs!!.isCalendarEnabled
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
        when (requestCode) {
            CALENDAR_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                changeExportToCalendarPrefs()
            }
            CALENDAR_PERM -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSelectCalendarDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.export_and_sync))
            callback!!.onFragmentSelect(this)
        }
    }

    companion object {

        private val CALENDAR_CODE = 124
        private val CALENDAR_PERM = 500
    }
}

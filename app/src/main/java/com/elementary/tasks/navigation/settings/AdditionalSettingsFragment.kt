package com.elementary.tasks.navigation.settings

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.navigation.settings.additional.TemplatesFragment
import com.mcxiaoke.koi.ext.onClick
import kotlinx.android.synthetic.main.dialog_with_seek_and_title.view.*
import kotlinx.android.synthetic.main.fragment_settings_additional.*
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

class AdditionalSettingsFragment : BaseSettingsFragment() {

    override fun layoutRes(): Int = R.layout.fragment_settings_additional

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMissedPrefs()
        initMissedTimePrefs()
        initQuickSmsPrefs()
        initMessagesPrefs()
        followReminderPrefs.onClick { changeFollowPrefs() }
        followReminderPrefs.isChecked = prefs.isFollowReminderEnabled
    }

    private fun initMessagesPrefs() {
        val mMessagesPrefs = templatesPrefs
        mMessagesPrefs.onClick { replaceFragment(TemplatesFragment(), getString(R.string.messages)) }
        mMessagesPrefs.setDependentView(quickSMSPrefs)
    }

    private fun initQuickSmsPrefs() {
        quickSMSPrefs.onClick {changeQuickSmsPrefs()}
        quickSMSPrefs.isChecked = prefs.isQuickSmsEnabled
    }

    private fun initMissedTimePrefs() {
        missedTimePrefs.onClick { showTimePickerDialog() }
        missedTimePrefs.setDependentView(missedPrefs)
        showTime()
    }

    private fun showTime() {
        missedTimePrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                prefs.missedReminderTime.toString()))
    }

    private fun initMissedPrefs() {
        missedPrefs.onClick { changeMissedPrefs() }
        missedPrefs.isChecked = prefs.isMissedReminderEnabled
    }

    private fun changeFollowPrefs() {
        if (!Permissions.checkPermission(activity!!, Permissions.READ_PHONE_STATE)) {
            Permissions.requestPermission(activity!!, FOLLOW, Permissions.READ_PHONE_STATE)
            return
        }
        val isChecked = followReminderPrefs.isChecked
        followReminderPrefs.isChecked = !isChecked
        prefs.isFollowReminderEnabled = !isChecked
    }

    private fun changeMissedPrefs() {
        if (!Permissions.checkPermission(activity!!, Permissions.READ_PHONE_STATE)) {
            Permissions.requestPermission(activity!!, MISSED, Permissions.READ_PHONE_STATE)
            return
        }
        val isChecked = missedPrefs.isChecked
        missedPrefs.isChecked = !isChecked
        prefs.isMissedReminderEnabled = !isChecked
    }

    private fun changeQuickSmsPrefs() {
        if (!Permissions.checkPermission(activity!!, Permissions.READ_PHONE_STATE)) {
            Permissions.requestPermission(activity!!, QUICK_SMS, Permissions.READ_PHONE_STATE)
            return
        }
        val isChecked = quickSMSPrefs.isChecked
        quickSMSPrefs.isChecked = !isChecked
        prefs.isQuickSmsEnabled = !isChecked
    }

    private fun showTimePickerDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(R.string.interval)
        val b = LayoutInflater.from(context).inflate(R.layout.dialog_with_seek_and_title, null)
        b.seekBar.max = 60
        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes),
                        progress.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        val time = prefs.missedReminderTime
        b.seekBar.progress = time
        b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes), time.toString())
        builder.setView(b)
        builder.setPositiveButton(R.string.ok) { _, _ ->
            prefs.missedReminderTime = b.seekBar.progress
            showTime()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback?.onTitleChange(getString(R.string.additional))
            callback?.onFragmentSelect(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty()) return
        when (requestCode) {
            MISSED -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                changeMissedPrefs()
            }
            QUICK_SMS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                changeQuickSmsPrefs()
            }
            FOLLOW -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                changeFollowPrefs()
            }
        }
    }

    companion object {
        private const val MISSED = 107
        private const val QUICK_SMS = 108
        private const val FOLLOW = 109
    }
}

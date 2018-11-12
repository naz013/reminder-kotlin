package com.elementary.tasks.navigation.settings

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import com.elementary.tasks.R
import com.elementary.tasks.core.additional.FollowReminderActivity
import com.elementary.tasks.core.additional.QuickSmsActivity
import com.elementary.tasks.core.data.models.MissedCall
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.missedCalls.MissedCallDialogActivity
import com.elementary.tasks.navigation.settings.additional.TemplatesFragment
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
        ViewUtils.listenScrollableView(scrollView) {
            callback?.onScrollUpdate(it)
        }

        initMissedPrefs()
        initMissedTimePrefs()
        initQuickSmsPrefs()
        initMessagesPrefs()
        followReminderPrefs.setOnClickListener { changeFollowPrefs() }
        followReminderPrefs.isChecked = prefs.isFollowReminderEnabled

        missedCallWindow.setOnClickListener {
            MissedCallDialogActivity.mockTest(context!!,
                    MissedCall(number = "0662552549", dateTime = System.currentTimeMillis()))
        }

        quickSmsWindow.setOnClickListener {
            QuickSmsActivity.openScreen(context!!, "0662552549")
        }

        afterCallWindow.setOnClickListener {
            FollowReminderActivity.mockScreen(context!!, "0662552549", System.currentTimeMillis())
        }
    }

    private fun initMessagesPrefs() {
        val mMessagesPrefs = templatesPrefs
        mMessagesPrefs.setOnClickListener { callback?.openFragment(TemplatesFragment(), getString(R.string.messages)) }
        mMessagesPrefs.setDependentView(quickSMSPrefs)
    }

    private fun initQuickSmsPrefs() {
        quickSMSPrefs.setOnClickListener {changeQuickSmsPrefs()}
        quickSMSPrefs.isChecked = prefs.isQuickSmsEnabled
    }

    private fun initMissedTimePrefs() {
        missedTimePrefs.setOnClickListener { showTimePickerDialog() }
        missedTimePrefs.setDependentView(missedPrefs)
        showTime()
    }

    private fun showTime() {
        missedTimePrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                prefs.missedReminderTime.toString()))
    }

    private fun initMissedPrefs() {
        missedPrefs.setOnClickListener { changeMissedPrefs() }
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
        val dialog = builder.create()
        dialog.show()
        Dialogues.setFullWidthDialog(dialog, activity!!)
    }

    override fun getTitle(): String = getString(R.string.additional)

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

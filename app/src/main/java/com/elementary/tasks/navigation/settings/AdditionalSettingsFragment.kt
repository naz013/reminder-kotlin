package com.elementary.tasks.navigation.settings

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.views.PrefsView
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsAdditionalBinding
import com.elementary.tasks.navigation.settings.additional.TemplatesFragment

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

class AdditionalSettingsFragment : BaseSettingsFragment() {

    private var binding: FragmentSettingsAdditionalBinding? = null
    private var mMissedPrefs: PrefsView? = null
    private var mQuickSmsPrefs: PrefsView? = null
    private val mMissedClick = { view -> changeMissedPrefs() }
    private val mMissedTimeClick = { view -> showTimePickerDialog() }
    private val mQuickSmsClick = { view -> changeQuickSmsPrefs() }
    private val mFollowClick = { view -> changeFollowPrefs() }
    private val mMessagesClick = { view -> replaceFragment(TemplatesFragment(), getString(R.string.messages)) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsAdditionalBinding.inflate(inflater, container, false)
        initMissedPrefs()
        initMissedTimePrefs()
        initQuickSmsPrefs()
        initMessagesPrefs()
        binding!!.followReminderPrefs.setOnClickListener(mFollowClick)
        binding!!.followReminderPrefs.isChecked = prefs!!.isFollowReminderEnabled
        return binding!!.root
    }

    private fun initMessagesPrefs() {
        val mMessagesPrefs = binding!!.templatesPrefs
        mMessagesPrefs.setOnClickListener(mMessagesClick)
        mMessagesPrefs.setDependentView(mQuickSmsPrefs)
    }

    private fun initQuickSmsPrefs() {
        mQuickSmsPrefs = binding!!.quickSMSPrefs
        mQuickSmsPrefs!!.setOnClickListener(mQuickSmsClick)
        mQuickSmsPrefs!!.isChecked = prefs!!.isQuickSmsEnabled
    }

    private fun initMissedTimePrefs() {
        binding!!.missedTimePrefs.setOnClickListener(mMissedTimeClick)
        binding!!.missedTimePrefs.setDependentView(mMissedPrefs)
        showTime()
    }

    private fun showTime() {
        binding!!.missedTimePrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                prefs!!.missedReminderTime.toString()))
    }

    private fun initMissedPrefs() {
        mMissedPrefs = binding!!.missedPrefs
        mMissedPrefs!!.setOnClickListener(mMissedClick)
        mMissedPrefs!!.isChecked = prefs!!.isMissedReminderEnabled
    }

    private fun changeFollowPrefs() {
        if (!Permissions.checkPermission(activity, Permissions.READ_PHONE_STATE)) {
            Permissions.requestPermission(activity, FOLLOW, Permissions.READ_PHONE_STATE)
            return
        }
        val isChecked = binding!!.followReminderPrefs.isChecked
        binding!!.followReminderPrefs.isChecked = !isChecked
        prefs!!.isFollowReminderEnabled = !isChecked
    }

    private fun changeMissedPrefs() {
        if (!Permissions.checkPermission(activity, Permissions.READ_PHONE_STATE)) {
            Permissions.requestPermission(activity, MISSED, Permissions.READ_PHONE_STATE)
            return
        }
        val isChecked = mMissedPrefs!!.isChecked
        mMissedPrefs!!.isChecked = !isChecked
        prefs!!.isMissedReminderEnabled = !isChecked
    }

    private fun changeQuickSmsPrefs() {
        if (!Permissions.checkPermission(activity, Permissions.READ_PHONE_STATE)) {
            Permissions.requestPermission(activity, QUICK_SMS, Permissions.READ_PHONE_STATE)
            return
        }
        val isChecked = mQuickSmsPrefs!!.isChecked
        mQuickSmsPrefs!!.isChecked = !isChecked
        prefs!!.isQuickSmsEnabled = !isChecked
    }

    private fun showTimePickerDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.interval)
        val b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(context))
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
        val time = prefs!!.missedReminderTime
        b.seekBar.progress = time
        b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes), time.toString())
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
            prefs!!.missedReminderTime = b.seekBar.progress
            showTime()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        builder.create().show()
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.additional))
            callback!!.onFragmentSelect(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
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

        private val MISSED = 107
        private val QUICK_SMS = 108
        private val FOLLOW = 109
    }
}

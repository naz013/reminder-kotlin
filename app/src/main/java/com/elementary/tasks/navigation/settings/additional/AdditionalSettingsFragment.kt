package com.elementary.tasks.navigation.settings.additional

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SeekBar
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsAdditionalBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
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
class AdditionalSettingsFragment : BaseSettingsFragment<FragmentSettingsAdditionalBinding>() {

    private var mItemSelect: Int = 0

    override fun layoutRes(): Int = R.layout.fragment_settings_additional

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setScroll(it)
        }

        initMissedPrefs()
        initMissedTimePrefs()
        initQuickSmsPrefs()
        initMessagesPrefs()
        binding.followReminderPrefs.setOnClickListener { changeFollowPrefs() }
        binding.followReminderPrefs.isChecked = prefs.isFollowReminderEnabled
        initPriority()
    }

    private fun initPriority() {
        binding.priorityPrefs.setOnClickListener { showPriorityDialog() }
        binding.priorityPrefs.setDependentView(binding.missedPrefs)
        showPriority()
    }

    private fun showPriorityDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(getString(R.string.default_priority))
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_single_choice, priorityList())
        mItemSelect = prefs.missedCallPriority
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which ->
            mItemSelect = which
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            prefs.missedCallPriority = mItemSelect
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnDismissListener { showPriority() }
        dialog.show()
    }

    private fun showPriority() {
        binding.priorityPrefs.setDetailText(priorityList()[prefs.missedCallPriority])
    }

    private fun initMessagesPrefs() {
        val mMessagesPrefs = binding.templatesPrefs
        mMessagesPrefs.setOnClickListener { callback?.openFragment(TemplatesFragment(), getString(R.string.messages)) }
        mMessagesPrefs.setDependentView(binding.quickSMSPrefs)
    }

    private fun initQuickSmsPrefs() {
        binding.quickSMSPrefs.setOnClickListener {changeQuickSmsPrefs()}
        binding.quickSMSPrefs.isChecked = prefs.isQuickSmsEnabled
    }

    private fun initMissedTimePrefs() {
        binding.missedTimePrefs.setOnClickListener { showTimePickerDialog() }
        binding.missedTimePrefs.setDependentView(binding.missedPrefs)
        showTime()
    }

    private fun showTime() {
        binding.missedTimePrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                prefs.missedReminderTime.toString()))
    }

    private fun initMissedPrefs() {
        binding.missedPrefs.setOnClickListener { changeMissedPrefs() }
        binding.missedPrefs.isChecked = prefs.isMissedReminderEnabled
    }

    private fun changeFollowPrefs() {
        if (!Permissions.ensurePermissions(activity!!, FOLLOW, Permissions.READ_PHONE_STATE)) {
            return
        }
        val isChecked = binding.followReminderPrefs.isChecked
        binding.followReminderPrefs.isChecked = !isChecked
        prefs.isFollowReminderEnabled = !isChecked
    }

    private fun changeMissedPrefs() {
        if (!Permissions.ensurePermissions(activity!!, MISSED, Permissions.READ_PHONE_STATE)) {
            return
        }
        val isChecked = binding.missedPrefs.isChecked
        binding.missedPrefs.isChecked = !isChecked
        prefs.isMissedReminderEnabled = !isChecked
    }

    private fun changeQuickSmsPrefs() {
        if (!Permissions.ensurePermissions(activity!!, QUICK_SMS, Permissions.READ_PHONE_STATE)) {
            return
        }
        val isChecked = binding.quickSMSPrefs.isChecked
        binding.quickSMSPrefs.isChecked = !isChecked
        prefs.isQuickSmsEnabled = !isChecked
    }

    private fun showTimePickerDialog() {
        val builder = dialogues.getDialog(context!!)
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
        val time = prefs.missedReminderTime
        b.seekBar.progress = time
        b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes), time.toString())
        builder.setView(b.root)
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
        if (Permissions.isAllGranted(grantResults)) {
            when (requestCode) {
                MISSED -> changeMissedPrefs()
                QUICK_SMS -> changeQuickSmsPrefs()
                FOLLOW -> changeFollowPrefs()
            }
        }
    }

    companion object {
        private const val MISSED = 107
        private const val QUICK_SMS = 108
        private const val FOLLOW = 109
    }
}

package com.elementary.tasks.navigation.settings.reminders

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.FragmentSettingsRemindersBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import java.util.*

/**
 * Copyright 2018 Nazar Suhovich
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
class RemindersSettingsFragment : BaseSettingsFragment<FragmentSettingsRemindersBinding>() {

    private var mItemSelect: Int = 0

    override fun layoutRes(): Int = R.layout.fragment_settings_reminders

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setScroll(it)
        }

        initDefaultPriority()
        initCompletedPrefs()
        initDoNotDisturbPrefs()
        initTimesPrefs()
        initActionPrefs()
        initIgnorePrefs()
    }

    private fun initIgnorePrefs() {
        binding.doNotDisturbIgnorePrefs.setOnClickListener { showIgnoreDialog() }
        binding.doNotDisturbIgnorePrefs.setDependentView(binding.doNotDisturbPrefs)
        showIgnore()
    }

    private fun showIgnore() {
        binding.doNotDisturbIgnorePrefs.setDetailText(ignoreList()[prefs.doNotDisturbIgnore])
    }

    private fun showIgnoreDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(getString(R.string.priority))
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_single_choice, ignoreList())
        mItemSelect = prefs.doNotDisturbIgnore
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which ->
            mItemSelect = which
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            prefs.doNotDisturbIgnore = mItemSelect
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnDismissListener { showIgnore() }
        dialog.show()
    }

    private fun initActionPrefs() {
        binding.doNotDisturbActionPrefs.setOnClickListener { showActionDialog() }
        binding.doNotDisturbActionPrefs.setDependentView(binding.doNotDisturbPrefs)
        showAction()
    }

    private fun showAction() {
        binding.doNotDisturbActionPrefs.setDetailText(actionList()[prefs.doNotDisturbAction])
    }

    private fun showActionDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(getString(R.string.events_that_occured_during))
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_single_choice, actionList())
        mItemSelect = prefs.doNotDisturbAction
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which ->
            mItemSelect = which
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            prefs.doNotDisturbAction = mItemSelect
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnDismissListener { showAction() }
        dialog.show()
    }

    private fun initTimesPrefs() {
        binding.doNotDisturbFromPrefs.setOnClickListener {
            showTimeDialog(prefs.doNotDisturbFrom) { i, j ->
                prefs.doNotDisturbFrom = TimeUtil.getBirthdayTime(i, j)
                showFromTime()
            }
        }
        binding.doNotDisturbFromPrefs.setDependentView(binding.doNotDisturbPrefs)

        binding.doNotDisturbToPrefs.setOnClickListener {
            showTimeDialog(prefs.doNotDisturbTo) { i, j ->
                prefs.doNotDisturbTo = TimeUtil.getBirthdayTime(i, j)
                showToTime()
            }
        }
        binding.doNotDisturbToPrefs.setDependentView(binding.doNotDisturbPrefs)

        showFromTime()
        showToTime()
    }

    private fun showTimeDialog(time: String, callback: (Int, Int) -> Unit) {
        val calendar = TimeUtil.getBirthdayCalendar(time)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val min = calendar.get(Calendar.MINUTE)
        TimeUtil.showTimePicker(context!!, themeUtil.dialogStyle, prefs.is24HourFormat,
                hour, min, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            callback.invoke(hourOfDay, minute)
        })
    }

    private fun showToTime() {
        binding.doNotDisturbToPrefs.setValueText(TimeUtil.getBirthdayVisualTime(prefs.doNotDisturbTo, prefs.is24HourFormat, prefs.appLanguage))
    }

    private fun showFromTime() {
        binding.doNotDisturbFromPrefs.setValueText(TimeUtil.getBirthdayVisualTime(prefs.doNotDisturbFrom, prefs.is24HourFormat, prefs.appLanguage))
    }

    private fun initDoNotDisturbPrefs() {
        binding.doNotDisturbPrefs.setOnClickListener { changeDoNotDisturb() }
        binding.doNotDisturbPrefs.isChecked = prefs.isDoNotDisturbEnabled
    }

    private fun changeDoNotDisturb() {
        val isChecked = binding.doNotDisturbPrefs.isChecked
        binding.doNotDisturbPrefs.isChecked = !isChecked
        prefs.isDoNotDisturbEnabled = !isChecked
    }

    private fun initDefaultPriority() {
        binding.defaultPriorityPrefs.setOnClickListener { showPriorityDialog() }
        showDefaultPriority()
    }

    private fun showPriorityDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(getString(R.string.default_priority))
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_single_choice, priorityList())
        mItemSelect = prefs.defaultPriority
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which ->
            mItemSelect = which
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            prefs.defaultPriority = mItemSelect
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnDismissListener { showDefaultPriority() }
        dialog.show()
    }

    private fun showDefaultPriority() {
        binding.defaultPriorityPrefs.setDetailText(priorityList()[prefs.defaultPriority])
    }

    private fun initCompletedPrefs() {
        binding.completedPrefs.setOnClickListener { changeCompleted() }
        binding.completedPrefs.isChecked = prefs.moveCompleted
    }

    private fun changeCompleted() {
        val isChecked = binding.completedPrefs.isChecked
        binding.completedPrefs.isChecked = !isChecked
        prefs.moveCompleted = !isChecked
    }

    private fun ignoreList(): Array<String> {
        return arrayOf(
                getString(R.string.priority_lowest) + " " + getString(R.string.and_above),
                getString(R.string.priority_low) + " " + getString(R.string.and_above),
                getString(R.string.priority_normal) + " " + getString(R.string.and_above),
                getString(R.string.priority_high) + " " + getString(R.string.and_above),
                getString(R.string.priority_highest),
                getString(R.string.do_not_allow)
        )
    }

    private fun actionList(): Array<String> {
        return arrayOf(
                getString(R.string.schedule_for_later),
                getString(R.string.ignore)
        )
    }

    override fun getTitle(): String = getString(R.string.reminders_)
}

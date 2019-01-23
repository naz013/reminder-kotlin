package com.elementary.tasks.navigation.settings.reminders

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import kotlinx.android.synthetic.main.fragment_settings_reminders.*
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
class RemindersSettingsFragment : BaseSettingsFragment() {

    private var mItemSelect: Int = 0

    override fun layoutRes(): Int = R.layout.fragment_settings_reminders

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(scrollView) {
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
        doNotDisturbIgnorePrefs.setOnClickListener { showIgnoreDialog() }
        doNotDisturbIgnorePrefs.setDependentView(doNotDisturbPrefs)
        showIgnore()
    }

    private fun showIgnore() {

    }

    private fun showIgnoreDialog() {

    }

    private fun initActionPrefs() {
        doNotDisturbActionPrefs.setOnClickListener { showActionDialog() }
        doNotDisturbActionPrefs.setDependentView(doNotDisturbPrefs)
        showAction()
    }

    private fun showAction() {

    }

    private fun showActionDialog() {

    }

    private fun initTimesPrefs() {
        doNotDisturbFromPrefs.setOnClickListener {
            showTimeDialog(prefs.doNotDisturbFrom) { i, j ->
                prefs.doNotDisturbFrom = TimeUtil.getBirthdayTime(i, j)
                showFromTime()
            }
        }
        doNotDisturbFromPrefs.setDependentView(doNotDisturbPrefs)

        doNotDisturbToPrefs.setOnClickListener {
            showTimeDialog(prefs.doNotDisturbTo) { i, j ->
                prefs.doNotDisturbTo = TimeUtil.getBirthdayTime(i, j)
                showToTime()
            }
        }
        doNotDisturbToPrefs.setDependentView(doNotDisturbPrefs)

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
        doNotDisturbToPrefs.setValueText(TimeUtil.getBirthdayVisualTime(prefs.doNotDisturbTo, prefs.is24HourFormat, prefs.appLanguage))
    }

    private fun showFromTime() {
        doNotDisturbFromPrefs.setValueText(TimeUtil.getBirthdayVisualTime(prefs.doNotDisturbFrom, prefs.is24HourFormat, prefs.appLanguage))
    }

    private fun initDoNotDisturbPrefs() {
        doNotDisturbPrefs.setOnClickListener { changeDoNotDisturb() }
        doNotDisturbPrefs.isChecked = prefs.isDoNotDisturbEnabled
    }

    private fun changeDoNotDisturb() {
        val isChecked = doNotDisturbPrefs.isChecked
        doNotDisturbPrefs.isChecked = !isChecked
        prefs.isDoNotDisturbEnabled = !isChecked
    }

    private fun initDefaultPriority() {
        defaultPriorityPrefs.setOnClickListener { showPriorityDialog() }
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
        defaultPriorityPrefs.setDetailText(priorityList()[prefs.defaultPriority])
    }

    private fun initCompletedPrefs() {
        completedPrefs.setOnClickListener { changeCompleted() }
        completedPrefs.isChecked = prefs.moveCompleted
    }

    private fun changeCompleted() {
        val isChecked = completedPrefs.isChecked
        completedPrefs.isChecked = !isChecked
        prefs.moveCompleted = !isChecked
    }

    private fun priorityList(): Array<String> {
        return arrayOf(
                getString(R.string.priority_lowest),
                getString(R.string.priority_low),
                getString(R.string.priority_normal),
                getString(R.string.priority_high),
                getString(R.string.priority_highest)
        )
    }

    override fun getTitle(): String = getString(R.string.reminders_)
}

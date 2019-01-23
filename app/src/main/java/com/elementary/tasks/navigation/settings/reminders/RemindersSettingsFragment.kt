package com.elementary.tasks.navigation.settings.reminders

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import kotlinx.android.synthetic.main.fragment_settings_reminders.*

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

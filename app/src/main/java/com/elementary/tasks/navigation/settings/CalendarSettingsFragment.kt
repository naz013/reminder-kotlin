package com.elementary.tasks.navigation.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.FragmentCalendarSettingsBinding
import com.elementary.tasks.navigation.settings.calendar.FragmentBirthdaysColor
import com.elementary.tasks.navigation.settings.calendar.FragmentEventsImport
import com.elementary.tasks.navigation.settings.calendar.FragmentRemindersColor
import com.elementary.tasks.navigation.settings.calendar.FragmentTodayColor

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

class CalendarSettingsFragment : BaseSettingsFragment() {

    private var binding: FragmentCalendarSettingsBinding? = null
    private var mItemSelect: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCalendarSettingsBinding.inflate(inflater, container, false)
        initBackgroundPrefs()
        initFuturePrefs()
        initRemindersPrefs()
        initFirstDayPrefs()
        binding!!.eventsImportPrefs.setOnClickListener { view -> replaceFragment(FragmentEventsImport(), getString(R.string.import_events)) }
        return binding!!.root
    }

    private fun initFirstDayPrefs() {
        binding!!.startDayPrefs.setOnClickListener { view -> showFirstDayDialog() }
        showFirstDay()
    }

    private fun showFirstDay() {
        val items = arrayOf(getString(R.string.sunday), getString(R.string.monday))
        binding!!.startDayPrefs.setDetailText(items[prefs!!.startDay])
    }

    private fun showFirstDayDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.first_day))
        val items = arrayOf(getString(R.string.sunday), getString(R.string.monday))
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, items)
        mItemSelect = prefs!!.startDay
        builder.setSingleChoiceItems(adapter, mItemSelect) { dialog, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, i ->
            prefs!!.startDay = mItemSelect
            showFirstDay()
            dialogInterface.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { dialogInterface -> mItemSelect = 0 }
        dialog.setOnDismissListener { dialogInterface -> mItemSelect = 0 }
        dialog.show()
    }

    private fun initRemindersColorPrefs() {
        binding!!.reminderColorPrefs.setDependentView(binding!!.reminderInCalendarPrefs)
        binding!!.reminderColorPrefs.setOnClickListener { view -> replaceFragment(FragmentRemindersColor(), getString(R.string.reminders_color)) }
        binding!!.reminderColorPrefs.setViewResource(ThemeUtil.getInstance(context).getIndicator(prefs!!.reminderColor))
    }

    private fun initRemindersPrefs() {
        binding!!.reminderInCalendarPrefs.isChecked = prefs!!.isRemindersInCalendarEnabled
        binding!!.reminderInCalendarPrefs.setOnClickListener { view -> changeRemindersPrefs() }
    }

    private fun changeRemindersPrefs() {
        val isChecked = binding!!.reminderInCalendarPrefs.isChecked
        binding!!.reminderInCalendarPrefs.isChecked = !isChecked
        prefs!!.isRemindersInCalendarEnabled = !isChecked
    }

    private fun initFuturePrefs() {
        binding!!.featureRemindersPrefs.isChecked = prefs!!.isFutureEventEnabled
        binding!!.featureRemindersPrefs.setOnClickListener { view -> changeFuturePrefs() }
    }

    private fun changeFuturePrefs() {
        val isChecked = binding!!.featureRemindersPrefs.isChecked
        binding!!.featureRemindersPrefs.isChecked = !isChecked
        prefs!!.isFutureEventEnabled = !isChecked
    }

    private fun initBackgroundPrefs() {
        binding!!.bgImagePrefs.isChecked = prefs!!.isCalendarImagesEnabled
        binding!!.bgImagePrefs.setOnClickListener { view -> changeBackgroundPrefs() }
    }

    private fun changeBackgroundPrefs() {
        val isChecked = binding!!.bgImagePrefs.isChecked
        binding!!.bgImagePrefs.isChecked = !isChecked
        prefs!!.isCalendarImagesEnabled = !isChecked
    }

    override fun onResume() {
        super.onResume()
        initRemindersColorPrefs()
        initTodayColorPrefs()
        initBirthdaysColorPrefs()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.calendar))
            callback!!.onFragmentSelect(this)
        }
    }

    private fun initBirthdaysColorPrefs() {
        binding!!.selectedColorPrefs.setOnClickListener { view -> replaceFragment(FragmentBirthdaysColor(), getString(R.string.birthdays_color)) }
        binding!!.selectedColorPrefs.setViewResource(ThemeUtil.getInstance(context).getIndicator(prefs!!.birthdayColor))
    }

    private fun initTodayColorPrefs() {
        binding!!.themeColorPrefs.setOnClickListener { view -> replaceFragment(FragmentTodayColor(), getString(R.string.today_color)) }
        binding!!.themeColorPrefs.setViewResource(ThemeUtil.getInstance(context).getIndicator(prefs!!.todayColor))
    }
}
